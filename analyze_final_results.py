#!/usr/bin/env python3
"""
Final Assignment – Analysis Script

Reads experiment results from final_results/ and produces:
  - Summary tables (mean ± std) for branches and errors per technique/problem
  - Convergence plots with confidence bands (mean ± std across runs)
  - Error overlap / Venn analysis
  - LaTeX-ready tables

Usage:
  python3 analyze_final_results.py [--results-dir final_results] [--output-dir final_plots]

Also supports reading the old single-run results from fuzzing_results/ and concolic_results/
if final_results/ doesn't exist yet (for development/testing).
"""

import os
import re
import sys
import argparse
import csv
import json
from collections import defaultdict
from pathlib import Path

import numpy as np

# Try importing matplotlib; if not available, skip plotting
try:
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    from matplotlib.lines import Line2D
    HAS_MPL = True
except ImportError:
    HAS_MPL = False
    print("WARNING: matplotlib not installed. Skipping plots. Install with: pip install matplotlib")

PROBLEMS = [11, 12, 13, 14, 15, 17]
DURATION = 300  # seconds


# ═══════════════════════════════════════════════════════════════════════════════
# Data loading
# ═══════════════════════════════════════════════════════════════════════════════

def load_branch_csv(path):
    """Load a branch convergence CSV → list of (elapsed_seconds, unique_branches)."""
    data = []
    if not os.path.exists(path):
        return data
    with open(path) as f:
        reader = csv.DictReader(f)
        for row in reader:
            t = float(row['elapsed_seconds'])
            b = int(row['unique_branches'])
            data.append((t, b))
    return data


def load_error_csv(path):
    """Load an error convergence CSV → list of (elapsed_seconds, error_code)."""
    data = []
    if not os.path.exists(path):
        return data
    with open(path) as f:
        reader = csv.DictReader(f)
        for row in reader:
            t = float(row['elapsed_seconds'])
            e = row['error_code'].strip()
            data.append((t, e))
    return data


def parse_log_file(path):
    """Parse a fuzzing/concolic log file for summary stats."""
    result = {'branches': 0, 'errors': 0, 'error_codes': set()}
    if not os.path.exists(path):
        return result
    with open(path) as f:
        for line in f:
            m = re.search(r'Total unique branches visited:\s*(\d+)', line)
            if m:
                result['branches'] = int(m.group(1))
            m = re.search(r'Triggered errors \((\d+)\):\s*\[(.+?)\]', line)
            if not m:
                m = re.search(r'Triggered errors \((\d+)\):\s*(.+)', line)
            if m:
                result['errors'] = int(m.group(1))
                codes = re.findall(r'error_\d+', m.group(2))
                result['error_codes'] = set(codes)
    return result


def load_task1_results(results_dir):
    """Load Task 1 results: {technique: {problem: [run_results]}}."""
    techniques = ['random', 'smart', 'concolic']
    data = {t: {p: [] for p in PROBLEMS} for t in techniques}

    for tech in techniques:
        tech_dir = os.path.join(results_dir, 'task1', tech)
        if not os.path.isdir(tech_dir):
            continue

        for run_dir in sorted(Path(tech_dir).glob('run*')):
            for p in PROBLEMS:
                log = parse_log_file(os.path.join(run_dir, f'problem{p}.log'))
                if tech == 'concolic':
                    branch_csv = load_branch_csv(os.path.join(run_dir, f'problem{p}_concolic_branches.csv'))
                    error_csv = load_error_csv(os.path.join(run_dir, f'problem{p}_concolic_errors.csv'))
                else:
                    branch_csv = load_branch_csv(os.path.join(run_dir, f'problem{p}_{tech}_branches.csv'))
                    error_csv = load_error_csv(os.path.join(run_dir, f'problem{p}_{tech}_errors.csv'))

                data[tech][p].append({
                    'branches': log['branches'],
                    'errors': log['errors'],
                    'error_codes': log['error_codes'],
                    'branch_convergence': branch_csv,
                    'error_convergence': error_csv,
                })

    return data


