package com.example.mcp.acl.adapter;

import com.example.mcp.acl.config.LegacySystemProperties;
import com.example.mcp.acl.mapper.LegacyProductMapper;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.ServedBy;
import com.example.mcp.domain.exception.LegacySystemException;
import com.example.mcp.domain.model.ProductRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class LegacyProductRestClientTest {

    private LegacyProductRestClient legacyProductRestClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://legacy.example");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        legacyProductRestClient = new LegacyProductRestClient(
                builder.build(),
                new LegacyProductMapper(),
                new LegacySystemProperties()
        );
    }

    @Test
    void shouldMapLegacyProductToDomainModel() {
        mockServer.expect(requestTo("http://legacy.example/legacy-api/products/page?q=analytics&pageNo=1&pageSize=20"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "code": "0000",
                          "message": "success",
                          "data": {
                            "records": [
                              {
                                "product_id": "p-1",
                                "product_code": "PRD-2026-0001",
                                "product_name": "Analytics Suite",
                                "category_name": "SOFTWARE",
                                "sale_price": 4999.00,
                                "currency": "CNY",
                                "enabled": true,
                                "gmt_create": "2026-04-01 09:30:00"
                              }
                            ],
                            "pageNo": 1,
                            "pageSize": 20,
                            "totalCount": 1
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        PageResult<ProductRecord> result = legacyProductRestClient.search("analytics", null, 0, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.servedBy()).isEqualTo(ServedBy.LEGACY);
        assertThat(result.items().getFirst().productCode()).isEqualTo("PRD-2026-0001");
    }

    @Test
    void shouldThrowLegacySystemExceptionWhenUpstreamFails() {
        mockServer.expect(requestTo("http://legacy.example/legacy-api/products/page?q=analytics&pageNo=1&pageSize=20"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> legacyProductRestClient.search("analytics", null, 0, 20))
                .isInstanceOf(LegacySystemException.class)
                .hasMessageContaining("旧系统商品调用失败");
    }

    @Test
    void shouldReadActiveProductCountFromDedicatedEndpoint() {
        mockServer.expect(requestTo("http://legacy.example/legacy-api/products/statistics/active"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "code": "0000",
                          "message": "success",
                          "data": {
                            "activeCount": 15
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(legacyProductRestClient.countActiveProducts().count()).isEqualTo(15);
    }
}
