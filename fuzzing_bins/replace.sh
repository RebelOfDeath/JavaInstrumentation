#!/bin/bash
AFL=/home/str/AFL/afl-2.52b
BINS=/home/str/JavaInstrumentation/fuzzing_bins

for p in 11 12 13 14 15 17; do
    FILE="$BINS/Problem${p}.c"
    
    sed -i 's/extern void __VERIFIER_error(int);/void __VERIFIER_error(int i) { fprintf(stderr, "error_%d ", i); assert(0); }/' "$FILE"
    sed -i 's/scanf("%d", \&input);/int ret = scanf("%d", \&input);\n\t\tif (ret != 1) return 0;/' "$FILE"
    
    echo "Problem${p}"
    grep -n "VERIFIER_error" "$FILE" | head -2
    grep -n "scanf\|ret" "$FILE" | head -3
    echo ""
done