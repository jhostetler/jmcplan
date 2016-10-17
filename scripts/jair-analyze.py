import itertools
import math
from operator import itemgetter
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
cl_parser.add_option( "--complete", dest="complete", action="store_true", default=False,
					  help="If specified, only parameterizations for which results are available for all budgets are considered." )
(options, args) = cl_parser.parse_args();

if options.output_file == "-":
	# Saying 'open( "sys.stdout" )' doesn't seem to accomplish this
	output_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )

out_attributes = [
"Domain", "Algorithm", "domain_params", "criterion", "ss.abstraction", "par.priority", "par.classifier", "random_abstraction.k", "ss.budget_type", "ss.budget",
"V_mean", "V_var", "V_conf", "ss.width", "ss.depth", "seed.world", "seed.sim"
]

# out_attributes = [
# "domain", "criterion", "ss.abstraction", "par.subtree_refinement_order", "par.classifier", "random_partition.k", "ss.budget_type", "ss.budget",
# "V_mean", "V_var", "V_conf", "ss.width", "ss.depth", "seed"
# ]

out_data = CsvDataset( attributes=[CsvAttribute( a ) for a in out_attributes], feature_vectors=[] )
	
with open( args[0], "r" ) as input_file:
	in_data = CsvDataset( input_file )
	
# [jhostetler:20160804] The Saving domain had an extra header in its output,
# causing everything to be shifted by 1.
Nepisodes_idx = len(in_data.attributes)
try:
	Nepisodes_idx = in_data.attribute_index( "Nepisodes" )
except KeyError:
	# Exception indicates that problematic field was not present
	pass
if Nepisodes_idx != len(in_data.attributes):
	print( "WARNING: Found 'Nepisodes' header; correcting dataset" )
	sailing_p = in_data.attribute_index( "sailing.p" )
	for fv in in_data.feature_vectors:
		assert( str(fv[sailing_p]) == "" )
		del fv[sailing_p]
	# Delete header last so that indexing is less confusing
	del in_data.attributes[Nepisodes_idx]
assert( len(in_data.attributes) == len(in_data.feature_vectors[0]) )
# /End hack

# Skip index < 4 because they are not found in the summary file
out_idx = []
for a in out_attributes[4:]:
	try:
		out_idx.append( in_data.attribute_index( a ) )
	except KeyError as ex:
		if a == "seed.world" or a == "seed.sim":
			out_idx.append( in_data.attribute_index( "seed" ) )
		else:
			raise ex
v_idx = in_data.attribute_index( "V_mean" )
	
# MAX criterion
max_value = dict()
max_params = dict()