def load_task2_results(results_dir):
    """Load Task 2 AFL results."""
    data = {p: [] for p in PROBLEMS}
    afl_dir = os.path.join(results_dir, 'task2', 'afl')
    if not os.path.isdir(afl_dir):
        return data

    for run_dir in sorted(Path(afl_dir).glob('run*')):
        for p in PROBLEMS:
            log = parse_log_file(os.path.join(run_dir, f'problem{p}.log'))
            error_csv = load_error_csv(os.path.join(run_dir, f'problem{p}_afl_errors.csv'))
            data[p].append({
                'errors': log['errors'],
                'error_codes': log['error_codes'],
                'error_convergence': error_csv,
            })

    return data


def load_task3_results(results_dir):
    """Load Task 3 results: {variant: {problem: [run_results]}}."""
    variants = ['smart', 'improved']
    data = {v: {p: [] for p in PROBLEMS} for v in variants}

    for var in variants:
        var_dir = os.path.join(results_dir, 'task3', var)
        if not os.path.isdir(var_dir):
            continue

        for run_dir in sorted(Path(var_dir).glob('run*')):
            for p in PROBLEMS:
                mode_name = var if var != 'smart' else 'smart'
                log = parse_log_file(os.path.join(run_dir, f'problem{p}.log'))
                branch_csv = load_branch_csv(os.path.join(run_dir, f'problem{p}_{mode_name}_branches.csv'))
                error_csv = load_error_csv(os.path.join(run_dir, f'problem{p}_{mode_name}_errors.csv'))

                data[var][p].append({
                    'branches': log['branches'],
                    'errors': log['errors'],
                    'error_codes': log['error_codes'],
                    'branch_convergence': branch_csv,
                    'error_convergence': error_csv,
                })

    return data


# Also support loading old single-run results for development
def load_old_results(repo_dir):
    """Load old single-run results from fuzzing_results/ and concolic_results/."""
    data = {'random': {}, 'smart': {}, 'concolic': {}}

    for tech in ['random', 'smart']:
        tech_dir = os.path.join(repo_dir, 'fuzzing_results', tech)
        if not os.path.isdir(tech_dir):
            continue
        for p in PROBLEMS:
            log = parse_log_file(os.path.join(tech_dir, f'problem{p}.log'))
            branch_csv = load_branch_csv(os.path.join(tech_dir, f'problem{p}_{tech}_branches.csv'))
            error_csv = load_error_csv(os.path.join(tech_dir, f'problem{p}_{tech}_errors.csv'))
            data[tech][p] = [{
                'branches': log['branches'],
                'errors': log['errors'],
                'error_codes': log['error_codes'],
                'branch_convergence': branch_csv,
                'error_convergence': error_csv,
            }]

    conc_dir = os.path.join(repo_dir, 'concolic_results')
    if os.path.isdir(conc_dir):
        for p in PROBLEMS:
            log = parse_log_file(os.path.join(conc_dir, f'problem{p}.log'))
            branch_csv = load_branch_csv(os.path.join(conc_dir, f'problem{p}_concolic_branches.csv'))
            error_csv = load_error_csv(os.path.join(conc_dir, f'problem{p}_concolic_errors.csv'))
            data['concolic'][p] = [{
                'branches': log['branches'],
                'errors': log['errors'],
                'error_codes': log['error_codes'],
                'branch_convergence': branch_csv,
                'error_convergence': error_csv,
            }]

    return data


# ═══════════════════════════════════════════════════════════════════════════════
# Analysis functions
# ═══════════════════════════════════════════════════════════════════════════════

def compute_stats(runs, key):
    """Compute mean and std for a numeric key across runs."""
    values = [r.get(key) for r in runs if r.get(key) is not None]
    if not values:
        return 0.0, 0.0
    return np.mean(values), np.std(values, ddof=1) if len(values) > 1 else 0.0


def interpolate_convergence(runs, key='branch_convergence', value_idx=1, max_time=300, step=5):
    """
    Interpolate convergence curves to a common time grid.
    Returns (time_grid, mean_values, std_values).
    """
    time_grid = np.arange(0, max_time + step, step)
    all_curves = []

    for run in runs:
        conv = run.get(key, [])
        if not conv:
            continue

        # Build curve from convergence data
        times = [0.0] + [c[0] for c in conv]
        if key == 'branch_convergence':
            values = [0] + [c[value_idx] for c in conv]
        else:
            # For error convergence, count cumulative unique errors over time
            times = [0.0]
            values = [0]
            seen = set()
            for t, code in conv:
                if code not in seen:
                    seen.add(code)
                times.append(t)
                values.append(len(seen))

        # Interpolate to grid (step function: use last known value)
        interp = np.zeros(len(time_grid))
        for i, t in enumerate(time_grid):
            # Find the last measurement at or before time t
            val = 0
            for j in range(len(times)):
                if times[j] <= t:
                    val = values[j]
                else:
                    break
            interp[i] = val

        all_curves.append(interp)

    if not all_curves:
        return time_grid, np.zeros(len(time_grid)), np.zeros(len(time_grid))

    all_curves = np.array(all_curves)
    mean = np.mean(all_curves, axis=0)
    std = np.std(all_curves, axis=0, ddof=1) if all_curves.shape[0] > 1 else np.zeros(len(time_grid))
    return time_grid, mean, std


