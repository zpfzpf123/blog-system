# Codex Skills 中文总览与内容解读

- 生成时间：2026-02-28 16:27:30
- 技能根目录：`C:\Users\26220\.codex\skills`
- 已收录技能数：`39`（按 `SKILL.md` 文件计数）

## 文档用途

- 这是一份“可迁移、可复查”的技能中文索引，帮助你在新机器或新团队成员环境中快速理解每个 skill 的作用与约束。
- 内容基于本地 `SKILL.md` 自动提取，重点包括：触发条件、章节结构、关键规则、配套脚本/资源。
- 若某个 skill 更新，重新生成本文件即可同步最新内容。

## 分类统计

- `.system`: 2 个 skill
- `brainstorming`: 1 个 skill
- `code-simplifier`: 1 个 skill
- `create-adaptable-composable`: 1 个 skill
- `dispatching-parallel-agents`: 1 个 skill
- `doc`: 1 个 skill
- `document-skills`: 1 个 skill
- `docx`: 1 个 skill
- `executing-plans`: 1 个 skill
- `finishing-a-development-branch`: 1 个 skill
- `frontend-design`: 1 个 skill
- `pdf`: 1 个 skill
- `pptx`: 1 个 skill
- `ralph-loop`: 5 个 skill
- `receiving-code-review`: 1 个 skill
- `requesting-code-review`: 1 个 skill
- `safe-delete-guard`: 1 个 skill
- `subagent-driven-development`: 1 个 skill
- `systematic-debugging`: 1 个 skill
- `test-driven-development`: 1 个 skill
- `ui-ux-pro-max`: 1 个 skill
- `using-git-worktrees`: 1 个 skill
- `using-superpowers`: 1 个 skill
- `verification-before-completion`: 1 个 skill
- `vue-best-practices`: 1 个 skill
- `vue-debug-guides`: 1 个 skill
- `vue-jsx-best-practices`: 1 个 skill
- `vue-options-api-best-practices`: 1 个 skill
- `vue-pinia-best-practices`: 1 个 skill
- `vue-router-best-practices`: 1 个 skill
- `vue-testing-best-practices`: 1 个 skill
- `writing-plans`: 1 个 skill
- `writing-skills`: 1 个 skill
- `xlsx`: 1 个 skill

## 全量目录索引

| # | Skill 名称 | 路径 | 触发描述 |
|---|---|---|---|
| 1 | `skill-creator` | `.system/skill-creator/SKILL.md` | Guide for creating effective skills. This skill should be used when users want to create a new skill (or update an existing skill) that extends Codex's capabilities with specialized knowledge, workflows, or tool integrations. |
| 2 | `skill-installer` | `.system/skill-installer/SKILL.md` | Install Codex skills into $CODEX_HOME/skills from a curated list or a GitHub repo path. Use when a user asks to list installable skills, install a curated skill, or install a skill from another repo (including private repos). |
| 3 | `brainstorming` | `brainstorming/SKILL.md` | You MUST use this before any creative work - creating features, building components, adding functionality, or modifying behavior. Explores user intent, requirements and design before implementation. |
| 4 | `code-simplifier` | `code-simplifier/SKILL.md` | Simplifies and refines code for clarity, consistency, and maintainability while preserving all functionality. Focuses on recently modified code unless instructed otherwise. |
| 5 | `create-adaptable-composable` | `create-adaptable-composable/SKILL.md` | Create a library-grade Vue composable that accepts maybe-reactive inputs (MaybeRef / MaybeRefOrGetter) so callers can pass a plain value, ref, or getter. Normalize inputs with toValue()/toRef() inside reactive effects (watch/watchEffect) to keep behavior predictable and reactive. Use this skill when user asks for creating adaptable or reusable composables. |
| 6 | `dispatching-parallel-agents` | `dispatching-parallel-agents/SKILL.md` | Use when facing 2+ independent tasks that can be worked on without shared state or sequential dependencies |
| 7 | `doc` | `doc/SKILL.md` | Use when the task involves reading, creating, or editing `.docx` documents, especially when formatting or layout fidelity matters; prefer `python-docx` plus the bundled `scripts/render_docx.py` for visual checks. |
| 8 | `doc` | `document-skills/SKILL.md` | Use when the task involves reading, creating, or editing `.docx` documents, especially when formatting or layout fidelity matters; prefer `python-docx` plus the bundled `scripts/render_docx.py` for visual checks. |
| 9 | `docx` | `docx/SKILL.md` | Document toolkit (.docx). Create/edit documents, tracked changes, comments, formatting preservation, text extraction, for professional document processing. |
| 10 | `executing-plans` | `executing-plans/SKILL.md` | Use when you have a written implementation plan to execute in a separate session with review checkpoints |
| 11 | `finishing-a-development-branch` | `finishing-a-development-branch/SKILL.md` | Use when implementation is complete, all tests pass, and you need to decide how to integrate the work - guides completion of development work by presenting structured options for merge, PR, or cleanup |
| 12 | `frontend-design` | `frontend-design/SKILL.md` | Create distinctive, production-grade frontend interfaces with high design quality. Use this skill when the user asks to build web components, pages, or applications. Generates creative, polished code that avoids generic AI aesthetics. |
| 13 | `pdf` | `pdf/SKILL.md` | PDF manipulation toolkit. Extract text/tables, create PDFs, merge/split, fill forms, for programmatic document processing and analysis. |
| 14 | `pptx` | `pptx/SKILL.md` | Presentation toolkit (.pptx). Create/edit slides, layouts, content, speaker notes, comments, for programmatic presentation creation and modification. |
| 15 | `ralph-loop` | `ralph-loop/SKILL.md` | 未在 frontmatter 中声明 description |
| 16 | `implement` | `ralph-loop/skills/implement/SKILL.md` | 未在 frontmatter 中声明 description |
| 17 | `init` | `ralph-loop/skills/init/SKILL.md` | 未在 frontmatter 中声明 description |
| 18 | `interview` | `ralph-loop/skills/interview/SKILL.md` | 未在 frontmatter 中声明 description |
| 19 | `loop` | `ralph-loop/skills/loop/SKILL.md` | 未在 frontmatter 中声明 description |
| 20 | `receiving-code-review` | `receiving-code-review/SKILL.md` | Use when receiving code review feedback, before implementing suggestions, especially if feedback seems unclear or technically questionable - requires technical rigor and verification, not performative agreement or blind implementation |
| 21 | `requesting-code-review` | `requesting-code-review/SKILL.md` | Use when completing tasks, implementing major features, or before merging to verify work meets requirements |
| 22 | `safe-delete-guard` | `safe-delete-guard/SKILL.md` | 未在 frontmatter 中声明 description |
| 23 | `subagent-driven-development` | `subagent-driven-development/SKILL.md` | Use when executing implementation plans with independent tasks in the current session |
| 24 | `systematic-debugging` | `systematic-debugging/SKILL.md` | Use when encountering any bug, test failure, or unexpected behavior, before proposing fixes |
| 25 | `test-driven-development` | `test-driven-development/SKILL.md` | Use when implementing any feature or bugfix, before writing implementation code |
| 26 | `ui-ux-pro-max` | `ui-ux-pro-max/SKILL.md` | UI/UX design intelligence. 50 styles, 21 palettes, 50 font pairings, 20 charts, 9 stacks (React, Next.js, Vue, Svelte, SwiftUI, React Native, Flutter, Tailwind, shadcn/ui). Actions: plan, build, create, design, implement, review, fix, improve, optimize, enhance, refactor, check UI/UX code. Projects: website, landing page, dashboard, admin panel, e-commerce, SaaS, portfolio, blog, mobile app, .html, .tsx, .vue, .svelte. Elements: button, modal, navbar, sidebar, card, table, form, chart. Styles: glassmorphism, claymorphism, minimalism, brutalism, neumorphism, bento grid, dark mode, responsive, skeuomorphism, flat design. Topics: color palette, accessibility, animation, layout, typography, font pairing, spacing, hover, shadow, gradient. Integrations: shadcn/ui MCP for component search and examples. |
| 27 | `using-git-worktrees` | `using-git-worktrees/SKILL.md` | Use when starting feature work that needs isolation from current workspace or before executing implementation plans - creates isolated git worktrees with smart directory selection and safety verification |
| 28 | `using-superpowers` | `using-superpowers/SKILL.md` | Use when starting any conversation - establishes how to find and use skills, requiring Skill tool invocation before ANY response including clarifying questions |
| 29 | `verification-before-completion` | `verification-before-completion/SKILL.md` | Use when about to claim work is complete, fixed, or passing, before committing or creating PRs - requires running verification commands and confirming output before making any success claims; evidence before assertions always |
| 30 | `vue-best-practices` | `vue-best-practices/SKILL.md` | MUST be used for Vue.js tasks. Strongly recommends Composition API with `<script setup>` and TypeScript as the standard approach. Covers Vue 3, SSR, Volar, vue-tsc. Load for any Vue, .vue files, Vue Router, Pinia, or Vite with Vue work. ALWAYS use Composition API unless the project explicitly requires Options API. |
| 31 | `vue-debug-guides` | `vue-debug-guides/SKILL.md` | Vue 3 debugging and error handling for runtime errors, warnings, async failures, and SSR/hydration issues. Use when diagnosing or fixing Vue issues. |
| 32 | `vue-jsx-best-practices` | `vue-jsx-best-practices/SKILL.md` | JSX syntax in Vue (e.g., class vs className, JSX plugin config). |
| 33 | `vue-options-api-best-practices` | `vue-options-api-best-practices/SKILL.md` | Vue 3 Options API style (data(), methods, this context). Each reference shows Options API solution only. |
| 34 | `vue-pinia-best-practices` | `vue-pinia-best-practices/SKILL.md` | Pinia stores, state management patterns, store setup, and reactivity with stores. |
| 35 | `vue-router-best-practices` | `vue-router-best-practices/SKILL.md` | Vue Router 4 patterns, navigation guards, route params, and route-component lifecycle interactions. |
| 36 | `vue-testing-best-practices` | `vue-testing-best-practices/SKILL.md` | Use for Vue.js testing. Covers Vitest, Vue Test Utils, component testing, mocking, testing patterns, and Playwright for E2E testing. |
| 37 | `writing-plans` | `writing-plans/SKILL.md` | Use when you have a spec or requirements for a multi-step task, before touching code |
| 38 | `writing-skills` | `writing-skills/SKILL.md` | Use when creating new skills, editing existing skills, or verifying skills work before deployment |
| 39 | `xlsx` | `xlsx/SKILL.md` | Spreadsheet toolkit (.xlsx/.csv). Create/edit with formulas/formatting, analyze data, visualization, recalculate formulas, for spreadsheet processing and analysis. |