class KeyBuilder:
	def __init__( self, data ):
		self._data = data
		self._ks = [
			"ss.abstraction", "par.priority", "par.classifier", "random_abstraction.k", "ss.budget_type", "ss.budget"
		]
		self._comp_ks = [
			"ss.abstraction", "par.priority", "par.classifier", "random_abstraction.k", "ss.width", "ss.depth", "ss.budget_type"
		]
		# self._ks = [
			# "ss.abstraction", "par.subtree_refinement_order", "par.classifier",
			# "random_partition.k", "ss.budget_type", "ss.budget"
		# ]
		self._idx = [data.attribute_index( k ) for k in self._ks]
		self._comp_idx = [data.attribute_index( k ) for k in self._comp_ks]
		
		# Find the largest and smallest k and call them "coarse" and "fine".
		# This basically just handles a special case for rg_s_1.
		# NOTE: We assume that the entire file being analyzed is for a single domain
		k = data.attribute_index( "random_abstraction.k" )
		b = data.attribute_index( "ss.budget" )
		K = set()
		B = set()
		for fv in data.feature_vectors:
			K.add( int(fv[k]) )
			B.add( int(fv[b]) )
		self._fine = max( K )
		self._coarse = min( K )
		self._budgets = sorted(list(B))
		print( "Budgets: " + str(self._budgets) )
		print( "self._fine: " + str(self._fine) )
		print( "self._coarse: " + str(self._coarse) )
		
		completed = dict()
		d = data.attribute_index( "ss.depth" )
		c = data.attribute_index( "ss.width" )
		for fv in data.feature_vectors:
			# fv_key = (self.key( fv ), int(fv[c]), int(fv[d]))
			fv_key = self._comp_key( fv )
			try:
				completed[fv_key].append( int(fv[b]) )
			except KeyError:
				completed[fv_key] = [int(fv[b])]
		
		self._completed = []
		for fv in data.feature_vectors:
			# fv_key = (self.key( fv ), int(fv[c]), int(fv[d]))
			fv_key = self._comp_key( fv )
			comp = completed[fv_key]
			# print( str(fv_key) )
			# print( str(comp) )
			if len(comp) != len(B):
				# print( "inc (len): " + str(fv_key) )
				continue
			ok = True
			for budget in B:
				if budget not in comp:
					# print( "inc (comp != B): " + str(fv_key) )
					ok = False
					break
			if ok:
				self._completed.append( fv[:] )
		
	def domain_idx( self, fv ):
		d = self._data.attribute_index( "domain" )
		domain_name = fv[d]
		idx = [d]
		for attr in self._data.attributes:
			# Include any attributes that start with '<domain-name>.' in the domain key
			if re.match( domain_name + "\\.", attr.name ) is not None:
				idx.append( self._data.attribute_index( attr ) )
		return idx
		
	def abstraction( self, fv ):
		name = fv[ self._data.attribute_index( "ss.abstraction" ) ]
		print( "abstraction: " + name )
		if name == "ground":
			return "Ground"
		elif name == "par":
			s = "PAR("
			priority = fv[ self._data.attribute_index( "par.priority" ) ]
			classifier = fv[ self._data.attribute_index( "par.classifier" ) ]
			if classifier == "decision_tree":
				return "PAR(" + priority + "; DT)"
			elif classifier == "random_partition":
				return "PAR(" + priority + "; random)"
		elif name == "random":
			k = int(fv[ self._data.attribute_index( "random_abstraction.k" ) ])
			print( "random.k: " + str(k) )
			if k == self._fine:
				print( "...fine" )
				return "Random (Fine)"
			elif k == self._coarse:
				print( "...coarse" )
				return "Random (Coarse)"
			else:
				return None
		elif name == "top":
			return "Top"
		raise KeyError()
		
	def domain_name( self, fv ):
		name = fv[ self._data.attribute_index( "domain" ) ]
		if name == "advising":
			return "Advising"
		elif name == "crossing":
			return "Crossing"
		elif name == "elevators":
			return "Elevators"
		elif name == "racegrid":
			subname = fv[ self._data.attribute_index( "racegrid.circuit" ) ]
			if subname == "bbs_small":
				return "Racetrack Small"
			elif subname == "bbs_large":
				return "Racetrack Large"
		elif name == "sailing":
			subname = fv[ self._data.attribute_index( "sailing.world" ) ]
			if subname == "empty":
				return "Sailing Empty"
			elif subname == "random":
				return "Sailing Random"
		elif name == "saving":
			sub = fv[ self._data.attribute_index( "saving.maturity_period" ) ]
			return "Saving (m = " + str(sub) + ")"
		elif name == "spbj":
			return "Spanish Blackjack"
		elif name == "tamarisk":
			return "Tamarisk"
		elif name == "tetris":
			return "Tetris"
		elif name == "weinstein_littman":
			return "Weinstein-Littman"
		raise KeyError()
		
	def key( self, fv ):
		return tuple( fv[i] for i in itertools.chain( self._idx, self.domain_idx( fv ) ) )
		
	def _comp_key( self, fv ):
		return tuple( fv[i] for i in itertools.chain( self._comp_idx, self.domain_idx( fv ) ) )

kb = KeyBuilder( in_data )

