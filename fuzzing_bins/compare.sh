#!/bin/bash
RESULTS=/home/str/JavaInstrumentation/fuzzing_results

for p in 11 12 13 14 15 17; do
    echo "Problem${p}"
    random_errors=$(tail -n +2 $RESULTS/random/problem${p}_random_errors.csv 2>/dev/null | wc -l)
    smart_errors=$(tail -n +2 $RESULTS/smart/problem${p}_smart_errors.csv 2>/dev/null | wc -l)
    afl_errors=$(tail -n +2 $RESULTS/afl/problem${p}_afl_errors.csv 2>/dev/null | wc -l)
    echo "  Random:       $random_errors"
    echo "  Hill Climber: $smart_errors"
    echo "  AFL:          $afl_errors"
    echo ""
done