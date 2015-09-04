#!/usr/bin/env python

# ----------------------------------------------------------------------------
# arff-reshape.py
#
# Applys a series of transformations to an .arff file, including:
#	- Deleting attributes by name or index.
#	- Adding new attributes at locations specified by index or by the name of
#	  neighboring attributes, with constant values supplied on the command 
#	  line.
#	- Adding attributes drawn from a second file, possibly requiring that the
#	  feature vector from which the value is drawn matches the feature vector
#	  in the reshaped file at one or more positions.
#	- Adding attributes computed by applying an arbitrary expression to an
#	  arbitrary list of existing attributes.
#
# This documentation refers to the file being changed as the 'input' file and
# the file from which new attributes are drawn as the 'from' file.
#
# There are four general kinds of commands: location commands, value commands,
# delete commands, and align commands. Location commands specify the position 
# in the feature vectors in the input file where the value specified by the 
# next value command will be inserted. Value commands must be immediately 
# preceded by a location command, and specify what kind of value to insert at 
# the location. Delete commands can stand alone, and specify that a particular 
# column should be deleted from the input file. Align commands specify how the
# feature vectors from the 'from' file should be matched to those in the
# input file.
#
# Here is a summary of the commands:
# 	- Location commands:
#		--append		
#			Put the value at the end of the feature vectors
#		--insert-at <index>
#			Insert the value at the specified integer index
#		--insert-after <name>
#			Insert the value after the named attribute
#		--insert-before <name>
#			Insert the value before the named attribute
#
#	- Value commands:
#		--attribute <name>
#			Add the named attribute, drawn from the 'from' file.
#		--constant <attr-name> <attr-type> <value>
#			Add a new attribute called 'attr-name' with type 'attr-type', and
#			assign the value 'value' to it in each feature vector.
#		--eval <attr-name> <attr-type> <arg-list> <lambda-expression>
#			Add a new attribute called 'attr-name' with type 'attr-type'. For
#			each feature vector, assign the value obtained by evaluating
#			'lambda-expression' on the attributes specified in 'arg-list'.
#			'arg-list' must be a string containing a Python list. For each
#			element of the list, if the element is a string, it will be
#			interpreted as the name of an attribute, and if it is an integer,
#			it will be interpreted as a column index. The arguments are
#			unpacked sequentially into the lambda expression. The arguments are
#			always strings.
#
#			Example usage: say I have a file of this form:
#
#			@attribute price numeric
#			@attribute quantity numeric
#			@data
#			12,4
#			10,5
#			...
#
#			Suppose I want to add a third attribute that contains the total
#			value of the items on hand (ie. price * quantity). I can do this
#			as follows:
#
#			arff-reshape.py --append --eval "value" "numeric" \
#				"['price', 'quantity']" "lambda p, q: int(p) * int(q)" ...
#
#			The resulting file would look like this:
#
#			@attribute price numeric
#			@attribute quantity numeric
#			@attribute value numeric
#			@data
#			12,4,48
#			10,5,50
#			...
#
#	- Delete commands:
#		--delete-matches <regex>
#			Delete all attributes matching 'regex'
#		--delete-attribute <name>
#			Delete the attribute called 'name'
#		--delete-column <index>
#			Delete the column with the given index
#
#	- Align commands:
#		--align <name>
#			Require that the value of the attribute with the given name in a
#			feature vector in the input file be the same as the value of that
#			attribute in the feature vector in the 'from' file from which we
#			will draw the value to add to the input file. You can specify this
#			command multiple times. If the script cannot find /exactly one/
#			feature vector in the 'from' file that matches all required
#			attribute values for a vector in the input file, it will abort.
#
#			This command is easiest to understand with an example. Say I have
#			an unlabeled dataset with primary key 'foo' and a separate label
#			file that contains only the primary key and the label. I want to 
#			make sure that the labels from the label file are matched with the 
#			correct feature vector in the input file. In this case, I would 
#			add '--align "foo"' to my command line.
#
#			If you do not specify any --align flags, the vectors will be drawn
#			sequentially from both files. If the 'from' file contains fewer
#			feature vectors than the input file, the script will abort.
#
# IMPORTANT: You can specify many different commands in a single command line.
# The commands execute *sequentially*. This means that the index of some
# attributes *might change* between commands. If you do something like this:
#		--insert-at 2 --attribute 'foo' --insert-at 1 --attribute 'bar'
# the feature vectors in your reshaped file will look like this:
#		oldval1,<barval>,oldval2,<fooval>,...
# which might not be what you wanted.
#
# To avoid surprises, specify your commands in left-to-right order.
#
# The beneficial upshot of the sequential processing is that commands that
# come later in the chain can refer to attributes added by previous commands.
# So, in our example above, this is perfectly fine:
#		--insert-at 2 --attribute 'foo' --insert-after 'foo' --attribute 'bar'
# This is the result you get:
#		oldval1,oldval2,<fooval>,<barval>,...
#
# Usage:
#	python arff-reshape.py [options] file
#
# Known issues:
#	- The --delete-matches feature will not affect attributes added by commands
#	  that precede it. This is a limitation of the structure of the
#	  implementation. Because we make all of the changes to the header before
#	  making any changes to the body, we have to cache the indices of columns
#	  that we're going to change, because changing the header would make the
#	  code that looks up the indices give a different result. To do this
#	  feature properly, the code would need to be restructured so that each
#	  command is applied to both the header and the body before moving on to
#	  the next command. The current implementation is a holdover from the
#	  original version of this script, which was copied from a script designed
#	  to produce incremental output for piping.
#
# TODO:
#	- The 'optparse' module is deprecated in favor of 'argparse', but
#	  'argparse' is not available in 2.4.3 (requires 2.7+)
#
# History:
#	[2010/11/03:hostetje] Created by copying 'arff-merge.py'
#	[2010/11/04:hostetje] Many additional features, making this sort of an
#		all-purpose .arff file maniuplation script.
#	[2010/12/03:hostetje] Added the --eval feature.
#	[2011/04/14:hostetje] Added --delete-matches feature.
# ----------------------------------------------------------------------------