def collect_all_errors(runs):
    """Collect the union of all error codes across runs."""
    all_errors = set()
    for r in runs:
        all_errors.update(r.get('error_codes', set()))
    return all_errors


# ═══════════════════════════════════════════════════════════════════════════════
# Output: Tables
# ═══════════════════════════════════════════════════════════════════════════════

def print_summary_table(data, title, techniques=None):
    """Print a summary table with mean ± std for branches and errors."""
    if techniques is None:
        techniques = list(data.keys())

    print(f"\n{'=' * 80}")
    print(f"  {title}")
    print(f"{'=' * 80}")

    # Branch coverage table
    print(f"\n  Unique Branches (mean ± std)")
    header = f"  {'Problem':<12}" + "".join(f"  {t:>20}" for t in techniques)
    print(header)
    print("  " + "-" * (12 + 22 * len(techniques)))

    for p in PROBLEMS:
        row = f"  Problem {p:<4}"
        for t in techniques:
            runs = data[t].get(p, [])
            if runs:
                m, s = compute_stats(runs, 'branches')
                row += f"  {m:>8.1f} ± {s:>6.1f}   "
            else:
                row += f"  {'N/A':>20}"
        print(row)

    # Error count table
    print(f"\n  Unique Errors (mean ± std)")
    header = f"  {'Problem':<12}" + "".join(f"  {t:>20}" for t in techniques)
    print(header)
    print("  " + "-" * (12 + 22 * len(techniques)))

    for p in PROBLEMS:
        row = f"  Problem {p:<4}"
        for t in techniques:
            runs = data[t].get(p, [])
            if runs:
                m, s = compute_stats(runs, 'errors')
                row += f"  {m:>8.1f} ± {s:>6.1f}   "
            else:
                row += f"  {'N/A':>20}"
        print(row)

    # Totals
    print(f"\n  Totals (sum of means across problems)")
    for metric, key in [("Branches", "branches"), ("Errors", "errors")]:
        row = f"  {metric + ' total':<12}"
        for t in techniques:
            total = 0
            for p in PROBLEMS:
                runs = data[t].get(p, [])
                if runs:
                    m, _ = compute_stats(runs, key)
                    total += m
            row += f"  {total:>20.1f}"
        print(row)


def generate_latex_table(data, techniques, caption, label):
    """Generate a LaTeX table for branches or errors."""
    n_tech = len(techniques)
    cols = "l" + "r" * n_tech
    lines = []
    lines.append(f"\\begin{{table}}[h]")
    lines.append(f"    \\centering")
    lines.append(f"    \\caption{{{caption}}}")
    lines.append(f"    \\label{{{label}}}")
    lines.append(f"    \\begin{{tabular}}{{{cols}}}")
    lines.append(f"        \\toprule")
    header = "        Problem & " + " & ".join(
        f"\\textbf{{{t.capitalize()}}}" for t in techniques
    ) + " \\\\"
    lines.append(header)
    lines.append(f"        \\midrule")

    for p in PROBLEMS:
        cells = [f"Problem {p}"]
        for t in techniques:
            runs = data[t].get(p, [])
            if runs:
                m, s = compute_stats(runs, 'branches')
                cells.append(f"${m:.1f} \\pm {s:.1f}$")
            else:
                cells.append("N/A")
        lines.append("        " + " & ".join(cells) + " \\\\")

    lines.append(f"        \\bottomrule")
    lines.append(f"    \\end{{tabular}}")
    lines.append(f"\\end{{table}}")
    return "\n".join(lines)


