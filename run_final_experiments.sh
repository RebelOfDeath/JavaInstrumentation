#!/bin/bash
# Final Assignment – Master Experiment Script
#
# ALL jobs are launched into a single parallel pool with --jobs concurrency.
#   Task 1: 5 runs × 3 techniques × 6 problems = 90 jobs
#   Task 3: 5 runs × 2 variants   × 6 problems = 60 jobs
#
# Usage:
#   ./run_final_experiments.sh [--duration 300] [--runs 5] [--task 1|3|all] [--jobs 6]

set -e

REPO_DIR="$(cd "$(dirname "$0")" && pwd)"
RESULTS_DIR="$REPO_DIR/final_results"
PROBLEMS=(11 12 13 14 15 17)

DURATION=300
NUM_RUNS=5
TASK="all"
MAX_JOBS=10

while [[ $# -gt 0 ]]; do
    case "$1" in
        --duration)  DURATION="$2";  shift 2 ;;
        --runs)      NUM_RUNS="$2";  shift 2 ;;
        --task)      TASK="$2";      shift 2 ;;
        --jobs|-j)   MAX_JOBS="$2";  shift 2 ;;
        *) echo "Unknown argument: $1"; exit 1 ;;
    esac
done

TIMEOUT=$((DURATION + 60))
SEEDS=(42 137 256 1024 7777)

cd "$REPO_DIR"

# ── macOS timeout compatibility ──────────────────────────────────────────────
# macOS doesn't have `timeout` by default. Use gtimeout if available, else a
# shell-based fallback.
if command -v timeout &>/dev/null; then
    TIMEOUT_CMD="timeout"
elif command -v gtimeout &>/dev/null; then
    TIMEOUT_CMD="gtimeout"
else
    # Write a portable timeout wrapper (in /tmp to avoid path-with-spaces issues)
    TIMEOUT_CMD="/tmp/.str_timeout_helper.sh"
    cat > "$TIMEOUT_CMD" <<'HELPER'
#!/bin/bash
secs=$1; shift
"$@" &
pid=$!
( sleep "$secs" && kill -TERM "$pid" 2>/dev/null ) &
watcher=$!
wait "$pid" 2>/dev/null
exit_code=$?
kill "$watcher" 2>/dev/null
wait "$watcher" 2>/dev/null
exit $exit_code
HELPER
    chmod +x "$TIMEOUT_CMD"
fi

# ── Job pool using GNU xargs -P ─────────────────────────────────────────────
# Each job is a self-contained shell snippet written to a script file.
# No export -f needed — each job script is independent.
JOB_DIR=$(mktemp -d)
JOB_COUNT=0

add_job() {
    JOB_COUNT=$((JOB_COUNT + 1))
    local SCRIPT="$JOB_DIR/job_$(printf '%04d' $JOB_COUNT).sh"
    cat > "$SCRIPT" <<JOBEOF
#!/bin/bash
cd "$REPO_DIR"
$1
JOBEOF
    chmod +x "$SCRIPT"
}

run_all_jobs() {
    local total=$JOB_COUNT
    echo "  Launching $total jobs with max $MAX_JOBS concurrent..."

    # Use xargs -P to run job scripts in parallel
    find "$JOB_DIR" -name 'job_*.sh' | sort | xargs -P "$MAX_JOBS" -n 1 bash

    echo "  All $total jobs complete."

    # Clean up
    rm -rf "$JOB_DIR"
    JOB_DIR=$(mktemp -d)
    JOB_COUNT=0
}

