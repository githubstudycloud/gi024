package com.example.mcp.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 启动烟雾测试，确保整体上下文可加载。
 */
@SpringBootTest
@ActiveProfiles("test")
class McpMigrationApplicationTest {

    @Test
    void contextLoads() {
    }
}
