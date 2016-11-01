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

from csv import CsvAttribute, CsvDataset

import argparse
import statistics

cl_parser = argparse.ArgumentParser( description="Creates random subsets of faults" )
cl_parser.add_argument( "input_file", type=str, nargs=1,
						help="A 'rewards.csv' input file" )
cl_parser.add_argument( "-d", type=int, default=60, help="Size of intervals for analysis" )
args = cl_parser.parse_args()

with open( args.input_file[0] ) as fin:
	data = CsvDataset( fin )
	intervals = []
	for fv in data.feature_vectors:
		i = data.attribute_index( "r1" )
		j = i + args.d
		while j < len(data.attributes):
			ri = float(fv[i])
			rj = float(fv[j])
			intervals.append( abs(rj - ri) )
			i += 2
			j += 2

print( "n:      " + str(len(intervals)) )
print( "mean:   " + str(statistics.mean(intervals)) )
print( "stddev: " + str(statistics.stdev(intervals)) )
print( "min:    " + str(min(intervals)) )
print( "max:    " + str(max(intervals)) )
