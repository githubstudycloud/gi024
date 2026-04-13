package com.example.mcp.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 应用启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.example.mcp")
@ConfigurationPropertiesScan(basePackages = "com.example.mcp")
@EntityScan(basePackages = "com.example.mcp")
@EnableJpaRepositories(basePackages = "com.example.mcp")
public class McpMigrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpMigrationApplication.class, args);
    }
}
