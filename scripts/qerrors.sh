#!/bin/bash

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
		# $(bash submit.sh "$1" "$f")
		$(bash "$1" "$2" "$f")
	fi
	# else
		# echo "$outfile"
	# fi
done
