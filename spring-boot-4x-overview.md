# Spring Boot 4.x 企业级技术文档

## 目录

1. 文档概述
2. 项目概述
3. 安装与配置
4. 核心能力与模块说明
5. API 接口说明
6. 代码示例
7. 开发与运维建议
8. 常见问题解答
9. 版本更新记录
10. 文档维护说明

## 1. 文档概述

本文档面向使用 Spring Boot 4.x 构建企业级应用的开发团队，提供一份可直接复用的技术基线说明。内容覆盖项目概述、环境准备、安装与配置、典型 API 设计、代码示例、常见问题以及版本更新记录，适合作为团队内部知识库或项目初始化文档。

文档中的接口与代码示例采用“企业级用户服务”场景进行说明，重点展示推荐结构和实现方式。若实际项目的业务领域不同，可在保留工程规范的前提下替换资源名称、字段定义和安全策略。

## 2. 项目概述

### 2.1 项目目标

基于 Spring Boot 4.x 构建一个具备以下能力的企业级后端服务：

- 提供标准 REST API
- 支持分层架构与领域化组织
- 集成数据访问、安全控制与可观测性
- 支持容器化部署与持续交付
- 为后续接入 Redis、Kafka、OpenTelemetry、Native Image 等能力预留扩展点

### 2.2 技术基线

| 技术项 | 推荐版本/要求 | 说明 |
|---|---|---|
| JDK | 17 及以上 | 建议团队统一到较新的 LTS 或已批准版本 |
| Spring Boot | 4.x | 作为应用框架与自动配置基础 |
| 构建工具 | Maven 3.9+ 或 Gradle 8.x+ | 保持团队统一，避免双构建工具并行 |
| 数据库 | PostgreSQL / MySQL | 示例以 PostgreSQL 为主 |
| 安全 | Spring Security | 实现认证、授权、接口保护 |
| 可观测性 | Actuator + OpenTelemetry | 提供健康检查、指标与链路追踪 |
| 部署方式 | Docker + Kubernetes | 适合企业级交付场景 |

### 2.3 架构原则

- **职责分离**：Controller 负责协议适配，Service 负责业务规则，Repository 负责持久化访问。
- **接口稳定**：对外暴露 DTO，而不是数据库实体。
- **配置外部化**：按环境管理配置，避免将敏感信息写死在代码中。
- **安全默认启用**：接口鉴权、最小权限、操作审计应纳入基线。
- **可观测性优先**：上线前即具备健康检查、日志、指标和追踪能力。

## 3. 安装与配置

### 3.1 前置条件

开始前请确保本地环境满足以下条件：

1. 已安装 JDK 17 及以上版本
2. 已安装 Maven 或 Gradle
3. 已安装 Git
4. 已准备 PostgreSQL 或 MySQL 实例
5. 如需容器化部署，已安装 Docker

### 3.2 项目初始化

推荐使用 Spring Initializr 初始化项目，常用依赖如下：

- Spring Web / WebMVC
- Spring Data JPA
- Spring Security
- Validation
- Actuator
- OpenTelemetry
- PostgreSQL Driver

示例 Maven `pom.xml` 片段：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.0</version>
</parent>

<properties>
    <java.version>17</java.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-opentelemetry</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 3.3 推荐项目结构

```text
src/main/java/com/example/enterprise
├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── exception
├── repository
├── security
├── service
└── EnterpriseApplication.java

src/main/resources
├── application.yml
└── db
    └── migration
```

### 3.4 应用配置示例

`application.yml` 示例：

```yaml
server:
  port: 8080

spring:
  application:
    name: enterprise-service
  datasource:
    url: jdbc:postgresql://localhost:5432/enterprise_db
    username: app_user
    password: change_me
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
  threads:
    virtual:
      enabled: false

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized

logging:
  level:
    root: INFO
    com.example.enterprise: DEBUG
```

### 3.5 分环境配置建议

- `application-dev.yml`：本地开发环境，开启调试日志
- `application-test.yml`：测试环境，连接测试数据库
- `application-prod.yml`：生产环境，仅保留必要日志级别

启动时可通过以下方式指定环境：

```bash
java -jar app.jar --spring.profiles.active=prod
```

### 3.6 本地运行与打包

Maven：

```bash
mvn clean spring-boot:run
mvn clean package
```

Gradle：

```bash
gradle bootRun
gradle clean build
```

### 3.7 容器化部署示例

示例 `Dockerfile`：

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/enterprise-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

## 4. 核心能力与模块说明

### 4.1 Spring Boot 4.x 关注点

- 模块化自动配置，减少不必要依赖加载
- 面向 Jakarta EE 11 的标准升级
- 更严格的配置校验与更清晰的自动装配边界
- 更完善的 AOT、可观测性与原生镜像支持

### 4.2 常用 Starter 参考

| 类别 | Starter | 主要用途 |
|---|---|---|
| Web | `spring-boot-starter-webmvc` | REST API、MVC 应用 |
| 响应式 | `spring-boot-starter-webflux` | 响应式接口与非阻塞处理 |
| 数据访问 | `spring-boot-starter-data-jpa` | ORM、事务与 Repository |
| 安全 | `spring-boot-starter-security` | 认证授权与接口保护 |
| 校验 | `spring-boot-starter-validation` | 请求参数校验 |
| 监控 | `spring-boot-starter-actuator` | 健康检查、指标、运行信息 |
| 可观测性 | `spring-boot-starter-opentelemetry` | 指标、追踪与导出 |
| 测试 | `spring-boot-starter-test` | 单元测试与集成测试 |

## 5. API 接口说明

以下接口以“用户管理服务”为例，用于说明标准 REST 设计方式。若项目业务不同，可保留规范并替换资源模型。

### 5.1 接口设计规范

