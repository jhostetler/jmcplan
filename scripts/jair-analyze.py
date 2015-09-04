import math
from optparse import OptionParser
import re
import sys

from csv import CsvAttribute, CsvDataset



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

out_attributes = [
"criterion", "ss.abstraction", "par.priority", "par.classifier", "random_abstraction.k", "ss.budget_type", "ss.budget",
"V_mean", "V_var", "V_conf", "ss.width", "ss.depth", "seed.world", "seed.sim"
]

out_data = CsvDataset( attributes=[CsvAttribute( a ) for a in out_attributes], feature_vectors=[] )
	
with open( args[0], "r" ) as input_file:
	in_data = CsvDataset( input_file )
	
out_idx = [in_data.attribute_index( a ) for a in out_attributes[1:]]
v_idx = in_data.attribute_index( "V_mean" )
	
# MAX criterion
max_value = dict()
max_params = dict()

class KeyBuilder:
	def __init__( self, data ):
		self._ks = [
			"ss.abstraction", "par.priority", "par.classifier", "random_abstraction.k", "ss.budget_type", "ss.budget"
		]
		self._idx = [data.attribute_index( k ) for k in self._ks]
		
	def key( self, fv ):
		return tuple( fv[i] for i in self._idx )

kb = KeyBuilder( in_data )

for fv in in_data.feature_vectors:
	k = kb.key( fv )
	try:
		old_v = max_value[k]
	except KeyError:
		old_v = -sys.float_info.max
		max_value[k] = old_v
	if float(fv[v_idx]) > old_v:
		max_value[k] = float(fv[v_idx])
		max_params[k] = fv[:]
		
for p in max_params.itervalues():
	output = ["max"]
	output.extend( p[i] for i in out_idx )
	out_data.feature_vectors.append( output )
	
output_file.write( repr(out_data) )
output_file.close()