## 逐项详细说明

### 1. skill-creator

- Skill 路径：`.system/skill-creator/SKILL.md`
- 中文触发解读：在以下场景优先启用：Guide for creating effective skills. This skill should be used when users want to create a new skill (or update an existing skill) that extends Codex's capabilities with specialized knowledge, workflows, or tool integrations.
- 内容规模：约`2566` 词
- 章节结构：
  - Skill Creator
  - About Skills
  - What Skills Provide
  - Core Principles
  - Concise is Key
  - Set Appropriate Degrees of Freedom
  - Anatomy of a Skill
  - SKILL.md (required)
  - Agents metadata (recommended)
  - Bundled Resources (optional)
  - What to Not Include in a Skill
  - Progressive Disclosure Design Principle
- 关键规则摘录：
  - Specialized workflows - Multi-step procedures for specific domains
  - Tool integrations - Instructions for working with specific file formats or APIs
  - Domain expertise - Company-specific knowledge, schemas, business logic
  - Bundled resources - Scripts, references, and assets for complex and repetitive tasks
  - **Body** (Markdown): Instructions and guidance for using the skill. Only loaded AFTER the skill triggers (if at all).
  - UI-facing metadata for skill lists and chips
  - Read references/openai_yaml.md before generating values and follow its descriptions and constraints
  - Create: human-facing `display_name`, `short_description`, and `default_prompt` by reading the skill
  - Generate deterministically by passing the values as `--interface key=value` to `scripts/generate_openai_yaml.py` or `scripts/init_skill.py`
  - On updates: validate `agents/openai.yaml` still matches SKILL.md; regenerate if stale
- 配套资源：
  - `scripts/`（3 个文件）
    - `scripts/generate_openai_yaml.py`
    - `scripts/init_skill.py`
    - `scripts/quick_validate.py`
  - `references/`（1 个文件）
    - `references/openai_yaml.md`
  - `assets/`（2 个文件）
    - `assets/skill-creator-small.svg`
    - `assets/skill-creator.png`

### 2. skill-installer

- Skill 路径：`.system/skill-installer/SKILL.md`
- 中文触发解读：在以下场景优先启用：Install Codex skills into $CODEX_HOME/skills from a curated list or a GitHub repo path. Use when a user asks to list installable skills, install a curated skill, or install a skill from another repo (including private repos).
- 内容规模：约`390` 词
- 章节结构：
  - Skill Installer
  - Communication
  - Scripts
  - Behavior and Options
  - Notes
- 关键规则摘录：
  - Install from the curated list when the user provides a skill name.
  - Install from another repo when the user provides a GitHub repo/path (including private repos).
  - skill-1
  - skill-2 (already installed)
  - `scripts/list-skills.py` (prints skills list with installed annotations)
  - `scripts/list-skills.py --format json`
  - Example (experimental list): `scripts/list-skills.py --path skills/.experimental`
  - `scripts/install-skill-from-github.py --repo <owner>/<repo> --path <path/to/skill> [<path/to/skill> ...]`
  - `scripts/install-skill-from-github.py --url https://github.com/<owner>/<repo>/tree/<ref>/<path>`
  - Example (experimental skill): `scripts/install-skill-from-github.py --repo openai/skills --path skills/.experimental/<skill-name>`
- 配套资源：
  - `scripts/`（4 个文件）
    - `scripts/__pycache__/github_utils.cpython-312.pyc`
    - `scripts/github_utils.py`
    - `scripts/install-skill-from-github.py`
    - `scripts/list-skills.py`
  - `assets/`（2 个文件）
    - `assets/skill-installer-small.svg`
    - `assets/skill-installer.png`

### 3. brainstorming

- Skill 路径：`brainstorming/SKILL.md`
- 中文触发解读：在以下场景优先启用：You MUST use this before any creative work - creating features, building components, adding functionality, or modifying behavior. Explores user intent, requirements and design before implementation.
- 内容规模：约`855` 词
- 章节结构：
  - Brainstorming Ideas Into Designs
  - Overview
  - Anti-Pattern: "This Is Too Simple To Need A Design"
  - Checklist
  - Process Flow
  - The Process
  - After the Design
  - Key Principles
- 强约束片段：
  - `HARD-GATE`: Do NOT invoke any implementation skill, write any code, scaffold any project, or take any implementation action until you have presented a design and the user has approved it. This applies to EVERY project regardless of perceived simplicity.
- 关键规则摘录：
  - **Explore project context** — check files, docs, recent commits
  - **Ask clarifying questions** — one at a time, understand purpose/constraints/success criteria
  - **Propose 2-3 approaches** — with trade-offs and your recommendation
  - **Present design** — in sections scaled to their complexity, get user approval after each section
  - **Write design doc** — save to `docs/plans/YYYY-MM-DD-<topic>-design.md` and commit
  - **Transition to implementation** — invoke writing-plans skill to create implementation plan
  - Check out the current project state first (files, docs, recent commits)
  - Ask questions one at a time to refine the idea
  - Prefer multiple choice questions when possible, but open-ended is fine too
  - Only one question per message - if a topic needs more exploration, break it into multiple questions
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 4. code-simplifier