def generate_latex_error_table(data, techniques, caption, label):
    """Generate a LaTeX table for error counts."""
    n_tech = len(techniques)
    cols = "l" + "r" * n_tech
    lines = []
    lines.append(f"\\begin{{table}}[h]")
    lines.append(f"    \\centering")
    lines.append(f"    \\caption{{{caption}}}")
    lines.append(f"    \\label{{{label}}}")
    lines.append(f"    \\begin{{tabular}}{{{cols}}}")
    lines.append(f"        \\toprule")
    header = "        Problem & " + " & ".join(
        f"\\textbf{{{t.capitalize()}}}" for t in techniques
    ) + " \\\\"
    lines.append(header)
    lines.append(f"        \\midrule")

    for p in PROBLEMS:
        cells = [f"Problem {p}"]
        for t in techniques:
            runs = data[t].get(p, [])
            if runs:
                m, s = compute_stats(runs, 'errors')
                cells.append(f"${m:.1f} \\pm {s:.1f}$")
            else:
                cells.append("N/A")
        lines.append("        " + " & ".join(cells) + " \\\\")

    lines.append(f"        \\bottomrule")
    lines.append(f"    \\end{{tabular}}")
    lines.append(f"\\end{{table}}")
    return "\n".join(lines)


# ═══════════════════════════════════════════════════════════════════════════════
# Output: Plots
# ═══════════════════════════════════════════════════════════════════════════════

COLORS = {
    'random': '#2196F3',
    'smart': '#FF9800',
    'concolic': '#4CAF50',
    'improved': '#E91E63',
    'afl': '#9C27B0',
}

LABELS = {
    'random': 'Random Fuzzing',
    'smart': 'Hill Climbing',
    'concolic': 'Concolic Execution',
    'improved': 'Improved HC',
    'afl': 'AFL',
}


def plot_convergence(data, techniques, problem, metric, output_path, title_suffix=""):
    """Plot convergence for a single problem with confidence bands."""
    if not HAS_MPL:
        return

    fig, ax = plt.subplots(figsize=(8, 5))

    for tech in techniques:
        runs = data[tech].get(problem, [])
        if not runs:
            continue

        if metric == 'branches':
            time_grid, mean, std = interpolate_convergence(runs, 'branch_convergence')
        else:
            time_grid, mean, std = interpolate_convergence(runs, 'error_convergence')

        color = COLORS.get(tech, '#666666')
        label = LABELS.get(tech, tech)
        ax.plot(time_grid, mean, color=color, label=label, linewidth=2)
        ax.fill_between(time_grid, mean - std, mean + std, color=color, alpha=0.15)

    metric_label = "Unique Branches" if metric == 'branches' else "Unique Errors"
    ax.set_xlabel("Time (seconds)", fontsize=12)
    ax.set_ylabel(metric_label, fontsize=12)
    ax.set_title(f"Problem {problem} – {metric_label} Convergence{title_suffix}", fontsize=13)
    ax.legend(fontsize=10)
    ax.grid(True, alpha=0.3)
    ax.set_xlim(0, DURATION)

    plt.tight_layout()
    plt.savefig(output_path, dpi=150, bbox_inches='tight')
    plt.close()


def plot_aggregate_convergence(data, techniques, metric, output_path, title_suffix=""):
    """Plot aggregated convergence across all problems."""
    if not HAS_MPL:
        return

    fig, axes = plt.subplots(2, 3, figsize=(18, 10))
    axes = axes.flatten()

    for idx, p in enumerate(PROBLEMS):
        ax = axes[idx]
        for tech in techniques:
            runs = data[tech].get(p, [])
            if not runs:
                continue

            if metric == 'branches':
                time_grid, mean, std = interpolate_convergence(runs, 'branch_convergence')
            else:
                time_grid, mean, std = interpolate_convergence(runs, 'error_convergence')

            color = COLORS.get(tech, '#666666')
            label = LABELS.get(tech, tech)
            ax.plot(time_grid, mean, color=color, label=label, linewidth=1.5)
            ax.fill_between(time_grid, mean - std, mean + std, color=color, alpha=0.12)

        metric_label = "Branches" if metric == 'branches' else "Errors"
        ax.set_title(f"Problem {p}", fontsize=11)
        ax.set_xlabel("Time (s)", fontsize=9)
        ax.set_ylabel(f"Unique {metric_label}", fontsize=9)
        ax.grid(True, alpha=0.3)
        ax.set_xlim(0, DURATION)

    # Shared legend
    handles = [Line2D([0], [0], color=COLORS.get(t, '#666'), linewidth=2, label=LABELS.get(t, t))
               for t in techniques if any(data[t].get(p, []) for p in PROBLEMS)]
    fig.legend(handles=handles, loc='upper center', ncol=len(techniques), fontsize=11,
               bbox_to_anchor=(0.5, 1.02))

    metric_full = "Branch Coverage" if metric == 'branches' else "Error Discovery"
    fig.suptitle(f"{metric_full} Convergence{title_suffix}", fontsize=14, y=1.05)
    plt.tight_layout()
    plt.savefig(output_path, dpi=150, bbox_inches='tight')
    plt.close()


