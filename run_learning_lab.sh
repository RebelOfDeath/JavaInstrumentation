#!/bin/bash
# Lab 4 – Model Learning Script
#
# Instruments and runs L* learning on RERS LTL problems and ProblemPin.
#
# Usage:
#   ./run_learning_lab.sh [--problems "1 2 4 7 Pin"] [--wdepth 3]
#
# Defaults: problems="1 2 4 7 Pin", wdepth=3 (ProblemPin uses wdepth=4)

REPO_DIR="$(cd "$(dirname "$0")" && pwd)"
RESULTS_DIR="$REPO_DIR/learning_results"
DATASET="../RERS"
CUSTOM_DATASET="$REPO_DIR/custom_problems"

# ── Parse arguments ───────────────────────────────────────────────────────────
PROBLEMS="1 2 4 7 Pin"
WDEPTH=3

while [[ $# -gt 0 ]]; do
    case "$1" in
        --problems) PROBLEMS="$2"; shift 2 ;;
        --wdepth)   WDEPTH="$2";  shift 2 ;;
        *) echo "Unknown argument: $1"; exit 1 ;;
    esac
done

cd "$REPO_DIR"

# ── 1. Build ──────────────────────────────────────────────────────────────────
echo "╔══════════════════════════════════════════════════════════╗"
echo "║           Lab 4 – Model Learning (L*)                   ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo "  Problems  : $PROBLEMS"
echo "  W-depth   : $WDEPTH"
echo ""
echo "[1/3] Building project..."
mvn clean package -q || { echo "ERROR: Maven build failed"; exit 1; }
echo "      Done."
echo ""

# ── 2. Instrument and compile ─────────────────────────────────────────────────
echo "[2/3] Instrumenting and compiling problems..."
mkdir -p instrumented

for N in $PROBLEMS; do
    echo "      Problem$N..."
    FILE="$CUSTOM_DATASET/Problem$N.java"
    if [ ! -f "$FILE" ]; then
        FILE="$DATASET/Problem$N/Problem$N.java"
    fi
    if [ ! -f "$FILE" ]; then
        echo "ERROR: Cannot find Problem$N.java"
        exit 1
    fi
    java -XX:+UseG1GC -Xmx4g -cp target/aistr.jar nl.tudelft.instrumentation.Main --type=learning --file="$FILE" > "instrumented/Problem$N.java" && \
    javac -cp target/aistr.jar:lib/com.microsoft.z3.jar:. Errors.java "instrumented/Problem$N.java" \
        || { echo "ERROR: Failed to instrument/compile Problem$N"; exit 1; }
done
echo "      Done."
echo ""

# ── 3. Run learning ──────────────────────────────────────────────────────────
echo "[3/3] Running L* learning algorithm..."
echo ""
mkdir -p "$RESULTS_DIR"

for N in $PROBLEMS; do
    # ProblemPin uses wdepth=4 per the assignment spec
    DEPTH=$WDEPTH
    if [ "$N" = "Pin" ]; then
        DEPTH=4
    fi

    echo "══════════════════════════════════════════════════════════"
    echo "  Problem$N (w-depth=$DEPTH)"
    echo "══════════════════════════════════════════════════════════"

    LOG="$RESULTS_DIR/problem${N}.log"
    DOT="$RESULTS_DIR/hypothesis_problem${N}.dot"

    java -Xmx4g \
        -Dlearning.wdepth="$DEPTH" \
        -Dlearning.dotfile="$DOT" \
        -cp target/aistr.jar:./instrumented:. "Problem$N" \
        2>&1 | tee "$LOG"

    # Try to render PDF if graphviz is available
    if [ -f "$DOT" ]; then
        echo "  Hypothesis saved to $DOT"
        if command -v dot &> /dev/null; then
            dot -Tpdf -o "$RESULTS_DIR/hypothesis_problem${N}.pdf" "$DOT"
            echo "  PDF rendered: $RESULTS_DIR/hypothesis_problem${N}.pdf"
        fi
    fi
    echo ""
done

# ── 4. Summary ────────────────────────────────────────────────────────────────
echo "╔══════════════════════════════════════════════════════════╗"
echo "║                    RESULTS SUMMARY                      ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""
printf "  %-12s  %-15s  %-15s\n" "Problem" "States" "Time (ms)"
printf "  %-12s  %-15s  %-15s\n" "--------" "------" "---------"
for N in $PROBLEMS; do
    LOG="$RESULTS_DIR/problem${N}.log"
    STATES="N/A"
    TIME="N/A"
    if [ -f "$LOG" ]; then
        STATES=$(grep "Final model has" "$LOG" | sed 's/.*has \([0-9]*\) states/\1/')
        TIME=$(grep "Total time:" "$LOG" | sed 's/.*: \([0-9]*\) ms/\1/')
        STATES="${STATES:-ERR}"; TIME="${TIME:-ERR}"
    fi
    printf "  %-12s  %-15s  %-15s\n" "Problem$N" "$STATES" "$TIME"
done
echo ""
echo "DOT files and logs saved to: $RESULTS_DIR/"
