#!/bin/bash
# Lab 2 – Concolic Execution Evaluation Script
#
# Runs Problems 11-15 and 17 concolically, in parallel.
# Total time: ~DURATION seconds.
#
# Results layout:
#   concolic_results/
#     problem{N}.log
#     problem{N}_concolic_branches.csv
#     problem{N}_concolic_errors.csv
#
# Usage:
#   ./run_concolic_lab.sh [--duration 300]
#
# Defaults: duration=300s

REPO_DIR="$(cd "$(dirname "$0")" && pwd)"
RESULTS_DIR="$REPO_DIR/concolic_results"
PROBLEMS=(11 12 13 14 15 17)

DURATION=300

while [[ $# -gt 0 ]]; do
    case "$1" in
        --duration) DURATION="$2"; shift 2 ;;
        *) echo "Unknown argument: $1"; exit 1 ;;
    esac
done

TIMEOUT=$((DURATION + 60))

cd "$REPO_DIR"

run_with_timeout() {
    local secs=$1; shift
    "$@" &
    local pid=$!
    ( sleep "$secs" && kill -TERM "$pid" 2>/dev/null ) &
    local watcher=$!
    wait "$pid" 2>/dev/null
    local exit_code=$?
    kill "$watcher" 2>/dev/null
    wait "$watcher" 2>/dev/null
    return $exit_code
}

echo "╔══════════════════════════════════════════════════════════╗"
echo "║         Lab 2 - Concolic Execution Evaluation           ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo "  Problems  : ${PROBLEMS[*]}"
echo "  Duration  : ${DURATION}s per problem"
echo ""

echo "[1/3] Building project..."
mvn clean package -q || { echo "ERROR: Maven build failed"; exit 1; }
echo "      Done."
echo ""

echo "[2/3] Compiling instrumented problems..."
for N in "${PROBLEMS[@]}"; do
    echo "      Problem$N..."
    javac -cp target/aistr.jar:. Errors.java "instrumented/Problem$N.java" \
        || { echo "ERROR: Failed to compile Problem$N"; exit 1; }
done
echo "      Done."
echo ""

mkdir -p "$RESULTS_DIR"

run_problem() {
    local N="$1"
    local LOG="$RESULTS_DIR/problem${N}.log"

    run_with_timeout "$TIMEOUT" \
        java \
            -Dconcolic.problem="$N" \
            -cp target/aistr.jar:lib/com.microsoft.z3.jar:./instrumented:. "Problem$N" \
        > "$LOG" 2>&1
}

echo "[3/3] Running concolic execution..."
echo "   Starting $(date '+%H:%M:%S') – running ${#PROBLEMS[@]} problems in parallel..."
echo ""

for N in "${PROBLEMS[@]}"; do
    run_problem "$N" &
done
wait

echo "   Finished $(date '+%H:%M:%S')"
echo ""

echo "╔══════════════════════════════════════════════════════════╗"
echo "║                    RESULTS SUMMARY                      ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

printf "  %-11s  %-16s  %-15s  %s\n" "Problem" "Total Branches" "Unique Errors" "Triggered Error Codes"
printf "  %-11s  %-16s  %-15s  %s\n" "-------" "--------------" "-------------" "---------------------"

for N in "${PROBLEMS[@]}"; do
    LOG="$RESULTS_DIR/problem${N}.log"
    TOTAL="N/A"; ERRS="N/A"; CODES="N/A"
    if [ -f "$LOG" ]; then
        TOTAL=$(grep "Total unique branches visited:" "$LOG" | awk '{print $NF}')
        ERRS=$(grep  "Triggered errors ("            "$LOG" | sed 's/Triggered errors (\([0-9]*\)).*/\1/')
        CODES=$(grep "Triggered errors ("            "$LOG" | sed 's/.*): //')
        TOTAL="${TOTAL:-ERR}"; ERRS="${ERRS:-ERR}"; CODES="${CODES:-none}"
    fi
    printf "  %-11s  %-16s  %-15s  %s\n" "Problem$N" "$TOTAL" "$ERRS" "$CODES"
done

echo ""
echo "Best traces:"
for N in "${PROBLEMS[@]}"; do
    LOG="$RESULTS_DIR/problem${N}.log"
    [ -f "$LOG" ] || continue
    TRACE=$(grep "Best trace:" "$LOG" | sed 's/Best trace: //')
    printf "  Problem%-3s : %s\n" "$N" "${TRACE:-N/A}"
done

echo ""
echo "Convergence CSVs: $RESULTS_DIR/"
echo "  Columns: elapsed_seconds, unique_branches  (branches CSV)"
echo "           elapsed_seconds, error_code       (errors CSV)"