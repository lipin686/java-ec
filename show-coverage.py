#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import csv
import sys
from pathlib import Path

def calculate_coverage(csv_file):
    """解析 JaCoCo CSV 並計算覆蓋率"""

    total_instruction_missed = 0
    total_instruction_covered = 0
    total_branch_missed = 0
    total_branch_covered = 0
    total_line_missed = 0
    total_line_covered = 0
    total_method_missed = 0
    total_method_covered = 0

    class_coverage = []

    with open(csv_file, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            class_name = row['CLASS']
            package = row['PACKAGE']

            inst_missed = int(row['INSTRUCTION_MISSED'])
            inst_covered = int(row['INSTRUCTION_COVERED'])
            branch_missed = int(row['BRANCH_MISSED'])
            branch_covered = int(row['BRANCH_COVERED'])
            line_missed = int(row['LINE_MISSED'])
            line_covered = int(row['LINE_COVERED'])
            method_missed = int(row['METHOD_MISSED'])
            method_covered = int(row['METHOD_COVERED'])

            total_instruction_missed += inst_missed
            total_instruction_covered += inst_covered
            total_branch_missed += branch_missed
            total_branch_covered += branch_covered
            total_line_missed += line_missed
            total_line_covered += line_covered
            total_method_missed += method_missed
            total_method_covered += method_covered

            # 計算行覆蓋率
            total_lines = line_missed + line_covered
            if total_lines > 0:
                line_coverage = (line_covered / total_lines) * 100
                class_coverage.append({
                    'package': package,
                    'class': class_name,
                    'coverage': line_coverage,
                    'lines': total_lines
                })

    # 計算總體覆蓋率
    total_inst = total_instruction_missed + total_instruction_covered
    total_branches = total_branch_missed + total_branch_covered
    total_lines = total_line_missed + total_line_covered
    total_methods = total_method_missed + total_method_covered

    inst_coverage = (total_instruction_covered / total_inst * 100) if total_inst > 0 else 0
    branch_coverage = (total_branch_covered / total_branches * 100) if total_branches > 0 else 0
    line_coverage = (total_line_covered / total_lines * 100) if total_lines > 0 else 0
    method_coverage = (total_method_covered / total_methods * 100) if total_methods > 0 else 0

    return {
        'instruction': inst_coverage,
        'branch': branch_coverage,
        'line': line_coverage,
        'method': method_coverage,
        'class_coverage': sorted(class_coverage, key=lambda x: x['coverage']),
        'total_lines': total_lines,
        'total_covered': total_line_covered,
        'total_methods': total_methods,
        'total_methods_covered': total_method_covered
    }

def print_coverage_report(coverage):
    """美化打印覆蓋率報告"""

    print("\n" + "="*60)
    print("📊 測試覆蓋率報告")
    print("="*60)

    print(f"\n整體覆蓋率:")
    print(f"  指令覆蓋率 (Instruction): {coverage['instruction']:.2f}%")
    print(f"  分支覆蓋率 (Branch):      {coverage['branch']:.2f}%")
    print(f"  行覆蓋率   (Line):        {coverage['line']:.2f}%")
    print(f"  方法覆蓋率 (Method):      {coverage['method']:.2f}%")

    print(f"\n詳細統計:")
    print(f"  總行數:     {coverage['total_lines']}")
    print(f"  已覆蓋:     {coverage['total_covered']}")
    print(f"  未覆蓋:     {coverage['total_lines'] - coverage['total_covered']}")
    print(f"  總方法數:   {coverage['total_methods']}")
    print(f"  已測試:     {coverage['total_methods_covered']}")

    # 評級
    line_cov = coverage['line']
    if line_cov >= 80:
        grade = "🏆 優秀"
        emoji = "✅"
    elif line_cov >= 70:
        grade = "👍 良好"
        emoji = "✅"
    elif line_cov >= 50:
        grade = "⚠️  及格"
        emoji = "⚠️"
    else:
        grade = "❌ 需改進"
        emoji = "❌"

    print(f"\n總體評級: {emoji} {line_cov:.2f}% - {grade}")

    print("\n" + "-"*60)
    print("🔴 需要提高覆蓋率的類別 (< 50%)")
    print("-"*60)

    low_coverage = [c for c in coverage['class_coverage'] if c['coverage'] < 50 and c['lines'] > 5]
    if low_coverage:
        for item in low_coverage[:10]:  # 只顯示前10個
            pkg_short = item['package'].split('.')[-1]
            print(f"  {item['coverage']:5.1f}% | {pkg_short:20s} | {item['class']}")
    else:
        print("  ✅ 沒有低覆蓋率的類別！")

    print("\n" + "-"*60)
    print("🟢 覆蓋率最高的類別 (> 80%)")
    print("-"*60)

    high_coverage = [c for c in coverage['class_coverage'] if c['coverage'] > 80]
    if high_coverage:
        for item in reversed(high_coverage[-10:]):  # 顯示最後10個（最高的）
            pkg_short = item['package'].split('.')[-1]
            print(f"  {item['coverage']:5.1f}% | {pkg_short:20s} | {item['class']}")
    else:
        print("  還沒有高覆蓋率的類別")

    print("\n" + "="*60)
    print("💡 下一步建議:")
    print("="*60)

    if line_cov < 70:
        print("  1. 為 Service 層編寫單元測試")
        print("  2. 為 Controller 補充異常情況測試")
        print("  3. 為工具類增加測試覆蓋")
    elif line_cov < 80:
        print("  1. 補充邊界條件測試")
        print("  2. 增加分支覆蓋率")
        print("  3. 測試異常處理邏輯")
    else:
        print("  1. 保持當前的測試質量")
        print("  2. 為新功能添加測試")
        print("  3. 定期審查測試用例")

    print("\n📁 詳細報告: spring-boot/target/site/jacoco/index.html")
    print("="*60 + "\n")

if __name__ == "__main__":
    csv_file = Path("spring-boot/target/site/jacoco/jacoco.csv")

    if not csv_file.exists():
        print("❌ 找不到覆蓋率報告文件")
        print("💡 請先執行: ./run-test-coverage-v2.sh")
        sys.exit(1)

    coverage = calculate_coverage(csv_file)
    print_coverage_report(coverage)

