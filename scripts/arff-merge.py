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
# arff-merge.py
#
# Concatenates several .arff files that are instances of the same domain into
# a single file. The @attribute declarations in the first file parsed are
# taken to be canonical. If a subsequent file has incompatible declarations,
# returns with an error.
#
# An "incompatible attribute declaration" means any of the following:
#	- An attribute is defined in one file but not in another
#	- An attribute with the same name is listed in different positions in two
#	  or more files
#	- An attribute with the same name has a different domain specification
#	  in two or more files
#
# Notice that the "different index" condition doesn't necessarily mean that
# the two files are incompatible; they might list exactly the same information
# but in a different order. However, resolving this case is difficult and not
# currently useful.
#
# Usage:
#	python arff-merge.py [options] file1 file2 ... filen
#
# Known issues:
#
# TODO:
#	- Make the printing incremental (instead of storing everything in a list
#	  and then printing it all at once).
#	- Since "integer" means the same as "numeric" to Weka, they should be
#	  considered equal in the attribute specification check.
#	- Add a command line flag to make the merge accept attributes with the
#	  same name as a canonical attribute if their domain is a *subset* of the
#	  domain of the canonical one.
#	- The 'optparse' module is deprecated in favor of 'argparse', but
#	  'argparse' is not available in 2.4.3 (requires 2.7+)
#
# History:
#	[2010/10/20:hostetje] Created
#	[2010/10/21:hostetje] Added support for ARFF comments
#	[2010/10/22:hostetje] Added missing docstring; fixed incorrect dictionary
#		membership check.
#	[2010/10/28:hostetje] Changed a \w to a \S in the parse_attribute regex;
#		this was causing it to reject attribute names containing hyphens.
#	[2011/04/14:hostetje] Re-implemented with the 'arff' package. The
#		'check_attribute_signatures' function is about the only code left from
#		the original version.
# ----------------------------------------------------------------------------

import fileinput
from optparse import OptionParser
import re
import sys

from arff import ArffDataset, ArffAttribute

# ----------------------------------------------------------------------------
# Definitions
# ----------------------------------------------------------------------------

def merge_datasets( canonical, new ):
	check_attributes( canonical, new )
	result = ArffDataset( canonical )
	result.feature_vectors.extend( new.feature_vectors )
	return result

def check_attributes( canonical, new ):
	"""Checks if the attributes in the ArffDataset 'new' are compatible with
	those in 'canonical'.
	"""
	for new_attr in new.attributes:
		try:
			canonical_idx = canonical.attribute_index( new_attr.name )
		except KeyError:
			sys.exit( "@attribute '" + new_attr.name + "' was not in the canonical set" )
		else:
			if canonical_idx != new.attribute_index( new_attr.name ):
				sys.exit( "Domain mismatch; different indices for '" 
							 + canonical_attr.name + "'; canonical index is " 
							 + str(canonical_attr.index) )
			check_attribute_signatures( canonical.attributes[canonical_idx], new_attr )
	
def check_attribute_signatures( canonical, new ):
	"""Compares two attribute signatures for equality.

	We are semi-smart about the comparison. Nominal attributes defined with the
	'{foo, bar}' syntax are compared elementwise in a consistent order.
	However, an attribute of type 'integer' will /not/ match an attribute of
	type 'numeric' even though they are both the same to Weka. Also, if the
	attributes appear at two different indices, they will not match, even
	though the overall attribute definitions might in fact be the same modulo
	ordering.
	"""
	match_obj = re.match( "\s*{(?P<elems>[^}]*)}", canonical.domain )
	if match_obj:
		canonical_elems = re.split( "\s*,\s*", match_obj.group( "elems" ) )
		match_obj = re.match( "\s*{(?P<elems>[^}]*)}", new.domain )
		if not match_obj:
			sys.exit( "Domain mismatch; canonical domain is '" 
								 + canonical.domain + "'" )
		new_elems = re.split( "\s*,\s*", match_obj.group( "elems" ) )
		if sorted( canonical_elems ) != sorted( new_elems ):
			sys.exit( "Domain mismatch; canonical domain is '" 
								 + canonical.domain + "'" )
	elif canonical.domain != new.domain:
		sys.exit( "Domain mismatch; canonical domain is '" 
							 + canonical.domain + "'" )

# ----------------------------------------------------------------------------
# Main
# ----------------------------------------------------------------------------

cl_parser = OptionParser( usage="%prog [options] file1 file2 ... filen" )
cl_parser.add_option( "-o", dest="output_file", type="string", default="-",
					  help="The file to write the output to (default: stdout)" )
cl_parser.add_option( "-r", "--relation", type="string", default=None,
					  help="The relation name to give to the merged file" )

(options, args) = cl_parser.parse_args();

if not options.relation:
	cl_parser.error( "You must specify a relation name with -r (--relation)" )

if options.output_file == "-":
	# Saying 'open( "sys.stdout" )' doesn't seem to accomplish this
	output_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )

merged_dataset = None
first = True
for arg in args:
	file = open( arg, "r" )
	if first:
		merged_dataset = ArffDataset( file )
		merged_dataset.relation = options.relation
		first = False
	else:
		next_dataset = ArffDataset( file )
		merged_dataset = merge_datasets( merged_dataset, next_dataset )
	file.close()
		
output_file.write( repr(merged_dataset) )
output_file.close()