fvs = kb._completed if options.complete else in_data.feature_vectors
# Find the best parameter settings for each PARSS variant (incl. ground, top, random)
for fv in fvs:
	k = kb.key( fv )
	try:
		old_v = max_value[k]
	except KeyError:
		old_v = -sys.float_info.max
		max_value[k] = old_v
	if float(fv[v_idx]) > old_v:
		max_value[k] = float(fv[v_idx])
		max_params[k] = fv[:]
		
# Some variants are equivalent when the 'width' parameter is small
# E.g. ground(width=1) and par(width=1) are the same, but par(width=1) is not
# in the results because that experiment would have been redundant.
#
# This next section finds the best ground(width \in {1,2}) performance for each
# budget and then modifies 'max_value' and 'max_params' if the ground
# performance was better
max_low_width = dict()			# budget -> width -> max value
max_low_width_params = dict()	# budget -> width -> fv that achieved max value
for fv in fvs:
	ab_idx = in_data.attribute_index( "ss.abstraction" )
	w_idx = in_data.attribute_index( "ss.width" )
	abstraction = fv[ab_idx]
	width = int(fv[w_idx])
	if abstraction == "ground" and width < 5:
		b_idx = in_data.attribute_index( "ss.budget" )
		budget = int(fv[b_idx])
		try:
			b_low_width = max_low_width[budget]
		except KeyError:
			b_low_width = { 1 : -sys.float_info.max, 2 : -sys.float_info.max }
			max_low_width[budget] = b_low_width
		value = float(fv[v_idx])
		if value > b_low_width[width]:
			b_low_width[width] = value
			try:
				b_low_width_params = max_low_width_params[budget]
			except KeyError:
				b_low_width_params = { 1 : [], 2 : [] }
				max_low_width_params[budget] = b_low_width_params
			b_low_width_params[width] = fv[:]
# This loop incorporates the old output generating code, but inserts a step
# that modifies the parameters output if 'ground' was better
for (k, fv) in max_params.iteritems():
	domain = ";".join( fv[i] for i in kb.domain_idx( fv ) )
	ab = kb.abstraction( fv )
	if ab is None:
		continue
	output = [kb.domain_name( fv ), ab, domain, "max"]
	# See if we need to overwrite parameters
	out_fv = fv[:]
	ab_idx = in_data.attribute_index( "ss.abstraction" )
	b_idx = in_data.attribute_index( "ss.budget" )
	abstraction = fv[ab_idx]
	budget = int(fv[b_idx])
	ground_width = []
	if abstraction == "par" or abstraction == "top":
		ground_width = [1]
	elif abstraction == "random":
		ground_width = [1, 2]
	for w in ground_width:
		lw_val = max_low_width[budget][w]
		if lw_val > max_value[k]:
			print( "Overwriting " + str([out_fv[i] for i in out_idx]) )
			print( "\t-> " + str([max_low_width_params[budget][w][i] for i in out_idx]) )
			max_value[k] = lw_val
			for i in out_idx:
				out_fv[i] = max_low_width_params[budget][w][i]
	# Now that we've done all required overwriting, output the parameters
	output.extend( out_fv[i] for i in out_idx )
	out_data.feature_vectors.append( output )
		
# for p in max_params.itervalues():
	# domain = ";".join( p[i] for i in kb.domain_idx( p ) )
	# ab = kb.abstraction( p )
	# if ab is None:
		# continue
	# output = [kb.domain_name( p ), ab, domain, "max"]
	# output.extend( p[i] for i in out_idx )
	# out_data.feature_vectors.append( output )
	
# out_sort_keys = ("ss.abstraction", "par.priority", "par.classifier", "random_abstraction.k", "ss.budget_type", "ss.budget")
# out_types = (str, str, str, int, str, int)
out_sort_keys = ("Algorithm", "ss.budget_type", "ss.budget")
out_types = (str, str, int)
out_sort_idx = [out_data.attribute_index( k ) for k in out_sort_keys]
def keyfun( x ):
	return [f(x[i]) for (f, i) in zip(out_types, out_sort_idx)]
out_data.feature_vectors.sort( key=keyfun )
	
output_file.write( repr(out_data) )
output_file.close()
