package com.example.mcp.acl.adapter;

import com.example.mcp.acl.mapper.LegacyUserMapper;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.ServedBy;
import com.example.mcp.domain.exception.LegacySystemException;
import com.example.mcp.domain.model.UserProfile;
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

class LegacyUserRestClientTest {

    private LegacyUserRestClient legacyUserRestClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://legacy.example");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        legacyUserRestClient = new LegacyUserRestClient(builder.build(), new LegacyUserMapper());
    }

    @Test
    void shouldMapLegacyUserToDomainModel() {
        mockServer.expect(requestTo("http://legacy.example/api/users?q=alice&page=0&size=20"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "data": [
                            {
                              "id": "u-1",
                              "user_name": "alice",
                              "display_name": "Alice",
                              "email": "alice@example.com",
                              "role": "ADMIN",
                              "is_active": true,
                              "create_time": "2026-04-01 08:30:00"
                            }
                          ],
                          "page": 0,
                          "size": 20,
                          "total": 1
                        }
                        """, MediaType.APPLICATION_JSON));

        PageResult<UserProfile> result = legacyUserRestClient.search("alice", null, 0, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.servedBy()).isEqualTo(ServedBy.LEGACY);
        assertThat(result.items().getFirst().username()).isEqualTo("alice");
        mockServer.verify();
    }

    @Test
    void shouldThrowLegacySystemExceptionWhenUpstreamFails() {
        mockServer.expect(requestTo("http://legacy.example/api/users?q=alice&page=0&size=20"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> legacyUserRestClient.search("alice", null, 0, 20))
                .isInstanceOf(LegacySystemException.class)
                .hasMessageContaining("旧系统调用失败");
    }

    @Test
    void shouldReadActiveUserCountFromDedicatedEndpoint() {
        mockServer.expect(requestTo("http://legacy.example/api/users/active-count"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "activeUsers": 12
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(legacyUserRestClient.countActiveUsers().count()).isEqualTo(12);
    }
}
