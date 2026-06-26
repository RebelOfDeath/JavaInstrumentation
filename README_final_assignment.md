# Final Assignment – Group 20

**Authors:** Roham Koohestani (5909465) & Tom Clark (5275040)
**Course:** CS4580 – Automated Software Testing and Reverse Engineering

## Prerequisites

- **Java 8** (JDK)
- **Maven 3.5.4+**
- **Z3 SMT Solver** (Java bindings at `lib/com.microsoft.z3.jar`)
- **Python 3.8+** with `numpy` and `matplotlib` (`pip install numpy matplotlib`)
- **AFL 2.52b** (for Task 2, pre-installed on the STR server)

## Project Structure

```
src/main/java/nl/tudelft/instrumentation/
  fuzzing/FuzzingLab.java          # Random, Hill Climbing, Improved HC
  concolic/ConcolicExecutionLab.java   # Concolic Execution
instrumented/Problem{11-17}.java   # Instrumented RERS problems
fuzzing_bins/                      # Compiled C binaries for AFL
```

## Running All Experiments

### Build

```bash
mvn clean package -q
```

### Task 1: Empirical Comparison (Random, Hill Climbing, Concolic)

Runs 5 independent runs per technique per problem (300s each):

```bash
./run_final_experiments.sh --duration 300 --runs 5 --task 1
```

Results are stored in `final_results/task1/{random,smart,concolic}/run{1-5}/`.

### Task 2: AFL

Must be run on the STR server where AFL is installed:

```bash
./run_afl_experiments.sh --duration 300 --runs 5
```

Results are stored in `final_results/task2/afl/run{1-5}/`.

### Task 3: Improvement Study (Base vs Improved Hill Climber)

```bash
./run_final_experiments.sh --duration 300 --runs 5 --task 3
```

Results are stored in `final_results/task3/{smart,improved}/run{1-5}/`.

### Run All Tasks at Once

```bash
./run_final_experiments.sh --duration 300 --runs 5 --task all
```

## Analyzing Results

After experiments complete, generate tables and plots:

```bash
python3 analyze_final_results.py --results-dir final_results --output-dir final_plots
```

This produces:
- LaTeX tables in `final_plots/task{1,2,3}_*_table.tex`
- Convergence plots in `final_plots/task{1,2,3}_*.png`
- Bar charts and error overlap heatmaps
- Console output with mean +/- std tables

To analyze old single-run results (from lab reports):

```bash
python3 analyze_final_results.py --old-results
```

## Technique Details

### Random Fuzzing (`-Dfuzzing.mode=random`)
Generates traces of length 10 by uniform random sampling. No feedback.

### Hill Climbing (`-Dfuzzing.mode=smart`)
Branch-distance-guided local search. 5 mutations per step. Restarts from random trace on local minimum.

### Concolic Execution
Z3-based symbolic execution. Negates branch conditions to discover new paths. Falls back to random when queue is empty.

### Improved Hill Climber (`-Dfuzzing.mode=improved`)
Addresses limitations of the base hill climber:
1. **Seed pool** (K=20): Restarts from best-seen traces instead of random
2. **Adaptive trace length**: 5-20 initial, up to 30 via mutations
3. **Hybrid exploration**: 80% hill climbing + 20% random + splice mutations

### AFL
Coverage-guided greybox fuzzing on compiled C binaries. Uses edge coverage feedback.

## Seed Configuration

All techniques support fixed seeds for reproducibility:
- Fuzzing: `-Dfuzzing.seed=42`
- Concolic: `-Dconcolic.seed=42`

The experiment scripts use seeds: 42, 137, 256, 1024, 7777.