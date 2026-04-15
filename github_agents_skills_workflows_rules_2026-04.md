# GitHub 开源 Agent / Skill / Workflow / 规则约束汇总（2026-04）

> 目标：给技术选型、团队规范、个人效率工具建设提供一份可直接落地的清单。  
> 范围：优先收录 GitHub 上活跃、知名、适用于不同场景的开源或源码可见项目；同时补充常见的规则文件、技能标准、约束模式与实践建议。  
> 说明：本文更偏“工程选型与规则设计”，不是学术论文综述。

---

## 1. 先看结论

如果你只想快速选型，可以先按下面这张表：

| 场景 | 优先看 | 为什么 |
|---|---|---|
| Python 代码式单/多 Agent 编排 | `langchain-ai/langgraph`、`openai/openai-agents-python`、`pydantic/pydantic-ai` | LangGraph 强状态流与长流程；OpenAI Agents SDK 偏轻量多 Agent 工作流；PydanticAI 类型约束和工程体验强 |
| 多角色协作 / 多 Agent 分工 | `crewAIInc/crewai`、`microsoft/agent-framework` | 角色协作清晰，适合研究、运营、企业助手等分工场景 |
| TypeScript / Node 生态 | `mastra-ai/mastra`、`langchain-ai/langgraphjs` | TS 友好，适合 Web 团队 |
| 可视化工作流 / 低代码搭建 | `langgenius/dify`、`FlowiseAI/Flowise`、`n8n-io/n8n` | 适合业务团队、POC、内部平台 |
| 浏览器自动化 / 网页代办 | `browser-use/browser-use`、`simular-ai/Agent-S` | 面向网页操作与 GUI 自动化 |
| 软件开发 Agent / 代码修复 | `OpenHands/OpenHands`、`openai/codex` | 偏“AI 工程师”与仓库协作 |
| 统一工具接入 / 外部系统能力扩展 | MCP 生态、`punkpeye/awesome-mcp-servers` | 适合把数据库、文件、GitHub、Slack 等能力统一暴露给 Agent |
| 规则 / 指令文件规范化 | `agentsmd/agents.md`、`CLAUDE.md`、Cursor Rules、Copilot Instructions | 适合团队把 AI 协作方式固化成仓库约定 |
| 可复用 Skill 资产建设 | Agent Skills 标准、`VoltAgent/awesome-agent-skills`、`heilcheng/awesome-agent-skills` | 适合沉淀跨项目可复用能力包 |

---

## 2. 概念先统一：Skill、Agent、Workflow、Rules 分别是什么

### 2.1 Skill
**Skill** 是一个可复用的能力包，通常包含：
- 适用场景描述
- 操作步骤
- 可选脚本/模板/示例
- 让 Agent 何时调用它的提示

常见形态是一个目录加一个 `SKILL.md` 文件。它更像“按需加载的专业知识与流程卡片”。

**适合：**
- 某类固定任务反复出现
- 你不想把长提示词塞进系统提示
- 你希望跨项目复用

---

### 2.2 Agent
**Agent** 是会“感知上下文、调用工具、做多步决策并执行任务”的主体。  
和单轮问答不同，Agent 通常会：
- 规划步骤
- 查资料或读代码
- 调工具
- 根据中间结果调整路径
- 输出结果或继续执行

**适合：**
- 任务不是一步完成
- 需要工具调用
- 需要多轮、多文件、多系统操作

---

### 2.3 Workflow
**Workflow** 是把任务拆成稳定步骤和分支条件后的执行流。  
常见形式有：
- DAG / 图编排
- 状态机
- 审批流 / HITL（human-in-the-loop）
- 多 Agent 接力
- 事件驱动自动化

**适合：**
- 过程比单点智能更重要
- 需要可靠、可回放、可观测
- 需要接企业系统

---

### 2.4 Rules / Constraints
**Rules / Constraints** 是给 Agent 的长期约束和边界。  
它们决定：
- 什么能做、什么不能做
- 遵循什么代码规范
- 修改前后要跑什么命令
- 什么场景必须人工确认
- 输出格式如何统一

**适合：**
- 团队协作
- 降低 agent 漂移
- 限制高风险动作
- 提升产出一致性

---

