# Spring Boot 4.x Multi-DataSource Architecture Design Document

**Version:** 1.0  
**Date:** April 2026  
**Platform:** Spring Boot 4.0.x / Spring Framework 7.x / Java 17+ (21 recommended)

---

## 1. Architecture Overview

This document describes how to build a shared infrastructure layer (the "common component") for a system composed of **1 public (shared) database** and **10 business-specific databases**, each potentially running different engines, topologies (standalone, replica-set, cluster), and schema versions. It also covers microservice communication protocols, serialization strategy, and cross-cutting tooling.

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot 4.x Application              │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Common DataSource Component              │  │
│  │  ┌─────────┐  ┌──────────┐  ┌─────────────────────┐  │  │
│  │  │ Router  │─▶│ Registry │─▶│ Health / Failover   │  │  │
│  │  └─────────┘  └──────────┘  └─────────────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
│         │                                                   │
│  ┌──────┴──────────────────────────────────────────────┐    │
│  │                 DataSource Pool                      │    │
│  │  ┌────────┐ ┌────────┐ ┌────────┐      ┌────────┐  │    │
│  │  │ Public │ │ Biz-01 │ │ Biz-02 │ ...  │ Biz-10 │  │    │
│  │  │Postgres│ │ MySQL  │ │ Postgres│      │ Mongo  │  │    │
│  │  │Cluster │ │Single  │ │Replica │      │Cluster │  │    │
│  │  └────────┘ └────────┘ └────────┘      └────────┘  │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. DataSource Configuration Model

### 2.1 YAML Configuration Schema

Spring Boot 4.x's modular architecture lets us define datasources declaratively. Each business datasource declares its topology, pool settings, and schema version independently.

```yaml
# application.yml
app:
  datasource:
    public:
      name: public-shared
      url: jdbc:postgresql://pg-cluster.internal:5432/public_db
      username: ${DB_PUBLIC_USER}
      password: ${DB_PUBLIC_PASS}
      driver-class-name: org.postgresql.Driver
      topology: cluster            # standalone | replica | cluster
      pool:
        maximum-pool-size: 30
        minimum-idle: 5
        connection-timeout: 3000
      schema-version: v3.2

    business:
      - name: order-service
        url: jdbc:mysql://mysql-01.internal:3306/orders
        username: ${DB_ORDER_USER}
        password: ${DB_ORDER_PASS}
        driver-class-name: com.mysql.cj.jdbc.Driver
        topology: standalone
        pool:
          maximum-pool-size: 15
        schema-version: v2.1

      - name: inventory-service
        url: jdbc:postgresql://pg-replica.internal:5432/inventory
        driver-class-name: org.postgresql.Driver
        topology: replica
        read-replicas:
          - jdbc:postgresql://pg-replica-r1.internal:5432/inventory
          - jdbc:postgresql://pg-replica-r2.internal:5432/inventory
        pool:
          maximum-pool-size: 20
        schema-version: v4.0

      - name: analytics-service
        url: mongodb://mongo-cluster.internal:27017/analytics
        driver-class-name: mongo           # sentinel for Mongo handling
        topology: cluster
        schema-version: v1.5

      # ... up to 10 business datasources
```

### 2.2 Properties Binding Class

Spring Boot 4.x uses JSpecify null-safety annotations throughout. We bind the YAML tree into a strongly-typed properties object.

```java
@ConfigurationProperties(prefix = "app.datasource")
public class MultiDataSourceProperties {

    private @NonNull DataSourceEntry publicDs;               // renamed field
    private @NonNull List<DataSourceEntry> business = new ArrayList<>();

    // -- nested record (Java 17+) --
    public record DataSourceEntry(
        String name,
        String url,
        String username,
        String password,
        String driverClassName,
        Topology topology,
        @Nullable ReadReplicaConfig readReplicas,
        PoolConfig pool,
        String schemaVersion
    ) {}

    public record PoolConfig(
        int maximumPoolSize,
        int minimumIdle,
        long connectionTimeout
    ) {
        // defaults
        public PoolConfig {
            if (maximumPoolSize <= 0) maximumPoolSize = 10;
            if (minimumIdle <= 0)     minimumIdle = 2;
            if (connectionTimeout <= 0) connectionTimeout = 5000;
        }
    }

    public enum Topology { STANDALONE, REPLICA, CLUSTER }
}
```

