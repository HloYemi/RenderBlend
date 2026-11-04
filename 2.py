# -*- coding: utf-8 -*-
"""
修复 renderblender-1.21.1-neoforge 工程：
  1. 在 gradle.properties 末尾追加代理配置（幂等）
  2. 重写 settings.gradle，加阿里云 + NeoForge + Parchment 镜像
  3. 修改 build.gradle 的 repositories 块，加阿里云镜像

用法：
  python fix_neoforge_proxy.py
  python fix_neoforge_proxy.py --proxy-port 10808
  python fix_neoforge_proxy.py --no-proxy
"""

import argparse
import re
import sys
from pathlib import Path

DEFAULT_PROJ = r"D:\AAA.DownLoad\RenderBlender-master\renderblender-1.21.1-neoforge"


def c(s, color):
    codes = {"g": "\033[92m", "y": "\033[93m", "r": "\033[91m",
             "c": "\033[96m", "d": "\033[90m", "x": "\033[0m"}
    return f"{codes.get(color, '')}{s}{codes['x']}"


def w(path: Path, content: str):
    path.write_bytes(content.encode("utf-8"))


# ============================================================
# 1. gradle.properties 追加代理（幂等）
# ============================================================
PROXY_BLOCK_START = "# ===== AutoProxy (managed by fix_neoforge_proxy.py) ====="
PROXY_BLOCK_END   = "# ===== End AutoProxy ====="

def update_gradle_properties(path: Path, port: int, enable: bool):
    if not path.exists():
        print(c(f"[ERR] 文件不存在: {path}", "r"))
        return False

    content = path.read_text(encoding="utf-8")

    # 移除旧的 AutoProxy 块（幂等）
    content = re.sub(
        re.escape(PROXY_BLOCK_START) + r".*?" + re.escape(PROXY_BLOCK_END) + r"\r?\n?",
        "",
        content,
        flags=re.DOTALL,
    )

    if enable:
        block = (
            f"\n{PROXY_BLOCK_START}\n"
            f"systemProp.http.proxyHost=127.0.0.1\n"
            f"systemProp.http.proxyPort={port}\n"
            f"systemProp.https.proxyHost=127.0.0.1\n"
            f"systemProp.https.proxyPort={port}\n"
            f"systemProp.http.nonProxyHosts=localhost|127.*|[::1]\n"
            f"{PROXY_BLOCK_END}\n"
        )
        content = content.rstrip() + "\n" + block
        w(path, content)
        print(c(f"[OK] gradle.properties 已写入代理（端口 {port}）", "g"))
    else:
        content = content.rstrip() + "\n"
        w(path, content)
        print(c("[OK] gradle.properties 已移除代理", "g"))
    return True


# ============================================================
# 2. settings.gradle 重写
# ============================================================
SETTINGS_GRADLE = """\
pluginManagement {
    repositories {
        // 国内镜像优先
        maven { url = 'https://maven.aliyun.com/repository/public' }
        maven { url = 'https://maven.aliyun.com/repository/gradle-plugin' }
        // NeoForge 官方
        maven { url = 'https://maven.neoforged.net/releases' }
        maven { url = 'https://maven.parchmentmc.org' }
        gradlePluginPortal()
    }
}
"""

def update_settings_gradle(path: Path):
    w(path, SETTINGS_GRADLE)
    print(c("[OK] settings.gradle 已重写（含阿里云镜像）", "g"))


# ============================================================
# 3. build.gradle 的 repositories 块追加阿里云
# ============================================================
ALIYUN_REPO_LINE = "    maven { url = 'https://maven.aliyun.com/repository/public' }\n"

def update_build_gradle(path: Path):
    if not path.exists():
        print(c(f"[ERR] 文件不存在: {path}", "r"))
        return

    content = path.read_text(encoding="utf-8")

    # 找 repositories { 块
    m = re.search(r"(?m)^repositories\s*\{\s*\r?\n", content)
    if not m:
        print(c("[WARN] build.gradle 未找到 repositories 块", "y"))
        return

    # 已经加过就跳过
    after = content[m.end():]
    if ALIYUN_REPO_LINE in after[:500]:
        print(c("[SKIP] build.gradle 已有阿里云镜像", "d"))
        return

    # 在 repositories { 后插入阿里云镜像
    new_content = content[:m.end()] + ALIYUN_REPO_LINE + content[m.end():]
    w(path, new_content)
    print(c("[OK] build.gradle repositories 已加阿里云镜像", "g"))


# ============================================================
# 主流程
# ============================================================
def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--proj",       default=DEFAULT_PROJ)
    ap.add_argument("--proxy-port", type=int, default=10808)
    ap.add_argument("--no-proxy",   action="store_true", help="移除代理配置")
    args = ap.parse_args()

    proj = Path(args.proj)

    print(c("===== 前置检查 =====", "c"))
    if not proj.exists():
        print(c(f"[ERR] 工程目录不存在: {proj}", "r"))
        sys.exit(1)
    print(c(f"[OK] 工程目录: {proj}", "g"))

    print()
    print(c("===== 1. gradle.properties =====", "c"))
    update_gradle_properties(
        proj / "gradle.properties",
        port=args.proxy_port,
        enable=not args.no_proxy,
    )

    print()
    print(c("===== 2. settings.gradle =====", "c"))
    update_settings_gradle(proj / "settings.gradle")

    print()
    print(c("===== 3. build.gradle =====", "c"))
    update_build_gradle(proj / "build.gradle")

    print()
    print(c("===== 完成 =====", "c"))
    print()
    print("下一步：")
    print(f"  cd {proj}")
    print("  .\\gradlew.bat --stop")
    print("  .\\gradlew.bat clean build")
    print()
    if not args.no_proxy:
        print(c(f"代理端口: {args.proxy_port}（可用 --proxy-port 覆盖）", "y"))


if __name__ == "__main__":
    main()