# ── 1. Build ──────────────────────────────────────────────────────────────────
TASK1_JOBS=$((NUM_RUNS * 3 * ${#PROBLEMS[@]}))
TASK3_JOBS=$((NUM_RUNS * 2 * ${#PROBLEMS[@]}))
case "$TASK" in
    1)   TOTAL_JOBS=$TASK1_JOBS ;;
    3)   TOTAL_JOBS=$TASK3_JOBS ;;
    all) TOTAL_JOBS=$((TASK1_JOBS + TASK3_JOBS)) ;;
esac
EST_BATCHES=$(( (TOTAL_JOBS + MAX_JOBS - 1) / MAX_JOBS ))
EST_MINUTES=$(( EST_BATCHES * DURATION / 60 ))

echo "╔══════════════════════════════════════════════════════════╗"
echo "║     Final Assignment – Master Experiment Runner         ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo "  Task(s)     : $TASK"
echo "  Problems    : ${PROBLEMS[*]}"
echo "  Duration    : ${DURATION}s per problem"
echo "  Runs        : $NUM_RUNS (seeds: ${SEEDS[*]})"
echo "  Max jobs    : $MAX_JOBS concurrent"
echo "  Total jobs  : $TOTAL_JOBS"
echo "  Est. time   : ~${EST_MINUTES} minutes"
echo ""
echo "[1/3] Building project..."
mvn clean package -q || { echo "ERROR: Maven build failed"; exit 1; }
echo "      Done."
echo ""

# ── 2. Instrument & compile ──────────────────────────────────────────────────
RERS_DIR="${RERS_DIR:-./SeqReachabilityRers2020}"
CUSTOM_DIR="./custom_problems"

# Z3 jar: use the bundled jar (pre-generics 4.8.x API, Java 8 class files).
# The apt jar on Ubuntu 20.04 is the same API version; on Ubuntu 22.04 it is
# NOT compatible (newer generics API).  Always prefer the bundled jar.
Z3_JAR=lib/com.microsoft.z3.jar
FUZZ_INSTR_DIR="instrumented_fuzzing"
CONC_INSTR_DIR="instrumented_concolic"
mkdir -p "$FUZZ_INSTR_DIR" "$CONC_INSTR_DIR"

needs_instrument=false
for N in "${PROBLEMS[@]}"; do
    if [ ! -f "$FUZZ_INSTR_DIR/Problem${N}.java" ] || [ ! -f "$CONC_INSTR_DIR/Problem${N}.java" ]; then
        needs_instrument=true
        break
    fi
done

echo "[2/3] Instrumenting RERS problems..."
if $needs_instrument; then
    for N in "${PROBLEMS[@]}"; do
        SRC="$RERS_DIR/Problem${N}/Problem${N}.java"
        if [ -f "$CUSTOM_DIR/Problem${N}.java" ]; then
            SRC="$CUSTOM_DIR/Problem${N}.java"
        fi

        if [ ! -f "$SRC" ]; then
            if [ -f "instrumented/Problem${N}.java" ]; then
                echo "      Problem$N: using existing instrumented file"
                cp "instrumented/Problem${N}.java" "$FUZZ_INSTR_DIR/Problem${N}.java" 2>/dev/null || true
                cp "instrumented/Problem${N}.java" "$CONC_INSTR_DIR/Problem${N}.java" 2>/dev/null || true
                continue
            fi
            echo "ERROR: Cannot find source for Problem$N at $SRC"
            echo "       Set RERS_DIR env var (e.g., RERS_DIR=../RERS ./run_final_experiments.sh)"
            exit 1
        fi

        if [ ! -f "$FUZZ_INSTR_DIR/Problem${N}.java" ]; then
            echo "      Problem$N (fuzzing)..."
            java -XX:+UseG1GC -Xmx4g -cp target/aistr.jar \
                nl.tudelft.instrumentation.Main --type=fuzzing --file="$SRC" \
                > "$FUZZ_INSTR_DIR/Problem${N}.java"
        fi

        if [ ! -f "$CONC_INSTR_DIR/Problem${N}.java" ]; then
            echo "      Problem$N (concolic)..."
            java -XX:+UseG1GC -Xmx4g -cp target/aistr.jar \
                nl.tudelft.instrumentation.Main --type=concolic --file="$SRC" \
                > "$CONC_INSTR_DIR/Problem${N}.java"
        fi
    done
else
    echo "      Using cached instrumented files."
fi
echo "      Done."
echo ""

echo "[3/3] Compiling instrumented problems..."
for N in "${PROBLEMS[@]}"; do
    echo "      Problem$N (fuzzing)..."
    javac -cp target/aistr.jar:. Errors.java "$FUZZ_INSTR_DIR/Problem$N.java" \
        || { echo "ERROR: Failed to compile Problem$N (fuzzing)"; exit 1; }
    echo "      Problem$N (concolic)..."
    javac -cp target/aistr.jar:"$Z3_JAR":. Errors.java "$CONC_INSTR_DIR/Problem$N.java" \
        || { echo "ERROR: Failed to compile Problem$N (concolic)"; exit 1; }
done
echo "      Done."
echo ""

# ── Job templates ────────────────────────────────────────────────────────────
# Each job is a self-contained bash snippet (no function exports needed).

make_fuzzing_job() {
    local N="$1" MODE="$2" SEED="$3" OUT_DIR="$4" MUTS="${5:-5}"
    cat <<EOF
mkdir -p "$OUT_DIR"
"$TIMEOUT_CMD" $TIMEOUT java \\
    -Dfuzzing.mode="$MODE" \\
    -Dfuzzing.problem="$N" \\
    -Dfuzzing.duration="$DURATION" \\
    -Dfuzzing.mutations="$MUTS" \\
    -Dfuzzing.seed="$SEED" \\
    -Dfuzzing.output.dir="$OUT_DIR" \\
    -cp target/aistr.jar:./$FUZZ_INSTR_DIR:. "Problem$N" \\
    2>&1 | grep -v -e "^Found a new branch\$" -e "^Invalid input: Current state.*\$" \\
    > "$OUT_DIR/problem${N}.log" 2>&1 || true
echo "  Done: $MODE P$N seed=$SEED"
EOF
}

make_concolic_job() {
    local N="$1" SEED="$2" OUT_DIR="$3"
    # Z3_JAR is the primary jar (apt 4.8.x on Docker, bundled on macOS).
    # lib/com.microsoft.z3.jar is listed second as a fallback if Z3_JAR is wrong path.
    # Java uses the first entry that provides the class, so Z3_JAR wins.
    local CP="target/aistr.jar:${Z3_JAR}:lib/com.microsoft.z3.jar:${CONC_INSTR_DIR}:."
    # java.library.path: covers macOS (. and lib/ for libz3java.dylib),
    # and Docker on amd64/arm64 (/usr/lib/jni, /usr/lib/*/jni).
    local JLP=".:lib:/usr/lib/jni:/usr/lib/x86_64-linux-gnu:/usr/lib/aarch64-linux-gnu:/usr/lib/x86_64-linux-gnu/jni:/usr/lib/aarch64-linux-gnu/jni"
    cat <<EOF
mkdir -p "$OUT_DIR"
"$TIMEOUT_CMD" $TIMEOUT java -Djava.library.path="$JLP" -Dconcolic.problem="$N" -Dconcolic.seed="$SEED" -Dconcolic.duration="$DURATION" -Dconcolic.output.dir="$OUT_DIR" -cp "$CP" Problem${N} > "$OUT_DIR/problem${N}.log" 2>&1 || true
echo "  Done: concolic P$N seed=$SEED"
EOF
}

# ── Task 1: Build jobs ──────────────────────────────────────────────────────
run_task1() {
    echo "═══════════════════════════════════════════════════════════"
    echo "  TASK 1: Empirical Comparison (random, smart, concolic)"
    echo "  ${TASK1_JOBS} jobs total"
    echo "═══════════════════════════════════════════════════════════"

    for run_idx in $(seq 1 $NUM_RUNS); do
        SEED=${SEEDS[$((run_idx - 1))]}
        for N in "${PROBLEMS[@]}"; do
            add_job "$(make_fuzzing_job  $N random  $SEED "$RESULTS_DIR/task1/random/run${run_idx}"  5)"
            add_job "$(make_fuzzing_job  $N smart   $SEED "$RESULTS_DIR/task1/smart/run${run_idx}"   5)"
            add_job "$(make_concolic_job $N         $SEED "$RESULTS_DIR/task1/concolic/run${run_idx}")"
        done
    done

    run_all_jobs
    echo ""
}

# ── Task 3: Build jobs ──────────────────────────────────────────────────────
run_task3() {
    echo "═══════════════════════════════════════════════════════════"
    echo "  TASK 3: Improvement Study (smart vs improved)"
    echo "  ${TASK3_JOBS} jobs total"
    echo "═══════════════════════════════════════════════════════════"

    for run_idx in $(seq 1 $NUM_RUNS); do
        SEED=${SEEDS[$((run_idx - 1))]}
        for N in "${PROBLEMS[@]}"; do
            add_job "$(make_fuzzing_job $N smart    $SEED "$RESULTS_DIR/task3/smart/run${run_idx}"    5)"
            add_job "$(make_fuzzing_job $N improved $SEED "$RESULTS_DIR/task3/improved/run${run_idx}" 10)"
        done
    done

    run_all_jobs
    echo ""
}

# ── Execute ──────────────────────────────────────────────────────────────────
START_ALL=$(date +%s)

case "$TASK" in
    1)   run_task1 ;;
    3)   run_task3 ;;
    all) run_task1; run_task3 ;;
    *) echo "ERROR: unknown task '$TASK' (use 1|3|all)"; exit 1 ;;
esac

END_ALL=$(date +%s)
ELAPSED=$((END_ALL - START_ALL))
ELAPSED_MIN=$((ELAPSED / 60))
echo "╔══════════════════════════════════════════════════════════╗"
echo "║  ALL EXPERIMENTS COMPLETE                                ║"
echo "║  Total time: ${ELAPSED}s (${ELAPSED_MIN} min)            ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""
echo "Results in: $RESULTS_DIR/"
echo "Run the analysis script next:"
echo "  python3 analyze_final_results.py"

# Clean up
rm -rf "$JOB_DIR"
