import os
import re

PROBLEMS = [11, 12, 13, 14, 15, 17]
BINS_DIR = "fuzzing_bins"

def count_defined_errors():
    for p in PROBLEMS:
        file_path = os.path.join(BINS_DIR, f"Problem{p}.c")
        if not os.path.exists(file_path):
            print(f"Problem{p}.c not found.")
            continue
        
        with open(file_path) as f:
            content = f.read()
            errors = re.findall(r'__VERIFIER_error\s*\(\s*(\d+)\s*\)', content)
            unique_errors = sorted(list(set(map(int, errors))))
            print(f"Problem {p}: {len(unique_errors)} defined errors (IDs: {unique_errors[:5]}...)")

if __name__ == "__main__":
    count_defined_errors()
