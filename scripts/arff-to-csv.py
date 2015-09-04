#!/usr/bin/env python

# ----------------------------------------------------------------------------
# arff-to-csv.py
#
# Converts an .arff file to a .csv file. CSV is a strict subset of ARFF, so
# this is completely unsupervised.
#
# Usage:
#	arff-to-csv.py [options] arff_file
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
#	[2010/10/21:hostetje] Created
#	[2010/10/22:hostetje] Added missing docstrings
#	[2010/10/28:hostetje] Changed a \w to a \S in the parse_attribute regex;
#		this was causing it to reject attribute names containing hyphens.
#	[2010/11/22:hostetje] Rewritten to use the ArffDataset and CsvDataset
#		classes.
#	[2012/03/01:hostetje] Moved ARFF -> CSV conversion code to ArffDataset.
# ----------------------------------------------------------------------------

from arff import ArffDataset
from csv import CsvDataset, CsvAttribute

from optparse import OptionParser
import re
import sys

# ----------------------------------------------------------------------------
# Main
# ----------------------------------------------------------------------------

cl_parser = OptionParser( usage="%prog [options] file" )
cl_parser.add_option( "-o", dest="output_file", type="string", default=None,
					  help="The file to write the output to (default: stdout)" )
(options, args) = cl_parser.parse_args();

if len( args ) == 0:
	cl_parser.error( "No input file" )
elif len(args) > 1:
	print( "WARNING: Multiple input files; ignoring all but the first" )

arff_file = open( args[0], "r" )

if options.output_file is None:
	# Saying 'open( "sys.stdout" )' doesn't seem to accomplish this
	output_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )

arff_dataset = ArffDataset( arff_file )
csv_dataset = CsvDataset.from_arff_dataset( arff_dataset )

output_file.write( repr(csv_dataset) )
output_file.close()