- Skill 路径：`code-simplifier/SKILL.md`
- 中文触发解读：在以下场景优先启用：Simplifies and refines code for clarity, consistency, and maintainability while preserving all functionality. Focuses on recently modified code unless instructed otherwise.
- 内容规模：约`408` 词
- 章节结构：未检测到 Markdown 标题
- 关键规则摘录：
  - **Preserve Functionality**: Never change what the code does - only how it does it. All original features, outputs, and behaviors must remain intact.
  - **Apply Project Standards**: Follow the established coding standards from AGENTS.md (primary) and CLAUDE.md including:
  - Use ES modules with proper import sorting and extensions
  - Prefer `function` keyword over arrow functions
  - Use explicit return type annotations for top-level functions
  - Follow proper React component patterns with explicit Props types
  - Use proper error handling patterns (avoid try/catch when possible)
  - Maintain consistent naming conventions
  - **Enhance Clarity**: Simplify code structure by:
  - Reducing unnecessary complexity and nesting
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 5. create-adaptable-composable

- Skill 路径：`create-adaptable-composable/SKILL.md`
- 中文触发解读：在以下场景优先启用：Create a library-grade Vue composable that accepts maybe-reactive inputs (MaybeRef / MaybeRefOrGetter) so callers can pass a plain value, ref, or getter. Normalize inputs with toValue()/toRef() inside reactive effects (watch/watchEffect) to keep behavior predictable and reactive. Use this skill when user asks for creating adaptable or reusable composables.
- 内容规模：约`299` 词
- 章节结构：
  - Create Adaptable Composable
  - Core Type Concepts
  - Type Utilities
  - Policy and Rules
  - Examples
- 关键规则摘录：
  - Confirm the composable's purpose and API design and expected inputs/outputs.
  - Identify inputs params that should be reactive (MaybeRef / MaybeRefOrGetter).
  - Use `toValue()` or `toRef()` to normalize inputs inside reactive effects.
  - Implement the core logic of the composable using Vue's reactivity APIs.
  - value or writable ref (value/ref/shallowRef/writable computed)
  - MaybeRef<T> + ComputedRef<T> + () => T
  - Read-only, computed-friendly input: use `MaybeRefOrGetter`
  - Needs to be writable / two-way input: use `MaybeRef`
  - Parameter might be a function value (callback/predicate/comparator): do not use `MaybeRefOrGetter`, or you may accidentally invoke it as a getter.
  - DOM/Element targets: if you want computed/derived targets, use `MaybeRefOrGetter`.
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 6. dispatching-parallel-agents

- Skill 路径：`dispatching-parallel-agents/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when facing 2+ independent tasks that can be worked on without shared state or sequential dependencies
- 内容规模：约`850` 词
- 章节结构：
  - Dispatching Parallel Agents
  - Overview
  - When to Use
  - The Pattern
  - 1. Identify Independent Domains
  - 2. Create Focused Agent Tasks
  - 3. Dispatch in Parallel
  - 4. Review and Integrate
  - Agent Prompt Structure
  - Common Mistakes
  - When NOT to Use
  - Real Example from Session
- 关键规则摘录：
  - 3+ test files failing with different root causes
  - Multiple subsystems broken independently
  - Each problem can be understood without context from others
  - No shared state between investigations
  - Failures are related (fix one might fix others)
  - Need to understand full system state
  - Agents would interfere with each other
  - File A tests: Tool approval flow
  - File B tests: Batch completion behavior
  - File C tests: Abort functionality
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 7. doc

- Skill 路径：`doc/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when the task involves reading, creating, or editing `.docx` documents, especially when formatting or layout fidelity matters; prefer `python-docx` plus the bundled `scripts/render_docx.py` for visual checks.
- 内容规模：约`390` 词
- 章节结构：
  - DOCX Skill
  - When to use
  - Workflow
  - Temp and output conventions
  - Dependencies (install if missing)
  - macOS (Homebrew)
  - Ubuntu/Debian
  - Environment
  - Rendering commands
  - Quality expectations
  - Final checks
- 关键规则摘录：
  - Read or review DOCX content where layout matters (tables, diagrams, pagination).
  - Create or edit DOCX files with professional formatting.
  - Validate visual layout before delivery.
  - Prefer visual review (layout, tables, diagrams).
  - If `soffice` and `pdftoppm` are available, convert DOCX -> PDF -> PNGs.
  - Or use `scripts/render_docx.py` (requires `pdf2image` and Poppler).
  - If these tools are missing, install them or ask the user to review rendered pages locally.
  - Use `python-docx` for edits and structured creation (headings, styles, tables, lists).
  - After each meaningful change, re-render and inspect the pages.
  - If visual review is not possible, extract text with `python-docx` as a fallback and call out layout risk.
- 配套资源：
  - `scripts/`（1 个文件）
    - `scripts/render_docx.py`
  - `assets/`（2 个文件）
    - `assets/doc-small.svg`
    - `assets/doc.png`

### 8. doc

- Skill 路径：`document-skills/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when the task involves reading, creating, or editing `.docx` documents, especially when formatting or layout fidelity matters; prefer `python-docx` plus the bundled `scripts/render_docx.py` for visual checks.
- 内容规模：约`390` 词
- 章节结构：
  - DOCX Skill
  - When to use
  - Workflow
  - Temp and output conventions
  - Dependencies (install if missing)
  - macOS (Homebrew)
  - Ubuntu/Debian
  - Environment
  - Rendering commands
  - Quality expectations
  - Final checks
- 关键规则摘录：
  - Read or review DOCX content where layout matters (tables, diagrams, pagination).
  - Create or edit DOCX files with professional formatting.
  - Validate visual layout before delivery.
  - Prefer visual review (layout, tables, diagrams).
  - If `soffice` and `pdftoppm` are available, convert DOCX -> PDF -> PNGs.
  - Or use `scripts/render_docx.py` (requires `pdf2image` and Poppler).
  - If these tools are missing, install them or ask the user to review rendered pages locally.
  - Use `python-docx` for edits and structured creation (headings, styles, tables, lists).
  - After each meaningful change, re-render and inspect the pages.
  - If visual review is not possible, extract text with `python-docx` as a fallback and call out layout risk.
- 配套资源：
  - `scripts/`（1 个文件）
    - `scripts/render_docx.py`
  - `assets/`（2 个文件）
    - `assets/doc-small.svg`
    - `assets/doc.png`

### 9. docx

- Skill 路径：`docx/SKILL.md`
- 中文触发解读：在以下场景优先启用：Document toolkit (.docx). Create/edit documents, tracked changes, comments, formatting preservation, text extraction, for professional document processing.
- 内容规模：约`1498` 词
- 章节结构：
  - DOCX creation, editing, and analysis
  - Overview
  - Visual Enhancement with Scientific Schematics
  - Workflow Decision Tree
  - Reading/Analyzing Content
  - Creating New Document
  - Editing Existing Document
  - Reading and analyzing content
  - Text extraction
  - Convert document to markdown with tracked changes
  - Options: --track-changes=accept/reject/all
  - Raw XML access
- 关键规则摘录：
  - Use the **scientific-schematics** skill to generate AI-powered publication-quality diagrams
  - Simply describe your desired diagram in natural language
  - Nano Banana Pro will automatically generate, review, and refine the schematic
  - Create publication-quality images with proper formatting
  - Review and refine through multiple iterations
  - Ensure accessibility (colorblind-friendly, high contrast)
  - Save outputs in the figures/ directory
  - Document workflow diagrams
  - Process flowcharts
  - System architecture illustrations
- 配套资源：
  - `scripts/`（8 个文件）
    - `scripts/__init__.py`
    - `scripts/document.py`
    - `scripts/templates/comments.xml`
    - `scripts/templates/commentsExtended.xml`
    - `scripts/templates/commentsExtensible.xml`
    - `scripts/templates/commentsIds.xml`

### 10. executing-plans

- Skill 路径：`executing-plans/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when you have a written implementation plan to execute in a separate session with review checkpoints
- 内容规模：约`402` 词
- 章节结构：
  - Executing Plans
  - Overview
  - The Process
  - Step 1: Load and Review Plan
  - Step 2: Execute Batch
  - Step 3: Report
  - Step 4: Continue
  - Step 5: Complete Development
  - When to Stop and Ask for Help
  - When to Revisit Earlier Steps
  - Remember
  - Integration