- 基础路径：`/api/v1`
- 数据格式：`application/json`
- 认证方式：Bearer Token
- 时间格式：ISO-8601
- 响应建议统一封装：`code`、`message`、`data`

统一响应示例：

```json
{
  "code": "SUCCESS",
  "message": "Request processed successfully",
  "data": {
    "id": 1001
  }
}
```

### 5.2 认证接口

#### POST `/api/v1/auth/token`

用于用户登录并获取访问令牌。

请求体：

```json
{
  "username": "admin",
  "password": "Password@123"
}
```

成功响应：

```json
{
  "code": "SUCCESS",
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.xxx",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

#### POST `/api/v1/auth/refresh`

用于刷新访问令牌。

### 5.3 用户接口

#### GET `/api/v1/users`

分页查询用户列表。

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | integer | 否 | 页码，默认 0 |
| size | integer | 否 | 每页数量，默认 20 |
| keyword | string | 否 | 用户名或邮箱模糊搜索 |

成功响应：

```json
{
  "code": "SUCCESS",
  "message": "Query successful",
  "data": {
    "content": [
      {
        "id": 1001,
        "username": "alice",
        "email": "alice@example.com",
        "status": "ACTIVE"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1
  }
}
```

#### GET `/api/v1/users/{id}`

根据用户 ID 查询详情。

#### POST `/api/v1/users`

创建用户。

请求体：

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "Password@123"
}
```

#### PUT `/api/v1/users/{id}`

更新用户基础信息。

#### DELETE `/api/v1/users/{id}`

删除指定用户。

### 5.4 健康检查接口

#### GET `/actuator/health`

返回服务健康状态，适用于负载均衡与容器探针。

#### GET `/actuator/metrics`

返回应用指标集合，便于接入 Prometheus/Grafana。

### 5.5 错误响应规范

建议统一错误结构：

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "data": [
    {
      "field": "email",
      "reason": "must be a well-formed email address"
    }
  ]
}
```

常见错误码建议：

| 错误码 | 说明 |
|---|---|
| `VALIDATION_ERROR` | 请求参数校验失败 |
| `UNAUTHORIZED` | 未认证或令牌失效 |
| `FORBIDDEN` | 权限不足 |
| `RESOURCE_NOT_FOUND` | 资源不存在 |
| `CONFLICT` | 数据冲突 |
| `INTERNAL_SERVER_ERROR` | 服务器内部异常 |

## 6. 代码示例

### 6.1 DTO 示例

```java
package com.example.enterprise.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        @Email String email,
        @NotBlank String password
) {
}
```

### 6.2 Controller 示例

```java
package com.example.enterprise.controller;

import com.example.enterprise.dto.request.CreateUserRequest;
import com.example.enterprise.dto.response.UserResponse;
import com.example.enterprise.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }
}
```

### 6.3 Service 示例

```java
package com.example.enterprise.service;

import com.example.enterprise.dto.request.CreateUserRequest;
import com.example.enterprise.dto.response.UserResponse;

public interface UserService {

    UserResponse getById(Long id);

    UserResponse create(CreateUserRequest request);
}
```

### 6.4 声明式 HTTP 客户端示例

```java
package com.example.enterprise.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/v1/users")
public interface UserHttpClient {

    @GetExchange("/{id}")
    String getUserById(@PathVariable Long id);
}
```

### 6.5 全局异常处理示例

```java
package com.example.enterprise.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation failed");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }
}
```

## 7. 开发与运维建议

### 7.1 安全建议

- 使用构造函数注入，降低隐式依赖风险
- 对所有写接口启用鉴权与审计
- 密码、密钥、数据库凭据统一交由密钥管理系统维护
- 对外接口启用速率限制、输入校验和日志脱敏

### 7.2 数据与迁移建议

- 使用 Flyway 或 Liquibase 管理数据库变更
- 明确实体与 DTO 边界，避免 API 直接返回 Entity
- 对关键表建立唯一约束与必要索引

### 7.3 可观测性建议

- 启用 `/actuator/health` 作为探针检查入口
- 暴露必要指标并接入集中监控平台
- 对核心业务链路配置追踪和错误告警

## 8. 常见问题解答

### Q1：Spring Boot 4.x 是否必须升级到 Jakarta EE 11？

是。4.x 的技术基线已全面转向 Jakarta 生态，因此旧版 `javax.*` 代码通常需要迁移为 `jakarta.*`。

### Q2：为什么推荐 DTO，而不是直接返回 Entity？

DTO 可以隔离数据库模型与接口模型，减少字段泄露、懒加载问题和接口结构耦合，便于版本演进。

### Q3：生产环境是否建议开启 `ddl-auto=update`？

不建议。生产环境应使用 Flyway 或 Liquibase 做数据库版本化迁移，避免不可控结构变更。

### Q4：声明式 HTTP 客户端相比传统调用方式有什么优势？

它更接近接口定义思维，代码更简洁、可测试性更高，也更适合在企业项目中统一封装远程调用约定。

### Q5：如何选择模块化单体还是微服务？

如果团队规模、业务复杂度和交付压力尚未达到微服务治理阈值，优先采用模块化单体通常更稳妥；当团队边界、独立扩容和独立发布成为刚需时，再考虑拆分微服务。

## 9. 版本更新记录

| 版本 | 日期 | 更新内容 |
|---|---|---|
| 1.0.0 | 2026-04-07 | 初版重构完成，新增项目概述、安装配置、API 说明、代码示例、FAQ 与版本记录 |

## 10. 文档维护说明

- 本文档适合作为团队项目模板文档的基础版本
- 后续新增业务模块时，应同步补充接口说明、示例请求与错误码说明
- 若依赖版本、部署方式或安全策略发生变化，应优先更新本文档后再通知团队使用
