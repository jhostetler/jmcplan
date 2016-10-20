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

from optparse import OptionParser
import os

cl_parser = OptionParser( usage="%prog [options] file..." )
cl_parser.add_option( "-o", dest="output_file", type="string", default="-",
					  help="The file to write the output to (default: stdout)" )
cl_parser.add_option( "--fix-fault-time", action="store_true", dest="fix_fault_time", default=False,
					  help="Enable correction for early experiments that activated the fault one step later" )
(options, args) = cl_parser.parse_args();

if options.output_file == "-":
	output_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )
	
out_fields = ["Tstable", "Tepisode", "Vu", "Ru_end", "Nfaults", "fault0", "fault1"]
out_data = CsvDataset( attributes=map( CsvAttribute, out_fields ),
					   feature_vectors=[] )

for filename in args:
	with open( filename ) as f:
		data = CsvDataset( f )
		[iTstable, iTepisode, iNfaults, ifault0, ifault1] =	map( 
			data.attribute_index, ["Tstable", "Tepisode", "Nfaults", "fault0", "fault1"] )
		for fv in data.feature_vectors:
			Tstable = int(fv[iTstable])
			Tepisode = int(fv[iTepisode])
			T = Tstable + Tepisode
			r = 0.0
			Rend = 0
			for t in range(0, T):
				i = data.attribute_index( "t" + str(t) )
				r += float(fv[i])
				if t == T - 1:
					Rend = float(fv[i])
			# Special case to fix early results that had the fault occur one
			# step later: Duplicate last time step reward to compensate
			if opt.fix_fault_time:
				r -= float(fv[data.attribute_index( "t8" )])
				r += Rend
			out_fv = map( str, [Tstable, Tepisode, r, Rend, fv[iNfaults], fv[ifault0], fv[ifault1]] )
			assert( len(out_fv) == len(out_data.attributes) )
			out_data.feature_vectors.append( out_fv )

out_data.feature_vectors.sort( key=lambda fv: (float(fv[2]), float(fv[3]), int(fv[5]), int(fv[6])) )
output_file.write( repr(out_data) )
output_file.close()