def plot_error_overlap(data, techniques, output_path):
    """Plot error overlap heatmap across techniques for each problem."""
    if not HAS_MPL:
        return

    fig, axes = plt.subplots(2, 3, figsize=(18, 10))
    axes = axes.flatten()

    for idx, p in enumerate(PROBLEMS):
        ax = axes[idx]
        tech_errors = {}
        for tech in techniques:
            runs = data[tech].get(p, [])
            all_err = set()
            for r in runs:
                all_err.update(r.get('error_codes', set()))
            tech_errors[tech] = all_err

        # Build overlap matrix
        n = len(techniques)
        overlap = np.zeros((n, n))
        for i, t1 in enumerate(techniques):
            for j, t2 in enumerate(techniques):
                if i == j:
                    overlap[i][j] = len(tech_errors[t1])
                else:
                    overlap[i][j] = len(tech_errors[t1] & tech_errors[t2])

        im = ax.imshow(overlap, cmap='YlOrRd', aspect='auto')
        ax.set_xticks(range(n))
        ax.set_yticks(range(n))
        labels = [LABELS.get(t, t)[:8] for t in techniques]
        ax.set_xticklabels(labels, fontsize=8, rotation=45)
        ax.set_yticklabels(labels, fontsize=8)
        ax.set_title(f"Problem {p}", fontsize=11)

        # Annotate cells
        for i in range(n):
            for j in range(n):
                ax.text(j, i, f"{int(overlap[i][j])}", ha='center', va='center', fontsize=9,
                        color='white' if overlap[i][j] > overlap.max() * 0.6 else 'black')

    fig.suptitle("Error Overlap Between Techniques", fontsize=14)
    plt.tight_layout()
    plt.savefig(output_path, dpi=150, bbox_inches='tight')
    plt.close()


def plot_bar_comparison(data, techniques, metric, output_path, title=""):
    """Bar chart comparing techniques across problems."""
    if not HAS_MPL:
        return

    fig, ax = plt.subplots(figsize=(12, 6))

    x = np.arange(len(PROBLEMS))
    width = 0.8 / len(techniques)

    for i, tech in enumerate(techniques):
        means = []
        stds = []
        for p in PROBLEMS:
            runs = data[tech].get(p, [])
            if runs:
                m, s = compute_stats(runs, metric)
                means.append(m)
                stds.append(s)
            else:
                means.append(0)
                stds.append(0)

        offset = (i - len(techniques) / 2 + 0.5) * width
        color = COLORS.get(tech, '#666666')
        label = LABELS.get(tech, tech)
        ax.bar(x + offset, means, width, yerr=stds, label=label, color=color, alpha=0.8,
               capsize=3, error_kw={'linewidth': 1})

    metric_label = "Unique Branches" if metric == 'branches' else "Unique Errors"
    ax.set_xlabel("Problem", fontsize=12)
    ax.set_ylabel(f"{metric_label} (mean ± std)", fontsize=12)
    ax.set_title(title or f"{metric_label} by Technique", fontsize=13)
    ax.set_xticks(x)
    ax.set_xticklabels([f"P{p}" for p in PROBLEMS])
    ax.legend(fontsize=10)
    ax.grid(True, alpha=0.3, axis='y')

    plt.tight_layout()
    plt.savefig(output_path, dpi=150, bbox_inches='tight')
    plt.close()


# ═══════════════════════════════════════════════════════════════════════════════
# Error complement analysis
# ═══════════════════════════════════════════════════════════════════════════════