## 3. 按场景汇总 GitHub 上值得关注的项目

## 3.1 通用 Agent 编排框架（代码优先）

### 3.1.1 `langchain-ai/langgraph`
**定位：** 面向长流程、状态化、可恢复 Agent 的低层编排框架。  
**特点：**
- 图式编排
- 强状态管理
- 支持循环、分支、检查点
- 适合长任务、复杂多步任务、人工介入

**适合场景：**
- 企业级客服流程
- 复杂审批流
- 需要 checkpoint / resume 的任务
- 要控制 agent 行为路径

**不太适合：**
- 只想快速做一个轻量聊天助手
- 团队不想处理较多工程细节

---

### 3.1.2 `openai/openai-agents-python`
**定位：** 轻量但完整的多 Agent 工作流 SDK。  
**特点：**
- 支持多 Agent workflow
- Provider-agnostic
- 集成 tracing
- 比较适合从“模型调用”往“agent workflow”升级

**适合场景：**
- 你已经有 Python 服务
- 想快速做可组合 Agent
- 想保留较高代码控制权

**不太适合：**
- 纯低代码团队
- 完全不写代码的业务方

---

### 3.1.3 `pydantic/pydantic-ai`
**定位：** 强类型、工程友好的 Python Agent 框架。  
**特点：**
- Pydantic 风格体验
- 数据结构约束强
- 对输入输出 schema 友好
- 适合“要工程严谨性”的团队

**适合场景：**
- 需要严格结构化输出
- 要把 Agent 接进后端服务
- 要减少脏输出和解析失败

**不太适合：**
- 更偏视觉化搭建的团队

---

### 3.1.4 `microsoft/agent-framework`
**定位：** 面向 Python / .NET 的 Agent 与多 Agent workflow 框架。  
**特点：**
- 微软系生态友好
- 强调构建、编排、部署
- 适合企业集成场景

**适合场景：**
- 企业内部平台
- .NET / Azure / Python 混合环境
- 需要多 Agent 协作与长期维护

---

### 3.1.5 `agentscope-ai/agentscope`
**定位：** 偏生产化、强调易用与可扩展的 Agent 框架。  
**特点：**
- 强调 production-ready
- 关注 rising model capability
- 不想把模型死死锁在过度僵化的 prompt 流里

**适合场景：**
- 想保留模型自主推理空间
- 想构建更偏“智能体原生”的系统

---

## 3.2 多 Agent 协作与角色分工

### 3.2.1 `crewAIInc/crewai`
**定位：** 角色化、多 Agent 协作框架。  
**特点：**
- 把 Agent 看成“有角色、有任务、有工具”的成员
- 适合 analyst / writer / reviewer / planner 分工
- 上手直观

**适合场景：**
- 调研、写作、运营方案
- 多角色接力
- 想快速验证多 Agent 价值

**常见坑：**
- 角色太多时成本和路径会膨胀
- 需要补规则，不然容易“热闹但不稳”

---

### 3.2.2 `microsoft/autogen`
**定位：** 经典多 Agent 框架。  
**说明：**
- 仍然很有影响力
- 但官方仓库已明确进入 **maintenance mode**
- 更适合学习其设计思想，不建议把新项目长期押在其未来路线图上

**适合场景：**
- 学习多 Agent 对话/协作模式
- 复用已有 AutoGen 存量系统

---

## 3.3 TypeScript / Node 场景

### 3.3.1 `mastra-ai/mastra`
**定位：** 面向 TypeScript 的现代 AI 应用与 Agent 框架。  
**特点：**
- TS 生态友好
- 支持多 provider 路由
- 适合前后端一体团队

**适合场景：**
- Node/TS 团队
- 希望快速集成到 Web 产品
- 想兼顾工作流、工具、记忆等能力

---

### 3.3.2 `langchain-ai/langgraphjs`
**定位：** LangGraph 的 JS/TS 版本。  
**特点：**
- 保留 LangGraph 状态图思想
- 适合已有 JS/TS 技术栈团队

**适合场景：**
- Web 平台
- Next.js / Node 服务
- 希望 workflow 可控

---

## 3.4 可视化 / 低代码 / 工作流平台

