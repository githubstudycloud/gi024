# Spring Boot 4 多数据源组件示例

该工程根据 [`spring-boot-4x-multi-datasource-design.md`](../spring-boot-4x-multi-datasource-design.md) 新建，用来演示如何把“多数据源公共能力”拆成一个可复用组件项目，并提供一个实际引用它的测试应用项目。

## 模块说明

- `common-datasource-component`
  多数据源公共组件，包含配置绑定、数据源工厂、数据源注册表、路由数据源、`@UseDataSource` 注解、AOP 路由和健康检查贡献器。
- `common-datasource-component-test-app`
  引用公共组件的测试应用，使用两个 H2 数据源模拟 `public` 和 `order-service`，通过集成测试验证路由是否生效。

## 当前实现范围

- 已实现 JDBC 数据源场景。
- 已实现 `STANDALONE`、`REPLICA`、`CLUSTER` 三种拓扑的基础建池逻辑。
- 已预留文档中的 Mongo 场景入口，但当前版本不实现 Mongo 驱动适配。
- 已提供基于 `JdbcTemplate` 的最小可运行验证。

## 快速验证

在当前目录执行：

```bash
mvn test
```

## 关键设计映射

- 文档中的 `DataSourceRegistry` 已落地为组件 Bean。
- 文档中的 `TenantRoutingDataSource` 已落地为主 `DataSource`。
- 文档中的 `@UseDataSource` + AOP 路由已在测试应用中被实际使用。
- 文档中的健康检查思路已落地为 `MultiDataSourceHealthContributor`。