def print_error_complement_analysis(data, techniques):
    """Analyze which errors are unique to each technique."""
    print(f"\n{'=' * 80}")
    print(f"  Error Complement Analysis")
    print(f"{'=' * 80}")

    for p in PROBLEMS:
        tech_errors = {}
        for tech in techniques:
            runs = data[tech].get(p, [])
            all_err = set()
            for r in runs:
                all_err.update(r.get('error_codes', set()))
            tech_errors[tech] = all_err

        all_errors = set()
        for e in tech_errors.values():
            all_errors.update(e)

        if not all_errors:
            continue

        print(f"\n  Problem {p} ({len(all_errors)} unique errors total):")
        for tech in techniques:
            unique_to_tech = tech_errors[tech] - set().union(*(
                tech_errors[t] for t in techniques if t != tech
            ))
            if unique_to_tech:
                print(f"    Only {LABELS.get(tech, tech)}: {sorted(unique_to_tech)}")

        # Common to all
        common = set.intersection(*(tech_errors[t] for t in techniques if tech_errors[t]))
        if common:
            print(f"    Common to all: {len(common)} errors")


# ═══════════════════════════════════════════════════════════════════════════════
# LaTeX output
# ═══════════════════════════════════════════════════════════════════════════════

def write_latex_tables(data, techniques, output_dir, prefix="task1"):
    """Write all LaTeX tables to files."""
    os.makedirs(output_dir, exist_ok=True)

    # Branch table
    latex = generate_latex_table(
        data, techniques,
        f"Unique branches visited (mean $\\pm$ std, $n=5$).",
        f"tab:{prefix}-branches"
    )
    with open(os.path.join(output_dir, f'{prefix}_branches_table.tex'), 'w') as f:
        f.write(latex)

    # Error table
    latex = generate_latex_error_table(
        data, techniques,
        f"Unique errors triggered (mean $\\pm$ std, $n=5$).",
        f"tab:{prefix}-errors"
    )
    with open(os.path.join(output_dir, f'{prefix}_errors_table.tex'), 'w') as f:
        f.write(latex)

    print(f"  LaTeX tables written to {output_dir}/{prefix}_*.tex")


# ═══════════════════════════════════════════════════════════════════════════════
# Main
# ═══════════════════════════════════════════════════════════════════════════════