### 3.4.1 `langgenius/dify`
**定位：** 面向生产环境的 Agentic workflow 平台。  
**特点：**
- 可视化编排
- 应用、知识库、workflow、日志等能力完整
- 适合内部 AI 平台或业务团队共建

**适合场景：**
- 内部知识问答
- 业务助手
- 快速做 demo 到 production 的过渡
- 产品经理/运营也要参与搭建

---

### 3.4.2 `FlowiseAI/Flowise`
**定位：** 可视化搭建 AI Agent。  
**特点：**
- 上手快
- 图形化搭建体验强
- 很适合做 PoC 和内部工具

**适合场景：**
- 快速实验
- 需要面向非纯研发用户
- 可视化流程演示

**注意：**
- 仓库包含商业许可目录，严格说是“开源 + 商业部分并存”的结构，不是完全纯开源一刀切。

---

### 3.4.3 `n8n-io/n8n`
**定位：** 通用自动化平台，带原生 AI 能力。  
**特点：**
- 400+ integrations
- AI 节点和传统自动化并存
- 很适合“AI + 企业系统自动化”组合

**适合场景：**
- 邮件、表单、CRM、审批、通知等系统联动
- AI 只负责复杂理解，普通逻辑仍走传统节点
- 自动化团队或平台团队

**注意：**
- `n8n` 是 fair-code，不等同于传统 OSI 意义上的完全开源。

---

## 3.5 浏览器 / GUI / 操作型 Agent

### 3.5.1 `browser-use/browser-use`
**定位：** 让 AI agent 更容易操作网站。  
**特点：**
- 面向网页自动化
- 强调把网站“变成 agent 可用接口”
- 适合表单、后台系统、网页任务执行

**适合场景：**
- 网页填报
- 后台运营操作
- 多站点自动化
- 抓取 + 操作混合流程

**注意：**
- 这类项目天然要重点加“权限、确认、幂等、回滚、审计”。

---

### 3.5.2 `simular-ai/Agent-S`
**定位：** 面向 GUI / 电脑交互的开放 agentic framework。  
**特点：**
- 强调像人一样使用电脑
- 面向 Agent-Computer Interface

**适合场景：**
- 桌面 GUI 自动化
- 跨应用协同
- 实验型操作代理

---

## 3.6 软件开发 Agent / 代码型 Agent

### 3.6.1 `OpenHands/OpenHands`
**定位：** 软件开发 Agent 平台。  
**特点：**
- 能改代码、跑命令、浏览网页、调用 API
- 面向“AI 软件工程师”式任务

**适合场景：**
- Bug 修复
- 自动提 PR
- Issue 到 patch
- 仓库级自动化

**注意：**
- 企业部分为 source-available，不是全部都按常规开源方式可自由商用。

---

### 3.6.2 `openai/codex`
**定位：** 本地运行的 coding agent / CLI。  
**特点：**
- 强调本地计算机上的代码协作
- 仓库里直接使用 `AGENTS.md`
- 适合命令行工作流

**适合场景：**
- 本地代码库开发
- 终端协作
- 把仓库规范显式写给 agent

---

## 3.7 MCP 生态与工具接入

### 3.7.1 MCP（Model Context Protocol）
**定位：** 把外部系统能力标准化暴露给 AI。  
**常见对象：**
- 文件系统
- 数据库
- GitHub
- Slack
- 日历
- 浏览器
- 内部 API

**适合场景：**
- 给多个 agent / IDE / chat 产品统一接入工具
- 降低每个产品单独做工具适配的成本

---

### 3.7.2 `punkpeye/awesome-mcp-servers`
**定位：** MCP Server 的知名汇总仓库。  
**适合：**
- 找现成 MCP server
- 了解生态覆盖面
- 快速验证某类工具是否已有实现

---

## 3.8 Skill / 提示 / 规则资源库

### 3.8.1 `VoltAgent/awesome-agent-skills`
**定位：** 大型 Agent Skills 汇总库。  
**适合：**
- 找现成 Skill
- 参考目录结构
- 设计自己的 skill 资产库

---

### 3.8.2 `heilcheng/awesome-agent-skills`
**定位：** 社区整理的 agent skills 清单。  
**适合：**
- 对比不同 skill 写法
- 学习跨工具兼容的 skill 组织方式

---