from arff import ArffDataset, ArffAttribute

from optparse import OptionParser
import re
import sys

# ----------------------------------------------------------------------------
# Definitions
# ----------------------------------------------------------------------------

# The meat of the script
def reshape( input_dataset, from_dataset, commands, align_attributes ):
	check_name_collisions( input_dataset, commands )
	operate_header( input_dataset, from_dataset, commands )
	operate_feature_vectors( input_dataset, from_dataset, commands, align_attributes )
	

# See if we're trying to add an attribute with the same name as an attribute
# that is in 'input_dataset' and will not have been deleted by the time we
# try to add it.
def check_name_collisions( input_dataset, commands ):
	deleted = []
	for comm in commands:
		if comm[0] == "constant" and comm[1][0] not in deleted:
			try:
				input_dataset.attribute( comm[1][0] )
			except KeyError:
				pass
			else:
				sys.exit( "Attribute named '" + comm[1][0] + "' already in"
						  + " input dataset" )
		elif comm[0] == "attribute" and comm[1] not in deleted:
			try:
				input_dataset.attribute( comm[1] )
			except KeyError:
				pass
			else:
				sys.exit( "Attribute named '" + comm[1] + "' already in"
						  + " input dataset" )
		elif comm[0] == "delete-column":
			deleted.append( input_dataset.attributes[int(comm[1])].name )
		elif comm[0] == "delete-attribute":
			deleted.append( comm[1] )

# If 'command' refers to attributes by name, convert it to an equivalent
# command that refers only to column indices.
#
# It is necessary to do this up front because messing with the header would
# cause the attribute_index() query to return different values when we get to
# processing the body.
def resolve_index( input_dataset, command ):
	if command[0] == "append":
		command[0] = "insert-at"
		command[1] = len(input_dataset.attributes)
	elif command[0] == "insert-after":
		command[0] = "insert-at"
		command[1] = input_dataset.attribute_index( command[1] ) + 1
	elif command[0] == "insert-before":
		command[0] = "insert-at"
		command[1] = input_dataset.attribute_index( command[1] )
	elif command[0] == "insert-at":
		command[1] = int(command[1])
	elif command[0] == "delete-attribute":
		command[0] = "delete-column"
		command[1] = input_dataset.attribute_index( command[1] )
	elif command[0] == "delete-column":
		command[1] = int(command[1])
	elif command[0] == "eval":
		arg_list = eval( command[1][2] )
		index_list = []
		for a in arg_list:
			if isinstance( a, str ):
				index_list.append( input_dataset.attribute_index( a ) )
			else:
				index_list.append( a )
		command[1][2] = index_list
	else:
		pass
		
# Turns the 'delete-matches' command into a series of 'delete-attribute'
# commands.
def expand_commands( input_dataset, commands ):
	result = []
	for command in commands:
		if command[0] == "delete-matches":
			matches = []
			for attr in input_dataset.attributes:
				if re.match( command[1], attr.name ):
					matches.append( ["delete-attribute", attr.name] )
			result.extend( matches )
		else:
			result.append( command )
	return result

