# Claude Code 2025 新功能使用指南与配置手册

> 文档生成日期：2026年4月13日  
> 适用版本：Claude Code v2.x

---

## 一、Git 仓库信息

当前项目Git地址：
```
origin	https://github.com/githubstudycloud/gi024.git (fetch)
origin	https://github.com/githubstudycloud/gi024.git (push)
```

---

## 二、Claude Code 2025 主要新功能

### 1. 增强的Agent能力（子代理与异步代理）

| 功能 | 说明 | 使用方法 |
|------|------|----------|
| **Subagents（子代理）** | 将任务委派给专门的AI实例，拥有独立的上下文窗口和权限 | 使用 `Agent` 工具，指定 `subagent_type` |
| **Async Agents（异步代理）** | 完全自主运行的代理，可发送进度更新而不阻塞主工作 | 设置 `run_in_background: true` |
| **Skills System（技能系统）** | 动态加载团队规范、API标准和架构模式的知识包 | 2025年10月推出，组织内共享 |

**子代理类型：**
- `general-purpose` - 通用研究和多步骤任务
- `Explore` - 快速探索代码库
- `Plan` - 软件架构设计
- `code-reviewer` - 代码审查专家
- `bug-analyzer` - 调试专家

**使用示例：**
```json
{
  "description": "代码审查",
  "subagent_type": "code-reviewer",
  "prompt": "审查这个PR的安全性..."
}
```

### 2. IDE 与编辑器集成

| 功能 | 说明 | 使用方法 |
|------|------|----------|
| **VS Code / JetBrains 扩展** | 实时内联编辑，无需切换上下文 | 2025年5月发布，安装对应插件 |
| **LSP 支持** | 语言服务器协议集成，提供IDE级智能 | 2025年12月发布，支持跳转定义、查找引用 |

### 3. 上下文与记忆管理

#### CLAUDE.md 文件
在项目根目录创建 `.claude/CLAUDE.md` 文件，存储技术栈、编码偏好和项目上下文：

```markdown
# 项目概述

## 技术栈
- 前端: React + TypeScript
- 后端: Node.js + Express
- 数据库: PostgreSQL

## 编码规范
- 使用 2 空格缩进
- 优先使用函数组件
- 错误处理必须使用 try-catch
```

#### 上下文窗口管理

| 功能 | 说明 | 使用方法 |
|------|------|----------|
| **1M Token 上下文窗口** | Claude Sonnet 4 支持超大上下文 | 自动可用（API Tier 4） |
| **上下文压缩** | 无限长度对话（通过智能摘要） | 2025年11月发布，自动处理 |
| **命名会话与恢复** | 命名会话并在之后恢复 | `/rename 会话名` 然后 `claude --resume 会话名` |

### 4. 工作流与规划工具

#### Plan Mode（计划模式）
在编写代码之前创建详细的实施计划：
- **快捷键**: `Shift+Tab` 或 `Alt+M`
- **用途**: 防止实施偏差，确保与用户需求对齐

**使用流程：**
1. 用户描述需求
2. Claude 进入 Plan Mode，创建详细计划
3. 用户审核并批准计划
4. 执行计划

#### 扩展思考模式
根据需要选择不同的推理深度：

| 命令 | 说明 | 使用场景 |
|------|------|----------|
| `/think` | 基础思考模式 | 简单问题分析 |
| `/think hard` | 深度思考 | 复杂逻辑分析 |
| `/think harder` | 更强推理 | 架构设计决策 |
| `/ultrathink` | 终极推理 | 最复杂的分析和设计 |

#### 自定义斜杠命令
在 `.claude/commands/` 目录创建 markdown 文件：

```markdown
# .claude/commands/review.md

请审查以下代码的：
1. 安全性问题
2. 性能优化点
3. 代码可读性

代码：
$ARGUMENTS
```

使用方式：`/review 代码内容`

#### Hooks（钩子）
在 `.claude/settings.json` 中配置自动化钩子：

```json
{
  "hooks": {
    "pre-file-write": ["eslint --fix {{file}}"],
    "pre-commit": ["npm test"]
  }
}
```

### 5. MCP（Model Context Protocol）扩展