### 3.8.3 `bonigarcia/context-engineering`
**定位：** 偏结构化 prompt / context engineering 参考库。  
**适合：**
- 设计 prompt 模板
- 比较不同框架的上下文组织方式

---

### 3.8.4 `steipete/agent-rules`
**定位：** 面向 Claude Code / Cursor 等工具的规则与知识整理。  
**适合：**
- 参考规则写法
- 看真实工程团队如何管理 agent 规则

---

## 4. 常见“有名的规则和约束”体系

## 4.1 `AGENTS.md`
**是什么：**
- 一个给 coding agents 的开放格式
- 类似“给 agent 的 README”

**适合放什么：**
- 安装/构建/测试命令
- 代码风格
- 修改边界
- 审核要求
- 风险约束
- 提交规范

**优点：**
- 通用、简单、可读
- 适合跨工具共享
- 很适合仓库根目录放一个总纲

**建议：**
- 根目录放总纲
- 子目录按模块放局部 `AGENTS.md`
- 靠近代码的位置放局部约束

---

## 4.2 `CLAUDE.md`
**是什么：**
- Claude Code 的持久项目记忆文件
- 用来给 Claude 提供长期项目上下文

**适合放什么：**
- 项目结构
- 常用命令
- 测试方式
- 本仓库约定
- 常犯错误提醒
- 关键路径文件

**优点：**
- 与 Claude Code 配合自然
- 适合持续纠偏
- 可与 auto memory 配合

**建议：**
- 写项目最重要的 20% 信息
- 不要把整本编码规范硬塞进去
- 多引用“规范文件位置”，少复制规范全文

---

## 4.3 Cursor Rules（`.cursor/rules/*.mdc`）
**是什么：**
- Cursor 的持久规则系统

**常见模式：**
- Always Apply
- Apply Intelligently
- Apply to Specific Files
- Apply Manually

**适合放什么：**
- 代码风格核心约束
- 文件级模式
- 构建/测试命令
- 提交前检查
- 某些文件夹专属规范

**建议：**
- 规则短而关键
- 不要替代 linter / formatter
- 对路径敏感规则单独拆文件

---

## 4.4 GitHub Copilot Custom Instructions / AGENTS.md / Skills
**是什么：**
GitHub Copilot 现在支持多层自定义：
- `.github/copilot-instructions.md`
- `.github/instructions/**/*.instructions.md`
- `AGENTS.md`
- `.github/agents/*.agent.md`
- agent skills

**适合：**
- 仓库级说明
- 路径级说明
- 为 cloud agent 或 CLI 定义更专业的行为
- 用 skill 做任务级能力扩展

**建议：**
- 简单通用规范放 custom instructions
- 复杂专项能力放 skill
- 更长流程角色化定义放 custom agent

---

## 4.5 Agent Skills（开放 Skill 标准）
**是什么：**
- 一个开放的 skill 规范
- 基本形态是目录 + `SKILL.md`
- 可包含元数据、说明、脚本、模板

**适合：**
- 可移植能力包
- 跨项目复用
- 跨工具共享

**适合收敛的内容：**
- PR 审查
- 数据分析
- 安全检查
- 发布流程
- 文档生成
- 事故排查
- API 设计评审

---

## 4.6 MCP 作为“能力约束层”
严格说 MCP 不是规则文件，但在工程上它常被拿来做“能力边界层”：
- 只暴露有限工具
- 给工具做鉴权
- 对高危操作强制确认
- 让 agent 只能通过标准接口触发动作

这比把所有权限直接暴露给 shell 更安全，也更便于审计。

---

## 5. 不同场景下该怎么选

## 5.1 个人开发者 / 本地 coding agent
**推荐组合：**
- `openai/codex` 或 Claude Code / Copilot CLI
- 根目录 `AGENTS.md`
- 局部模块 `AGENTS.md`
- 额外补 `SKILL.md`

**重点：**
- 命令要准确
- 改代码后必须验证
- 把“不要做什么”写清楚

---

## 5.2 团队代码仓库协作
**推荐组合：**
- 根目录：总 `AGENTS.md`
- 模块目录：局部 `AGENTS.md`
- Copilot：`.github/copilot-instructions.md`
- Cursor：`.cursor/rules/`
- Claude Code：`CLAUDE.md`
- 可复用流程：`skills/`

