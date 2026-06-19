#!/bin/bash
BINS=/home/str/JavaInstrumentation/fuzzing_bins
RESULTS=/home/str/JavaInstrumentation/fuzzing_results/afl
SCRIPTS=/home/str/JavaInstrumentation/scripts

mkdir -p $RESULTS

for p in 11 12 13 14 15 17; do
    echo "Problem${p}"
    
    python3 $SCRIPTS/analyze_afl.py \
        $BINS/findings_${p} \
        $BINS/Problem${p} 2>/dev/null > /tmp/afl_summary_${p}.txt
    
    echo "elapsed_seconds,error_code" > $RESULTS/problem${p}_afl_errors.csv
    grep "found in" /tmp/afl_summary_${p}.txt | while read line; do
        code=$(echo $line | awk '{print $1}')
        time=$(echo $line | awk '{print $4}' | tr -d 's')
        echo "$time,$code"
    done >> $RESULTS/problem${p}_afl_errors.csv
    
    grep -E "^(error_|Found)" /tmp/afl_summary_${p}.txt
done