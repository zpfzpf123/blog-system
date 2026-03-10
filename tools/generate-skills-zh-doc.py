#!/usr/bin/env python3
"""
生成 Codex Skills 中文总览文档。

用途：
1. 扫描技能目录中的所有 SKILL.md
2. 提取 frontmatter、章节结构、关键列表项、资源目录
3. 输出统一的中文索引文档（Markdown）
"""

from __future__ import annotations

import argparse
import re
from datetime import datetime
from pathlib import Path
from typing import Iterable


ZH = {
    "title": "# Codex Skills 中文总览与内容解读",
    "gen_time": "- 生成时间：",
    "root": "- 技能根目录：",
    "count": "- 已收录技能数：",
    "purpose_h2": "## 文档用途",
    "purpose_1": "- 这是一份“可迁移、可复查”的技能中文索引，帮助你在新机器或新团队成员环境中快速理解每个 skill 的作用与约束。",
    "purpose_2": "- 内容基于本地 `SKILL.md` 自动提取，重点包括：触发条件、章节结构、关键规则、配套脚本/资源。",
    "purpose_3": "- 若某个 skill 更新，重新生成本文件即可同步最新内容。",
    "cat_h2": "## 分类统计",
    "index_h2": "## 全量目录索引",
    "table_header": "| # | Skill 名称 | 路径 | 触发描述 |",
    "detail_h2": "## 逐项详细说明",
    "path": "- Skill 路径：",
    "desc": "- 中文触发解读：在以下场景优先启用：",
    "size": "- 内容规模：约",
    "sections": "- 章节结构：",
    "no_sections": "- 章节结构：未检测到 Markdown 标题",
    "gates": "- 强约束片段：",
    "rules": "- 关键规则摘录：",
    "no_rules": "- 关键规则摘录：未提取到列表项（可能以段落叙述为主）",
    "resources": "- 配套资源：",
    "no_resources": "- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录",
    "reuse_h2": "## 如何在新机器快速复用这份文档",
    "reuse_1": "1. 复制当前 `~/.codex/skills` 到新机器。",
    "reuse_2": "2. 在新机器运行同样的生成脚本（或把本文档一起拷过去）。",
    "reuse_3": "3. 每次新增/修改 skill 后，重新生成文档，保证文档与技能目录一致。",
}


ENCODINGS = ("utf-8", "utf-8-sig", "gb18030", "cp936")
RESOURCE_DIRS = ("scripts", "references", "assets", "templates", "skills")
GATE_TAGS = ("HARD-GATE", "EXTREMELY-IMPORTANT", "CHECKLIST")


def read_text_with_fallback(path: Path) -> str:
    """按常见编码顺序读取文本，避免编码差异导致解析失败。"""
    for enc in ENCODINGS:
        try:
            return path.read_text(encoding=enc)
        except Exception:
            continue
    return path.read_bytes().decode("utf-8", errors="replace")


def parse_frontmatter(text: str) -> tuple[dict[str, str], str]:
    """解析 YAML frontmatter，并返回 metadata 与正文。"""
    frontmatter: dict[str, str] = {}
    body = text
    match = re.match(r"^---\s*\n(.*?)\n---\s*\n?(.*)$", text, flags=re.S)
    if not match:
        return frontmatter, body

    fm_block = match.group(1)
    body = match.group(2)

    for raw_line in fm_block.splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or ":" not in line:
            continue
        key, value = line.split(":", 1)
        frontmatter[key.strip()] = value.strip().strip("\"'")

    return frontmatter, body


def extract_sections(body: str, max_sections: int) -> list[str]:
    """提取 Markdown 标题，作为 skill 内容结构。"""
    sections = [s.strip() for s in re.findall(r"^#{1,4}\s+(.+?)\s*$", body, flags=re.M)]
    return sections[:max_sections]


