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
cl_parser.add_option( "--complete", dest="complete", action="store_true", default=False,
					  help="If specified, only parameterizations for which results are available for all budgets are considered." )
(options, args) = cl_parser.parse_args();

if options.output_file == "-":
	# Saying 'open( "sys.stdout" )' doesn't seem to accomplish this
	output_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )

with open( args[0], "r" ) as input_file:
	in_data = CsvDataset( input_file )

domain_kb = KeyBuilder( in_data, ["domain_params"] )
# par_kb = KeyBuilder( in_data, ["ss.abstraction", "par.priority", "par.classifier"] )
par_kb = KeyBuilder( in_data, ["Algorithm"] )
values = dict()
# The "blocks" are Domain x Budget
block_kb = KeyBuilder( in_data, ["domain_params", "ss.budget"] )
column_kb = KeyBuilder( in_data, ["Algorithm"] )
friedman = dict()
alg_set = set()
for fv in in_data.feature_vectors:
	if not fv[in_data.attribute_index( "Algorithm" )].startswith( "PAR" ): # != "par":
		continue
	print( "par: " + fv[in_data.attribute_index( "Algorithm" )] )
	d = domain_kb.key( fv )
	print( "d: " + str(d) )
	try:
		dmap = values[d]
	except KeyError:
		dmap = dict()
		values[d] = dmap
	p = par_kb.key( fv )
	print( "p: " + str(p) )
	try:
		v = dmap[p]
	except KeyError:
		v = 0
	V_mean = float(fv[in_data.attribute_index( "V_mean" )])
	dmap[p] = v + V_mean
	
	# Stuff for Friedman's test
	b = block_kb.key( fv )
	try:
		block = friedman[b]
	except KeyError:
		block = dict()
		friedman[b] = block
	c = column_kb.key( fv )
	block[c] = V_mean
	alg_set.add( c )

block_idx = dict()
i = 0
for name in sorted(list(alg_set)):
	print( str(name) + " => " + str(i) )
	block_idx[name] = i
	i += 1
for (b, block) in friedman.iteritems():
	v = [0] * len(alg_set)
	for (c, column) in block.iteritems():
		v[block_idx[c]] = column
	print( v )

minmax = dict()
for (domain, dmap) in values.iteritems():
	dom_minmax = dict()
	minmax[domain] = dom_minmax
	dom_minmax["min"] = (None, sys.float_info.max)
	dom_minmax["max"] = (None, -sys.float_info.max)
	for (k, v) in dmap.iteritems():
		if v > dom_minmax["max"][1]:
			dom_minmax["max"] = (k, v)
			print( "dom_minmax[" + str(domain) + "][max] = " + str(v) )
		if v < dom_minmax["min"][1]:
			dom_minmax["min"] = (k, v)
			print( "dom_minmax[" + str(domain) + "][min] = " + str(v) )

out_data = CsvDataset( attributes=in_data.attributes[:], feature_vectors=[] )

for fv in in_data.feature_vectors:
	d = domain_kb.key( fv )
	k = par_kb.key( fv )
	if minmax[d]["min"][0] == k:
		cp = fv[:]
		cp[in_data.attribute_index( "Algorithm" )] = "PAR (worst)"
		out_data.feature_vectors.append( cp )
	if minmax[d]["max"][0] == k:
		cp = fv[:]
		cp[in_data.attribute_index( "Algorithm" )] = "PAR (best)"
		out_data.feature_vectors.append( cp )

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
