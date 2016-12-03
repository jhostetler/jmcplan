# LICENSE
# Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 
# 1. Redistributions of source code must retain the above copyright notice,
# this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
# this list of conditions and the following disclaimer in the documentation
# and/or other materials provided with the distribution.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

from csv import CsvDataset, CsvAttribute

import glob
import math
from optparse import OptionParser
import os
import statistics

cl_parser = OptionParser( usage="%prog [options] file..." )
cl_parser.add_option( "-i", dest="input_file", type="string", default=None,
					  help="Read input files from file" )
cl_parser.add_option( "-o", dest="output_file", type="string", default=None,
					  help="The file to write the output to (default: stdout)" )
cl_parser.add_option( "--sort", action="store_true", default=False,
					  help="Sort output (in ascending order of total value)" )
cl_parser.add_option( "--legacy", action="store_true", default=False,
					  help="Enable corrections for earlier experiments with different output format" )
cl_parser.add_option( "--named-faults", action="store_true", default=False,
					  help="Indicates that the files use the old 'Nfaults,fault0,fault1...' format" )
cl_parser.add_option( "--algorithm", type="string", default=None,
					  help="If 'algorithm' is not a field in the input files, specify the name here" )
(options, args) = cl_parser.parse_args();

# Read input files from file
if options.input_file is not None:
	with open( options.input_file ) as inputs:
		args = [line.rstrip() for line in inputs]

# Set output
if options.output_file is None:
	output_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )
	
out_fields = ["experiment", "algorithm", "faults", "N", "Nblackout",
			  "V", "V_stdev", "V_median", "V_min", "V_max",
			  "R_end", "R_end_stdev", "R_end_median", "R_end_min", "R_end_max",
			  "t_blackout", "t_blackout_stdev", "t_blackout_median", "t_blackout_min", "t_blackout_max",
			  "NLoadShed", "NLoadShed_stdev", "NLoadShed_median", "NLoadShed_min", "NLoadShed_max",
			  "NIsland", "NIsland_stdev", "NIsland_median", "NIsland_min", "NIsland_max"]
out_data = CsvDataset( attributes=list( map( CsvAttribute, out_fields ) ), feature_vectors=[] )

missing = []
for filename in args:
# for filename in glob.iglob( args[0] ):
	# experiment = os.path.dirname( filename )
	experiment = filename
	print( "'" + experiment + "'" )
	with open( experiment + ".csv" ) as fparams:
		params = CsvDataset( fparams )
		try:
			f = open( os.path.join( filename, "rewards.csv" ) )
		except IOError as ex:
			print( "WARNING: Skipping " + filename )
			print( str(ex) )
			missing.append( params.feature_vectors[0][params.attribute_index("fault")] )
		else:
			with f:
				data = CsvDataset( f )
				# assert( len(params.feature_vectors) == len(data.feature_vectors) )
				[iTstable, iTepisode] = map( params.attribute_index, ["Tstable", "Tepisode"] )
				(sV, sR_end, st_blackout, sNLoadShed, sNIsland) = ([], [], [], [], [])
				Nblackout = 0
				params_fv = params.feature_vectors[0]
				Tstable = int(params_fv[iTstable])
				Tepisode = int(params_fv[iTepisode])
				
				if options.algorithm is not None:
					algorithm = options.algorithm
				else:
					algorithm = params_fv[params.attribute_index( "algorithm" )]
					alg_params = []
					for i in range(0, len(params.attributes)):
						attr = params.attributes[i]
						if attr.name.startswith( algorithm ):
							alg_params.append( params_fv[i] )
					algorithm = algorithm + "[" + ";".join( alg_params ) + "]"
				
				if options.named_faults:
					Nfaults = int( params_fv[params.attribute_index( "Nfaults" )] )
					faults = []
					for i in range(0, Nfaults):
						ifault = params.attribute_index( "fault" + str(i) )
						faults.append( params_fv[ifault] )
					faults = ";".join( faults )
				else:
					faults = params_fv[params.attribute_index( "fault" )]
				
				for i in range(0, len(data.feature_vectors)):
					T = Tstable + Tepisode
					fv = data.feature_vectors[i]
					# Note: This is a low-effort way to detect errors, but it would be
					# nice to read errors from a separate list file.
					if len(fv) != len(data.attributes):
						# assert( float(fv[-1]) == 0.0 )
						if float(fv[-1]) != 0.0:
							print( "Error: " + experiment + " episode " + str(i) )
							break
					V = 0.0
					NLoadShed = 0
					NIsland = 0
					t_blackout = T+1
					for t in range(0, T+1):
						if options.legacy:
							if t == T:
								# Legacy files don't include reward for time 'T', so
								# we assume it's the same as at T-1
								ri = data.attribute_index( "t" + str(T-1) )
							else:
								ri = data.attribute_index( "t" + str(t) )
						else:
							ri = data.attribute_index( "r" + str(t) )
							if t < T:
								ai = data.attribute_index( "a" + str(t) )
								if ai < len(fv):
									a = fv[ai]
									# 'ShedZone', 'ShedGlobal', and 'ShedLoad'
									# are all valid actions
									if "Shed" in a or ("TripShunt" in a and t >= Tstable):
										NLoadShed += 1
									elif "Island" in a:
										NIsland += 1
						if ri < len(fv):
							vt = float(fv[ri])
						else:
							vt = 0.0
						V += vt
						# FIXME: We shouldn't need 't > 0', but there is a bug
						# with the problem domain that causes reward to be 0
						# at time t = 0.
						if t > 0 and vt == 0 and t_blackout > t:
							t_blackout = t
						if t == T: # Do this here so we don't have to lookup index again
							Rend = vt
					# Special case to fix early results that had the fault occur one
					# step later: Duplicate last time step reward to compensate
					if options.legacy:
						V -= float(fv[data.attribute_index( "t8" )])
						V += Rend
						
					sV.append( V )
					sR_end.append( Rend )
					if t_blackout <= T:
						Nblackout += 1
						st_blackout.append( t_blackout )
					sNLoadShed.append( NLoadShed )
					sNIsland.append( NIsland )
				
				N = len(sV)
				aggregate = [experiment, algorithm, faults, N, Nblackout]
				for v in (sV, sR_end, st_blackout, sNLoadShed, sNIsland):
					if len(v) > 0:
						aggregate.append( statistics.mean( v ) )
						if len(v) > 1:
							aggregate.append( statistics.stdev( v ) )
						else:
							aggregate.append( 0 )
						aggregate.append( statistics.median( v ) )
						aggregate.append( min( v ) )
						aggregate.append( max( v ) )
					else:
						aggregate.extend( [math.nan] * 5 )
				out_fv = list( map( str, aggregate ) )
				assert( len(out_fv) == len(out_data.attributes) )
				out_data.feature_vectors.append( out_fv )

if options.sort:
	def key( fv ):
		k = [float(fv[3]), float(fv[4])]
		k.extend( map( float, faults.split( ";" ) ) )
		return tuple( k )
	out_data.feature_vectors.sort( key=key )
output_file.write( repr(out_data) )
output_file.close()

if missing:
	print( "WARNING: Missing faults:" )
	for m in missing:
		print( m )
