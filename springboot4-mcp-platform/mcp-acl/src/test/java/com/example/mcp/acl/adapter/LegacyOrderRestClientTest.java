package com.example.mcp.acl.adapter;

import com.example.mcp.acl.mapper.LegacyOrderMapper;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.ServedBy;
import com.example.mcp.domain.exception.LegacySystemException;
import com.example.mcp.domain.model.OrderRecord;
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

class LegacyOrderRestClientTest {

    private LegacyOrderRestClient legacyOrderRestClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://legacy.example");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        legacyOrderRestClient = new LegacyOrderRestClient(builder.build(), new LegacyOrderMapper());
    }

    @Test
    void shouldMapLegacyOrderToDomainModel() {
        mockServer.expect(requestTo("http://legacy.example/api/orders?q=acme&page=0&size=20"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "data": [
                            {
                              "id": "o-1",
                              "order_no": "SO-2026-0001",
                              "customer_name": "Acme Corp",
                              "order_status": "PENDING",
                              "total_amount": 2999.50,
                              "currency_code": "CNY",
                              "create_time": "2026-04-01 09:30:00"
                            }
                          ],
                          "page": 0,
                          "size": 20,
                          "total": 1
                        }
                        """, MediaType.APPLICATION_JSON));

        PageResult<OrderRecord> result = legacyOrderRestClient.search("acme", null, 0, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.servedBy()).isEqualTo(ServedBy.LEGACY);
        assertThat(result.items().getFirst().orderNo()).isEqualTo("SO-2026-0001");
        mockServer.verify();
    }

    @Test
    void shouldThrowLegacySystemExceptionWhenUpstreamFails() {
        mockServer.expect(requestTo("http://legacy.example/api/orders?q=acme&page=0&size=20"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> legacyOrderRestClient.search("acme", null, 0, 20))
                .isInstanceOf(LegacySystemException.class)
                .hasMessageContaining("旧系统订单调用失败");
    }

    @Test
    void shouldReadPendingOrderCountFromDedicatedEndpoint() {
        mockServer.expect(requestTo("http://legacy.example/api/orders/pending-count"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "pendingOrders": 7
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(legacyOrderRestClient.countPendingOrders().count()).isEqualTo(7);
    }
}
