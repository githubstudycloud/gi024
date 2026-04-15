package com.example.multids.testapp;

import com.example.multids.component.health.MultiDataSourceHealthContributor;
import com.example.multids.component.registry.DataSourceRegistry;
import com.example.multids.testapp.service.CountryDirectoryService;
import com.example.multids.testapp.service.OrderStatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.HealthContributors;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证测试应用是否正确引用公共组件。
 */
@SpringBootTest
class ComponentReferenceIntegrationTest {

    @Autowired
    private CountryDirectoryService countryDirectoryService;

    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @Autowired
    private DataSourceRegistry dataSourceRegistry;

    @Autowired
    private MultiDataSourceHealthContributor multiDataSourceHealthContributor;

    @Test
    void shouldRouteToPublicDataSource() {
        assertThat(countryDirectoryService.listCountryNames())
                .containsExactly("中国", "美国");
    }

    @Test
    void shouldRouteToOrderDataSource() {
        assertThat(orderStatisticsService.countOrders()).isEqualTo(2L);
        assertThat(orderStatisticsService.totalAmount()).hasToString("208.50");
    }

    @Test
    void shouldRegisterAllNamedDataSources() {
        assertThat(dataSourceRegistry.allNames())
                .containsExactlyInAnyOrder("public-shared", "order-service");
    }

    @Test
    void shouldExposeHealthContributorsForAllDataSources() {
        List<String> contributorNames = new ArrayList<>();
        for (var iterator = multiDataSourceHealthContributor.iterator(); iterator.hasNext(); ) {
            HealthContributors.Entry contributor = iterator.next();
            contributorNames.add(contributor.name());
        }

        assertThat(contributorNames)
                .containsExactlyInAnyOrder("public-shared", "order-service");
    }
}
