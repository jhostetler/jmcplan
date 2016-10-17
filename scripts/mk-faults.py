from optparse import OptionParser

cl_parser = OptionParser( usage="%prog [options] file" )
(options, args) = cl_parser.parse_args();

Tstable = 10
Tepisode = 300
T = Tstable + Tepisode
verbose = False
Nfaults = 2

# TODO: Make this a parameter
Nbranch = 46

headers = ["Tstable", "Tepisode", "Nfaults"]
for i in range(0, Nfaults):
	headers.append( "fault" + str(i) )
headers.append( "verbose" )
	
print( ",".join( headers ) )

for i in range(1, Nbranch+1):
	for j in range(1, Nbranch+1):
		if i < j:
			line = [Tstable, Tepisode, Nfaults, i, j, verbose]
			print( ",".join( map( str, line ) ) )
