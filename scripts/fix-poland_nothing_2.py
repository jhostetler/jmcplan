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
cl_parser.add_option( "-i", dest="header_file", type="string", default=None,
					  help="The canonical header file (default: stdin)" )
cl_parser.add_option( "--delim", dest="delim", type="string", default=",",
					  help="""The delimiter string (default: ","). """ )
cl_parser.add_option( "--loose", dest="loose", action="store_true", default=False,
					  help="If specified, files are not checked for header or column count equality." )
(options, args) = cl_parser.parse_args();

def get_header_string( file ):
	return file.readline().strip()

# try:
	# if options.header_file is None:
		# header_file = sys.stdin
	# else:
		# header_file = open( options.header_file, "r" )
	# header_dataset = CsvDataset( header_file, headers=True )
	# print( "Canonical: " + ",".join( repr(h) for h in header_dataset.attributes ) )
# finally:
	# header_file.close()

for rewards_filename in args:
	missing = None
	with open( rewards_filename, "r+" ) as rewards_file:
		lines = sum( 1 for line in rewards_file )
		
		if lines == 2:
			# Assumed good
			print( rewards_filename )
		
		if False:
		# if lines == 1:
			print( "Missing header: " + rewards_filename )
			rewards_file.seek( 0, 0 )
			bad_dataset = CsvDataset( rewards_file, headers=False )
			bad_dataset.attributes = header_dataset.attributes[:]
			params_filename = os.path.dirname( rewards_filename ) + ".csv"
			print( params_filename )
			with open( params_filename ) as params_file:
				params_dataset = CsvDataset( params_file )
			params = params_dataset.feature_vectors[0]
			# Fix the feature vectors
			fv = bad_dataset.feature_vectors[0]
			fv[0] = str(0) # Filled with NUL for some reason
			for i in range(0, 3):
				# Copy missing parameters
				attr_name = header_dataset.attributes[i].name
				attr_idx = params_dataset.attribute_index( attr_name )
				print( "\tInsert " + attr_name + " = " + params[attr_idx] )
				fv.insert( i, params[attr_idx] )
			# Commit changes
			rewards_file.seek( 0, 0 )
			rewards_file.write( repr(bad_dataset) )