- 关键规则摘录：
  - Read plan file
  - Review critically - identify any questions or concerns about the plan
  - If concerns: Raise them with your human partner before starting
  - If no concerns: Create TodoWrite and proceed
  - Mark as in_progress
  - Follow each step exactly (plan has bite-sized steps)
  - Run verifications as specified
  - Mark as completed
  - Show what was implemented
  - Show verification output
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 11. finishing-a-development-branch

- Skill 路径：`finishing-a-development-branch/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when implementation is complete, all tests pass, and you need to decide how to integrate the work - guides completion of development work by presenting structured options for merge, PR, or cleanup
- 内容规模：约`641` 词
- 章节结构：
  - Finishing a Development Branch
  - Overview
  - The Process
  - Step 1: Verify Tests
  - Run project's test suite
  - Step 2: Determine Base Branch
  - Try common base branches
  - Step 3: Present Options
  - Step 4: Execute Choice
  - Option 1: Merge Locally
  - Switch to base branch
  - Pull latest
- 关键规则摘录：
  - Merge back to <base-branch> locally
  - Push and create a Pull Request
  - Keep the branch as-is (I'll handle it later)
  - Discard this work
  - [ ] <verification steps>
  - Branch <name>
  - All commits: <commit-list>
  - Worktree at <path>
  - **Problem:** Merge broken code, create failing PR
  - **Fix:** Always verify tests before offering options
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 12. frontend-design

- Skill 路径：`frontend-design/SKILL.md`
- 中文触发解读：在以下场景优先启用：Create distinctive, production-grade frontend interfaces with high design quality. Use this skill when the user asks to build web components, pages, or applications. Generates creative, polished code that avoids generic AI aesthetics.
- 内容规模：约`930` 词
- 章节结构：
  - Design Thinking
  - Workspace-Specific Rules (Mandatory)
  - Frontend Aesthetics Guidelines
- 关键规则摘录：
  - **Purpose**: What problem does this interface solve? Who uses it?
  - **Constraints**: Technical requirements (framework, performance, accessibility).
  - **Differentiation**: What makes this UNFORGETTABLE? What's the one thing someone will remember?
  - **Overall style matching is default**: Unless the user explicitly says style matching is not required, every modified page must match the existing product/page style language.
  - **Page content boundary is mandatory**: Unless explicitly instructed, do not add content, remove content, or change the information structure.
  - **UX-first operation principle is mandatory**: Ensure users can understand quickly, find targets quickly, click with confidence, and complete tasks smoothly.
  - **UX optimization focus is mandatory**: Keep information hierarchy clear, shorten key action paths, make button semantics explicit, and provide timely perceivable feedback.
  - **Efficiency over decoration on complex pages**: Prioritize readability and operability; do not sacrifice practical efficiency for visual effects.
  - **Post-implementation style audit is mandatory**: After coding, re-check all modified pages and UI areas for style coherence with the overall product before completion.
  - Production-grade and functional
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 13. pdf

- Skill 路径：`pdf/SKILL.md`
- 中文触发解读：在以下场景优先启用：PDF manipulation toolkit. Extract text/tables, create PDFs, merge/split, fill forms, for programmatic document processing and analysis.
- 内容规模：约`1005` 词
- 章节结构：
  - PDF Processing Guide
  - Overview
  - Visual Enhancement with Scientific Schematics
  - Quick Start
  - Read a PDF
  - Extract text
  - Python Libraries
  - pypdf - Basic Operations
  - Merge PDFs
  - Split PDF
  - Extract Metadata
  - Rotate Pages
- 关键规则摘录：
  - Use the **scientific-schematics** skill to generate AI-powered publication-quality diagrams
  - Simply describe your desired diagram in natural language
  - Nano Banana Pro will automatically generate, review, and refine the schematic
  - Create publication-quality images with proper formatting
  - Review and refine through multiple iterations
  - Ensure accessibility (colorblind-friendly, high contrast)
  - Save outputs in the figures/ directory
  - PDF processing workflow diagrams
  - Document manipulation flowcharts
  - Form processing visualizations
- 配套资源：
  - `scripts/`（8 个文件）
    - `scripts/check_bounding_boxes.py`
    - `scripts/check_bounding_boxes_test.py`
    - `scripts/check_fillable_fields.py`
    - `scripts/convert_pdf_to_images.py`
    - `scripts/create_validation_image.py`
    - `scripts/extract_form_field_info.py`

### 14. pptx

- Skill 路径：`pptx/SKILL.md`
- 中文触发解读：在以下场景优先启用：Presentation toolkit (.pptx). Create/edit slides, layouts, content, speaker notes, comments, for programmatic presentation creation and modification.
- 内容规模：约`3583` 词
- 章节结构：
  - PPTX creation, editing, and analysis
  - Overview
  - Visual Enhancement with Scientific Schematics
  - Reading and analyzing content
  - Text extraction
  - Convert document to markdown
  - Raw XML access
  - Unpacking a file
  - Key file structures
  - Typography and color extraction
  - Creating a new PowerPoint presentation **without a template**
  - Design Principles
- 关键规则摘录：
  - Use the **scientific-schematics** skill to generate AI-powered publication-quality diagrams
  - Simply describe your desired diagram in natural language
  - Nano Banana Pro will automatically generate, review, and refine the schematic
  - Create publication-quality images with proper formatting
  - Review and refine through multiple iterations
  - Ensure accessibility (colorblind-friendly, high contrast)
  - Save outputs in the figures/ directory
  - Presentation workflow diagrams for slides
  - Slide design process flowcharts
  - Content organization diagrams
- 配套资源：
  - `scripts/`（5 个文件）
    - `scripts/html2pptx.js`
    - `scripts/inventory.py`
    - `scripts/rearrange.py`
    - `scripts/replace.py`
    - `scripts/thumbnail.py`

### 15. ralph-loop

- Skill 路径：`ralph-loop/SKILL.md`
- 中文触发解读：在以下场景优先启用：未在 frontmatter 中声明 description
- 内容规模：约`603` 词
- 章节结构：
  - Ralph Loop Skills
  - What is Ralph?
  - Available Skills
  - Core Skills (Use in Order)
  - Support Skills
  - Quick Start
  - 1. Initialize Ralph in your project
  - 2. Create specs for what you want to build
  - 3. Review the generated specs (IMPORTANT!)
  - Edit specs/features/*.md and specs/implementation-plans/*.md
  - 4. Run the loop
  - Or for attended mode:
- 关键规则摘录：
  - Gives ONE task per context window
  - Exits cleanly after completing that task
  - Starts a fresh context window for the next task
  - Repeats until all tasks are done
  - `- [ ]` = Not done (Ralph picks first one)
  - `- [x]` = Done (Ralph skips these)
  - **Outer Harness:** Your bash script - handles git, evaluation, loop control
  - **Inner Harness:** Claude - handles implementation
  - ~5K: Specs (the Pin)
  - ~2K: Implementation plan
- 配套资源：
  - `templates/`（6 个文件）
    - `templates/implementation-plan.md`
    - `templates/prompt-implement.md`
    - `templates/ralph.sh`
    - `templates/security-checklist.md`
    - `templates/specs-readme.md`
    - `templates/test-wrapper.sh`
  - `skills/`（4 个文件）
    - `skills/implement/SKILL.md`
    - `skills/init/SKILL.md`
    - `skills/interview/SKILL.md`
    - `skills/loop/SKILL.md`

### 16. implement

- Skill 路径：`ralph-loop/skills/implement/SKILL.md`
- 中文触发解读：在以下场景优先启用：未在 frontmatter 中声明 description
- 内容规模：约`1165` 词
- 章节结构：
  - Ralph Implement Skill
  - Purpose
  - When to Use
  - Philosophy
  - Token Budget Awareness
  - How to Use
  - ralph.sh calls this via:
  - Execution Steps
  - Step 1: Read the Pin
  - Step 2: Read the Implementation Plan
  - Step 3: Search for Related Code
  - Database Layer
- 关键规则摘录：
  - Each iteration of the Ralph loop calls this skill
  - Manual attended mode (watching like a fireplace)
  - Testing before running unattended
  - Every spec file
  - Every source file
  - Full directory listings
  - [x] Create types in `src/auth/types.ts`        ← Skip (done)
  - [x] Create database migration                   ← Skip (done)
  - [ ] Create repository in `src/db/auth.ts`      ← THIS IS YOUR TASK
  - [ ] Add validation schemas                      ← Ignore (future)
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 17. init

- Skill 路径：`ralph-loop/skills/init/SKILL.md`
- 中文触发解读：在以下场景优先启用：未在 frontmatter 中声明 description
- 内容规模：约`1156` 词
- 章节结构：
  - Ralph Init Skill
  - Purpose
  - When to Use
  - What This Skill Does
  - How to Use
  - Execution Steps
  - Step 1: Security Pre-Flight
  - Step 2: Create Directory Structure
  - Step 3: Detect Existing Codebase
  - Find source directories
  - Find existing README or docs
  - Find package files
- 关键规则摘录：
  - Starting a new project that will use Ralph loops
  - Adding Ralph to an existing project
  - After cloning a repo that needs Ralph setup
  - **Creates directory structure:**
  - **Installs scripts:**
  - `ralph.sh` - The outer harness loop runner
  - `test-wrapper.sh` - Minimal test output wrapper
  - **Runs security checklist:**
  - Asks about infrastructure (local vs ephemeral VM)
  - Documents blast radius
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 18. interview

- Skill 路径：`ralph-loop/skills/interview/SKILL.md`
- 中文触发解读：在以下场景优先启用：未在 frontmatter 中声明 description
- 内容规模：约`1206` 词
- 章节结构：
  - Ralph Interview Skill
  - Purpose
  - When to Use
  - Philosophy
  - What This Skill Does
  - How to Use
  - Execution Steps
  - Step 1: Load Context
  - Step 2: Conduct the Interview
  - Round 1: Problem Discovery
  - Round 2: Solution Exploration
  - Round 3: Integration Analysis
- 关键规则摘录：
  - Adding a new feature to the project
  - Starting work on a new module
  - Refining an existing feature specification
  - Before any Ralph implementation loop
  - **Conducts a structured interview** about what you want to build
  - **Generates a feature specification** based on your answers
  - **Creates an implementation plan** with atomic tasks
  - **Updates the Pin** (specs/README.md) with the new feature
  - "What problem are you trying to solve?"
  - "Who experiences this problem?"
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 19. loop

- Skill 路径：`ralph-loop/skills/loop/SKILL.md`
- 中文触发解读：在以下场景优先启用：未在 frontmatter 中声明 description
- 内容规模：约`1278` 词
- 章节结构：
  - Ralph Loop Skill
  - Purpose
  - When to Use
  - Philosophy
  - Prerequisites
  - How to Use
  - Execution Modes
  - Mode 1: Attended (Recommended for Starting)
  - Run one iteration manually
  - Review what happened
  - If good, run another
  - If bad, reset and adjust
- 关键规则摘录：
  - Ready to start autonomous implementation
  - Resuming a paused loop
  - Checking loop status
  - Learning how to run Ralph
  - **Security signed off:** `.ralph-security` exists
  - **Specs exist:** `specs/README.md` has content
  - **Plan exists:** `specs/implementation-plans/active-plan.md` has tasks
  - **Tasks remain:** At least one `- [ ]` in the plan
  - **Scripts ready:** `ralph.sh` and `test-wrapper.sh` are executable
  - Notice patterns in model behavior
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 20. receiving-code-review

- Skill 路径：`receiving-code-review/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when receiving code review feedback, before implementing suggestions, especially if feedback seems unclear or technically questionable - requires technical rigor and verification, not performative agreement or blind implementation
- 内容规模：约`953` 词
- 章节结构：
  - Code Review Reception
  - Overview
  - The Response Pattern
  - Forbidden Responses
  - Handling Unclear Feedback
  - UI Style Consistency Feedback
  - Source-Specific Handling
  - From your human partner
  - From External Reviewers
  - YAGNI Check for "Professional" Features
  - Implementation Order
  - When To Push Back
