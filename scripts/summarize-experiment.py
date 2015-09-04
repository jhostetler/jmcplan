#!/usr/bin/env python

# ----------------------------------------------------------------------------
# summarize-experiment.py
#
# Usage:
#	summarize-experiment.py [options] csv_file
#
# Options:
#	-o <file> Output results to <file> (default: stdout)
#
# Known issues:
#
# TODO:
#	- The 'optparse' module is deprecated in favor of 'argparse', but
#	  'argparse' is not available in 2.4.3 (requires 2.7+)
#
# History:
#	[2013/01/28:hostetje] Created
# ----------------------------------------------------------------------------

from csv import CsvDataset, CsvAttribute

import copy
from optparse import OptionParser
import os
import re
import sys

# ----------------------------------------------------------------------------
# Definitions
# ----------------------------------------------------------------------------

# Copied from: http://rightfootin.blogspot.com/2006/09/more-on-python-flatten.html
def flatten(l, ltypes=(list, tuple)):
	ltype = type(l)
	l = list(l)
	i = 0
	while i < len(l):
		while isinstance(l[i], ltypes):
			if not l[i]:
				l.pop(i)
				i -= 1
				break
			else:
				l[i:i + 1] = l[i]
		i += 1
	return ltype(l)

class Summary:
	def __init__( self ):
		self.win_counts = [0, 0]
		self.score = MeanVarianceAccumulator()
		self.switches = [MeanVarianceAccumulator(), MeanVarianceAccumulator()]
		
	def __str__( self ):
		return ",".join( [str(x) for x in flatten( [self.win_counts, self.score, self.switches] )] )
	
class MeanVarianceAccumulator:
	def __init__( self ):
		self.n = 0
		self._mean = 0.0
		self._m2 = 0.0
	
	def add( self, x ):
		self.n += 1
		delta = x - self._mean
		self._mean += (delta / self.n)
		self._m2 += delta*(x - self._mean)
		
	def mean( self ):
		return self._mean
		
	def variance( self ):
		if self.n > 1:
			return self._m2 / (self.n - 1)
		else:
			return 0.0
			
	def __str__( self ):
		return ",".join( [str(self.mean()), str(self.variance())] )
	
def summarize( dir ):
	Nagents = 2
	summary = Summary()
	for w in os.listdir( dir ):
		wpath = os.path.join( dir, w )
		if os.path.isdir( wpath ):
			print( wpath )
			try:
				wfile = open( os.path.join( wpath, "result.csv" ) )
			except:
				print( "! No 'results.csv' in '" + wpath + "'" )
				continue
			results = CsvDataset( wfile )
			wfile.close()
			try:
				wfile = open( os.path.join( wpath, "game-log.csv" ) )
			except:
				print( "! No 'game-log.csv' in '" + wpath + "'" )
				continue
			log = CsvDataset( wfile )
			wfile.close()
			
			# Commit
			log_ai = [log.attribute_index( "a0" ), log.attribute_index( "a1" )]
			for fv in results.feature_vectors:
				if fv[0] == "winner":
					summary.win_counts[int(fv[1])] += 1
				elif fv[0] == "score":
					summary.score.add( float(fv[1]) )
			policies = [None, None]
			switches = [0, 0]
			for fv in log.feature_vectors:
				for i in range(0, Nagents):
					if policies[i] is None:
						policies[i] = fv[log_ai[i]]
					elif policies[i] != fv[log_ai[i]]:
						switches[i] += 1
						policies[i] = fv[log_ai[i]]
			for i in range(0, Nagents):
				summary.switches[i].add( switches[i] )
	return summary
	
def write_results( results, output_file ):
	Nagents = 2
	columns = []
	columns.append( "instance" )
	columns.extend( ["wins" + str(i) for i in range(0, Nagents)] )
	columns.extend( ["score_mean", "score_variance"] )
	for i in range(0, Nagents):
		columns.extend( ["switches" + str(i) + "_mean", "switches" + str(i) + "_variance"] )
	output_file.write( ",".join( columns ) + "\n" )
	for (k, v) in results.iteritems():
		s = ",".join( [str(k), str(v)] )
		output_file.write( s + "\n" )

# ----------------------------------------------------------------------------
# Main
# ----------------------------------------------------------------------------

cl_parser = OptionParser( usage="%prog [options] file" )
cl_parser.add_option( "-o", dest="output_file", type="string", default=None,
					  help="The file to write the output to (default: stdout)" )

(options, args) = cl_parser.parse_args();

if len( args ) != 1:
	print( cl_parser.print_usage() )
	sys.exit( 0 )

results = {}
for f in os.listdir( args[0] ):
	path = os.path.join( args[0], f )
	if os.path.isdir( path ):
		print( path )
		results[f] = summarize( path )

if options.output_file is None:
	# Saying 'open( "sys.stdout" )' doesn't seem to accomplish this
	output_file = sys.stdout
else:
	output_file = open( options.output_file, "w" )

write_results( results, output_file )
output_file.close()