# Apply commands to the header.
def operate_header( input_dataset, from_dataset, commands ):
	location = None
	for comm in commands:
		resolve_index( input_dataset, comm )
		if comm[0] == "delete-column":
			input_dataset.attributes.pop( int(comm[1]) )
		# Location statements
		elif comm[0] == "insert-at":
			location = int(comm[1])
		# Make sure they've already supplied a location
		elif location is None:
			sys.exit( "Value statement '--" + comm[0] + " " + comm[1] 
					  + " not preceded by location statement" )
		# Value statements
		elif comm[0] == "attribute":
			if from_dataset is None:
				sys.exit( "--attribute statement requires a 'from' file" )
			input_dataset.attributes.insert( 
				location, from_dataset.attribute( comm[1] ) )
			location = None
		elif comm[0] == "constant" or comm[0] == "eval":
			input_dataset.attributes.insert( 
				location, ArffAttribute( comm[1][0], comm[1][1] ) )
			location = None
		# Unrecognized
		else:
			sys.exit( "Unrecognized statement '--" + comm[0] )
	
# Apply commands to the feature vectors.
def operate_feature_vectors( input_dataset, from_dataset, commands, align_attributes ):
	from_vector_count = 0
	for v in input_dataset.feature_vectors:
		location = None
		for comm in commands:
			if comm[0] == "delete-column":
				v.pop( int(comm[1]) )
			# Location statements
			elif comm[0] == "insert-at":
				location = int(comm[1])
			# Make sure they've already supplied a location
			elif location is None:
				sys.exit( "Value statement '--" + comm[0] + " " + comm[1] 
						  + " not preceded by location statement" )
			# Value statements
			elif comm[0] == "attribute":
				if from_dataset is None:
					sys.exit( "--attribute statement requires a 'from' file" )
				from_vector = None
				if len(align_attributes) != 0:
					matches = find_match( input_dataset, v, from_dataset,
										  from_dataset.feature_vectors, 
										  align_attributes )
					if len(matches) == 1:
						from_vector = matches[0]
					elif len(matches) == 0:
						sys.exit( "Couldn't align " + ",".join( v ) )
					else:
						sys.exit( "Multiple matches for " + ",".join( v ) )
				elif from_vector_count < len(from_dataset.feature_vectors):
					from_vector = from_dataset.feature_vectors[from_vector_count]
					from_vector_count += 1
				else:
					sys.exit( "More feature vectors in 'input_file' than in 'from_file'" )
				idx = from_dataset.attribute_index( comm[1] )
				v.insert( location, from_vector[idx] )
				location = None
			elif comm[0] == "constant":
				v.insert( location, comm[1][2] )
				location = None
			elif comm[0] == "eval":
				arg_list = comm[1][2]
				args = []
				for a in arg_list:
					assert( isinstance( a, int ) )
					args.append( v[a] )
				fun = eval( comm[1][3] )
				result = fun( *args )
				v.insert( location, result )
				location = None
			# Unrecognized
			else:
				sys.exit( "Unrecognized command '--" + comm[0] )

# Find a feature vector in 'from_vectors' that satisfies all of the alignment
# criteria.
def find_match( input_dataset, input_vector, from_dataset, from_vectors, align_attributes ):
	align_indices = []
	# TODO: For some reason, this doesn't throw an exception if 'name' is not
	# in one of the datasets.
	for name in align_attributes:
		align_indices.append( (input_dataset.attribute_index( name ), 
							   from_dataset.attribute_index( name )) )

	matches = []
	for fv in from_vectors:
		for (i, f) in align_indices:
			if input_vector[i] == fv[f]:
				matches.append( fv )
	return matches

# Command-line parsing callback. Adds the command to a list.
def add_command( option, opt, value, parser, *args, **kwargs ):
	if isinstance( value, tuple ):
		kwargs["commands"].append( [kwargs["tag"], list(value)] )
	else:
		kwargs["commands"].append( [kwargs["tag"], value] )

# ----------------------------------------------------------------------------
# Main
# ----------------------------------------------------------------------------

commands = []

cl_parser = OptionParser( usage="%prog [options] file" )
cl_parser.add_option( "-o", dest="output_file", type="string", default="-",
					  help="The file to write the output to (default: stdout)" )
cl_parser.add_option( "-f", "--from", dest="from_file", type="string", default=None,
					  help="The .arff file to draw the new attributes from" )
cl_parser.add_option( "--delete-column", type="int",
					  action="callback", callback=add_command,
					  callback_kwargs={"tag" : "delete-column", "commands" : commands},
					  metavar="INDEX",
					  help="Delete the column with the given index." )
cl_parser.add_option( "--delete-matches", type="string",
					  action="callback", callback=add_command,
					  callback_kwargs={"tag" : "delete-matches", "commands" : commands},
					  metavar="REGEX",
					  help="Delete all attributes matching <regex>. NOTE: If other commands add attributes that match the regex, these attributes WILL NOT be affected, even if --delete-matches comes after the command that added them." )