def extract_bullets(body: str, max_bullets: int) -> list[str]:
    """提取列表规则，用于快速查看 skill 的执行要点。"""
    bullets: list[str] = []
    for raw_line in body.splitlines():
        line = raw_line.strip()
        if not line:
            continue
        if not re.match(r"^(?:[-*]|\d+\.)\s+", line):
            continue

        rule = re.sub(r"^(?:[-*]|\d+\.)\s+", "", line).strip()
        if 5 <= len(rule) <= 180 and rule not in bullets:
            bullets.append(rule)
        if len(bullets) >= max_bullets:
            break
    return bullets


def extract_gate_blocks(body: str) -> list[tuple[str, str]]:
    """提取强约束标签块，帮助识别硬性规则。"""
    results: list[tuple[str, str]] = []
    for tag in GATE_TAGS:
        match = re.search(rf"<{tag}>\s*(.*?)\s*</{tag}>", body, flags=re.S | re.I)
        if not match:
            continue
        content = " ".join(line.strip() for line in match.group(1).splitlines() if line.strip())
        if content:
            results.append((tag, content))
    return results


def collect_resources(skill_dir: Path, max_resource_samples: int) -> list[tuple[str, int, list[str]]]:
    """收集 skill 下的脚本/参考资料目录，便于定位可复用资源。"""
    resources: list[tuple[str, int, list[str]]] = []
    for folder in RESOURCE_DIRS:
        subdir = skill_dir / folder
        if not subdir.exists() or not subdir.is_dir():
            continue

        files = sorted(path for path in subdir.rglob("*") if path.is_file())
        sample = [
            str(path.relative_to(skill_dir)).replace("\\", "/")
            for path in files[:max_resource_samples]
        ]
        resources.append((folder, len(files), sample))
    return resources


def count_words(text: str) -> int:
    """使用非空白分割做粗略词数统计。"""
    return len(re.findall(r"\S+", text))


def parse_skill(
    skill_file: Path,
    skills_root: Path,
    max_sections: int,
    max_bullets: int,
    max_resource_samples: int,
) -> dict[str, object]:
    """将单个 SKILL.md 解析成结构化信息。"""
    text = read_text_with_fallback(skill_file)
    frontmatter, body = parse_frontmatter(text)
    skill_dir = skill_file.parent
    relative_path = str(skill_file.relative_to(skills_root)).replace("\\", "/")

    return {
        "name": frontmatter.get("name", skill_dir.name),
        "description": frontmatter.get("description", "未在 frontmatter 中声明 description"),
        "relative_path": relative_path,
        "word_count": count_words(body),
        "sections": extract_sections(body, max_sections),
        "bullets": extract_bullets(body, max_bullets),
        "gates": extract_gate_blocks(body),
        "resources": collect_resources(skill_dir, max_resource_samples),
    }


def scan_skills(
    skills_root: Path,
    max_sections: int,
    max_bullets: int,
    max_resource_samples: int,
) -> list[dict[str, object]]:
    """扫描并解析技能目录内全部 SKILL.md。"""
    skills: list[dict[str, object]] = []
    for skill_file in sorted(skills_root.rglob("SKILL.md"), key=lambda p: str(p).lower()):
        skill = parse_skill(
            skill_file=skill_file,
            skills_root=skills_root,
            max_sections=max_sections,
            max_bullets=max_bullets,
            max_resource_samples=max_resource_samples,
        )
        skills.append(skill)
    return skills


def build_category_counts(skills: Iterable[dict[str, object]]) -> dict[str, int]:
    """按顶层目录做分类统计，帮助建立全局视图。"""
    categories: dict[str, int] = {}
    for skill in skills:
        relative_path = str(skill["relative_path"])
        top = relative_path.split("/")[0]
        categories[top] = categories.get(top, 0) + 1
    return categories


