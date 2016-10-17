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
