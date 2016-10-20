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
# csv-select.py
# ----------------------------------------------------------------------------

import math
from optparse import OptionParser
import os
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
		expanded_fv = ["" for i in range(0, len(master.attributes))]
		for i in range(0, len(fv)):
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
cl_parser.add_option( "--select", dest="select", type="string", default="*",
					  help="Either a Python list of attribute names, or the special string \"*\" meaning \"all\"" )
cl_parser.add_option( "--attribute", dest="attribute", type="string", default=None,
					  help="The attribute to match" )
cl_parser.add_option( "--value", dest="value", type="string", default=None,
					  help="The value to match" )
(options, args) = cl_parser.parse_args();

def open_output( options ):
	if options.output_file == "-":
		# Saying 'open( "sys.stdout" )' doesn't seem to accomplish this
		return sys.stdout
	else:
		return open( options.output_file, "w" )
		

with open( args[0], "r" ) as input_file:
	in_data = CsvDataset( input_file )
	out_data = CsvDataset( attributes=in_data.attributes, feature_vectors=[] )
	idx = in_data.attribute_index( options.attribute )
	for fv in in_data.feature_vectors:
		if fv[idx] == options.value:
			out_data.feature_vectors.append( fv[:] )
with open_output( options ) as output_file:
	output_file.write( repr(out_data) )
