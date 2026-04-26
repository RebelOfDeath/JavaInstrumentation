#!/bin/bash
AFL=/home/str/AFL/afl-2.52b
BINS=/home/str/JavaInstrumentation/fuzzing_bins

for p in 11 12 13 14 15 17; do
    echo "Running AFL on Problem${p} for 5 minutes..."
    mkdir -p "$BINS/findings_${p}"
    timeout 300 env AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 $AFL/afl-fuzz -i "$BINS/tests_${p}" -o "$BINS/findings_${p}" "$BINS/Problem${p}"
    echo "Done with Problem${p}"
done