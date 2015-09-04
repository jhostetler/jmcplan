from csv import CsvDataset, CsvAttribute

from optparse import OptionParser
import os

cl_parser = OptionParser( usage="%prog [options] file" )
cl_parser.add_option( "--subdivide", dest="subdivide", type="string", default=None,
					  help="Attribute to subdivide." )
cl_parser.add_option( "--subdivisions", dest="subdivisions", type="int", default=1,
					  help="Number of subdivisions." )
(options, args) = cl_parser.parse_args();

csv_file = open( args[0], "r" )
csv_dataset = CsvDataset( csv_file )

count = 0
for fv in csv_dataset.feature_vectors:
	print( fv )
	exploded = CsvDataset( attributes=csv_dataset.attributes[:], feature_vectors=[fv[:]] )
	print( exploded.feature_vectors[0] )
	if options.subdivide is not None:
		idx = exploded.attribute_index( options.subdivide )
		n = int(exploded.feature_vectors[0][idx])
		q = n / options.subdivisions
		r = n % options.subdivisions
		sub = [q] * options.subdivisions
		for i in range(0, r):
			sub[i] += 1
		for i in range(0, len(sub)):
			subdiv_fv = exploded.feature_vectors[0][:]
			subdiv_fv[idx] = sub[i]
			subdivided = CsvDataset( attributes=exploded.attributes[:], feature_vectors=[subdiv_fv] )
			name = os.path.splitext( args[0] )[0] + "_" + str(count) + "_" + str(i) + ".csv"
			out = open( name, "w" )
			out.write( repr(subdivided) )
			out.close()
	else:
		name = os.path.splitext( args[0] )[0] + "_" + str(count) + ".csv"
		out = open( name, "w" )
		out.write( repr(exploded) )
		out.close()
	count += 1