cl_parser.add_option( "--delete-attribute", type="string",
					  action="callback", callback=add_command,
					  callback_kwargs={"tag" : "delete-attribute", "commands" : commands},
					  metavar="ATTR-NAME",
					  help="Delete the named attribute." )
cl_parser.add_option( "--append", nargs=0,
					  action="callback", callback=add_command,
					  callback_kwargs={"tag" : "append", "commands" : commands},
					  help="Begins an 'append' command. The next command must"
					  	   + " specify what to append""" )
cl_parser.add_option( "--insert-before", type="string", default=None,
					  action="callback", callback=add_command,
					  callback_kwargs={"tag" : "insert-before", "commands" : commands},
					  metavar="ATTR-NAME",
					  help="Begins an 'insert-before' command. The argument"
					  	   + " specifies the name of the attribute to insert"
						   + " before. The next command must specify what to"
						   + " insert." )
cl_parser.add_option( "--insert-after", type="string", default=None,
					  action="callback", callback=add_command,
					  callback_kwargs={"tag" : "insert-after", "commands" : commands},
					  metavar="ATTR-NAME",
					  help="Begins an 'insert-after' command. The argument"
						   + " specifies the name of the attribute to insert"
						   + " after. The next command must specify what to"
						   + " insert." )
cl_parser.add_option( "--insert-at", type="int", default=None,
					  action="callback", callback=add_command,	
					  callback_kwargs={"tag" : "insert-at", "commands" : commands},
					  metavar="INDEX",
					  help="Begins an 'insert-at' command. The argument"
					  	   + " specifies the index to insert at. The value is"
						   + " inserted before the value that is currently at"
						   + " that index. The next command must specify what"
						   + " to insert." )
cl_parser.add_option( "--constant", type="string", nargs=3,
					  action="callback", callback=add_command,
					  callback_kwargs={"tag" : "constant", "commands" : commands},
					  metavar="ATTR-NAME ATTR-TYPE VALUE",
					  help="Causes the specified constant to be placed at the"
						   + " current location. Must be preceded by a location"
						   + " command, such as --insert-at." )
cl_parser.add_option( "--attribute", type="string",
					  action="callback", callback=add_command,
					  callback_kwargs={"tag" : "attribute", "commands" : commands},
					  metavar="ATTR-NAME",
					  help="Causes the value of the specified attribute in"
					  	   + " the matching record of the 'from' file to be"
						   + " placed at the current location. Must be preceded"
						   + " by a location command, such as --insert-at." )
cl_parser.add_option( "--eval", type="string", nargs=4,
					  action="callback", callback=add_command,
					  callback_kwargs={"tag" : "eval", "commands" : commands},
					  metavar="ATTR-NAME ATTR-TYPE ARG-LIST LAMBDA-EXP",
					  help="Causes a new attribute to be added at the current"
						   + " location. The value of the attribute is"
						   + " calculated by evaluating the supplied lambda"
						   + " expression on the supplied arguments at each"
						   + " feature vector. Must be preceded by a location"
						   + " command, such as --insert-at." )
cl_parser.add_option( "--align", dest="align_attributes", type="string",
					  default=[], action="append",
					  metavar="ATTR-NAME",
					  help="Require that the named attribute match in both"
					  	   + " datasets. Specifically, an error will be raised"
						   + " if there exists a feature vector in the input"
						   + " dataset such that there is no feature vector in"
						   + " the 'from' dataset that matches it at the given"
						   + " attribute. You can specify this flag multiple"
						   + " times. If omitted, feature vectors will be"
						   + " matched in the order they appear in the"
						   + " datasets." )

(options, args) = cl_parser.parse_args();

if len( args ) == 0:
	cl_parser.error( "No input file" )
elif len( args ) > 1:
	print( "WARNING: Multiple input files; ignoring all but the first" )

input_file = open( args[0], "r" )

from_file = None
if options.from_file:
	from_file = open( options.from_file, "r" )

if options.output_file == "-":
	# Saying 'open( "sys.stdout" )' doesn't seem to accomplish this
	output_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )

input_dataset = ArffDataset( input_file )
input_file.close()

from_dataset = None
if from_file is not None:
	from_dataset = ArffDataset( from_file )
	from_file.close()
	
expanded_commands = expand_commands( input_dataset, commands )

try:
	reshape( input_dataset, from_dataset, expanded_commands, options.align_attributes )
except KeyError, ex:
	sys.exit( "Attribute " + str(ex) 
			  + " named in command line not present in required file" )

output_file.write( repr(input_dataset) )
output_file.close()