**重点：**
- 同一条规范尽量有单一事实来源
- 不要三套工具各写一份完全不同的规则
- 建议做一层“人类规范”文档，再派生到 agent 文件

---

## 5.3 企业内部智能助手 / 知识工作流
**推荐组合：**
- `dify` / `n8n` / `Flowise`
- MCP server 接内部系统
- HITL 审批
- 审计日志
- 细粒度权限

**重点：**
- 先把自动化分层：传统逻辑 vs AI 理解
- 高风险动作必须审批
- 每一步可观测、可回放、可限流

---

## 5.4 多 Agent 协作研究 / 调研 / 写作
**推荐组合：**
- `crewai` / `langgraph`
- Planner / Researcher / Writer / Reviewer 四角色即可起步
- 技术报告、市场分析、竞品研究都适合

**重点：**
- 角色不要过多
- 要限制每个角色权限和输出格式
- 加 reviewer 纠偏比盲目加 agent 数量更有效

---

## 5.5 网页自动化 / 运营代办
**推荐组合：**
- `browser-use`
- `n8n`
- MCP / API 封装
- 明确审批节点

**重点：**
- 幂等设计
- 限制“真实点击/提交”动作
- 必要时拆成“只读 agent + 执行 agent”两层

---

## 6. 规则设计最佳实践

## 6.1 规则里最应该写什么
优先写这 6 类：

1. **命令**
   - 安装
   - 启动
   - 测试
   - lint
   - typecheck

2. **目录与边界**
   - 哪些目录能改
   - 哪些目录不能改
   - 配置文件在哪

3. **关键样例**
   - 参考哪个现成文件写
   - 采用哪个模式

4. **验证要求**
   - 改后必须跑什么
   - PR 前必须检查什么

5. **风险约束**
   - 禁止删库
   - 禁止大规模格式化
   - 禁止无确认外网调用
   - 禁止变更公共接口不通知

6. **输出要求**
   - 先给计划还是直接改
   - 提交信息格式
   - 报告模板

---

## 6.2 规则里不该写什么
尽量不要写：
- 全量编码规范手册
- 与 linter/formatter 重复的内容
- 低频边角案例
- 过时路径或命令
- 模糊口号式要求

坏例子：
- “请写高质量代码”
- “遵循最佳实践”
- “注意性能和安全”

好例子：
- “修改 TypeScript 代码后，运行 `pnpm typecheck`；仅在受影响包运行测试”
- “新增 API route 时参考 `app/api/projects/[id]/route.ts` 的错误处理模式”
- “禁止修改 `infra/terraform/prod`，除非用户明确要求”

---

## 6.3 一套通用约束模板
可以直接转成 `AGENTS.md` / `CLAUDE.md` / Cursor Rules：

```md
# Project Rules

## Build & Verify
- Install: `pnpm install`
- Dev: `pnpm dev`
- Typecheck: `pnpm typecheck`
- Test: `pnpm test -- --runInBand`

## Boundaries
- Only modify files under `apps/web` and `packages/ui`
- Do not change `infra/` or `.github/workflows/` unless explicitly asked

## Coding Patterns
- Follow the structure used in `apps/web/app/projects/[id]/page.tsx`
- Reuse existing hooks before creating new abstractions

## Safety
- Ask for confirmation before destructive DB or migration changes
- Avoid broad formatting-only edits
- Do not add dependencies if an existing package already solves the problem

## Output
- Summarize changed files
- Mention commands run and any skipped validations
```

---

## 7. 组织 Skill 目录的建议

推荐目录：

```text
skills/
  code-review/
    SKILL.md
    checklist.md
    scripts/
  incident-triage/
    SKILL.md
    queries.md
    templates/
  release-readiness/
    SKILL.md
    scripts/
  api-design-review/
    SKILL.md
    rubric.md
```

### 7.1 每个 Skill 至少要有
- 名称
- 何时触发
- 输入要求
- 输出格式
- 步骤
- 边界条件
- 可选脚本/模板

### 7.2 适合做成 Skill 的典型任务
- 代码审查
- 生成 PR 描述
- 发布检查
- 事故排查
- SQL 风险检查
- 安全基线检查
- 文档规范化
- API 设计评审
- 架构巡检