def render_markdown(skills: list[dict[str, object]], skills_root: Path) -> str:
    """将结构化技能信息渲染为 Markdown 文档。"""
    categories = build_category_counts(skills)
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    lines: list[str] = []
    lines.append(ZH["title"])
    lines.append("")
    lines.append(f"{ZH['gen_time']}{now}")
    lines.append(f"{ZH['root']}`{skills_root}`")
    lines.append(f"{ZH['count']}`{len(skills)}`（按 `SKILL.md` 文件计数）")
    lines.append("")
    lines.append(ZH["purpose_h2"])
    lines.append("")
    lines.append(ZH["purpose_1"])
    lines.append(ZH["purpose_2"])
    lines.append(ZH["purpose_3"])
    lines.append("")
    lines.append(ZH["cat_h2"])
    lines.append("")

    for key in sorted(categories):
        lines.append(f"- `{key}`: {categories[key]} 个 skill")

    lines.append("")
    lines.append(ZH["index_h2"])
    lines.append("")
    lines.append(ZH["table_header"])
    lines.append("|---|---|---|---|")

    for i, skill in enumerate(skills, start=1):
        desc = str(skill["description"]).replace("|", r"\|")
        lines.append(
            f"| {i} | `{skill['name']}` | `{skill['relative_path']}` | {desc} |"
        )

    lines.append("")
    lines.append(ZH["detail_h2"])
    lines.append("")

    for i, skill in enumerate(skills, start=1):
        lines.append(f"### {i}. {skill['name']}")
        lines.append("")
        lines.append(f"{ZH['path']}`{skill['relative_path']}`")
        lines.append(f"{ZH['desc']}{skill['description']}")
        lines.append(f"{ZH['size']}`{skill['word_count']}` 词")

        sections = list(skill["sections"])
        if sections:
            lines.append(ZH["sections"])
            for section in sections:
                lines.append(f"  - {section}")
        else:
            lines.append(ZH["no_sections"])

        gates = list(skill["gates"])
        if gates:
            lines.append(ZH["gates"])
            for tag, content in gates:
                lines.append(f"  - `{tag}`: {content}")

        bullets = list(skill["bullets"])
        if bullets:
            lines.append(ZH["rules"])
            for bullet in bullets:
                lines.append(f"  - {bullet}")
        else:
            lines.append(ZH["no_rules"])

        resources = list(skill["resources"])
        if resources:
            lines.append(ZH["resources"])
            for folder, file_count, sample_paths in resources:
                lines.append(f"  - `{folder}/`（{file_count} 个文件）")
                for sample_path in sample_paths:
                    lines.append(f"    - `{sample_path}`")
        else:
            lines.append(ZH["no_resources"])

        lines.append("")

    lines.append(ZH["reuse_h2"])
    lines.append("")
    lines.append(ZH["reuse_1"])
    lines.append(ZH["reuse_2"])
    lines.append(ZH["reuse_3"])
    lines.append("")

    return "\n".join(lines)


def build_parser() -> argparse.ArgumentParser:
    """构建命令行参数，便于在不同机器上复用。"""
    parser = argparse.ArgumentParser(
        description="Generate a Chinese catalog markdown from Codex SKILL.md files."
    )
    parser.add_argument(
        "--skills-root",
        type=Path,
        default=Path.home() / ".codex" / "skills",
        help="Skill root directory. Default: ~/.codex/skills",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("docs/skills/skills-zh-catalog.md"),
        help="Output markdown path. Default: docs/skills/skills-zh-catalog.md",
    )
    parser.add_argument(
        "--max-sections",
        type=int,
        default=12,
        help="Max number of headings per skill section.",
    )
    parser.add_argument(
        "--max-bullets",
        type=int,
        default=10,
        help="Max number of bullet rules per skill section.",
    )
    parser.add_argument(
        "--max-resource-samples",
        type=int,
        default=6,
        help="Max number of sample files shown for each resource directory.",
    )
    return parser


def main() -> int:
    """程序入口：扫描、渲染并写入文档。"""
    args = build_parser().parse_args()
    skills_root = args.skills_root.expanduser().resolve()
    output_path = args.output.resolve()

    if not skills_root.exists() or not skills_root.is_dir():
        raise SystemExit(f"Skill root not found or not a directory: {skills_root}")

    skills = scan_skills(
        skills_root=skills_root,
        max_sections=args.max_sections,
        max_bullets=args.max_bullets,
        max_resource_samples=args.max_resource_samples,
    )
    markdown = render_markdown(skills=skills, skills_root=skills_root)

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(markdown, encoding="utf-8")

    print(f"Generated: {output_path}")
    print(f"Skill files: {len(skills)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