---

## 3. DataSource Registry and Router

The core of the common component is a **registry** that holds every configured `DataSource` and a **router** that picks the correct one per request context.

### 3.1 DataSource Registry

```java
@Component
public class DataSourceRegistry {

    // key = logical name ("public-shared", "order-service", ...)
    private final Map<String, DataSource> sources = new ConcurrentHashMap<>();
    private final Map<String, Topology> topologies = new ConcurrentHashMap<>();

    /**
     * Called at startup by the auto-configuration to register each entry.
     */
    public void register(String name, DataSource ds, Topology topology) {
        sources.put(name, ds);
        topologies.put(name, topology);
    }

    public DataSource resolve(String name) {
        DataSource ds = sources.get(name);
        if (ds == null) throw new UnknownDataSourceException(name);
        return ds;
    }

    public Collection<String> allNames() {
        return Collections.unmodifiableCollection(sources.keySet());
    }
}
```

### 3.2 Context-Aware DataSource Router

We use Spring's `AbstractRoutingDataSource` with a `ThreadLocal` (or virtual-thread-safe `ScopedValue` on Java 21+) to let callers declare which datasource they need.

```java
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    // Java 21+ alternative: ScopedValue<String> CURRENT = ScopedValue.newInstance();
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    public static void setCurrent(String dsName) { CURRENT.set(dsName); }
    public static void clear()                    { CURRENT.remove(); }

    @Override
    protected Object determineCurrentLookupKey() {
        return CURRENT.get();  // returns the logical datasource name
    }
}
```

### 3.3 Usage via Annotation (Declarative Routing)

Define a custom annotation so service methods declare their datasource at the method or class level.

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UseDataSource {
    String value();   // logical name, e.g. "order-service"
}
```

An AOP aspect intercepts calls and sets/clears the routing context:

```java
@Aspect
@Component
public class DataSourceRoutingAspect {

    @Around("@annotation(useDs)")
    public Object route(ProceedingJoinPoint pjp, UseDataSource useDs) throws Throwable {
        TenantRoutingDataSource.setCurrent(useDs.value());
        try {
            return pjp.proceed();
        } finally {
            TenantRoutingDataSource.clear();
        }
    }
}
```

Service code becomes:

```java
@Service
public class OrderService {

    @UseDataSource("order-service")
    @Transactional
    public Order placeOrder(OrderRequest req) {
        // automatically uses the order-service datasource
        return orderRepository.save(toEntity(req));
    }

    @UseDataSource("public-shared")
    @Transactional(readOnly = true)
    public List<Country> listCountries() {
        return countryRepository.findAll();
    }
}
```

---

## 4. Topology-Aware DataSource Factory

Different topologies (standalone, replica, cluster) require different pool creation and health-check strategies.

```java
@Configuration
public class DataSourceFactoryConfig {

    @Bean
    public DataSourceFactory dataSourceFactory() {
        return (entry) -> switch (entry.topology()) {

            case STANDALONE -> buildStandalone(entry);

            case REPLICA    -> buildReplicaAware(entry);

            case CLUSTER    -> buildCluster(entry);
        };
    }

    // ---------- Standalone (single node) ----------
    private DataSource buildStandalone(DataSourceEntry entry) {
        var config = new HikariConfig();
        config.setJdbcUrl(entry.url());
        config.setUsername(entry.username());
        config.setPassword(entry.password());
        config.setMaximumPoolSize(entry.pool().maximumPoolSize());
        config.setPoolName("pool-" + entry.name());
        return new HikariDataSource(config);
    }

