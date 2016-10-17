from csv import CsvDataset, CsvAttribute

import glob
from optparse import OptionParser
import os

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
	
out_fields = ["experiment", "faults", "V", "R_end", "t_blackout", "NLoadShed", "NIsland"]
out_data = CsvDataset( attributes=list( map( CsvAttribute, out_fields ) ), feature_vectors=[] )

for filename in args:
# for filename in glob.iglob( args[0] ):
	# experiment = os.path.dirname( filename )
	experiment = filename
	print( "'" + experiment + "'" )
	with open( experiment + ".csv" ) as fparams:
		try:
			f = open( os.path.join( filename, "rewards.csv" ) )
		except IOError as ex:
			print( "WARNING: Skipping " + filename )
			print( str(ex) )
		else:
			with f:
				params = CsvDataset( fparams )
				data = CsvDataset( f )
				assert( len(params.feature_vectors) == len(data.feature_vectors) )
				[iTstable, iTepisode] = map( params.attribute_index, ["Tstable", "Tepisode"] )
				for i in range(0, len(data.feature_vectors)):
					params_fv = params.feature_vectors[i]
					Tstable = int(params_fv[iTstable])
					Tepisode = int(params_fv[iTepisode])
					T = Tstable + Tepisode
					fv = data.feature_vectors[i]
					# Note: This is a low-effort way to detect errors, but it would be
					# nice to read errors from a separate list file.
					if len(fv) != len(data.attributes):
						print( "Error: " + experiment )
						break
					if options.named_faults:
						Nfaults = int( params_fv[params.attribute_index( "Nfaults" )] )
						faults = []
						for i in range(0, Nfaults):
							ifault = params.attribute_index( "fault" + str(i) )
							faults.append( params_fv[ifault] )
						faults = ";".join( faults )
					else:
						faults = params_fv[params.attribute_index( "fault" )]
					V = 0.0
					NLoadShed = 0
					NIsland = 0
					t_blackout = T+1
					for t in range(0, T+1):
						if options.legacy:
							if t == T:
								# Legacy files don't include reward for time 'T', so
								# we assume it's the same as at T-1
								i = data.attribute_index( "t" + str(T-1) )
							else:
								i = data.attribute_index( "t" + str(t) )
						else:
							i = data.attribute_index( "r" + str(t) )
							if t < T:
								ai = data.attribute_index( "a" + str(t) )
								a = fv[ai]
								if "ShedZone" in a:
									NLoadShed += 1
								elif "Island" in a:
									NIsland += 1
						vt = float(fv[i])
						V += vt
						# FIXME: We shouldn't need 't > 0', but there is a bug
						# with the problem domain that causes reward to be 0
						# at time t = 0.
						if t > 0 and vt == 0 and t_blackout > t:
							t_blackout = t
						if t == T: # Do this here so we don't have to lookup index again
							Rend = float(fv[i])
					# Special case to fix early results that had the fault occur one
					# step later: Duplicate last time step reward to compensate
					if options.legacy:
						V -= float(fv[data.attribute_index( "t8" )])
						V += Rend
					out_fv = list( map( str, [experiment, faults, V, Rend, t_blackout, NLoadShed, NIsland] ) )
					assert( len(out_fv) == len(out_data.attributes) )
					out_data.feature_vectors.append( out_fv )

if options.sort:
	def key( fv ):
		k = [float(fv[2]), float(fv[3])]
		k.extend( map( float, faults.split( ";" ) ) )
		return tuple( k )
	out_data.feature_vectors.sort( key=key )
output_file.write( repr(out_data) )
output_file.close()
