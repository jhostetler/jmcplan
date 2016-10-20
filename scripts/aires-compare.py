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

import itertools
import math
from operator import itemgetter
from optparse import OptionParser
import re
import sys

from csv import CsvAttribute, CsvDataset



# ----------------------------------------------------------------------------
# Main
# ----------------------------------------------------------------------------

cl_parser = OptionParser( usage="%prog [options] file" )
cl_parser.add_option( "-o", dest="output_file", type="string", default="-",
					  help="The file to write the output to (default: stdout)" )
cl_parser.add_option( "--complete", dest="complete", action="store_true", default=False,
					  help="If specified, only parameterizations for which results are available for all budgets are considered." )
(options, args) = cl_parser.parse_args();

if options.output_file == "-":
	# Saying 'open( "sys.stdout" )' doesn't seem to accomplish this
	output_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )

fault_str = "faults"
out_attributes = [
"Domain", "domain_parameters", fault_str, "Algorithm", "algorithm_parameters", "budget_type", "budget",
"Vc", "Vu", "dV", "Rc_end", "Ru_end", "dR", "NLoadShed", "NIsland"
]

out_data = CsvDataset( attributes=[CsvAttribute( a ) for a in out_attributes], feature_vectors=[] )
	
with open( args[0], "r" ) as input_file:
	c_data = CsvDataset( input_file )
with open( args[1], "r" ) as input_file:
	u_data = CsvDataset( input_file )
	
# Index map for fault sets in baseline data
u_idx = dict()
for i in range(0, len(u_data.feature_vectors)):
	u_fv = u_data.feature_vectors[i]
	u_idx[u_fv[u_data.attribute_index(fault_str)]] = i
	
for c_fv in c_data.feature_vectors:
	faults = c_fv[c_data.attribute_index(fault_str)]
	Vc = float( c_fv[c_data.attribute_index("V")] )
	Rc_end = float( c_fv[c_data.attribute_index("R_end")] )
	ui = u_idx[faults]
	u_fv = u_data.feature_vectors[ui]
	Vu = float( u_fv[u_data.attribute_index("V")] )
	Ru_end = float( u_fv[u_data.attribute_index("R_end")] )
	
	out_fv = [""] * len(out_attributes)
	out_fv[0] = c_fv[c_data.attribute_index("experiment")]
	out_fv[1] = ""			# TODO domain_params
	out_fv[2] = faults
	out_fv[3] = "pr"		# TODO algorithm
	out_fv[4] = ""			# TODO algorithm_params
	out_fv[5] = "trajectory"	# TODO budget_type
	out_fv[6] = "99999"		# TODO budget
	out_fv[7] = Vc
	out_fv[8] = Vu
	out_fv[9] = Vc - Vu
	out_fv[10] = Rc_end
	out_fv[11] = Ru_end
	out_fv[12] = Rc_end - Ru_end
	out_fv[13] = c_fv[c_data.attribute_index("NLoadShed")]
	out_fv[14] = c_fv[c_data.attribute_index("NIsland")]
	out_data.feature_vectors.append( out_fv )
	
output_file.write( repr(out_data) )
output_file.close()
