import itertools
import math
from optparse import OptionParser
import re
import sys

from csv import CsvAttribute, CsvDataset

class KeyBuilder:
	def __init__( self, data, ks ):
		self._data = data
		self._ks = ks
		self._idx = [data.attribute_index( k ) for k in self._ks]
		
	def key( self, fv ):
		return tuple( fv[i] for i in self._idx )

# ----------------------------------------------------------------------------
# Main
# ----------------------------------------------------------------------------

cl_parser = OptionParser( usage="%prog [options] file" )
cl_parser.add_option( "-o", dest="output_file", type="string", default="-",
					  help="The file to write the output to (default: stdout)" )
# cl_parser.add_option( "--complete", dest="complete", action="store_true", default=False,
					  # help="If specified, only parameterizations for which results are available for all budgets are considered." )
(options, args) = cl_parser.parse_args();

if options.output_file == "-":
	# Saying 'open( "sys.stdout" )' doesn't seem to accomplish this
	output_file = sys.stdout
	rank_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )
	rank_file = open( "rank-" + options.output_file, "w" )

with open( args[0], "r" ) as input_file:
	in_data = CsvDataset( input_file )

# The "blocks" are Domain x Budget
block_kb = KeyBuilder( in_data, ["domain_params", "ss.budget"] )
# The "treatments" are the different PARSS variants
column_kb = KeyBuilder( in_data, ["Algorithm"] )
# This will be a two-level map Block -> Column -> V_mean
friedman = dict()
alg_set = set( ["PAR(bf; DT)", "PAR(bf; random)", "PAR(uniform; DT)", 
				"PAR(uniform; random)", "PAR(variance; DT)", "PAR(variance; random)"] )
# Gather results into 'friedman' table
for fv in in_data.feature_vectors:
	alg = fv[in_data.attribute_index( "Algorithm" )]
	if alg not in alg_set:
		continue
	
	V_mean = float(fv[in_data.attribute_index( "V_mean" )])
	# Stuff for Friedman's test
	b = block_kb.key( fv )
	try:
		block = friedman[b]
	except KeyError:
		block = dict()
		friedman[b] = block
	c = alg
	block[c] = V_mean

# Assign sequential indices to algorithms
alg_set = sorted(list(alg_set))
alg_idx = dict()
i = 0
for name in alg_set:
	print( str(name) + " => " + str(i) )
	alg_idx[name] = i
	i += 1
# Size constants
N = len(friedman)
k = len(alg_set)
print( "N = " + str(N) )
print( "k = " + str(k) )
	
out_data = CsvDataset( attributes=[CsvAttribute( name ) for name in alg_set], feature_vectors=[] )
rank_data = CsvDataset( attributes=[CsvAttribute( name ) for name in alg_set], feature_vectors=[] )
for (b, block) in friedman.iteritems():
	# Unpack performance on this block into numeric vector
	v = [0] * len(alg_set)
	for (c, column) in block.iteritems():
		v[alg_idx[c]] = column
	# print( v )
	
	out_data.feature_vectors.append( v )
	order = sorted( ((e, i) for (i, e) in enumerate(v)), reverse=True )
	o = [0] * len(alg_set)
	r = 0
	rank = 1
	while r < k:
		(e, i) = order[r]
		avg_rank = rank
		ties = set( [i] )
		next = r + 1
		# Count entries that are tied with the current entry
		while next < k:
			(enext, inext) = order[next]
			if enext != e: # Not tied
				break
			ties.add( inext )
			rank += 1
			avg_rank += rank
			next += 1
		if len(ties) > 1:
			avg_rank = float(avg_rank) / len(ties)
		# Assign average rank to all tied entries
		for itie in ties:
			o[itie] = avg_rank
		r = next
		rank += 1
	# print( o )
	rank_data.feature_vectors.append( o )

# Calculate average ranks
R = [0] * len(alg_set)
for rs in rank_data.feature_vectors:
	for i in range(0, len(alg_set)):
		R[i] += rs[i]
R = [float(r) / len(rank_data.feature_vectors) for r in R]
print( R )

# \chi^2 statistic (Demsar, pp. 11)
chisquared_F = ( (12.0*N)/(k*(k+1)) ) * ( sum( Rj*Rj for Rj in R ) - (k*(k+1)*(k+1) / 4.0) )
print( "\chi^2_F(" + str(k-1) + ") = " + str(chisquared_F) )

# F statistic (Demsar, pp. 11)
F_F = ( (N-1)*chisquared_F ) / ( N*(k-1) - chisquared_F )
print( "F_F(" + str(k-1) + ", " + str( (k-1)*(N-1) ) + ") = " + str(F_F) )

# Nemenyi critical difference (Demsar, pp. 11-12)
assert( k <= 10 );
# Critical values for 2-tailed Nemenyi test at p=0.05 (Demsar, Table 5)
q_05 = [float("NaN"), float("NaN"), 1.960, 2.343, 2.569, 2.728, 2.850, 2.949, 3.031, 3.102, 3.164]
CD = q_05[k] * math.sqrt( k*(k+1) / (6.0 * N) )
print( "CD (0.05) = " + str(CD) )

for i in range(0, k):
	row = []
	for j in range(0, k):
		if abs( R[i] - R[j] ) > CD:
			if R[i] < R[j]:
				row.append( "<" )
			else:
				row.append( ">" )
		else:
			row.append( "=" )
	print( " ".join( row ) )


output_file.write( repr(out_data) )
output_file.close()

rank_file.write( repr(rank_data) )
rank_file.close()
