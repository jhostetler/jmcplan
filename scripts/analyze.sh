#!/bin/bash

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
