#!/bin/bash
AFL=/home/str/AFL/afl-2.52b
BINS=/home/str/JavaInstrumentation/fuzzing_bins

for p in 11 12 13 14 15 17; do
    FILE="$BINS/Problem${p}.c"
    INPUTS=$(grep "int inputs\[\]" "$FILE" | grep -o '{[^}]*}' | tr -d '{}' | tr ',' ' ')
    mkdir -p "$BINS/tests_${p}"
    for i in $INPUTS; do
        i=$(echo $i | tr -d ' ')
        echo "$i" > "$BINS/tests_${p}/${i}.txt"
    done
    
    echo "Compiling Problem${p}"
    $AFL/afl-gcc "$FILE" -o "$BINS/Problem${p}" -lm
done