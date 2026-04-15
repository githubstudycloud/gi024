package com.example.multids.component.health;

import com.example.multids.component.registry.DataSourceRegistry;
import org.springframework.boot.health.contributor.CompositeHealthContributor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthContributor;
import org.springframework.boot.health.contributor.HealthContributors;
import org.springframework.boot.health.contributor.HealthIndicator;

import javax.sql.DataSource;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 为每个已注册数据源提供健康检查。
 */
public class MultiDataSourceHealthContributor implements CompositeHealthContributor {

    private final Map<String, HealthContributor> contributors;

    public MultiDataSourceHealthContributor(DataSourceRegistry registry) {
        this.contributors = registry.allNames().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        name -> name,
                        name -> (HealthIndicator) () -> probe(name, registry.resolve(name), registry)
                ));
    }

    @Override
    public HealthContributor getContributor(String name) {
        return contributors.get(name);
    }

    @Override
    public Stream<HealthContributors.Entry> stream() {
        return contributors.entrySet().stream()
                .map(entry -> new HealthContributors.Entry(entry.getKey(), entry.getValue()));
    }

    private Health probe(String name, DataSource dataSource, DataSourceRegistry registry) {
        try (var connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            if (valid) {
                return Health.up()
                        .withDetail("datasource", name)
                        .withDetail("topology", registry.topologyOf(name).name())
                        .build();
            }
            return Health.down()
                    .withDetail("datasource", name)
                    .withDetail("reason", "连接校验失败")
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("datasource", name)
                    .build();
        }
    }
}
