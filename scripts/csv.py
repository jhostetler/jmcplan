# ----------------------------------------------------------------------------
# csv.py
#
# Defines classes for working with .csv format data files. You can parse such
# a file by creating an instance of CsvDataset with a file object as the
# parameter.
#
# Known issues:
#
# History:
#	[2010/11/05:hostetje] Created by copying code from arff.py
#	[2012/02/10:hostetje] Added option to CsvDataset constructor to specify
#		that the data has no column headers.
#	[2013/01/28:hostetje] Copied from 'bwposthoc' source tree.
#	[2015/01/28:hostetje] Fixed erroneous type check in 'attribute_index()'
# ----------------------------------------------------------------------------

import re
import sys

class _ParserState:
	HEADER = "header"
	BODY = "body"

	def __init__( self, init=HEADER ):
		self.section = init

class ParseError(RuntimeError):
	"""Indicates an error parsing a .csv file.
	"""
	def __init__( self, value ):
		self.value = value

	def __str__( self ):
		return repr( self.value )


class CsvAttribute:
	"""Represents a single attribute of a .csv dataset.

	.name = The name of the attribute.
	"""
	def __init__( self, name ):
		self.name = name

	def __copy__( self ):
		return CsvAttribute( self.name )

	def __repr__( self ):
		return self.name

def _dequote( name ):
	if len(name) < 2:
		return name
	if name[0] == "\"" and name[-1] == "\"":
		return name[1:-1]
	return name
		
class CsvDataset:
	@staticmethod
	def from_arff_dataset( arff_dataset ):
		headers = [CsvAttribute( _dequote( attr.name ) ) for attr in arff_dataset.attributes]
		return CsvDataset( attributes=headers, feature_vectors=[[str(e) for e in fv] for fv in arff_dataset.feature_vectors] )

	"""Represents a .csv dataset, including the column names and the feature 
	vectors.
	
	.attributes = An array of CsvAttribute objects, in the same order as they
		appear in the input file.
	.feature_vectors = An array of arrays representing the feature vectors,
		in the same order as they appear in the input file.
	"""
	def __init__( self, *args, **kwargs ):
		"""Creates an object to represent the dataset stored in 'csv_file'.
		"""
		self.attributes = []
		self.feature_vectors = []
		
		if len(args) == 1:
			if isinstance(args[0], CsvDataset):
				# Copy constructor
				that = args[0]
				self.attributes = that.attributes[:]
				self.feature_vectors = that.feature_vectors[:]
			else:
				# Construct from iterable (ie. file)
				try: headers = kwargs["headers"]
				except KeyError: headers = True
				if headers:
					state = _ParserState( init=_ParserState.HEADER )
				else:
					state = _ParserState( init=_ParserState.BODY )
				for line in args[0]:
					self._process_line( line, state )
		elif len(args) == 0:
			self.attributes = kwargs["attributes"]
			self.feature_vectors = kwargs["feature_vectors"]
		else:
			raise Exception( "Bad call to CsvDataset constructor" )

	def __copy__( self ):
		return CsvDataset( self )

	def attribute( self, name ):
		"""Retrieve the CsvAttribute with name 'name'.
		
		Raises a KeyError if an attribute with the specified name is not in
		the dataset.
		"""
		for attr in self.attributes:
			if attr.name == name:
				return attr
		raise KeyError( name )

	def attribute_index( self, arg ):
		"""Get the index of an attribute.
	
		If 'arg' is an CsvAttribute, its 'name' property is compared to the
		names of the attributes in the CsvDataset. Otherwise, it is assumed
		that 'arg' is a string containing the name of an attribute.

		Raises a KeyError if an attribute with the specified name is not in
		the dataset.
		"""
		if isinstance(arg, CsvAttribute):
			name = arg.name
		else:
			assert( type(arg) is str )
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
		if line == "":
			return
		if state.section == _ParserState.HEADER:
			self._process_header( line, state )
		else:
			self._process_feature_vector( line, state )

	def _process_header( self, line, state ):
		"""Processes the header, which just defines the column names.
		"""
		for name in re.split( ",", line ):
			self.attributes.append( CsvAttribute( name ) )
		state.section = _ParserState.BODY

	def _process_feature_vector( self, line, state ):
		"""Processes a 'feature vector', which is any non-empty line that is not
		the column header line.
		"""
		self.feature_vectors.append( re.split( ",", line ) )

	def __repr__( self ):
		result = []
		result.append( ",".join( [repr(attr) for attr in self.attributes] ) )
		for v in self.feature_vectors:
			result.append( ",".join( map(str, v) ) )
		return "\n".join( result )