2025年5月推出的可扩展性层，连接Claude与外部系统。

**常用MCP服务器：**

| MCP服务器 | 功能 | 配置方式 |
|-----------|------|----------|
| PostgreSQL | 直接数据库查询 | `mcp__mcp-pg__query` |
| MySQL | MySQL数据库操作 | `mcp__mcp-mysql__execute_sql` |
| Redis | 缓存管理 | `mcp__mcp-redis__get` |
| Playwright | 浏览器自动化 | `mcp__mcp-playwright__navigate` |
| Slack | 消息通知 | `mcp__mcp-slack__send_message` |

**在 settings.json 中配置 MCP：**

```json
{
  "mcpServers": {
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres", "postgresql://localhost/mydb"]
    },
    "slack": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-slack"]
    }
  }
}
```

### 6. 模型更新

| 模型 | 发布日期 | 特点 |
|------|----------|------|
| **Claude Opus 4.5** | 2025年11月 | 最强大的模型，工具调用错误减少50-75%，20万token上下文 |
| **Claude Haiku 4.5** | 2025年10月 | 最快、最经济，性能匹配Sonnet 4 |
| **Claude Sonnet 4.6** | 2026年 | 当前版本，100万token上下文 |

**模型切换：**
- 快捷键: `Option+P` (Mac) / `Alt+P` (Windows/Linux)
- 用途: 在Sonnet（速度）和Opus（复杂性）之间切换

### 7. 团队与企业功能

| 功能 | 说明 | 可用性 |
|------|------|--------|
| **Premium Seats** | 更高使用配额 | 2025年8月，Team/Enterprise计划 |
| **Project Sharing** | 组织级权限和共享 | 2025年8月 |
| **Slack 集成** | 从Slack委派编码任务 | 2025年12月，@提及Claude |
| **Claude for Excel** | 数据透视表、图表支持 | 2025年11月Beta |

---

## 三、配置指南

### 1. 配置文件位置

| 平台 | 全局配置路径 | 项目配置路径 |
|------|--------------|--------------|
| **Windows** | `%APPDATA%\ClaudeCode\settings.json` | `.claude/settings.json` |
| **macOS** | `~/.claude/settings.json` | `.claude/settings.json` |
| **Linux/WSL** | `~/.claude/settings.json` | `.claude/settings.json` |

### 2. 完整配置示例

```json
{
  "\$schema": "https://json.schemastore.org/claude-code-settings.json",
  
  "telemetry": "off",
  "includeCoAuthoredBy": false,
  
  "env": {
    "DISABLE_TELEMETRY": "1",
    "DISABLE_ERROR_REPORTING": "1",
    "DISABLE_BUG_COMMAND": "1",
    "CLAUDE_CODE_DISABLE_FEEDBACK_SURVEY": "1"
  },
  
  "hooks": {
    "pre-file-write": [],
    "post-file-write": [],
    "pre-commit": [],
    "post-commit": []
  },
  
  "mcpServers": {
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres", "postgresql://localhost/mydb"]
    }
  },
  
  "commands": {
    "review": {
      "description": "代码审查",
      "file": ".claude/commands/review.md"
    }
  }
}
```

---

## 四、关闭遥测配置（隐私设置）

### 方法1：环境变量（推荐）

在 shell 配置文件中添加：

**Bash (`~/.bashrc`) / Zsh (`~/.zshrc`)：**
```bash
# 核心遥测控制
export DISABLE_TELEMETRY=1                          # 退出使用遥测/分析
export DISABLE_ERROR_REPORTING=1                    # 禁用Sentry错误报告
export DISABLE_BUG_COMMAND=1                        # 禁用 /bug 命令
export CLAUDE_CODE_DISABLE_FEEDBACK_SURVEY=1        # 停止反馈调查
export DISABLE_NON_ESSENTIAL_MODEL_CALLS=1          # 减少LLM调用
```

**PowerShell (`$PROFILE`)：**
```powershell
# 核心遥测控制
$env:DISABLE_TELEMETRY = "1"
$env:DISABLE_ERROR_REPORTING = "1"
$env:DISABLE_BUG_COMMAND = "1"
$env:CLAUDE_CODE_DISABLE_FEEDBACK_SURVEY = "1"
$env:DISABLE_NON_ESSENTIAL_MODEL_CALLS = "1"
```

