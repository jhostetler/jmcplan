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

# ----------------------------------------------------------------------------
# arff.py
#
# Defines classes for working with .arff format data files. You can parse such
# a file by creating an instance of ArffDataset with a file object as the
# parameter.
#
# Known issues:
#
# TODO:
#	- Support the 'date' attribute type.
#
# History:
#	[2010/11/03:hostetje] Created by copying code from arff-merge.py
#	[2010/11/05:hostetje] Renamed to 'arff.py'; improved horribly inefficient
#		string concatenation in ArffDataset.__repr__; added copyability; some
#		constructor overloads to ArffDataset.
#	[2010/11/22:hostetje] Updated _process_relation and _process_attribute
#		regexes to handle quoted names that contain whitespace.
#	[2011/04/14:hostetje] We now enclose attribute names in double-quotes
#		by default. The quotes are stripped when the file is read into memory,
#		so if you request the name of an attribute, you will get the un-quoted
#		version. However, the repr() of an attribute will have the name
#		double-quoted.
#	[2011/12/04:jhostetler] Added equi_join() and supporting functions.
# ----------------------------------------------------------------------------

import re
import sys

def remove_comments( line ):
	"""Removes comments (which start with a '%' character) from a line.
	"""
	match_obj = re.match( "^(?P<non_comment>[^%]*)%.*$", line )
	if match_obj:
		return match_obj.group( "non_comment" )
	else:
		return line

class _ParserState:
	HEADER = "header"
	BODY = "body"

	def __init__( self ):
		self.section = _ParserState.HEADER

class ParseError(RuntimeError):
	"""Indicates an error parsing a .arff file.
	"""
	def __init__( self, value ):
		self.value = value

	def __str__( self ):
		return repr( self.value )


class ArffAttribute:
	"""Represents a single attribute of a .arff dataset.

	.name = The name of the attribute.
	.domain = The domain specification.
	"""
	def __init__( self, name, domain ):
		self.name = name
		self.domain = domain

	def __copy__( self ):
		return ArffAttribute( self.name, self.domain )

	def __repr__( self ):
		return "@attribute \"" + self.name + "\" " + self.domain

class ArffDataset:
	"""Represents a .arff dataset, including the relation name, the attribute
	definitions, and the feature vectors.
	
	.relation = The name of the relation
	.attributes = An array of ArffAttribute objects, in the same order as they
		appear in the input file.
	.feature_vectors = An array of arrays representing the feature vectors,
		in the same order as they appear in the input file.
	"""
	def __init__( self, *args, **kwargs ):
		"""Creates an object to represent the dataset stored in 'arff_file'.
		"""
		self.relation = ""
		self.attributes = []
		self.feature_vectors = []
		
		if len(args) == 1:
			if isinstance(args[0], ArffDataset):
				# Copy constructor
				that = args[0]
				self.relation = that.relation
				self.attributes = that.attributes[:]
				self.feature_vectors = that.feature_vectors[:]
			# Create from string representation (ie. a file handle)
			else:
				state = _ParserState()
				for line in args[0]:
					self._process_line( line, state )
		elif len(args) == 0:
			self.relation = kwargs["relation"]
			self.attributes = kwargs["attributes"]
			self.feature_vectors = kwargs["feature_vectors"]
		else:
			raise Exception( "Bad call to ArffDataset constructor" ) 

	def __copy__( self ):
		return ArffDataset( self )

	def attribute( self, name ):
		"""Retrieve the ArffAttribute with name 'name'.
		
		Raises a KeyError if an attribute with the specified name is not in
		the dataset.
		"""
		for attr in self.attributes:
			if attr.name == name:
				return attr
		raise KeyError( name )

	def _dequote( self, name ):
		if len(name) < 2:
			return name
		if name[0] == "\"" and name[-1] == "\"":
			return name[1:-1]
		return name

	def attribute_index( self, arg ):
		"""Get the index of an attribute.
	
		If 'arg' is an ArffAttribute, its 'name' property is compared to the
		names of the attributes in the ArffDataset. Otherwise, it is assumed
		that 'arg' is a string containing the name of an attribute.

		Raises a KeyError if an attribute with the specified name is not in
		the dataset.
		"""
		if arg is ArffAttribute:
			name = arg.name
		else:
			name = arg
		index = 0;
		for attr in self.attributes:
			if attr.name == name:
				return index
			index += 1
		raise KeyError( arg )

	def _process_line( self, line, state ):
		"""Processes one line of input.
	
		Delegates to process_tag() if the line is a @tag, and to
		process_feature_vector() if it is not a tag.
		"""
		line = line.strip()
		line = remove_comments( line )
		if line == "":
			return
		# Tags take the form @<tag> ...
		match_obj = re.match( "^\s*@(?P<tag>\w+)", line )
		if match_obj:
			self._process_tag( match_obj.group( "tag" ).strip(), line, state )
		else:
			self._process_feature_vector( line, state )

	def _process_tag( self, tag, line, state ):
		"""Processes a @tag.
	
		Currently, @relation, @attribute, and @data are supported. The @relation
		and @data tags just cause the parser state to change. The @attribute tag
		receives more extensive handling.
		"""
		if tag.lower() == "relation":
			state.section = _ParserState.HEADER
			self._process_relation( line, state )
		elif tag.lower() == "attribute":
			self._process_attribute( line, state )
		elif tag.lower() == "data":
			state.section = _ParserState.BODY

	def _process_relation( self, line, state ):
		"""Parses the @relation tag
		"""
		if state.section != _ParserState.HEADER:
			raise ParseError( "@relation declaration after @data tag" )
		match_obj = re.match( """^\s*@\w+\s+(?P<name>("[^"]*"|'[^']*'|[^"'\s]\S*))""", line )
		if match_obj:
			self.relation = match_obj.group( "name" ).strip()
		else:	
			raise ParseError( "Failed to parse @relation tag" )

	def _process_attribute( self, line, state ):
		"""Parses an @attribute tag and adds an ArffAttribute object to the
		ArffDataset.
		"""
		if state.section != _ParserState.HEADER:
			raise ParseError( "@attribute declaration after @data tag" )
		match_obj = re.match( """^\s*@\w+\s+(?P<attr>("[^"]*"|'[^']*'|[^"'\s]\S*))\s+(?P<domain>.*)""", line )
		if match_obj:
			attribute = ArffAttribute( name=self._dequote(match_obj.group( "attr" ).strip()),
								  	   domain=match_obj.group( "domain" ).strip() )
			self.attributes.append( attribute )
		else:
			raise ParseError( "@attribute tag did not define attribute name and/or domain" )

	def _process_feature_vector( self, line, state ):
		"""Processes a 'feature vector', which is any non-empty line that is not
		a @tag.
		"""
		if state.section != _ParserState.BODY:
			raise ParseError( "Feature vectors commence before @data tag" )
		self.feature_vectors.append( re.split( ",", line ) )

	def __repr__( self ):
		result = []
		result.append( "@relation " + self.relation + "\n" )
		for attr in self.attributes:
			result.append( repr(attr) )
		result.append( "\n@data\n" )
		for v in self.feature_vectors:
			result.append( ",".join( map( str, v ) ) )
		return "\n".join( result ) + "\n"

