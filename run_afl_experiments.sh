#!/bin/bash
# Final Assignment – AFL Experiment Script (Task 2)
#
# Runs AFL on RERS Problems 11-15 and 17, 5 independent times each.
# Must be run on the STR server where AFL is installed.
#
# Results layout:
#   final_results/task2/afl/run{S}/
#     findings_{N}/        – AFL output directory
#     problem{N}_afl_errors.csv   – error convergence CSV
#     problem{N}_afl_branches.csv – branch convergence CSV (from AFL plot_data)
#     problem{N}.log               – summary log (includes "Total unique branches visited:")
#
# Usage:
#   ./run_afl_experiments.sh [--duration 300] [--runs 5] [--compile] [--afl-dir PATH]
#
# Prerequisites:
#   - AFL installed at /home/str/AFL/afl-2.52b (or set AFL_DIR env var, or in PATH)
#   - C sources in fuzzing_bins/Problem{N}.c
#   - Use --compile to build C binaries with afl-gcc (required in Docker or on first run)
#   - Without --compile, pre-compiled binaries must exist in fuzzing_bins/

set -e

REPO_DIR="$(cd "$(dirname "$0")" && pwd)"
RESULTS_DIR="$REPO_DIR/final_results/task2/afl"
BINS_DIR="$REPO_DIR/fuzzing_bins"
PROBLEMS=(11 12 13 14 15 17)

DURATION=300
NUM_RUNS=5
COMPILE=false
# AFL location: apt-installed (/usr/bin), STR server (/home/str/AFL/afl-2.52b), or custom
if [ -n "$AFL_DIR" ]; then
    : # use as-is
elif command -v afl-fuzz &>/dev/null; then
    AFL_DIR="$(dirname "$(command -v afl-fuzz)")"
else
    AFL_DIR="/home/str/AFL/afl-2.52b"
fi

while [[ $# -gt 0 ]]; do
    case "$1" in
        --duration) DURATION="$2"; shift 2 ;;
        --runs)     NUM_RUNS="$2"; shift 2 ;;
        --afl-dir)  AFL_DIR="$2";  shift 2 ;;
        --compile)  COMPILE=true;  shift ;;
        *) echo "Unknown argument: $1"; exit 1 ;;
    esac
done

echo "╔══════════════════════════════════════════════════════════╗"
echo "║     Task 2 – AFL Experiment Runner                      ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo "  Problems  : ${PROBLEMS[*]}"
echo "  Duration  : ${DURATION}s per problem"
echo "  Runs      : $NUM_RUNS"
echo "  AFL       : $AFL_DIR"
echo "  Binaries  : $BINS_DIR"
echo ""

# Verify AFL exists
if [ ! -f "$AFL_DIR/afl-fuzz" ]; then
    echo "ERROR: AFL not found at $AFL_DIR/afl-fuzz"
    echo "Set AFL_DIR env variable or use --afl-dir"
    exit 1
fi

# ── Compile C binaries with afl-gcc if needed ─────────────────────────────────
needs_compile=false
if $COMPILE; then
    needs_compile=true
else
    for N in "${PROBLEMS[@]}"; do
        if [ ! -f "$BINS_DIR/Problem$N" ]; then
            needs_compile=true
            break
        fi
    done
fi

if $needs_compile; then
    AFL_GCC="$AFL_DIR/afl-gcc"
    if [ ! -f "$AFL_GCC" ]; then
        # Try system afl-gcc
        if command -v afl-gcc &>/dev/null; then
            AFL_GCC="$(command -v afl-gcc)"
        else
            echo "ERROR: afl-gcc not found (tried $AFL_DIR/afl-gcc and PATH)"
            exit 1
        fi
    fi
    echo "  Compiling C binaries with afl-gcc ($AFL_GCC)..."
    for N in "${PROBLEMS[@]}"; do
        SRC="$BINS_DIR/Problem${N}.c"
        if [ ! -f "$SRC" ]; then
            echo "ERROR: C source not found: $SRC"; exit 1
        fi
        # Regenerate seed files from input alphabet in the C source
        INPUTS=$(grep "int inputs\[\]" "$SRC" | grep -o '{[^}]*}' | tr -d '{}' | tr ',' ' ')
        mkdir -p "$BINS_DIR/tests_${N}"
        for i in $INPUTS; do
            i=$(echo "$i" | tr -d ' ')
            echo "$i" > "$BINS_DIR/tests_${N}/${i}.txt"
        done
        echo "    Problem${N}..."
        AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 \
            "$AFL_GCC" "$SRC" -o "$BINS_DIR/Problem${N}" -lm
    done
    echo "  Compilation done."
fi

# Verify binaries exist after possible compilation
for N in "${PROBLEMS[@]}"; do
    if [ ! -f "$BINS_DIR/Problem$N" ]; then
        echo "ERROR: Binary not found: $BINS_DIR/Problem$N"
        echo "Use --compile to build with afl-gcc, or compile manually: cd fuzzing_bins && ./compile.sh"
        exit 1
    fi
done

