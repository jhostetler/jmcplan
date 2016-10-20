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

Nlanes = 7
road_length = 9
road_vision = road_length - 1
attributes = [ArffAttribute( "x", "numeric" ), ArffAttribute( "y", "numeric" )]
for i in reversed(range(-Nlanes + 1, Nlanes + 1)):
	for j in range(-road_vision, road_vision + 1):
		# if i == 0 and j == 0:
			# continue
		attributes.append( ArffAttribute(
			"car_x" + ("+" if j >= 0 else "") + str(j) + "_y" + ("+" if i >= 0 else "") + str(i), "numeric" ) )
attributes.append( ArffAttribute( src.attributes[-1].name, src.attributes[-1].domain ) )

dest = ArffDataset( relation="frogger_2_single", attributes=attributes, feature_vectors=[] )
for fv in src.feature_vectors:
	result = [0] * len(attributes)
	x = int(fv[0])
	y = int(fv[1])
	result[0] = fv[0]
	result[1] = fv[1]
	idx = 2
	for i in reversed(range(-Nlanes + 1, Nlanes + 1)):
		for j in range(-road_vision, road_vision + 1):
			# if i == 0 and j == 0:
				# continue
			dx = j + x
			dy = i + y
			if dx >= 0 and dx < road_length and dy >= 1 and dy <= Nlanes:
				result[idx] = fv[2 + (dy-1)*road_length + dx]
			idx += 1
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
