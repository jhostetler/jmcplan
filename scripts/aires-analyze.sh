#!/bin/bash

cwd=$(pwd)
experiments=$(cat $1)

analyses=()
for e in $experiments; do
	echo "Experiment $e"
	summary_file="$e"/"summary_""$e"".csv"
	analysis_file="$e"/"analysis_""$e"".csv"
	post_file="$e"/"post-analysis_""$e"".csv"
	final_file="$e"/"final-analysis_""$e"".csv"
	analyses+=("$final_file")
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

echo "Concatenating..."
echo "${analyses[@]}"
python ../scripts/csv-cat.py -o "analysis_""$1"".csv" "${analyses[@]}"
