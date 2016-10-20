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

#!/usr/bin/env python

# ----------------------------------------------------------------------------
# csv-cat.py
#
# Combines multiple .csv files into a single .csv file.
#
# Usage:
#	filter-arff.py [options] files
#
# Options:
#	-o <file> Output results to <file> (default: stdout)
#
# Known issues:
#
# TODO:
#	- The 'optparse' module is deprecated in favor of 'argparse', but
#	  'argparse' is not available in 2.4.3 (requires 2.7+)
#
# History:
#	[sometime in 2014] Created from 'filter-arff.py'
#	[2015/01/28:hostetje] Improved to handle merging files with different
#		header sets. We assume that one of the files has a header set that is
#		a superset of all the other files. Will not work otherwise.
# ----------------------------------------------------------------------------

import math
from optparse import OptionParser
import re
import sys

from csv import CsvDataset

# ----------------------------------------------------------------------------
# Globals
# ----------------------------------------------------------------------------

# The file to write the output to
output_file = None

# Cmd line args
options = None
args = None

# ----------------------------------------------------------------------------
# Definitions
# ----------------------------------------------------------------------------

def on_error( s ):
	global options
	if options.loose:
		print( "Warning: " + s )
	else:
		raise ValueError( s )
		
def process_data( master, data ):
	global options
	for fv in data.feature_vectors:
		if len(fv) != len(data.attributes):
			on_error( "len(fv) " + str(len(fv)) + " != len(attributes) " + str(len(data.attributes)) )
		expanded_fv = ["" for i in range(0, len(master.attributes))]
		for i in range(0, len(data.attributes)):
			if i < len(fv):
				j = master.attribute_index( data.attributes[i] )
				expanded_fv[j] = fv[i]
		master.feature_vectors.append( expanded_fv )
	
class HeaderAccumulator:
	def __init__( self ):
		self.attributes = []
		self.attribute_set = set()
		
	def add( self, attributes ):
		for a in attributes:
			if a.name not in self.attribute_set:
				self.attribute_set.add( a.name )
				self.attributes.append( a )

# ----------------------------------------------------------------------------
# Main
# ----------------------------------------------------------------------------

cl_parser = OptionParser( usage="%prog [options] file" )
cl_parser.add_option( "-o", dest="output_file", type="string", default="-",
					  help="The file to write the output to (default: stdout)" )
cl_parser.add_option( "--delim", dest="delim", type="string", default=",",
					  help="""The delimiter string (default: ","). """ )
cl_parser.add_option( "--loose", dest="loose", action="store_true", default=False,
					  help="If specified, files are not checked for header or column count equality." )
(options, args) = cl_parser.parse_args();

if options.output_file == "-":
	output_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )

# Find the largest set of headers
headers = HeaderAccumulator()
for file in args:
	input_file = open( file, "r" )
	in_data = CsvDataset( input_file )
	print( file + ": " + str(len(in_data.attributes)) )
	headers.add( in_data.attributes )
	input_file.close()
print( headers.attributes )

# Treat the largest header set as canonical
master = CsvDataset( attributes=headers.attributes[:], feature_vectors=[] )
for file in args:
	input_file = open( file, "r" )
	in_data = CsvDataset( input_file )
	process_data( master, in_data )
	input_file.close()

output_file.write( repr(master) )
output_file.close()
