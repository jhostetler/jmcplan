#!/bin/bash

# This script finds all experiments that are not scheduled (ie. not in state
# 'r' or 'qw' or ...) and examines their output files to see if they
# completed successfully. If they did not, it re-submits the job.
#
# Usage:
# 	qerrors.sh <name of JAR file>

trim () { while read -r line; do echo "$line"; done; }
scheduled=$(python $HOME/scripts/myqstat.py | cut -f 3 | trim)
files=$(ls -1 *.csv.sh)
notscheduled=$(comm -13 <(echo "$scheduled") <(echo "$files"))

for f in $notscheduled
do
	# For every experiment that is not scheduled, open the corresponding log
	# files and check for errors
	
	# Most recent output file for this experiment
	# Note: this take a really long time because it repeatedly globs over a
	#       all of the files in the directory
    outfile=$(ls -t1 "$f"".o"* | head -n 1)
	if ! grep -q "Alles gut!" $outfile; then
		# Found an error => resubmit
		# TODO: Take different actions depending on error? (OOM, etc)
		
		echo "ERROR: $outfile"
		# Use submit.sh so that the script is re-generated. This allows you
		# to use a new blacklist.
		$(bash submit.sh "$1" "$f")
	fi
	# else
		# echo "$outfile"
	# fi
done