---

## 8. 推荐你重点关注的“组合拳”

## 8.1 偏研发团队
- 编排：`langgraph` / `openai-agents-python`
- 仓库规则：`AGENTS.md`
- 本地协作：Codex / Claude Code / Copilot CLI
- 技能沉淀：Agent Skills

**适合：**
- 工程团队
- 后端 / 平台 / 基础架构

---

## 8.2 偏产品和业务团队
- 平台：`dify` / `Flowise`
- 自动化：`n8n`
- 工具接入：MCP
- 人工审批：HITL 流程

**适合：**
- 业务自动化
- 内部助手
- 多部门协作

---

## 8.3 偏操作自动化
- 浏览器：`browser-use`
- 流程编排：`n8n`
- 风险控制：审批 + 审计 + 幂等

**适合：**
- 运营流程
- 网页任务代办
- 多后台系统串联

---

## 8.4 偏多 Agent 协作实验
- `crewai`
- `langgraph`
- 角色清单 + 输出 schema + reviewer

**适合：**
- 调研
- 咨询报告
- 研究原型

---

## 9. 我建议你实际落地时的优先级

### 9.1 第一阶段：先立规矩
先做：
- 根目录 `AGENTS.md`
- 一两个关键模块的局部 `AGENTS.md`
- 一个 `code-review` skill
- 一个 `incident-triage` skill

### 9.2 第二阶段：再做工作流
再选：
- 代码优先：`langgraph` / `openai-agents-python`
- 可视化优先：`dify` / `n8n` / `Flowise`

### 9.3 第三阶段：最后做多 Agent
只有当下面成立时再上多 Agent：
- 单 Agent 已经不够
- 流程能稳定拆分
- 成本可控
- 你能观察每个角色产出

---

## 10. 一份务实的总建议

### 值得优先关注的 12 个项目 / 标准
1. `langchain-ai/langgraph`
2. `openai/openai-agents-python`
3. `pydantic/pydantic-ai`
4. `crewAIInc/crewai`
5. `microsoft/agent-framework`
6. `langgenius/dify`
7. `FlowiseAI/Flowise`
8. `n8n-io/n8n`
9. `browser-use/browser-use`
10. `OpenHands/OpenHands`
11. `agentsmd/agents.md`
12. Agent Skills / MCP 生态

### 最重要的认知
- **不要先迷信多 Agent，先把规则写清楚。**
- **不要把所有能力塞进系统提示，尽量拆成 Skill。**
- **不要让 Agent 直接拥有无限制执行权，要加约束层。**
- **Workflow 的稳定性，往往比模型“聪明一点点”更重要。**
- **企业场景里最关键的是审计、权限、确认、回滚，而不是炫技。**

---

## 11. 附：一个仓库的推荐最小落地结构

```text
.
├─ AGENTS.md
├─ CLAUDE.md
├─ .github/
│  ├─ copilot-instructions.md
│  ├─ instructions/
│  │  └─ backend.instructions.md
│  └─ agents/
│     └─ reviewer.agent.md
├─ .cursor/
│  ├─ rules/
│  │  ├─ core.mdc
│  │  └─ backend.mdc
│  └─ skills/
├─ skills/
│  ├─ code-review/
│  │  └─ SKILL.md
│  ├─ incident-triage/
│  │  └─ SKILL.md
│  └─ release-readiness/
│     └─ SKILL.md
└─ docs/
   └─ engineering-standards.md
```

---

## 12. 最后怎么选

### 你只做本地编码协作
优先：
- `AGENTS.md`
- Codex / Claude Code / Copilot CLI
- 少量技能包

### 你要做企业级 agent workflow
优先：
- `langgraph` / `openai-agents-python` / `agent-framework`
- MCP
- 审批与审计

### 你要让业务方也能参与
优先：
- `dify` / `Flowise` / `n8n`

### 你要网页代办
优先：
- `browser-use`
- 审批和回滚

### 你要研究多 Agent
优先：
- `crewai`
- `langgraph`

---

> 这份文档适合当“第一版选型底稿”。  
> 真正落地时，建议你再按你的技术栈补一版“Java / Spring Boot / 企业内网 / 审批与审计 / 多环境部署”的专门版本。