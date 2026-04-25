#!/bin/bash
# Evaluation script for Lab 1 - Random Fuzzer
# Runs Problems 11-15 and 17 for 5 minutes each and summarises results.

REPO_DIR="$(cd "$(dirname "$0")" && pwd)"
RESULTS_DIR="$REPO_DIR/fuzzing_results"
PROBLEMS=(11 12 13 14 15 17)
DURATION=300   # 5 minutes internal timer (must match FuzzingLab.run())
TIMEOUT=$((DURATION + 60))  # external kill-switch with 1-min grace period

cd "$REPO_DIR"

# macOS does not ship 'timeout'; this pure-bash replacement works everywhere.
# Runs "$@", kills it after $TIMEOUT seconds if it hasn't exited on its own.
run_with_timeout() {
    local secs=$1; shift
    "$@" &
    local pid=$!
    # Watcher: sleep then kill the child if still alive
    ( sleep "$secs" && kill -TERM "$pid" 2>/dev/null ) &
    local watcher=$!
    wait "$pid" 2>/dev/null
    local exit_code=$?
    # Clean up watcher whether or not it already fired
    kill "$watcher" 2>/dev/null
    wait "$watcher" 2>/dev/null
    return $exit_code
}

# ── 1. Build ─────────────────────────────────────────────────────────────────
echo "=== Lab 1 – Random Fuzzer Evaluation ==="
echo "Problems : ${PROBLEMS[*]}"
echo "Duration : ${DURATION}s per problem"
echo ""
echo "[1/3] Building project (mvn clean package)..."
mvn clean package -q
echo "      Done."
echo ""

# ── 2. Recompile instrumented files ──────────────────────────────────────────
mkdir -p "$RESULTS_DIR"
echo "[2/3] Compiling instrumented problems..."
for N in "${PROBLEMS[@]}"; do
    echo "      Problem$N..."
    javac -cp target/aistr.jar:. Errors.java "instrumented/Problem$N.java" \
        || { echo "ERROR: Failed to compile Problem$N"; exit 1; }
done
echo "      Done."
echo ""

# ── 3. Run each problem ───────────────────────────────────────────────────────
echo "[3/3] Running fuzzer..."
for N in "${PROBLEMS[@]}"; do
    LOG="$RESULTS_DIR/problem${N}.log"
    echo ""
    echo "┌─ Problem $N ─────────────────────────────────"
    echo "│  Log: $LOG"
    echo "│  Start: $(date '+%H:%M:%S')"

    # The internal 5-min timer in FuzzingLab.run() drives termination.
    # run_with_timeout is a safety net in case the JVM hangs.
    # Filter out the "Found a new branch" spam printed by DistanceTracker.myIf
    # on every if-statement — keeping it would make logs arbitrarily large.
    run_with_timeout "$TIMEOUT" \
        java -cp target/aistr.jar:./instrumented:. "Problem$N" \
        2>&1 | grep -v -e "^Found a new branch$" -e "^Invalid input:.*$" > "$LOG"
    EXIT=${PIPESTATUS[0]}

    if [ $EXIT -eq 143 ]; then  # SIGTERM exit code
        echo "│  WARNING: external timeout hit (JVM did not exit cleanly)"
    elif [ $EXIT -ne 0 ]; then
        echo "│  WARNING: process exited with code $EXIT"
    fi

    # Extract key lines for immediate feedback
    TOTAL=$(grep  "Total unique branches visited:"         "$LOG" | awk '{print $NF}')
    BEST=$(grep   "Max unique branches in a single trace:" "$LOG" | awk '{print $NF}')
    ERRORS=$(grep "Triggered errors"                       "$LOG" | sed 's/Triggered errors ([0-9]*): //')
    echo "│  Total unique branches   : ${TOTAL:-not found}"
    echo "│  Best single-trace count : ${BEST:-not found}"
    echo "│  Triggered errors        : ${ERRORS:-none}"
    echo "└──────────────────────────────────────────────"
done

# ── 4. Summary table ──────────────────────────────────────────────────────────
echo ""
echo "╔══════════════════════════════════════════════════════════════════════╗"
echo "║                      FUZZING RESULTS SUMMARY                        ║"
echo "╠════════════╦════════════════╦═════════════════╦══════════════════════════════╣"
printf "║ %-10s ║ %-14s ║ %-15s ║ %-28s ║\n" \
    "Problem" "Total Branches" "Best Single Run" "Triggered Errors"
echo "╠════════════╬════════════════╬═════════════════╬══════════════════════════════╣"

for N in "${PROBLEMS[@]}"; do
    LOG="$RESULTS_DIR/problem${N}.log"
    TOTAL="N/A"; BEST="N/A"; ERR_SUMMARY="N/A"
    if [ -f "$LOG" ]; then
        TOTAL=$(grep  "Total unique branches visited:"         "$LOG" | awk '{print $NF}')
        BEST=$(grep   "Max unique branches in a single trace:" "$LOG" | awk '{print $NF}')
        ERR_SUMMARY=$(grep "Triggered errors" "$LOG" | sed 's/Triggered errors //' | sed 's/: /: /')
        TOTAL="${TOTAL:-ERR}"; BEST="${BEST:-ERR}"; ERR_SUMMARY="${ERR_SUMMARY:-none}"
    fi
    printf "║ %-10s ║ %-14s ║ %-15s ║ %-28s ║\n" \
        "Problem$N" "$TOTAL" "$BEST" "$ERR_SUMMARY"
done

echo "╚════════════╩════════════════╩═════════════════╩══════════════════════════════╝"
echo ""
echo "Full logs: $RESULTS_DIR/"
echo ""
echo "Best traces (full):"
for N in "${PROBLEMS[@]}"; do
    LOG="$RESULTS_DIR/problem${N}.log"
    if [ -f "$LOG" ]; then
        TRACE=$(grep "Best trace:" "$LOG" | sed 's/Best trace: //')
        echo "  Problem$N : $TRACE"
    fi
done