# ── Run AFL and extract errors ───────────────────────────────────────────────
run_afl_problem() {
    local N="$1"
    local RUN_DIR="$2"
    local FINDINGS="$RUN_DIR/findings_${N}"

    mkdir -p "$FINDINGS"

    echo "      Problem$N: running AFL for ${DURATION}s..."

    # Run AFL
    timeout "$DURATION" env AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 \
        "$AFL_DIR/afl-fuzz" \
        -i "$BINS_DIR/tests_${N}" \
        -o "$FINDINGS" \
        "$BINS_DIR/Problem$N" \
        > /dev/null 2>&1 || true

    echo "      Problem$N: extracting errors and branch coverage..."

    # Extract errors from crashes
    local CSV="$RUN_DIR/problem${N}_afl_errors.csv"
    local BRANCH_CSV="$RUN_DIR/problem${N}_afl_branches.csv"
    local LOG="$RUN_DIR/problem${N}.log"
    if [ -d "$FINDINGS/default" ]; then
        FINDINGS="$FINDINGS/default"
    fi
    local CRASH_DIR="$FINDINGS/crashes"

    echo "elapsed_seconds,error_code" > "$CSV"

    # Get AFL start time from fuzzer_stats (needed for both errors and branches)
    local START_TIME=$(grep "start_time" "$FINDINGS/fuzzer_stats" 2>/dev/null | awk '{print $3}')
    START_TIME=${START_TIME:-$(date +%s)}

    if [ -d "$CRASH_DIR" ]; then
        local ERROR_COUNT=0
        local UNIQUE_ERRORS=""

        for crash_file in "$CRASH_DIR"/id:*; do
            [ -f "$crash_file" ] || continue
            local FILE_TIME=$(stat -c %Y "$crash_file" 2>/dev/null || stat -f %m "$crash_file" 2>/dev/null)
            local ELAPSED=$(echo "$FILE_TIME - $START_TIME" | bc 2>/dev/null || echo "0")

            # Run the binary with crash input to get error code
            local ERR_OUTPUT=$(timeout 5 "$BINS_DIR/Problem$N" < "$crash_file" 2>&1 || true)
            local ERROR_CODE=$(echo "$ERR_OUTPUT" | grep -o 'error_[0-9]*' | head -1)

            if [ -n "$ERROR_CODE" ]; then
                echo "${ELAPSED},${ERROR_CODE}" >> "$CSV"
                if ! echo "$UNIQUE_ERRORS" | grep -q "$ERROR_CODE"; then
                    UNIQUE_ERRORS="$UNIQUE_ERRORS $ERROR_CODE"
                    ERROR_COUNT=$((ERROR_COUNT + 1))
                fi
            fi
        done

        echo "Problem$N: $ERROR_COUNT unique errors found" > "$LOG"
        echo "Errors: $UNIQUE_ERRORS" >> "$LOG"

        # Also count unique crashes
        local CRASH_COUNT=$(ls "$CRASH_DIR"/id:* 2>/dev/null | wc -l)
        echo "Total crashes: $CRASH_COUNT" >> "$LOG"
    else
        echo "Problem$N: no crashes directory found" > "$LOG"
    fi

    # ── Branch coverage from AFL plot_data ────────────────────────────────────
    # plot_data columns: unix_time, cycles_done, cur_path, paths_total,
    #   pending_total, pending_favs, map_size, unique_crashes, unique_hangs,
    #   max_depth, execs_per_sec
    # map_size is "X.XX%" of a 65536-entry bitmap → edges ≈ 65536 * X.XX / 100
    echo "elapsed_seconds,unique_branches" > "$BRANCH_CSV"
    if [ -f "$FINDINGS/plot_data" ]; then
        tail -n +2 "$FINDINGS/plot_data" | awk -F', ' -v start="$START_TIME" '
        {
            unix_time = $1
            map_pct   = $7
            gsub(/%/, "", map_pct)
            elapsed = unix_time - start
            edges   = int(65536 * map_pct / 100 + 0.5)
            if (elapsed >= 0) print elapsed "," edges
        }' >> "$BRANCH_CSV"

        # Final edges_found from fuzzer_stats (more accurate than last plot_data row)
        local EDGES_FOUND=$(grep "edges_found" "$FINDINGS/fuzzer_stats" 2>/dev/null | awk '{print $3}')
        if [ -n "$EDGES_FOUND" ]; then
            echo "Total unique branches visited: $EDGES_FOUND" >> "$LOG"
        fi
    fi

    echo "      Problem$N: done."
}

# ── Main loop ────────────────────────────────────────────────────────────────
for run_idx in $(seq 1 $NUM_RUNS); do
    echo "── Run $run_idx/$NUM_RUNS ──"
    RUN_DIR="$RESULTS_DIR/run${run_idx}"
    mkdir -p "$RUN_DIR"

    for N in "${PROBLEMS[@]}"; do
        run_afl_problem "$N" "$RUN_DIR"
    done

    echo ""
done

echo "╔══════════════════════════════════════════════════════════╗"
echo "║  AFL EXPERIMENTS COMPLETE                                ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo "Results in: $RESULTS_DIR/"