def main():
    parser = argparse.ArgumentParser(description="Analyze final assignment results")
    parser.add_argument('--results-dir', default='final_results', help='Results directory')
    parser.add_argument('--output-dir', default='final_plots', help='Output directory for plots')
    parser.add_argument('--old-results', action='store_true',
                        help='Also load old single-run results from fuzzing_results/ and concolic_results/')
    args = parser.parse_args()

    output_dir = args.output_dir
    os.makedirs(output_dir, exist_ok=True)

    # ── Task 1 ────────────────────────────────────────────────────────────────
    print("\n" + "=" * 80)
    print("  TASK 1: Empirical Comparison")
    print("=" * 80)

    task1_data = load_task1_results(args.results_dir)

    # Check if we have data
    has_task1 = any(
        task1_data[t].get(p, []) for t in ['random', 'smart', 'concolic'] for p in PROBLEMS
    )

    if not has_task1 and args.old_results:
        print("  No Task 1 multi-run results found. Loading old single-run results...")
        task1_data = load_old_results('.')
        has_task1 = any(
            task1_data[t].get(p, []) for t in ['random', 'smart', 'concolic'] for p in PROBLEMS
        )

    if has_task1:
        techniques_1 = ['random', 'smart', 'concolic']
        print_summary_table(task1_data, "Task 1: Technique Comparison", techniques_1)
        print_error_complement_analysis(task1_data, techniques_1)
        write_latex_tables(task1_data, techniques_1, output_dir, "task1")

        if HAS_MPL:
            plot_aggregate_convergence(task1_data, techniques_1, 'branches',
                                       os.path.join(output_dir, 'task1_branch_convergence.png'))
            plot_aggregate_convergence(task1_data, techniques_1, 'errors',
                                       os.path.join(output_dir, 'task1_error_convergence.png'))
            plot_bar_comparison(task1_data, techniques_1, 'branches',
                                os.path.join(output_dir, 'task1_branches_bar.png'),
                                "Task 1: Branch Coverage Comparison")
            plot_bar_comparison(task1_data, techniques_1, 'errors',
                                os.path.join(output_dir, 'task1_errors_bar.png'),
                                "Task 1: Error Discovery Comparison")
            plot_error_overlap(task1_data, techniques_1,
                               os.path.join(output_dir, 'task1_error_overlap.png'))

            # Individual problem convergence plots
            for p in PROBLEMS:
                plot_convergence(task1_data, techniques_1, p, 'branches',
                                 os.path.join(output_dir, f'task1_p{p}_branches.png'))
                plot_convergence(task1_data, techniques_1, p, 'errors',
                                 os.path.join(output_dir, f'task1_p{p}_errors.png'))

            print(f"  Plots written to {output_dir}/task1_*.png")
    else:
        print("  No Task 1 results found. Run experiments first.")

    # ── Task 2: AFL ───────────────────────────────────────────────────────────
    print("\n" + "=" * 80)
    print("  TASK 2: AFL Comparison")
    print("=" * 80)

    task2_data = load_task2_results(args.results_dir)
    has_task2 = any(task2_data[p] for p in PROBLEMS)

    if has_task2 and has_task1:
        # Merge AFL into task1 data for combined comparison
        combined = dict(task1_data)
        combined['afl'] = {p: task2_data[p] for p in PROBLEMS}
        techniques_2 = ['random', 'smart', 'concolic', 'afl']
        print_summary_table(combined, "Task 2: Including AFL", techniques_2)
        print_error_complement_analysis(combined, techniques_2)
        write_latex_tables(combined, techniques_2, output_dir, "task2")

        if HAS_MPL:
            plot_aggregate_convergence(combined, techniques_2, 'errors',
                                       os.path.join(output_dir, 'task2_error_convergence.png'),
                                       " (incl. AFL)")
            plot_bar_comparison(combined, techniques_2, 'errors',
                                os.path.join(output_dir, 'task2_errors_bar.png'),
                                "Task 2: Error Discovery incl. AFL")
            print(f"  Plots written to {output_dir}/task2_*.png")
    elif has_task2:
        print("  AFL results found but Task 1 data missing for comparison.")
    else:
        print("  No AFL results found. Run AFL experiments first.")

    # ── Task 3: Improvement Study ─────────────────────────────────────────────
    print("\n" + "=" * 80)
    print("  TASK 3: Improvement Study")
    print("=" * 80)

    task3_data = load_task3_results(args.results_dir)
    has_task3 = any(
        task3_data[v].get(p, []) for v in ['smart', 'improved'] for p in PROBLEMS
    )

    if has_task3:
        techniques_3 = ['smart', 'improved']
        print_summary_table(task3_data, "Task 3: Base vs Improved Hill Climber", techniques_3)
        print_error_complement_analysis(task3_data, techniques_3)
        write_latex_tables(task3_data, techniques_3, output_dir, "task3")

        if HAS_MPL:
            plot_aggregate_convergence(task3_data, techniques_3, 'branches',
                                       os.path.join(output_dir, 'task3_branch_convergence.png'),
                                       " (Base vs Improved)")
            plot_aggregate_convergence(task3_data, techniques_3, 'errors',
                                       os.path.join(output_dir, 'task3_error_convergence.png'),
                                       " (Base vs Improved)")
            plot_bar_comparison(task3_data, techniques_3, 'branches',
                                os.path.join(output_dir, 'task3_branches_bar.png'),
                                "Task 3: Branch Coverage – Base vs Improved")
            plot_bar_comparison(task3_data, techniques_3, 'errors',
                                os.path.join(output_dir, 'task3_errors_bar.png'),
                                "Task 3: Error Discovery – Base vs Improved")

            # Per-problem convergence
            for p in PROBLEMS:
                plot_convergence(task3_data, techniques_3, p, 'branches',
                                 os.path.join(output_dir, f'task3_p{p}_branches.png'),
                                 " (Base vs Improved)")
                plot_convergence(task3_data, techniques_3, p, 'errors',
                                 os.path.join(output_dir, f'task3_p{p}_errors.png'),
                                 " (Base vs Improved)")

            print(f"  Plots written to {output_dir}/task3_*.png")
    else:
        print("  No Task 3 results found. Run experiments first.")

    print("\n" + "=" * 80)
    print("  DONE. All analysis complete.")
    print("=" * 80)


if __name__ == '__main__':
    main()
