package com.example.multids.testapp.bootstrap;

import com.example.multids.component.registry.DataSourceRegistry;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 初始化测试应用所需的演示表结构和数据。
 */
@Configuration
public class DemoSchemaInitializer {

    @Bean
    SmartInitializingSingleton initDemoSchema(DataSourceRegistry dataSourceRegistry) {
        return () -> {
            initPublicDataSource(dataSourceRegistry.resolve("public-shared"));
            initOrderDataSource(dataSourceRegistry.resolve("order-service"));
        };
    }

    private void initPublicDataSource(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS country_dict (
                    code VARCHAR(8) PRIMARY KEY,
                    name VARCHAR(64) NOT NULL
                )
                """);
        jdbcTemplate.execute("MERGE INTO country_dict (code, name) KEY(code) VALUES ('CN', '中国')");
        jdbcTemplate.execute("MERGE INTO country_dict (code, name) KEY(code) VALUES ('US', '美国')");
    }

    private void initOrderDataSource(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS order_record (
                    id BIGINT PRIMARY KEY,
                    biz_no VARCHAR(32) NOT NULL,
                    amount DECIMAL(18, 2) NOT NULL
                )
                """);
        jdbcTemplate.execute("MERGE INTO order_record (id, biz_no, amount) KEY(id) VALUES (1, 'SO-1001', 120.50)");
        jdbcTemplate.execute("MERGE INTO order_record (id, biz_no, amount) KEY(id) VALUES (2, 'SO-1002', 88.00)");
    }
}
