#!/usr/bin/env python3
"""Reject recognizable credentials in Git-tracked files without printing them."""

import re
import subprocess
from pathlib import Path


PATTERNS = {
    "Google OAuth client secret": rb"GOCSPX-[A-Za-z0-9_-]{20,}",
    "Google API key": rb"AIza[0-9A-Za-z_-]{35}",
    "private key": rb"-----BEGIN (?:[A-Z0-9]+ )?PRIVATE KEY-----",
    "GitHub token": rb"(?:gh[pousr]_[A-Za-z0-9]{30,}|github_pat_[A-Za-z0-9_]{30,})",
    "AWS access key": rb"(?:AKIA|ASIA)[A-Z0-9]{16}",
}


def main():
    root = Path(subprocess.check_output(["git", "rev-parse", "--show-toplevel"], text=True).strip())
    paths = subprocess.check_output(["git", "ls-files", "-z"], cwd=root).decode().split("\0")
    findings = []
    for name in filter(None, paths):
        path = root / name
        if not path.is_file():
            continue
        contents = path.read_bytes()
        for kind, pattern in PATTERNS.items():
            for match in re.finditer(pattern, contents):
                line = contents[:match.start()].count(b"\n") + 1
                findings.append(f"{name}:{line}: {kind}")
        if path.suffix in (".yml", ".yaml", ".properties"):
            for line, value in enumerate(contents.decode(errors="replace").splitlines(), 1):
                match = re.match(r"\s*client[-_]secret\s*[:=]\s*(.+)", value)
                if match and not match.group(1).strip("\"'").startswith("${"):
                    findings.append(f"{name}:{line}: hardcoded OAuth client secret")
    for finding in findings:
        print(finding)
    if findings:
        print("Secret check failed. Remove credentials and rotate any exposed secrets.")
        return 1
    print("No recognized credentials found in the current tracked files (history not scanned).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