# ----------------------------------------------------------------------------

def _concat( left, right, left_skip, right_skip ):
	"""
	Concatenates two lists, skipping the indicated indices in each.
	"""
	result = []
	for i in range(0, len(left)):
		if i not in left_skip:
			result.append( left[i] )
	for j in range(0, len(right)):
		if j not in right_skip:
			result.append( right[j] )
	return result
	
def _compare( left, right, left_indices, right_indices ):
	"""
	Calls 'cmp' on pairs of elements in each list in the order specified in
	the '_indices' lists. Returns the first non-zero result, or zero if the two
	lists are equal at all indicated positions.
	"""
	assert( len(left_indices) == len(right_indices) )
	c = 0;
	for (i, j) in [(k, k) for k in range(0, len(left_indices))]:
		c = cmp( left[left_indices[i]], right[right_indices[j]] )
		if c != 0:
			break
	return c
	
def equi_join( left, right, columns, relation ):
	"""
	Performs an "equi-join" on the two relations 'left' and 'right'. An
	equi-join is an inner join in which all of the predicates are equality
	comparisons between columns.
	
	See 'equi_join_list' for a description of the algorithm.
	
	@param left An ArffDataset
	@param right An ArffDataset
	@param columns A list of strings naming the columns that have equality
	constraints between them.
	@param relation The name to give to the output relation.
	@return An ArffDataset containing the join result
	"""
	left_idx = []
	right_idx = []
	for column in columns:
		left_idx.append( left.attribute_index( column ) )
		right_idx.append( right.attribute_index( column ) )
	feature_vectors = equi_join_list( left.feature_vectors, right.feature_vectors, left_idx, right_idx )
	attributes = _concat( left.attributes, right.attributes, [], right_idx )
	return ArffDataset( relation=relation, feature_vectors=feature_vectors, attributes=attributes )
	
def equi_join_list( left, right, left_idx, right_idx ):
	"""
	Performs an "equi-join" on two lists. The '_idx' lists give the indices in
	the corresponding list that have equality constraints between them. That
	is, two tuples will be joined in the resulting relation if and only if
	left[left_idx[i]] == right[right_idx[i]] for all appropriate i.
	
	The algorithm is a sort-merge join. The complexity is O(k n log n) where
	n is the number of tuples and k is the number of key columns.
	"""
	assert( len(left_idx) == len(right_idx) )
	for i in range(0, len(left_idx)):
		left_i = left_idx[i]
		right_i = right_idx[i]
		left.sort( lambda p, q: cmp( p[left_i], q[left_i] ) )
		right.sort( lambda p, q: cmp( p[right_i], q[right_i] ) )
	
	result = []
	(left_pos, right_pos) = (0, 0)
	while left_pos < len(left) and right_pos < len(right):
		left_elem = left[left_pos]
		match_count = 0
		while (right_pos + match_count) < len(right):
			right_elem = right[right_pos + match_count]
			c = _compare( left_elem, right_elem, left_idx, right_idx )
			if c != 0:
				break
			else:
				result.append( _concat( left_elem, right_elem, [], right_idx ) )
				match_count += 1
		if match_count > 0:
			left_pos += 1
		elif c < 0:
			left_pos += 1
		elif c > 0:
			right_pos += 1
	return result
	