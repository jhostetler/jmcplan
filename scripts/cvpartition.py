#!/usr/bin/env python

# ----------------------------------------------------------------------------
# cvpartition.py
#
# Given either a file containing one "instance" per line or a directory
# containing one file per instance, splits the lines or file names into several
# files each containing some of the instances, according to specified criteria.
#
# Usage:
#	cvpartition.py [options] file
#
# Options:
#	-k <int>			The number of partitions (default: 10)
#	--prefix=<string>	A string to prepend to the file names of the partition
#						files. Must end in a '/' if it's supposed to be a 
#						directory!
#
# Input:
#	If 'file' is a regular file, cvpartition will split the lines in the file
#	into partitions. If 'file' is a directory, cvpartition will split the names
#	of all regular files under 'file' into partitions. There is currently no
#	option for recursive traversal.
#
# Known issues:
#
# TODO:
#	- The 'optparse' module is deprecated in favor of 'argparse', but
#	  'argparse' is not available in 2.4.3 (requires 2.7+)
#
# History:
#	[2011/04/27:hostetje] Created
# ----------------------------------------------------------------------------

from optparse import OptionParser
import os
import random
import sys

from arff import ArffDataset
from csv import CsvDataset

def check_args( options ):
	pass
	
def cvpartition( data, options ):
	partitions = []
	random.shuffle( data )
	part_size = len(data) / options.k
	rem = len(data) % options.k
	first = 0
	last = 0
	for unused in range(0, options.k):
		last += part_size
		if rem > 0:
			last += 1
			rem -= 1
		partitions.append( data[first:last] )
		first = last
	assert( last == len(data) )
	return partitions
	
def write_part( part, spec, data, file, options ):
	if options.format == "arff":
		dataset = ArffDataset( relation=(spec.relation + "_part" + str(part)),
							   attributes=spec.attributes,
							   feature_vectors=data )
		file.write( repr(dataset) )
	elif options.format == "csv":
		raise AssertionError()
	else:
		for line in data:
			file.write( line )
			if not line.endswith( "\n" ):
				file.write( "\n" )
				
def read_file( file, options ):
	if options.format == "arff":
		dataset = ArffDataset( file )
		return (dataset, dataset.feature_vectors)
	elif options.format == "csv":
		raise AssertionError()
	else:
		return (None, file.readlines())

# ----------------------------------------------------------------------------
# Main
# ----------------------------------------------------------------------------

cl_parser = OptionParser( usage="%prog [options] file" )
cl_parser.add_option( "-k", type="int", default=10,
					  help="The number of partitions (default: 10)" )
cl_parser.add_option( "--prefix", type="string", default="",
					  help="The prefix to prepend to the file names of the partition files (default: \"\")" )
cl_parser.add_option( "--format", type="string", default=None,
					  help="One of {csv, arff}. If not specified, file is assumed to be one instance per line" )

(options, args) = cl_parser.parse_args();

if len( args ) == 0:
	cl_parser.error( "No input file" )
elif len(args) > 1:
	print( "WARNING: Multiple input files; ignoring all but the first" )

check_args( options )

if os.path.isdir( args[0] ):
	# TODO: 'not isdir' does what you want, but 'isfile' doesn't. Why???
	data = [f for f in os.listdir( args[0] ) if not os.path.isdir( f )]
	(name, ext) = (args[0], ".txt")
else:
	input_file = open( args[0], "r" )
	(name, ext) = os.path.splitext( args[0] )
	(spec, data) = read_file( input_file, options )
	input_file.close()

partitions = cvpartition( data, options )

count = 0
for p in partitions:
	part_file = open( name + "_part" + str(count) + ext, "w" )
	write_part( count, spec, p, part_file, options )
	part_file.close()
	count += 1
assert( count == options.k )