- 关键规则摘录：
  - READ: Complete feedback without reacting
  - UNDERSTAND: Restate requirement in own words (or ask)
  - VERIFY: Check against codebase reality
  - EVALUATE: Technically sound for THIS codebase?
  - RESPOND: Technical acknowledgment or reasoned pushback
  - IMPLEMENT: One item at a time, test each
  - "You're absolutely right!" (explicit CLAUDE.md violation)
  - "Great point!" / "Excellent feedback!" (performative)
  - "Let me implement that now" (before verification)
  - Restate the technical requirement
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 21. requesting-code-review

- Skill 路径：`requesting-code-review/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when completing tasks, implementing major features, or before merging to verify work meets requirements
- 内容规模：约`402` 词
- 章节结构：
  - Requesting Code Review
  - When to Request Review
  - How to Request
  - Example
  - Integration with Workflows
  - Red Flags
- 关键规则摘录：
  - After each task in subagent-driven development
  - After completing major feature
  - Before merge to main
  - For UI/page changes: request review that explicitly checks overall style matching across all modified pages (unless user explicitly waived style matching)
  - For UI/page changes: include fullscreen and non-fullscreen visual consistency in review scope
  - When stuck (fresh perspective)
  - Before refactoring (baseline check)
  - After fixing complex bug
  - `{WHAT_WAS_IMPLEMENTED}` - What you just built
  - `{PLAN_OR_REQUIREMENTS}` - What it should do
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 22. safe-delete-guard

- Skill 路径：`safe-delete-guard/SKILL.md`
- 中文触发解读：在以下场景优先启用：未在 frontmatter 中声明 description
- 内容规模：约`693` 词
- 章节结构：
  - Safe Delete Guard
  - Overview
  - Mandatory Rules
  - Workflow
  - Guardrails
  - Integration with Superpowers
  - References
- 关键规则摘录：
  - Treat these as destructive operations: `rd`, `rmdir`, `del`, `erase`, `Remove-Item`, `rm`, `git clean -fdx`, `docker system prune`, and any equivalent command that removes files.
  - Route every destructive action through `scripts/safe_delete.ps1`.
  - Never bypass preflight with direct shell deletion, including for small cleanup.
  - Do not reject delete requests by keyword only. Run preflight and decide from the result.
  - If preflight returns `BLOCKED`, stop and fix targets/options before execution.
  - If preflight returns `REVIEW`, require explicit large-delete acknowledgement, then continue only after status becomes `READY`.
  - If preflight returns `READY`, continue with confirmation flow.
  - Collect a single combined confirmation reply that includes both confirmation intents:
  - `确认删除 <TOKEN>; 二次确认删除 <TOKEN>`
  - For large-impact deletes: `确认删除 <TOKEN>; 二次确认删除 <TOKEN>; 大删确认 <TOKEN>`
- 配套资源：
  - `scripts/`（1 个文件）
    - `scripts/safe_delete.ps1`
  - `references/`（1 个文件）
    - `references/delete-risk-checklist.md`

### 23. subagent-driven-development

- Skill 路径：`subagent-driven-development/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when executing implementation plans with independent tasks in the current session
- 内容规模：约`1231` 词
- 章节结构：
  - Subagent-Driven Development
  - When to Use
  - The Process
  - Prompt Templates
  - Example Workflow
  - Advantages
  - Red Flags
  - Integration
