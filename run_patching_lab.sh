#!/bin/bash
# Lab 3 – Automated Code Patching Evaluation Script
#
# Runs Problems 1, 4, 7, 11, 12, and 15 with the patching EA.
# Iterates through different mutation rates automatically.
#
# Usage:
#   ./run_patching_lab.sh [--duration 300] [--rates "0.0 0.3 0.6"]
#
# Defaults: duration=300s (5min), parallel execution, rates="0.0 0.3 0.6"

REPO_DIR="$(cd "$(dirname "$0")" && pwd)"
PROBLEMS=(1 4 7 11 12 15)

DURATION=300
RATES="0.0 0.3 0.6"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --duration)  DURATION="$2";  shift 2 ;;
        --rates)     RATES="$2";     shift 2 ;;
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
echo "║       Lab 3 - Automated Code Patching Evaluation        ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo "  Problems  : ${PROBLEMS[*]}"
echo "  Duration  : ${DURATION}s per problem"
echo "  Rates     : $RATES"
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

run_problem() {
    local N="$1"
    local RATE="$2"
    local OUT_DIR="$REPO_DIR/patching_results_${RATE}"
    local LOG="$OUT_DIR/problem${N}.log"

    run_with_timeout "$TIMEOUT" \
        java \
            -Dpatching.duration="$DURATION" \
            -Dpatching.mutationRate="$RATE" \
            -cp target/aistr.jar:./instrumented:. "Problem$N" \
        > "$LOG" 2>&1

    # Move the patch file into results dir if it was created
    if [ -f "$REPO_DIR/patching_results_Problem${N}.txt" ]; then
        mv "$REPO_DIR/patching_results_Problem${N}.txt" "$OUT_DIR/"
    fi
}

echo "[3/3] Running patching EA..."

for RATE in $RATES; do
    echo "── Mutation Rate: $RATE ────────────────────────"
    OUT_DIR="$REPO_DIR/patching_results_${RATE}"
    mkdir -p "$OUT_DIR"
    
    echo "   Starting $(date '+%H:%M:%S') – running ${#PROBLEMS[@]} problems in parallel..."
    for N in "${PROBLEMS[@]}"; do
        run_problem "$N" "$RATE" &
    done
    wait
    echo "   Finished $(date '+%H:%M:%S')"
    echo ""
done

echo "╔══════════════════════════════════════════════════════════╗"
echo "║                    RESULTS SUMMARY                      ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

for RATE in $RATES; do
    OUT_DIR="$REPO_DIR/patching_results_${RATE}"
    echo "  ── Mutation Rate: $RATE ──"
    printf "  %-11s  %-15s  %-14s  %s\n" "Problem" "Best Fitness" "Generations" "Operators Patched"
    printf "  %-11s  %-15s  %-14s  %s\n" "-------" "------------" "-----------" "-----------------"

    for N in "${PROBLEMS[@]}"; do
        LOG="$OUT_DIR/problem${N}.log"
        FITNESS="N/A"; GENS="N/A"; PATCHED="N/A"
        if [ -f "$LOG" ]; then
            FITNESS=$(grep "^  Best fitness:" "$LOG" | tail -1 | awk '{print $NF}')
            GENS=$(grep "^  Generations:" "$LOG" | tail -1 | awk '{print $NF}')
            PATCHED=$(grep "^  Total operators patched:" "$LOG" | tail -1 | awk '{print $NF}')
            FITNESS="${FITNESS:-ERR}"; GENS="${GENS:-ERR}"; PATCHED="${PATCHED:-ERR}"
        fi
        printf "  %-11s  %-15s  %-14s  %s\n" "Problem$N" "$FITNESS" "$GENS" "$PATCHED"
    done
    echo ""
done

echo "Detailed results are in patching_results_{RATE}/ directories."
