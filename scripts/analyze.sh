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

cwd=$(pwd)
experiments=$(cat $1)
expr_name="$1"

analyses=()
for e in $experiments; do
	echo "Experiment $e"
	summary_file="$e"/"summary_""$expr_name""_""$e"".csv"
	analysis_file="$e"/"analysis_""$expr_name""_""$e"".csv"
	post_file="$e"/"post-analysis_""$expr_name""_""$e"".csv"
	final_file="$e"/"final-analysis_""$expr_name""_""$e"".csv"
	analyses+=("$final_file")
	
	# rm "$analysis_file"
	
	if [ ! -e "$analysis_file" ]; then
		if [ ! -e "$summary_file" ]; then
			echo "...summarizing"
			python ../scripts/csv-cat.py -o "$summary_file" "$e"/"$e"*/data*.csv
		fi
		echo "...analyzing"
		python ../scripts/jair-analyze.py -o "$analysis_file" "$summary_file"
	fi
	python ../scripts/post-analyze.py -o "$post_file" "$analysis_file"
	python ../scripts/csv-cat.py -o "$final_file" "$analysis_file" "$post_file"
done

analysis_master="analysis_""$expr_name"".csv"
echo "Concatenating..."
echo "${analyses[@]}"
python ../scripts/csv-cat.py -o "$analysis_master" "${analyses[@]}"

echo "Friedman test"
python ../scripts/mk-friedman.py -o "friedman_""$expr_name"".csv" "$analysis_master"