- 关键规则摘录：
  - Same session (no context switch)
  - Fresh subagent per task (no context pollution)
  - Two-stage review after each task: spec compliance first, then code quality
  - Faster iteration (no human-in-loop between tasks)
  - `./implementer-prompt.md` - Dispatch implementer subagent
  - `./spec-reviewer-prompt.md` - Dispatch spec compliance reviewer subagent
  - `./code-quality-reviewer-prompt.md` - Dispatch code quality reviewer subagent
  - Implemented install-hook command
  - Added tests, 5/5 passing
  - Self-review: Found I missed --force flag, added it
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 24. systematic-debugging

- Skill 路径：`systematic-debugging/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when encountering any bug, test failure, or unexpected behavior, before proposing fixes
- 内容规模：约`1486` 词
- 章节结构：
  - Systematic Debugging
  - Overview
  - The Iron Law
  - When to Use
  - The Four Phases
  - Phase 1: Root Cause Investigation
  - Phase 2: Pattern Analysis
  - Phase 3: Hypothesis and Testing
  - Phase 4: Implementation
  - Red Flags - STOP and Follow Process
  - your human partner's Signals You're Doing It Wrong
  - Common Rationalizations
- 关键规则摘录：
  - Test failures
  - Bugs in production
  - Unexpected behavior
  - Performance problems
  - Build failures
  - Integration issues
  - Under time pressure (emergencies make guessing tempting)
  - "Just one quick fix" seems obvious
  - You've already tried multiple fixes
  - Previous fix didn't work
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 25. test-driven-development

- Skill 路径：`test-driven-development/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when implementing any feature or bugfix, before writing implementation code
- 内容规模：约`1567` 词
- 章节结构：
  - Test-Driven Development (TDD)
  - Overview
  - When to Use
  - The Iron Law
  - Red-Green-Refactor
  - RED - Write Failing Test
  - Verify RED - Watch It Fail
  - GREEN - Minimal Code
  - Verify GREEN - Watch It Pass
  - REFACTOR - Clean Up
  - Repeat
  - Good Tests
- 关键规则摘录：
  - New features
  - Bug fixes
  - Refactoring
  - Behavior changes
  - UI/page layout changes (must include failing tests/checks for fullscreen + non-fullscreen consistency)
  - UI/page style changes (must include failing checks for overall style consistency with existing pages unless user explicitly waives matching)
  - Throwaway prototypes
  - Generated code
  - Configuration files
  - Don't keep it as "reference"
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 26. ui-ux-pro-max

- Skill 路径：`ui-ux-pro-max/SKILL.md`
- 中文触发解读：在以下场景优先启用：UI/UX design intelligence. 50 styles, 21 palettes, 50 font pairings, 20 charts, 9 stacks (React, Next.js, Vue, Svelte, SwiftUI, React Native, Flutter, Tailwind, shadcn/ui). Actions: plan, build, create, design, implement, review, fix, improve, optimize, enhance, refactor, check UI/UX code. Projects: website, landing page, dashboard, admin panel, e-commerce, SaaS, portfolio, blog, mobile app, .html, .tsx, .vue, .svelte. Elements: button, modal, navbar, sidebar, card, table, form, chart. Styles: glassmorphism, claymorphism, minimalism, brutalism, neumorphism, bento grid, dark mode, responsive, skeuomorphism, flat design. Topics: color palette, accessibility, animation, layout, typography, font pairing, spacing, hover, shadow, gradient. Integrations: shadcn/ui MCP for component search and examples.
- 内容规模：约`2461` 词
- 章节结构：
  - UI/UX Pro Max - Design Intelligence
  - When to Apply
  - Workspace-Specific Rules (Mandatory)
  - Rule Categories by Priority
  - Quick Reference
  - 1. Accessibility (CRITICAL)
  - 2. Touch & Interaction (CRITICAL)
  - 3. Performance (HIGH)
  - 4. Layout & Responsive (HIGH)
  - 5. Typography & Color (MEDIUM)
  - 6. Animation (MEDIUM)
  - 7. Style Selection (MEDIUM)
- 关键规则摘录：
  - Designing new UI components or pages
  - Choosing color palettes and typography
  - Reviewing code for UX issues
  - Building landing pages or dashboards
  - Implementing accessibility requirements
  - **Document adaptations explicitly**: Before implementation, state what was kept from the reference and what was adapted for usability, business context, or technical constraints.
  - **Overall style matching is required by default**: Unless the user explicitly says matching is unnecessary, all modified pages must align with the existing product visual language.
  - **Page content boundary is mandatory**: Without explicit instruction, do not add content, remove content, or alter page information architecture.
  - **UX-first usability principle is mandatory**: Ensure users can quickly understand, locate, click, and complete operations.
  - **UX focus areas are mandatory**: Keep hierarchy clear, shorten key action paths, use explicit button semantics, and provide timely perceivable feedback.
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 27. using-git-worktrees

- Skill 路径：`using-git-worktrees/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when starting feature work that needs isolation from current workspace or before executing implementation plans - creates isolated git worktrees with smart directory selection and safety verification
- 内容规模：约`751` 词
- 章节结构：
  - Using Git Worktrees
  - Overview
  - Directory Selection Process
  - 1. Check Existing Directories
  - Check in priority order
  - 2. Check CLAUDE.md
  - 3. Ask User
  - Safety Verification
  - For Project-Local Directories (.worktrees or worktrees)
  - Check if directory is ignored (respects local, global, and system gitignore)
  - For Global Directory (~/.config/superpowers/worktrees)
  - Creation Steps
- 关键规则摘录：
  - .worktrees/ (project-local, hidden)
  - ~/.config/superpowers/worktrees/<project-name>/ (global location)
  - Add appropriate line to .gitignore
  - Commit the change
  - Proceed with worktree creation
  - **Problem:** Worktree contents get tracked, pollute git status
  - **Fix:** Always use `git check-ignore` before creating project-local worktree
  - **Problem:** Creates inconsistency, violates project conventions
  - **Fix:** Follow priority: existing > CLAUDE.md > ask
  - **Problem:** Can't distinguish new bugs from pre-existing issues
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 28. using-superpowers

- Skill 路径：`using-superpowers/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when starting any conversation - establishes how to find and use skills, requiring Skill tool invocation before ANY response including clarifying questions
- 内容规模：约`1317` 词
- 章节结构：
  - How to Access Skills
  - Using Skills
  - The Rule
  - Mandatory Deletion Intercept
  - Mandatory UI Layout Consistency Guard
  - Mandatory Page Content Change Boundary Guard
  - Mandatory UX Practicality Guard
  - Mandatory Chinese Comment Density and Encoding Guard
  - Mandatory File Reference Link Guard
  - Red Flags
  - Skill Priority
  - Skill Types
- 强约束片段：
  - `EXTREMELY-IMPORTANT`: If you think there is even a 1% chance a skill might apply to what you are doing, you ABSOLUTELY MUST invoke the skill. IF A SKILL APPLIES TO YOUR TASK, YOU DO NOT HAVE A CHOICE. YOU MUST USE IT. This is not negotiable. This is not optional. You cannot rationalize your way out of this.
