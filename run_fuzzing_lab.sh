#!/bin/bash
# Lab 1 – Fuzzing Evaluation Script
#
# Runs Problems 11-15 and 17 in both RANDOM and SMART (hill climber) modes.
# Problems within each mode are run IN PARALLEL to reduce total wall-clock time.
# Total time: ~2 × DURATION (one pass per mode).
#
# Results layout:
#   fuzzing_results/
#     random/  problem{N}.log  problem{N}_random_branches.csv  problem{N}_random_errors.csv
#     smart/   problem{N}.log  problem{N}_smart_branches.csv   problem{N}_smart_errors.csv
#
# Usage:
#   ./run_fuzzing_lab.sh [--mode random|smart|both] [--duration 300] [--mutations 5]
#
# Defaults: mode=both, duration=300s, mutations=5

REPO_DIR="$(cd "$(dirname "$0")" && pwd)"
RESULTS_DIR="$REPO_DIR/fuzzing_results"
PROBLEMS=(11 12 13 14 15 17)

# ── Parse arguments ───────────────────────────────────────────────────────────
RUN_MODE="both"   # random | smart | both
DURATION=300
MUTATIONS=5

while [[ $# -gt 0 ]]; do
    case "$1" in
        --mode)      RUN_MODE="$2";  shift 2 ;;
        --duration)  DURATION="$2";  shift 2 ;;
        --mutations) MUTATIONS="$2"; shift 2 ;;
        *) echo "Unknown argument: $1"; exit 1 ;;
    esac
done

TIMEOUT=$((DURATION + 60))  # external kill-switch with 1-min grace period

cd "$REPO_DIR"

# ── macOS-compatible timeout replacement ──────────────────────────────────────
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

# ── 1. Build ──────────────────────────────────────────────────────────────────
echo "╔══════════════════════════════════════════════════════════╗"
echo "║           Lab 1 – Fuzzing Evaluation                    ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo "  Mode      : $RUN_MODE"
echo "  Problems  : ${PROBLEMS[*]}"
echo "  Duration  : ${DURATION}s per problem"
echo "  Mutations : $MUTATIONS (hill climber only)"
echo ""
echo "[1/3] Building project..."
mvn clean package -q || { echo "ERROR: Maven build failed"; exit 1; }
echo "      Done."
echo ""

# ── 2. Compile instrumented problems ─────────────────────────────────────────
echo "[2/3] Compiling instrumented problems..."
for N in "${PROBLEMS[@]}"; do
    echo "      Problem$N..."
    javac -cp target/aistr.jar:. Errors.java "instrumented/Problem$N.java" \
        || { echo "ERROR: Failed to compile Problem$N"; exit 1; }
done
echo "      Done."
echo ""

# ── 3. Run problems ───────────────────────────────────────────────────────────

# run_problem <problem_number> <mode>
# Runs the problem in the given mode, logs to fuzzing_results/<mode>/problem<N>.log
# The JVM writes convergence CSVs directly to fuzzing_results/<mode>/.
run_problem() {
    local N="$1"
    local MODE="$2"
    local SUBDIR="$RESULTS_DIR/$MODE"
    local LOG="$SUBDIR/problem${N}.log"

    mkdir -p "$SUBDIR"

    run_with_timeout "$TIMEOUT" \
        java \
            -Dfuzzing.mode="$MODE" \
            -Dfuzzing.problem="$N" \
            -Dfuzzing.duration="$DURATION" \
            -Dfuzzing.mutations="$MUTATIONS" \
            -Dfuzzing.output.dir="$SUBDIR" \
            -cp target/aistr.jar:./instrumented:. "Problem$N" \
        2>&1 \
        | grep -v -e "^Found a new branch$" -e "^Invalid input: Current state.*$" \
        > "$LOG"
}

run_mode() {
    local MODE="$1"
    echo "── $MODE mode ──────────────────────────────────────────────"
    echo "   Starting $(date '+%H:%M:%S') – running ${#PROBLEMS[@]} problems in parallel..."

    for N in "${PROBLEMS[@]}"; do
        run_problem "$N" "$MODE" &
    done
    wait   # wait for all problems in this mode to finish

    echo "   Finished $(date '+%H:%M:%S')"
    echo ""
}

echo "[3/3] Running fuzzer..."
echo ""

case "$RUN_MODE" in
    random) run_mode "random" ;;
    smart)  run_mode "smart"  ;;
    both)   run_mode "random"; run_mode "smart" ;;
    *) echo "ERROR: unknown mode '$RUN_MODE' (use random|smart|both)"; exit 1 ;;
esac

# ── 4. Summary ────────────────────────────────────────────────────────────────

print_mode_table() {
    local MODE="$1"
    local SUBDIR="$RESULTS_DIR/$MODE"
    echo "  ── $MODE ──"
    printf "  %-11s  %-16s  %-15s  %s\n" "Problem" "Total Branches" "Best Run" "Triggered Errors"
    printf "  %-11s  %-16s  %-15s  %s\n" "-------" "--------------" "--------" "----------------"
    for N in "${PROBLEMS[@]}"; do
        local LOG="$SUBDIR/problem${N}.log"
        local TOTAL="N/A" BEST="N/A" ERRS="N/A"
        if [ -f "$LOG" ]; then
            TOTAL=$(grep "Total unique branches visited:"         "$LOG" | awk '{print $NF}')
            BEST=$(grep  "Max unique branches in a single trace:" "$LOG" | awk '{print $NF}')
            ERRS=$(grep  "Triggered errors"                       "$LOG" | sed 's/.*: //')
            TOTAL="${TOTAL:-ERR}"; BEST="${BEST:-ERR}"; ERRS="${ERRS:-none}"
        fi
        printf "  %-11s  %-16s  %-15s  %s\n" "Problem$N" "$TOTAL" "$BEST" "$ERRS"
    done
    echo ""
}

echo "╔══════════════════════════════════════════════════════════╗"
echo "║                    RESULTS SUMMARY                      ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

[[ "$RUN_MODE" == "random" || "$RUN_MODE" == "both" ]] && print_mode_table "random"
[[ "$RUN_MODE" == "smart"  || "$RUN_MODE" == "both" ]] && print_mode_table "smart"

echo "Best traces:"
for MODE in random smart; do
    [[ "$RUN_MODE" != "both" && "$RUN_MODE" != "$MODE" ]] && continue
    echo "  [$MODE]"
    for N in "${PROBLEMS[@]}"; do
        LOG="$RESULTS_DIR/$MODE/problem${N}.log"
        [ -f "$LOG" ] || continue
        TRACE=$(grep "Best trace:" "$LOG" | sed 's/Best trace: //')
        printf "    Problem%-3s : %s\n" "$N" "${TRACE:-N/A}"
    done
    echo ""
done

echo "Convergence CSVs (for plotting): $RESULTS_DIR/{random,smart}/"
echo "  Columns: elapsed_seconds, unique_branches  (branches CSV)"
echo "           elapsed_seconds, error_code       (errors CSV)"