**Windows CMD：**
```cmd
setx DISABLE_TELEMETRY 1
setx DISABLE_ERROR_REPORTING 1
setx DISABLE_BUG_COMMAND 1
setx CLAUDE_CODE_DISABLE_FEEDBACK_SURVEY 1
setx DISABLE_NON_ESSENTIAL_MODEL_CALLS 1
```

### 方法2：settings.json 配置

```json
{
  "\$schema": "https://json.schemastore.org/claude-code-settings.json",
  "telemetry": "off",
  "includeCoAuthoredBy": false,
  "env": {
    "DISABLE_TELEMETRY": "1",
    "DISABLE_ERROR_REPORTING": "1",
    "DISABLE_BUG_COMMAND": "1",
    "CLAUDE_CODE_DISABLE_FEEDBACK_SURVEY": "1"
  }
}
```

### 重要警告：遥测与功能标志耦合问题

设置 `DISABLE_TELEMETRY=1` 或 `CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC=1` 会**同时禁用**与 GrowthBook/Statsig（功能标志基础设施）的通信，导致以下功能不可用：

| 受影响功能 | 结果 |
|-----------|------|
| **Remote Control（远程控制）** | `/remote-control` 命令显示"尚未启用"错误 |
| **Channels（频道）** | `--channels` 标志显示"当前不可用" |
| **Opus 4.6 1M 上下文** | 在Max/Team计划上静默禁用高级模型访问 |
| **新功能** | 功能门控发布不可用 |

**解决方案：**

1. **完全移除遥测设置**（权衡：重新启用遥测）
   ```bash
   unset DISABLE_TELEMETRY
   unset CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC
   # 然后重启 Claude Code
   ```

2. **使用替代变量**（可能行为不同）
   ```bash
   export CLAUDE_CODE_ENABLE_TELEMETRY=0
   # 而不是 DISABLE_TELEMETRY=1
   ```

3. **等待官方修复** - GitHub issues (#34178, #38450) 表明Anthropic正在解决这个问题

### 方法3：OpenTelemetry 导出到自有后端

如果需要遥测用于监控但不想发送到Anthropic：

```json
{
  "env": {
    "CLAUDE_CODE_ENABLE_TELEMETRY": "1",
    "OTEL_METRICS_EXPORTER": "otlp",
    "OTEL_LOGS_EXPORTER": "otlp",
    "OTEL_EXPORTER_OTLP_ENDPOINT": "http://your-collector:4317"
  }
}
```

### 方法4：账户级隐私设置

在 Claude.ai 网站上禁用模型训练：

1. 访问 [claude.ai](https://claude.ai) → **Settings** → **Privacy**
2. 关闭 **"Help improve Claude"** 开关
3. **注意**：必须在2025年9月28日之前完成

### 验证遥测是否已禁用

```bash
# 检查环境变量
env | grep -E "(DISABLE_TELEMETRY|CLAUDE_CODE_ENABLE_TELEMETRY)"

# 监控网络连接（Linux/macOS）
ss -tunap | grep claude

# 或检查进程
ps aux | grep claude
```

---

## 五、常用快捷键

| 快捷键 | 功能 |
|--------|------|
| `Shift+Tab` / `Alt+M` | 进入/退出 Plan Mode |
| `Option+P` / `Alt+P` | 切换模型（Sonnet/Opus） |
| `Ctrl+C` | 取消当前操作 |
| `Ctrl+D` | 退出 Claude Code |
| `/help` | 显示帮助 |
| `/clear` | 清除对话历史 |
| `/compact` | 压缩上下文 |

---

## 六、参考资源

- [Claude Code 官方文档](https://code.claude.com/docs)
- [Claude Code 发布说明](https://support.anthropic.com/en/articles/12138966-release-notes)
- [GitHub Issues](https://github.com/anthropics/claude-code/issues)
- [MCP 协议规范](https://modelcontextprotocol.io/)

---

*本文档基于 Claude Code 2025年发布的功能整理，具体功能可能随版本更新而变化。*