    // ---------- Read-Replica routing ----------
    private DataSource buildReplicaAware(DataSourceEntry entry) {
        // primary for writes
        DataSource primary = buildStandalone(entry);

        // one pool per read replica
        List<DataSource> replicas = entry.readReplicas().urls().stream()
            .map(url -> {
                var copy = cloneEntry(entry, url);
                return buildStandalone(copy);
            })
            .toList();

        return new ReadWriteSplittingDataSource(primary, replicas);
    }

    // ---------- Cluster (e.g. CockroachDB, Citus, Mongo) ----------
    private DataSource buildCluster(DataSourceEntry entry) {
        // Cluster-aware drivers handle node discovery internally.
        // We just provide the connection string and a larger pool.
        var config = new HikariConfig();
        config.setJdbcUrl(entry.url());     // e.g. jdbc:postgresql://node1,node2,node3/db
        config.setMaximumPoolSize(entry.pool().maximumPoolSize() * 2);
        config.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(config);
    }
}
```

### 4.1 Read-Write Splitting DataSource (Pseudocode)

```java
public class ReadWriteSplittingDataSource extends AbstractDataSource {

    private final DataSource primary;
    private final List<DataSource> replicas;
    private final AtomicInteger roundRobin = new AtomicInteger(0);

    @Override
    public Connection getConnection() {
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            // round-robin across healthy replicas
            int idx = roundRobin.getAndIncrement() % replicas.size();
            return replicas.get(idx).getConnection();
        }
        return primary.getConnection();
    }
}
```

---

## 5. Schema Version Management

Each business database may run a different schema version. The common component integrates Flyway (or Liquibase) per datasource.

```java
@Configuration
public class PerDataSourceMigrationConfig {

    @Bean
    public SmartInitializingSingleton runMigrations(
            DataSourceRegistry registry,
            MultiDataSourceProperties props) {

        return () -> {
            // migrate public datasource
            migrate(registry.resolve("public-shared"),
                    props.publicDs().schemaVersion(),
                    "classpath:db/migration/public");

            // migrate each business datasource
            for (var biz : props.business()) {
                migrate(registry.resolve(biz.name()),
                        biz.schemaVersion(),
                        "classpath:db/migration/" + biz.name());
            }
        };
    }

