# Spring Boot 4 MCP 迁移平台样板

这是一个基于 Spring Boot 4、Spring AI MCP Server 和六边形架构的迁移样板工程，用来演示如何在保留旧系统为权威数据源的前提下，逐步构建新的 REST `v2` 接口与 MCP 能力。

当前样板已经覆盖三个业务域：

- 用户域：保留基础的旧系统 REST 适配方案，用于演示最小迁移路径。
- 订单域：采用更贴近存量系统的统一响应体与 1 基分页协议，演示真实 ACL 改造。
- 商品域：作为第三个业务域，完整复用“旧系统 ACL + 新系统 JPA + REST + MCP”迁移模式。

## 工程目标

- 使用 Spring Boot 4.0.5 构建多模块迁移平台。
- 通过 Spring AI 2.0.0-M4 暴露 MCP Tool、Resource、Prompt。
- 通过特性开关控制用户域、订单域、商品域分别走旧系统还是新系统。
- 通过 PostgreSQL + Flyway 管理新系统结构演进。
- 在 `dev` 与 `test` 环境中保持可直接运行、可直接验证。

## 模块说明

- `mcp-domain`：领域模型、分页结果、端口接口和领域异常。
- `mcp-application`：应用服务与迁移开关路由逻辑。
- `mcp-acl`：旧系统 REST 适配层，负责 DTO 转换与错误归一化。
- `mcp-infrastructure-jpa`：新系统 JPA 查询适配层。
- `mcp-rest-api`：REST `v2` 控制器、响应 DTO 和统一异常处理。
- `mcp-mcp-server`：MCP Tool、Resource、Prompt 提供者。
- `mcp-bootstrap`：启动模块、配置、Flyway 脚本、开发态数据初始化和集成测试。

## 运行要求

- JDK 21
- Maven 3.9+
- PostgreSQL 14+

默认数据库配置位于 [`mcp-bootstrap/src/main/resources/application.yml`](./mcp-bootstrap/src/main/resources/application.yml)：

- `APP_DB_URL`，默认 `jdbc:postgresql://localhost:5432/mcp_platform`
- `APP_DB_USERNAME`，默认 `postgres`
- `APP_DB_PASSWORD`，默认 `postgres`

`test` 环境会自动切换到 H2 PostgreSQL 兼容模式并执行同一套 Flyway 脚本。

## 快速验证

### 1. 执行测试

```bash
mvn clean test
```

### 2. 本地启动开发环境

```bash
mvn -pl mcp-bootstrap spring-boot:run -Dspring-boot.run.profiles=dev
```

开发环境默认开启：

- `migration.flags.user-source-new=true`
- `migration.flags.order-source-new=true`
- `migration.flags.product-source-new=true`

因此会直接读取新系统数据库，并自动补充用户、订单、商品三类演示数据。

## 关键接口

### REST

- `GET /api/v2/users?query=alice`
- `GET /api/v2/users/{id}`
- `GET /api/v2/orders?query=acme`
- `GET /api/v2/orders/{id}`
- `GET /api/v2/products?query=analytics`
- `GET /api/v2/products/{id}`

### MCP Tool

- `search_users`
- `get_user_detail`
- `search_orders`
- `get_order_detail`
- `search_products`
- `get_product_detail`

### MCP Resource

- `mcp://users/active-count`
- `mcp://orders/pending-count`
- `mcp://products/active-count`

### MCP Prompt

- `weekly_user_summary`
- `weekly_order_summary`
- `weekly_product_summary`

## 迁移配置说明

默认配置采取“生产默认走旧系统，开发测试优先走新系统”的策略：

- `application.yml`：三个业务域默认都走旧系统。
- `application-dev.yml`：三个业务域默认切到新系统，便于联调。
- `application-test.yml`：三个业务域默认切到新系统，便于在 H2 中做回归测试。

订单域与商品域的旧系统接口已抽象为可配置路径：

- `legacy.order.search-path`
- `legacy.order.detail-path`
- `legacy.order.pending-count-path`
- `legacy.product.search-path`
- `legacy.product.detail-path`
- `legacy.product.active-count-path`

## 数据库迁移

Flyway 脚本位于 `mcp-bootstrap/src/main/resources/db/migration`：

- `V1__create_users.sql`
- `V2__create_orders.sql`
- `V3__create_products.sql`

应用默认使用 `ddl-auto=validate`，要求数据库结构始终由 Flyway 管理，不依赖运行时自动建表。

## 设计文档

详细设计见 [docs/详细设计.md](./docs/详细设计.md)。
