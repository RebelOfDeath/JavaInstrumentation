# Task 3

All scripts are in `/home/str/JavaInstrumentation/fuzzing_bins/`

## Step 1: Copy C files

Copy the C problem files from the RERS directory into fuzzing_bins:

```bash
for p in 11 12 13 14 15 17; do cp /home/str/RERS/Problem${p}/Problem${p}.c /home/str/JavaInstrumentation/fuzzing_bins/; done
```

## Step 2: Patch C files

Run `replace.sh`: makes the two required changes to each C file (replacing the extern error declaration and adding the EOF scanf check).

```bash
sh replace.sh
```

## Step 3: Compile and create test inputs

Run `compile.sh`: compiles each C file using afl-gcc and creates the test input directories automatically from each problem's valid input symbols.

```bash
sh compile.sh
```

## Step 4: Run AFL

Run `afl.sh`: runs AFL on all 6 problems for 5 minutes each (~30 mins total). Results go into `findings_11/`, `findings_12/` etc.

```bash
sh afl.sh
```

## Step 5: Extract errors

Run `analyze.sh` - runs analyze_afl.py on each findings directory and saves the results to `fuzzing_results/afl/`.

```bash
sh analyze.sh
```

## Step 6: Compare results

Run `compare.sh` - prints the number of unique errors found by each fuzzer per problem.

```bash
bash compare.sh
```

## Results

| Problem | Random | Hill Climber | AFL |
|---------|--------|--------------|-----|
| 11      | 18     | 18           | 15  |
| 12      | 0      | 0            | 10  |
| 13      | 22     | 22           | 19  |
| 14      | 6      | 5            | 11  |
| 15      | 15     | 21           | 2   |
| 17      | 30     | 30           | 13  |