    private void migrate(DataSource ds, String targetVersion, String location) {
        Flyway.configure()
              .dataSource(ds)
              .locations(location)
              .target(MigrationVersion.fromVersion(targetVersion))
              .baselineOnMigrate(true)
              .load()
              .migrate();
    }
}
```

---

## 6. Health Check and Observability

Spring Boot 4.x ships an OpenTelemetry starter out of the box. We register per-datasource health indicators and export connection-pool metrics.

```java
@Component
public class MultiDataSourceHealthContributor
        implements CompositeHealthContributor {

    private final DataSourceRegistry registry;

    @Override
    public Iterator<NamedContributor<HealthContributor>> iterator() {
        return registry.allNames().stream()
            .map(name -> NamedContributor.of(name,
                    (HealthIndicator) () -> probe(registry.resolve(name))))
            .map(nc -> (NamedContributor<HealthContributor>) nc)
            .iterator();
    }

    private Health probe(DataSource ds) {
        try (var conn = ds.getConnection()) {
            conn.isValid(2);
            return Health.up()
                         .withDetail("pool-active", poolMetric(ds, "active"))
                         .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

---

## 7. Microservice Communication Protocols

### 7.1 Protocol Comparison Matrix

| Criterion | gRPC (Protobuf) | REST + JSON | REST + XML (SOAP/Plain) | GraphQL | RSocket |
|---|---|---|---|---|---|
| Serialization speed | Very fast (binary) | Moderate | Slow | Moderate | Fast |
| Payload size | Small | Medium | Large | Medium | Small |
| Schema evolution | Excellent (proto3) | Manual (OpenAPI) | WSDL-rigid | Good | Manual |
| Browser support | Limited (gRPC-Web) | Native | Native | Native | Limited |
| Streaming | Bidirectional | SSE / chunked | No | Subscriptions | Bidirectional |
| Spring Boot 4 support | Spring gRPC GA | RestClient / HttpExchange | Jaxb / jackson-xml | Spring for GraphQL | spring-rsocket |

**Recommendation:** Use **gRPC** for internal service-to-service calls (speed, type safety, streaming). Use **REST + JSON via `@HttpExchange`** for external APIs and BFF layers. Reserve XML only for legacy integrations.

### 7.2 Spring Boot 4.x Declarative HTTP Clients (`@HttpExchange`)

Spring Boot 4.x promotes interface-based HTTP clients as a first-class replacement for `RestTemplate`.

```java
// Declare the contract
@HttpExchange(url = "/api/v1/products", accept = "application/json")
public interface ProductServiceClient {

    @GetExchange("/{id}")
    Product findById(@PathVariable String id);

    @PostExchange
    Product create(@RequestBody ProductRequest request);
}

// Register as a bean — Spring Boot 4 auto-configures the proxy
@Configuration
public class ClientConfig {

    @Bean
    public ProductServiceClient productClient(RestClient.Builder builder) {
        var restClient = builder.baseUrl("http://product-service:8080").build();
        var factory = RestClientAdapter.create(restClient);
        return HttpServiceProxyFactory.builderFor(factory).build()
                .createClient(ProductServiceClient.class);
    }
}
```

### 7.3 gRPC for Internal Communication (via Spring gRPC)

```protobuf
// inventory.proto
syntax = "proto3";
package inventory;

service InventoryService {
  rpc CheckStock (StockRequest) returns (StockResponse);
  rpc StreamUpdates (StockRequest) returns (stream StockUpdate);
}

message StockRequest  { string sku = 1; }
message StockResponse { string sku = 1; int32 quantity = 2; }
message StockUpdate   { string sku = 1; int32 delta = 2; int64 timestamp = 3; }
```

```java
@GrpcService
public class InventoryGrpcService extends InventoryServiceGrpc.InventoryServiceImplBase {

    @UseDataSource("inventory-service")
    @Override
    public void checkStock(StockRequest req, StreamObserver<StockResponse> observer) {
        int qty = inventoryRepo.countBySku(req.getSku());
        observer.onNext(StockResponse.newBuilder()
                        .setSku(req.getSku())
                        .setQuantity(qty)
                        .build());
        observer.onCompleted();
    }
}
```

---

## 8. JSON and XML Parsing — Library Comparison

### 8.1 JSON Libraries

| Library | Parse Speed | Memory | Streaming | Spring Boot 4 Default |
|---|---|---|---|---|
| **Jackson (jackson-databind)** | Fast | Moderate | Yes (`JsonParser`) | Yes |
| **Gson** | Moderate | Lower | Limited | No |
| **JSON-B (Yasson/Johnzon)** | Moderate | Moderate | No | Jakarta standard |
| **fastjson2** | Very fast | Low | Yes | No (3rd party) |

**Recommendation: Jackson** — it is the default in Spring Boot 4.x, supports streaming, data binding, tree-model, and has the broadest ecosystem (modules for Java 8 dates, Kotlin, etc.).

```java
// Jackson streaming for large payloads (pseudocode)
public List<Order> parseOrdersStreaming(InputStream in) throws IOException {
    var factory = new JsonFactory();
    var parser  = factory.createParser(in);
    var orders  = new ArrayList<Order>();

    while (parser.nextToken() != JsonToken.END_ARRAY) {
        if (parser.currentToken() == JsonToken.START_OBJECT) {
            orders.add(objectMapper.readValue(parser, Order.class));
        }
    }
    return orders;
}
```

### 8.2 XML Libraries

| Library | Parse Style | Speed | Memory | Best For |
|---|---|---|---|---|
| **Jackson XML (`jackson-dataformat-xml`)** | Binding + Stream | Fast | Moderate | Unified JSON/XML pipeline |
| **JAXB (Jakarta XML Binding)** | Binding | Moderate | High (DOM) | SOAP / schema-first |
| **StAX (`XMLStreamReader`)** | Pull-stream | Very fast | Very low | Huge XML files |
| **DOM (`DocumentBuilder`)** | Tree | Slow | Very high | Random access / XPath |
| **SAX** | Push-stream | Fast | Very low | Read-only pipelines |

**Recommendation: Jackson XML** for API-layer serialization (consistent ObjectMapper API with JSON). Fall back to **StAX** for extremely large files where full binding is too expensive.

```java
// Unified JSON/XML with Jackson — same ObjectMapper contract
@Bean
public ObjectMapper jsonMapper() {
    return JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();
}

@Bean
public XmlMapper xmlMapper() {
    return XmlMapper.builder()
        .addModule(new JavaTimeModule())
        .defaultUseWrapper(false)
        .build();
}

// Content negotiation in a single controller
@RestController
@RequestMapping(value = "/api/v1/orders",
                produces = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.APPLICATION_XML_VALUE })
public class OrderController {

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable String id) {
        return orderService.findById(id);    // Jackson picks JSON or XML via Accept header
    }
}
```

### 8.3 StAX for Large XML Streams

```java
public void processLargeCatalog(InputStream in) throws XMLStreamException {
    var reader = XMLInputFactory.newInstance().createXMLStreamReader(in);

    while (reader.hasNext()) {
        if (reader.isStartElement() && "product".equals(reader.getLocalName())) {
            String sku   = reader.getAttributeValue(null, "sku");
            String name  = reader.getElementText();  // advances to END_ELEMENT
            // process product without holding full DOM in memory
            processProduct(sku, name);
        }
        reader.next();
    }
    reader.close();
}
```

---

## 9. Common Component Module Structure

With Spring Boot 4.x's modular jar architecture, the common component is published as a small set of focused modules.

```
common-parent/
├── common-datasource/          ← registry, router, factory, health
│   └── src/main/java/com/acme/common/datasource/
│       ├── DataSourceRegistry.java
│       ├── TenantRoutingDataSource.java
│       ├── DataSourceFactoryConfig.java
│       ├── ReadWriteSplittingDataSource.java
│       ├── UseDataSource.java
│       └── DataSourceRoutingAspect.java
│
├── common-serialization/       ← Jackson JSON + XML mappers, content negotiation
│   └── src/main/java/com/acme/common/serialization/
│       ├── JacksonAutoConfiguration.java
│       ├── XmlMapperAutoConfiguration.java
│       └── StreamingParsers.java
│
├── common-comm/                ← HTTP client factories, gRPC interceptors
│   └── src/main/java/com/acme/common/comm/
│       ├── HttpClientAutoConfiguration.java
│       ├── GrpcClientAutoConfiguration.java
│       └── RetryInterceptor.java
│
├── common-observability/       ← health contributors, OTel config
│   └── src/main/java/com/acme/common/observability/
│       ├── MultiDataSourceHealthContributor.java
│       └── TracingConfig.java
│
└── common-bom/                 ← BOM pom that locks all versions
    └── pom.xml
```

### 9.1 Auto-Configuration Registration (Spring Boot 4.x Style)

```java
// META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
// (one fully-qualified class name per line)
com.acme.common.datasource.MultiDataSourceAutoConfiguration
com.acme.common.serialization.JacksonAutoConfiguration
com.acme.common.comm.HttpClientAutoConfiguration
com.acme.common.observability.TracingConfig
```

```java
@AutoConfiguration
@EnableConfigurationProperties(MultiDataSourceProperties.class)
public class MultiDataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataSourceRegistry dataSourceRegistry(
            MultiDataSourceProperties props,
            DataSourceFactory factory) {

        var registry = new DataSourceRegistry();

        // register public datasource
        var publicDs = factory.create(props.publicDs());
        registry.register(props.publicDs().name(), publicDs,
                          props.publicDs().topology());

        // register each business datasource
        for (var biz : props.business()) {
            var ds = factory.create(biz);
            registry.register(biz.name(), ds, biz.topology());
        }

        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantRoutingDataSource routingDataSource(DataSourceRegistry registry) {
        var router = new TenantRoutingDataSource();
        // convert registry into the target-datasources map
        Map<Object, Object> targets = new HashMap<>();
        for (String name : registry.allNames()) {
            targets.put(name, registry.resolve(name));
        }
        router.setTargetDataSources(targets);
        router.setDefaultTargetDataSource(registry.resolve("public-shared"));
        router.afterPropertiesSet();
        return router;
    }
}
```

---

## 10. Cross-Cutting Concerns

### 10.1 Resilience (Retry + Circuit Breaker)

Spring Boot 4.x works with Resilience4j out of the box.

```java
@Configuration
public class ResilienceConfig {

    @Bean
    public RetryRegistry retryRegistry() {
        var config = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(500))
            .retryOnException(e -> e instanceof TransientDataAccessException)
            .build();
        return RetryRegistry.of(config);
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        var config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .build();
        return CircuitBreakerRegistry.of(config);
    }
}
```

### 10.2 Distributed Tracing (OpenTelemetry)

```yaml
# application.yml — Spring Boot 4 OTel starter
management:
  tracing:
    sampling:
      probability: 1.0       # sample everything in dev
  otlp:
    tracing:
      endpoint: http://otel-collector:4318/v1/traces
```

No code required — the Boot 4 OTel auto-configuration instruments JDBC, HTTP clients, and gRPC automatically.

### 10.3 Caching Strategy (Multi-Tier)

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisFactory) {
        // L1: in-process Caffeine (fast, bounded)
        var caffeine = new CaffeineCacheManager();
        caffeine.setCaffeine(Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofMinutes(5)));

        // L2: Redis (shared, larger)
        var redis = RedisCacheManager.builder(redisFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)))
            .build();

        // composite: check L1 first, then L2
        return new CompositeCacheManager(caffeine, redis);
    }
}
```

### 10.4 API Versioning (Spring Boot 4.x Built-In)

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @GetMapping(value = "/{id}")
    @ApiVersion("1")
    public OrderV1 getOrderV1(@PathVariable String id) { /* ... */ }

    @GetMapping(value = "/{id}")
    @ApiVersion("2")
    public OrderV2 getOrderV2(@PathVariable String id) { /* ... */ }
}
```