- 关键规则摘录：
  - Invoke `safe-delete-guard` before drafting shell commands.
  - Route all destructive actions through `safe-delete-guard/scripts/safe_delete.ps1`.
  - Never run raw destructive commands directly (`rm`, `Remove-Item`, `del`, `rd`, `git clean`, `docker system prune`, etc.).
  - If `safe-delete-guard` is unavailable, do not run raw delete commands; first establish an equivalent preflight + confirmation guard.
  - Treat fullscreen and non-fullscreen visual consistency as a hard requirement.
  - Before claiming completion, verify there are no unexplained blank regions, overflow artifacts, clipping, overlap, or detached panels in either mode.
  - If either mode is broken, continue fixing; do not report completion.
  - Only allow free-form redesign when the user explicitly states style matching is not required.
  - After implementation, run a style-consistency pass for all modified pages and fix mismatches before completion.
  - Unless the user explicitly instructs it, do not add content, delete content, or alter the page information architecture.
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 29. verification-before-completion

- Skill 路径：`verification-before-completion/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when about to claim work is complete, fixed, or passing, before committing or creating PRs - requires running verification commands and confirming output before making any success claims; evidence before assertions always
- 内容规模：约`745` 词
- 章节结构：
  - Verification Before Completion
  - Overview
  - The Iron Law
  - The Gate Function
  - Common Failures
  - Red Flags - STOP
  - Rationalization Prevention
  - Key Patterns
  - Why This Matters
  - When To Apply
  - The Bottom Line
- 关键规则摘录：
  - IDENTIFY: What command proves this claim?
  - RUN: Execute the FULL command (fresh, complete)
  - READ: Full output, check exit code, count failures
  - VERIFY: Does output confirm the claim?
  - If NO: State actual status with evidence
  - If YES: State claim WITH evidence
  - ONLY THEN: Make the claim
  - Using "should", "probably", "seems to"
  - Expressing satisfaction before verification ("Great!", "Perfect!", "Done!", etc.)
  - About to commit/push/PR without verification
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 30. vue-best-practices

- Skill 路径：`vue-best-practices/SKILL.md`
- 中文触发解读：在以下场景优先启用：MUST be used for Vue.js tasks. Strongly recommends Composition API with `<script setup>` and TypeScript as the standard approach. Covers Vue 3, SSR, Volar, vue-tsc. Load for any Vue, .vue files, Vue Router, Pinia, or Vite with Vue work. ALWAYS use Composition API unless the project explicitly requires Options API.
- 内容规模：约`1476` 词
- 章节结构：
  - Vue Best Practices Workflow
  - Core Principles
  - 0) Design alignment before coding (required for UI/page tasks)
  - 1) Confirm architecture before coding (required)
  - 1.1 Must-read core references (required)
  - 1.2 Plan component boundaries before coding (required)
  - 2) Apply essential Vue foundations (required)
  - Reactivity
  - SFC structure and template safety
  - Keep components focused
  - Component data flow
  - Composables
- 关键规则摘录：
  - **Keep state predictable:** one source of truth, derive everything else.
  - **Make data flow explicit:** Props down, Events up for most cases.
  - **Favor small, focused components:** easier to test, reuse, and maintain.
  - **Avoid unnecessary re-renders:** use computed properties and watchers wisely.
  - **Readability counts:** write clear, self-documenting code.
  - If the user provides style/layout references, align the visual language, layout rhythm, and hierarchy first.
  - Similarity of structure and experience is required, but exact visual cloning is not required.
  - Design from user goals and task flows, not from component aesthetics alone.
  - Explicitly support fast browse/read flows and clear CRUD actions (create/add, edit/update, delete).
  - Before coding, state what is kept from the reference and what is intentionally adapted.
- 配套资源：
  - `references/`（22 个文件）
    - `references/animation-class-based-technique.md`
    - `references/animation-state-driven-technique.md`
    - `references/component-async.md`
    - `references/component-data-flow.md`
    - `references/component-fallthrough-attrs.md`
    - `references/component-keep-alive.md`

### 31. vue-debug-guides

- Skill 路径：`vue-debug-guides/SKILL.md`
- 中文触发解读：在以下场景优先启用：Vue 3 debugging and error handling for runtime errors, warnings, async failures, and SSR/hydration issues. Use when diagnosing or fixing Vue issues.
- 内容规模：约`1658` 词
- 章节结构：
  - Reactivity
  - Computed
  - Watchers
  - Components
  - Props & Emits
  - Templates
  - Template Refs
  - Forms & v-model
  - Events & Modifiers
  - Lifecycle
  - Slots
  - Provide/Inject
- 关键规则摘录：
  - Tracing unexpected re-renders and state updates → See [reactivity-debugging-hooks](reference/reactivity-debugging-hooks.md)
  - Ref values not updating due to missing .value access → See [ref-value-access](reference/ref-value-access.md)
  - State stops updating after destructuring reactive objects → See [reactive-destructuring](reference/reactive-destructuring.md)
  - Refs inside arrays, Maps, or Sets not unwrapping → See [refs-in-collections-need-value](reference/refs-in-collections-need-value.md)
  - Nested refs rendering as [object Object] in templates → See [template-ref-unwrapping-top-level](reference/template-ref-unwrapping-top-level.md)
  - Reactive proxy identity comparisons always return false → See [reactivity-proxy-identity-hazard](reference/reactivity-proxy-identity-hazard.md)
  - Third-party instances breaking when proxied → See [reactivity-markraw-for-non-reactive](reference/reactivity-markraw-for-non-reactive.md)
  - Watchers only firing once per tick unexpectedly → See [reactivity-same-tick-batching](reference/reactivity-same-tick-batching.md)
  - Computed getter triggers mutations or requests unexpectedly → See [computed-no-side-effects](reference/computed-no-side-effects.md)
  - Mutating computed values causes changes to disappear → See [computed-return-value-readonly](reference/computed-return-value-readonly.md)
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 32. vue-jsx-best-practices

- Skill 路径：`vue-jsx-best-practices/SKILL.md`
- 中文触发解读：在以下场景优先启用：JSX syntax in Vue (e.g., class vs className, JSX plugin config).
- 内容规模：约`26` 词
- 章节结构：
  - JSX
- 关键规则摘录：
  - Migrating React JSX code to Vue or getting attribute type errors → See [render-function-jsx-vue-vs-react](reference/render-function-jsx-vue-vs-react.md)
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 33. vue-options-api-best-practices

- Skill 路径：`vue-options-api-best-practices/SKILL.md`
- 中文触发解读：在以下场景优先启用：Vue 3 Options API style (data(), methods, this context). Each reference shows Options API solution only.
- 内容规模：约`130` 词
- 章节结构：
  - TypeScript
  - Methods & Lifecycle
- 关键规则摘录：
  - Need to enable TypeScript type inference for component properties → See [ts-options-api-use-definecomponent](reference/ts-options-api-use-definecomponent.md)
  - Enabling type safety for Options API this context → See [ts-strict-mode-options-api](reference/ts-strict-mode-options-api.md)
  - Using old TypeScript versions with prop validators → See [ts-options-api-arrow-functions-validators](reference/ts-options-api-arrow-functions-validators.md)
  - Event handler parameters need proper type safety → See [ts-options-api-type-event-handlers](reference/ts-options-api-type-event-handlers.md)
  - Need to type object or array props with interfaces → See [ts-options-api-proptype-complex-types](reference/ts-options-api-proptype-complex-types.md)
  - Injected properties missing TypeScript types completely → See [ts-options-api-provide-inject-limitations](reference/ts-options-api-provide-inject-limitations.md)
  - Complex computed properties lack clear type documentation → See [ts-options-api-computed-return-types](reference/ts-options-api-computed-return-types.md)
  - Methods aren't binding to component instance context → See [no-arrow-functions-in-methods](reference/no-arrow-functions-in-methods.md)
  - Lifecycle hooks losing access to component data → See [no-arrow-functions-in-lifecycle-hooks](reference/no-arrow-functions-in-lifecycle-hooks.md)
  - Debounced functions sharing state across component instances → See [stateful-methods-lifecycle](reference/stateful-methods-lifecycle.md)
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 34. vue-pinia-best-practices

- Skill 路径：`vue-pinia-best-practices/SKILL.md`
- 中文触发解读：在以下场景优先启用：Pinia stores, state management patterns, store setup, and reactivity with stores.
- 内容规模：约`84` 词
- 章节结构：
  - Store Setup
  - Reactivity
  - State Patterns
- 关键规则摘录：
  - Getting "getActivePinia was called" error at startup → See [pinia-no-active-pinia-error](reference/pinia-no-active-pinia-error.md)
  - Setup stores missing state in DevTools or SSR → See [pinia-setup-store-return-all-state](reference/pinia-setup-store-return-all-state.md)
  - Store destructuring stops updating UI reactively → See [pinia-store-destructuring-breaks-reactivity](reference/pinia-store-destructuring-breaks-reactivity.md)
  - Store methods lose context in template calls → See [store-method-binding-parentheses](reference/store-method-binding-parentheses.md)
  - Filters reset on refresh or can't be shared → See [state-url-for-ephemeral-filters](reference/state-url-for-ephemeral-filters.md)
  - Building production app without DevTools or conventions → See [state-use-pinia-for-large-apps](reference/state-use-pinia-for-large-apps.md)
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 35. vue-router-best-practices

- Skill 路径：`vue-router-best-practices/SKILL.md`
- 中文触发解读：在以下场景优先启用：Vue Router 4 patterns, navigation guards, route params, and route-component lifecycle interactions.
- 内容规模：约`192` 词
- 章节结构：
  - Design-first routing alignment (for page design tasks)
  - Navigation Guards
  - Route Lifecycle
  - Setup
- 关键规则摘录：
  - If the user provides layout/style references, route structure should support a similar information flow and page hierarchy.
  - Exact visual matching is not required, but browse paths should feel consistent with the reference pattern.
  - Build routes from user perspective: prioritize easy discovery, predictable back/forward behavior, and low-friction navigation.
  - For CRUD-style products, prefer explicit route semantics and flows:
  - list/browse view
  - create/new view
  - detail/read view
  - edit/update view
  - Keep delete flows safe and explicit with confirmation handling and clear return paths.
  - Navigating between same route with different params → See [router-beforeenter-no-param-trigger](reference/router-beforeenter-no-param-trigger.md)
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 36. vue-testing-best-practices

- Skill 路径：`vue-testing-best-practices/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use for Vue.js testing. Covers Vitest, Vue Test Utils, component testing, mocking, testing patterns, and Playwright for E2E testing.
- 内容规模：约`227` 词
- 章节结构：
  - Testing
  - Fullscreen Layout Consistency (Required for UI Pages)
  - Reference
