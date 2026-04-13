# Spring Boot 4 MCP 迁移平台样板

这是一个基于 Spring Boot 4、Spring AI MCP Server 和六边形架构的迁移样板工程，用来演示如何在保留旧系统为权威数据源的前提下，逐步构建新的 MCP 能力与 `v2` REST API。

## 工程目标

- 使用 Spring Boot 4.x 搭建新的应用骨架。
- 通过 ACL 访问旧 Spring Boot 2.7 系统接口。
- 通过特性开关控制用户域、订单域从旧系统切换到新系统数据源。
- 同时暴露 REST `v2` 接口和 MCP Tool / Resource / Prompt。

## 模块说明

- `mcp-domain`：领域模型、分页对象、端口和异常。
- `mcp-application`：应用服务与迁移开关路由。
- `mcp-acl`：旧系统 REST 适配层。
- `mcp-infrastructure-jpa`：新系统 JPA 查询实现。
- `mcp-rest-api`：REST `v2` 控制器与异常处理。
- `mcp-mcp-server`：MCP Tool / Resource / Prompt。
- `mcp-bootstrap`：启动模块、配置、开发数据初始化和烟雾测试。

## 快速启动

### 1. 构建

```bash
mvn clean test
```

### 2. 本地运行

```bash
mvn -pl mcp-bootstrap spring-boot:run -Dspring-boot.run.profiles=dev
```

开发环境默认开启 `migration.flags.user-source-new=true` 与 `migration.flags.order-source-new=true`，因此会直接读取本地 H2 中的演示数据。

## 关键入口

- REST 查询用户列表：`GET /api/v2/users?query=alice`
- REST 查询用户详情：`GET /api/v2/users/{id}`
- REST 查询订单列表：`GET /api/v2/orders?query=acme`
- REST 查询订单详情：`GET /api/v2/orders/{id}`
- MCP 服务入口：由 Spring AI MCP Server Starter 自动暴露
- 健康检查：`GET /actuator/health`

## 设计文档

详细设计见 [docs/详细设计.md](./docs/详细设计.md)。