---

## 11. Putting It All Together — Bootstrap Flow

```
1.  Spring Boot starts, scans AutoConfiguration.imports
2.  MultiDataSourceAutoConfiguration reads YAML
3.  DataSourceFactory creates HikariDataSource / ReplicaDS / ClusterDS per entry
4.  DataSourceRegistry stores all datasources by name
5.  TenantRoutingDataSource wraps the registry as the primary @Bean DataSource
6.  Flyway migrates each datasource to its declared schema-version
7.  HealthContributor registers per-datasource probes on /actuator/health
8.  OTel instruments JDBC, RestClient, gRPC channels
9.  Services use @UseDataSource("name") to route transparently
10. Controllers negotiate JSON/XML via Accept header, one ObjectMapper each
```

---

## 12. Quick-Reference Decision Table

| Decision | Recommendation |
|---|---|
| Connection pool | HikariCP (Spring Boot 4 default) |
| Routing mechanism | `AbstractRoutingDataSource` + ThreadLocal / ScopedValue |
| Schema migration | Flyway per datasource, version-targeted |
| JSON parsing | Jackson `jackson-databind` (streaming for large payloads) |
| XML parsing | Jackson XML for APIs; StAX for bulk files |
| Internal comms | gRPC with Spring gRPC starter |
| External APIs | `@HttpExchange` declarative clients (RestClient) |
| Resilience | Resilience4j (retry + circuit breaker) |
| Tracing | OpenTelemetry via `spring-boot-starter-actuator` |
| Caching | Caffeine (L1) + Redis (L2) composite |
| API versioning | Spring Boot 4 built-in `@ApiVersion` |
| Config secrets | Spring Boot `{cipher}` or Vault / AWS Secrets Manager |

---

*This document provides the architectural blueprint. Each section's pseudocode can be adapted into production-ready implementations by adding error handling, integration tests, and environment-specific profiles (`dev`, `staging`, `prod`).*