- 关键规则摘录：
  - Setting up test infrastructure for Vue 3 projects → See [testing-vitest-recommended-for-vue](reference/testing-vitest-recommended-for-vue.md)
  - Tests keep breaking when refactoring component internals → See [testing-component-blackbox-approach](reference/testing-component-blackbox-approach.md)
  - Tests fail intermittently with race conditions → See [testing-async-await-flushpromises](reference/testing-async-await-flushpromises.md)
  - Composables using lifecycle hooks or inject fail to test → See [testing-composables-helper-wrapper](reference/testing-composables-helper-wrapper.md)
  - Getting "injection Symbol(pinia) not found" errors in tests → See [testing-pinia-store-setup](reference/testing-pinia-store-setup.md)
  - Components with async setup won't render in tests → See [testing-suspense-async-components](reference/testing-suspense-async-components.md)
  - Snapshot tests keep passing despite broken functionality → See [testing-no-snapshot-only](reference/testing-no-snapshot-only.md)
  - Choosing end-to-end testing framework for Vue apps → See [testing-e2e-playwright-recommended](reference/testing-e2e-playwright-recommended.md)
  - Tests need to verify computed styles or real DOM events → See [testing-browser-vs-node-runners](reference/testing-browser-vs-node-runners.md)
  - Testing components created with defineAsyncComponent fails → See [async-component-testing](reference/async-component-testing.md)
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 37. writing-plans

- Skill 路径：`writing-plans/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when you have a spec or requirements for a multi-step task, before touching code
- 内容规模：约`533` 词
- 章节结构：
  - Writing Plans
  - Overview
  - Bite-Sized Task Granularity
  - Plan Document Header
  - [Feature Name] Implementation Plan
  - Task Structure
  - Task N: [Component Name]
  - Remember
  - Execution Handoff
- 关键规则摘录：
  - "Write the failing test" - step
  - "Run it to make sure it fails" - step
  - "Implement the minimal code to make the test pass" - step
  - "Run the tests and make sure they pass" - step
  - "Commit" - step
  - Create: `exact/path/to/file.py`
  - Modify: `exact/path/to/existing.py:123-145`
  - Test: `tests/exact/path/to/test.py`
  - Exact file paths always
  - Complete code in plan (not "add validation")
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 38. writing-skills

- Skill 路径：`writing-skills/SKILL.md`
- 中文触发解读：在以下场景优先启用：Use when creating new skills, editing existing skills, or verifying skills work before deployment
- 内容规模：约`3185` 词
- 章节结构：
  - Writing Skills
  - Overview
  - What is a Skill?
  - TDD Mapping for Skills
  - When to Create a Skill
  - Skill Types
  - Technique
  - Pattern
  - Reference
  - Directory Structure
  - SKILL.md Structure
  - Skill Name
- 关键规则摘录：
  - Technique wasn't intuitively obvious to you
  - You'd reference this again across projects
  - Pattern applies broadly (not project-specific)
  - Others would benefit
  - One-off solutions
  - Standard practices well-documented elsewhere
  - Project-specific conventions (put in CLAUDE.md)
  - Mechanical constraints (if it's enforceable with regex/validation, automate it—save documentation for judgment calls)
  - **Heavy reference** (100+ lines) - API docs, comprehensive syntax
  - **Reusable tools** - Scripts, utilities, templates
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

### 39. xlsx

- Skill 路径：`xlsx/SKILL.md`
- 中文触发解读：在以下场景优先启用：Spreadsheet toolkit (.xlsx/.csv). Create/edit with formulas/formatting, analyze data, visualization, recalculate formulas, for spreadsheet processing and analysis.
- 内容规模：约`1595` 词
- 章节结构：
  - Requirements for Outputs
  - All Excel files
  - Zero Formula Errors
  - Preserve Existing Templates (when updating templates)
  - Financial models
  - Color Coding Standards
  - Industry-Standard Color Conventions
  - Number Formatting Standards
  - Required Format Rules
  - Formula Construction Rules
  - Assumptions Placement
  - Formula Error Prevention
- 关键规则摘录：
  - Every Excel model MUST be delivered with ZERO formula errors (#REF!, #DIV/0!, #VALUE!, #N/A, #NAME?)
  - Study and EXACTLY match existing format, style, and conventions when modifying files
  - Never impose standardized formatting on files with established patterns
  - Existing template conventions ALWAYS override these guidelines
  - **Blue text (RGB: 0,0,255)**: Hardcoded inputs, and numbers users will change for scenarios
  - **Black text (RGB: 0,0,0)**: ALL formulas and calculations
  - **Green text (RGB: 0,128,0)**: Links pulling from other worksheets within same workbook
  - **Red text (RGB: 255,0,0)**: External links to other files
  - **Yellow background (RGB: 255,255,0)**: Key assumptions needing attention or cells that need to be updated
  - **Years**: Format as text strings (e.g., "2024" not "2,024")
- 配套资源：未发现 `scripts/`、`references/`、`assets/`、`templates/`、`skills/` 子目录

## 如何在新机器快速复用这份文档

1. 复制当前 `~/.codex/skills` 到新机器。
2. 在新机器运行同样的生成脚本（或把本文档一起拷过去）。
3. 每次新增/修改 skill 后，重新生成文档，保证文档与技能目录一致。
