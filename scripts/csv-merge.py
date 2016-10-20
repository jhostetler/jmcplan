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
# filter-arff.py
#
# Filters an .arff file by applying regular expressions to one or more
# headers of each record and returning only those records that match
# the expression.
#
# Accepts piped input and generates incremental output.
#
# Usage:
#	filter-arff.py [options] logfile
#
# Options:
#	--filter <field> <regex> Filter out all records whose 'field' attribute
#		does not match 'regex'. The match uses the 'match' function (ie. not
#		the 'search' function). The --filter argument can be supplied 0 or
#		more times.
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
#	[2010/10/22:hostetje] Minor cleanup of command-line parsing
#	[2010/10/28:hostetje] Changed a \w to a \S in the parse_attribute regex;
#		this was causing it to reject attribute names containing hyphens.
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

master = None
Ngames_idx = None
mean_attributes = []
var_attributes = []
min_attributes = []
max_attributes = []
acc = dict()

# ----------------------------------------------------------------------------
# Definitions
# ----------------------------------------------------------------------------

def on_error( s ):
	global options
	if options.loose:
		print( "Warning: " + s )
	else:
		raise ValueError( s )
		
def combine( exemplar, fv ):
	Nexemplar = int(exemplar[Ngames_idx])
	Nfv = int(fv[Ngames_idx])
	combined = exemplar[:]
	combined[Ngames_idx] = Nexemplar + Nfv
	for i in mean_attributes:
		exi = float(exemplar[i])
		fvi = float(fv[i])
		mu_combined = (Nexemplar*exi + Nfv*fvi) / (Nexemplar + Nfv)
		combined[i] = mu_combined
	for i in var_attributes:
		# Note: assuming 'mean' is always listed immediately before 'variance'
		mu_combined = float(combined[i-1])
		mu_exemplar = float(exemplar[i-1])
		var_exemplar = float(exemplar[i])
		mu_fv = float(fv[i-1])
		var_fv = float(fv[i])
		var_combined = ( (Nexemplar*(var_exemplar + (mu_exemplar - mu_combined)**2) 
							+ Nfv*(var_fv + (mu_fv - mu_combined)**2))
						  / (Nexemplar + Nfv) )
		combined[i] = var_combined
	for i in min_attributes:
		combined[i] = min(float(exemplar[i]), float(fv[i]))
	for i in max_attributes:
		combined[i] = max(float(exemplar[i]), float(fv[i]))
	return combined
		
def process_data( data ):
	global acc, headers, master, Ngames_idx, options
	if master is None:
		master = CsvDataset( attributes=data.attributes[:], feature_vectors=[] )
		Ngames_idx = master.attribute_index( "Ngames" )
		mean_attributes = [master.attribute_index( a ) for a in [
							"mean", "state_branching_mean", "action_branching_mean", "tree_depth_mean", "steps_mean"]]
		var_attributes = [master.attribute_index( a ) for a in [
							"var", "state_branching_var", "action_branching_var", "tree_depth_var", "steps_var"]]
		min_attributes = [master.attribute_index( a ) for a in ["steps_min"]]
		max_attributes = [master.attribute_index( a ) for a in ["steps_max"]]
	else:
		if len(master.attributes) != len(data.attributes):
			on_error( "Unequal column count" )
		for i in range(0, len(master.attributes)):
			if master.attributes[i].name != data.attributes[i].name:
				on_error( "Different headers" )
	hidx = None
	if options.combine is not None:
		hidx = [data.attribute_index( name ) for name in eval( options.combine )]
		# print( eval( options.combine ) )
	for fv in data.feature_vectors:
		key = len(acc)
		if hidx is not None:
			key = tuple(fv[i] for i in hidx)
		try:
			exemplar = acc[key]
			acc[key] = combine( exemplar, fv )
		except KeyError: 
			acc[key] = fv[:]

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
cl_parser.add_option( "--no-headers", dest="no_headers", action="store_true", default=False,
					  help="If specified, indicates that the files have no header rows." )
cl_parser.add_option( "--combine", dest="combine", type="string", default=None,
					  help="""A python list of header names. If specified, results will be combined for
							  all rows that have the same value in all specified columns.""" )
(options, args) = cl_parser.parse_args();

if options.output_file == "-":
	# Saying 'open( "sys.stdout" )' doesn't seem to accomplish this
	output_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )

for file in args:
	print( file )
	input_file = open( file, "r" )
	in_data = CsvDataset( input_file )
	process_data( in_data )
	input_file.close()
	
conf_idx = master.attribute_index( "conf" )
var_idx = master.attribute_index( "var" )
Ngames_idx = master.attribute_index( "Ngames" )
for fv in acc.values():
	fv[conf_idx] = 1.96 * math.sqrt( float(fv[var_idx]) ) / math.sqrt( float(fv[Ngames_idx]) )
master.feature_vectors = [map(str, v) for v in acc.values()]
output_file.write( repr(master) )
output_file.close()
