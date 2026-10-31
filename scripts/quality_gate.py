#!/usr/bin/env python3
"""
Quality Gate - Skilline DevSecOps Pipeline

Doc bao cao JSON cua Semgrep, phan loai tung phat hien thanh 4 muc
Critical/High/Medium/Low, doi chieu voi nguong trong threshold.yaml
va tra ve exit code de GitHub Actions quyet dinh PASS/FAIL.

    python scripts/quality_gate.py semgrep-report.json --config threshold.yaml

Exit code: 0 = PASS, 1 = BLOCK (vuot nguong), 2 = loi dau vao/cau hinh.
"""

import argparse
import json
import os
import sys
from datetime import datetime, timezone

try:
    import yaml
except ImportError:  # khong de loi thieu thu vien bi hieu nham la BLOCK
    print("[QUALITY GATE] Thieu thu vien PyYAML: pip install pyyaml")
    sys.exit(2)

LEVELS = ["Critical", "High", "Medium", "Low"]
SEVERITY_TO_LEVEL = {"ERROR": "High", "WARNING": "Medium", "INFO": "Low"}


def fail_input(message: str):
    print(f"[QUALITY GATE] {message}")
    sys.exit(2)


def load_json(path: str) -> dict:
    if not os.path.exists(path):
        fail_input(f"Khong tim thay bao cao Semgrep: {path}")
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def load_policy(path: str) -> dict:
    if not os.path.exists(path):
        fail_input(f"Khong tim thay file nguong: {path}")
    with open(path, encoding="utf-8") as f:
        policy = yaml.safe_load(f) or {}
    if "critical_rule_pattern" not in policy or "levels" not in policy:
        fail_input("threshold.yaml thieu 'critical_rule_pattern' hoac 'levels'")
    return policy


def classify(finding: dict, critical_pattern: str) -> str:
    """Anh xa 1 phat hien Semgrep sang muc Quality Gate (Bang 3.1)."""
    rule_id = finding.get("check_id", "").split(".")[-1]
    if critical_pattern in rule_id:
        return "Critical"
    severity = finding.get("extra", {}).get("severity", "INFO").upper()
    return SEVERITY_TO_LEVEL.get(severity, "Low")


def evaluate(counts: dict, levels: dict) -> list:
    """Tra ve danh sach (muc, so luong, nguong, trang thai)."""
    rows = []
    for level in LEVELS:
        rule = levels.get(level) or {}
        actual = counts[level]
        if "block_at" in rule:
            limit = f">= {rule['block_at']} chan"
            status = "BLOCK" if actual >= rule["block_at"] else "PASS"
        elif "warn_at" in rule:
            limit = f">= {rule['warn_at']} canh bao"
            status = "WARN" if actual >= rule["warn_at"] else "PASS"
        else:
            limit, status = "ghi nhan", "INFO"
        rows.append((level, actual, limit, status))
    return rows


def write_step_summary(rows, findings, decision):
    """Ghi bang ket qua vao trang Summary cua GitHub Actions (neu co)."""
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if not summary_path:
        return
    lines = [f"## Quality Gate: {decision}", "",
             "| Muc | So phat hien | Nguong | Trang thai |", "|---|---|---|---|"]
    lines += [f"| {lv} | {n} | {limit} | {st} |" for lv, n, limit, st in rows]
    blocking = [f for f in findings if f["level"] in ("Critical", "High")]
    if blocking:
        lines += ["", "| Muc | Rule | Vi tri |", "|---|---|---|"]
        lines += [f"| {f['level']} | `{f['rule']}` | `{f['path']}:{f['line']}` |" for f in blocking]
    with open(summary_path, "a", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")


def main():
    parser = argparse.ArgumentParser(description="Semgrep Quality Gate")
    parser.add_argument("report", nargs="?", default="semgrep-report.json")
    parser.add_argument("--config", default="threshold.yaml")
    parser.add_argument("--output", default="quality-gate-result.json")
    args = parser.parse_args()

    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")

    report = load_json(args.report)
    policy = load_policy(args.config)

    findings = []
    for r in report.get("results", []):
        findings.append({
            "level": classify(r, policy["critical_rule_pattern"]),
            "rule": r.get("check_id", "unknown").split(".")[-1],
            "severity": r.get("extra", {}).get("severity", "INFO"),
            "path": r.get("path", "unknown"),
            "line": r.get("start", {}).get("line", "?"),
            "message": (r.get("extra", {}).get("message", "").strip().splitlines() or [""])[0],
        })
    counts = {level: 0 for level in LEVELS}
    for f in findings:
        counts[f["level"]] += 1

    print("=" * 64)
    print("  SKILLINE DEVSECOPS - SEMGREP QUALITY GATE")
    print(f"  {datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M:%S UTC')}")
    print(f"  Bao cao: {args.report} | Chinh sach: {args.config}")
    print("=" * 64)

    print("\nChi tiet phat hien:")
    if not findings:
        print("  (khong co phat hien nao)")
    for i, f in enumerate(sorted(findings, key=lambda x: LEVELS.index(x["level"])), 1):
        print(f"  [{i}] [{f['level'].upper()}] {f['path']}:{f['line']}")
        print(f"       Rule  : {f['rule']} (severity goc: {f['severity']})")
        print(f"       Detail: {f['message']}")

    scan_errors = report.get("errors", [])
    if scan_errors:
        print(f"\n[CANH BAO] Semgrep ghi nhan {len(scan_errors)} loi khi phan tich (xem artifact).")

    rows = evaluate(counts, policy["levels"])
    print("\n  Muc       | So phat hien | Nguong            | Trang thai")
    print("  " + "-" * 58)
    for level, actual, limit, status in rows:
        print(f"  {level:<9} | {actual:>12} | {limit:<17} | {status}")

    blocked = any(status == "BLOCK" for *_, status in rows)
    decision = "FAILED" if blocked else "PASSED"
    write_step_summary(rows, findings, decision)
    with open(args.output, "w", encoding="utf-8") as f:
        json.dump({"decision": decision, "counts": counts, "findings": findings},
                  f, ensure_ascii=False, indent=2)

    print("\n" + "=" * 64)
    if blocked:
        print("  QUALITY GATE: FAILED -> pipeline bi chan, khong cho phep merge")
        print("  Hay khac phuc cac phat hien o muc bi BLOCK roi push lai.")
    else:
        print("  QUALITY GATE: PASSED -> cho phep merge")
    print("=" * 64)
    sys.exit(1 if blocked else 0)


if __name__ == "__main__":
    main()
