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

import fileinput
from optparse import OptionParser
import os
import re
import sys

from arff import ArffDataset, ArffAttribute

# ----------------------------------------------------------------------------

cl_parser = OptionParser( usage="%prog [options] file1 file2 ... filen" )
cl_parser.add_option( "-o", dest="output_file", type="string", default="-",
					  help="The file to write the output to (default: stdout)" )
cl_parser.add_option( "-r", "--relation", type="string", default=None,
					  help="The relation name to give to the merged file" )

(options, args) = cl_parser.parse_args();

if len(args) != 1:
	sys.exit( "Requires 1 argument" )

input_file = open( args[0], "r" )
src = ArffDataset( input_file )
input_file.close()

Nreaches = 7
Nhabitats = 6
attributes = []
species_native = 0
species_tamarisk = 1
species_none = 2
for r in range(0, Nreaches):
	for h in range(0, Nhabitats):
		attributes.append( ArffAttribute( "r" + str(r) + "h" + str(h) + "_Native", "numeric" ) )
		attributes.append( ArffAttribute( "r" + str(r) + "h" + str(h) + "_Tamarisk", "numeric" ) )
attributes.append( ArffAttribute( src.attributes[-1].name, src.attributes[-1].domain ) )

dest = ArffDataset( relation="tamarisk2_single", attributes=attributes, feature_vectors=[] )
for fv in src.feature_vectors:
	result = [0] * len(attributes)
	idx = 0
	for r in range(0, Nreaches):
		for h in range(0, Nhabitats):
			fv_idx = r*Nhabitats + h
			if int(fv[fv_idx]) == species_native:
				result[idx] = 1
			elif int(fv[fv_idx]) == species_tamarisk:
				result[idx + 1] = 1
			idx += 2
	result[idx] = fv[-1]
	idx += 1
	assert( idx == len(attributes) )
	dest.feature_vectors.append( result )

if options.output_file == "-":
	# Saying 'open( "sys.stdout" )' doesn't seem to accomplish this
	output_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )
output_file.write( repr(dest) )
output_file.close()
