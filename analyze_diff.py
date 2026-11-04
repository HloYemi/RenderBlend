import os
import filecmp

def compare_dirs(re_root, target_root):
    """比对 Re 源码与目标项目，找出缺失文件和存在差异的文件"""
    report = {
        "missing_in_target": [],
        "diff_exists": [],
        "identical": []
    }

    # 遍历 Re 仓库的 src/main/java 目录
    re_src = os.path.join(re_root, "src", "main", "java")
    target_src = os.path.join(target_root, "src", "main", "java")

    for root, dirs, files in os.walk(re_src):
        for file in files:
            if not file.endswith(".java"):
                continue
            re_path = os.path.join(root, file)
            # 计算相对路径，如 committee/nova/mods/avaritia/...
            rel_path = os.path.relpath(re_path, re_src)
            target_path = os.path.join(target_src, rel_path)

            if not os.path.exists(target_path):
                report["missing_in_target"].append(rel_path)
            else:
                # 简单内容比对（忽略空白差异）
                try:
                    with open(re_path, 'r', encoding='utf-8') as f1, open(target_path, 'r', encoding='utf-8') as f2:
                        content1 = f1.read().strip()
                        content2 = f2.read().strip()
                        if content1 != content2:
                            report["diff_exists"].append(rel_path)
                        else:
                            report["identical"].append(rel_path)
                except Exception as e:
                    print(f"比对失败: {rel_path} -> {e}")

    return report

if __name__ == "__main__":
    # 路径配置（根据实际克隆的文件夹名调整）
    re_repo_root = "./_ref_re_avaritia"          # Re 源码根目录
    target_project_root = "./renderblender-1.21.1-neoforge"  # 你的项目根目录

    if not os.path.exists(re_repo_root):
        print(f"错误: 未找到 Re 仓库: {re_repo_root}，请先执行 PowerShell 下载步骤。")
        exit(1)

    print("开始比对 Re 1.21.1 与 RenderBlender 1.21.1...")
    result = compare_dirs(re_repo_root, target_project_root)

    print("\n" + "="*50)
    print("📊 比对结果摘要")
    print("="*50)
    print(f"❌ 目标项目中缺失的文件: {len(result['missing_in_target'])} 个")
    print(f"⚠️  内容存在差异的文件: {len(result['diff_exists'])} 个")
    print(f"✅ 内容完全一致的文件: {len(result['identical'])} 个")

    # 输出详细清单
    if result["missing_in_target"]:
        print("\n--- 缺失文件清单 ---")
        for f in result["missing_in_target"]:
            print(f"  {f}")

    if result["diff_exists"]:
        print("\n--- 差异文件清单 ---")
        for f in result["diff_exists"]:
            print(f"  {f}")

    # 生成简要报告文件
    with open("diff_report.txt", "w", encoding="utf-8") as report_file:
        report_file.write("缺失文件:\n")
        for f in result["missing_in_target"]:
            report_file.write(f + "\n")
        report_file.write("\n差异文件:\n")
        for f in result["diff_exists"]:
            report_file.write(f + "\n")
    
    print(f"\n📄 详细清单已保存至: diff_report.txt")