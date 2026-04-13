package com.example.mcp.acl.adapter;

import com.example.mcp.acl.dto.LegacyActiveUserCountResponse;
import com.example.mcp.acl.dto.LegacyUserItemResponse;
import com.example.mcp.acl.dto.LegacyUserPageResponse;
import com.example.mcp.acl.mapper.LegacyUserMapper;
import com.example.mcp.domain.common.ActiveUserCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.ServedBy;
import com.example.mcp.domain.exception.LegacySystemException;
import com.example.mcp.domain.model.UserProfile;
import com.example.mcp.domain.port.out.LegacyUserGateway;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Optional;

/**
 * 使用 RestClient 访问旧系统用户接口。
 */
@Component
@Observed(name = "legacy.user.gateway")
public class LegacyUserRestClient implements LegacyUserGateway {

    private final RestClient legacyRestClient;
    private final LegacyUserMapper legacyUserMapper;

    public LegacyUserRestClient(RestClient legacyRestClient, LegacyUserMapper legacyUserMapper) {
        this.legacyRestClient = legacyRestClient;
        this.legacyUserMapper = legacyUserMapper;
    }

    @Override
    public PageResult<UserProfile> search(String query, String role, int page, int size) {
        try {
            LegacyUserPageResponse response = legacyRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/users")
                                .queryParam("q", query)
                                .queryParam("page", page)
                                .queryParam("size", size);
                        if (StringUtils.hasText(role)) {
                            uriBuilder.queryParam("role", role);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(LegacyUserPageResponse.class);

            if (response == null) {
                throw new LegacySystemException(502, "旧系统返回空分页响应", "LEGACY_EMPTY_PAGE");
            }

            List<UserProfile> items = Optional.ofNullable(response.data())
                    .orElseGet(List::of)
                    .stream()
                    .map(legacyUserMapper::toUserProfile)
                    .toList();

            return new PageResult<>(items, response.page(), response.size(), response.total(), ServedBy.LEGACY);
        } catch (RestClientResponseException ex) {
            throw toLegacyException(ex);
        } catch (RestClientException ex) {
            throw new LegacySystemException(502, "调用旧系统分页接口失败: " + ex.getMessage(), "LEGACY_CLIENT_ERROR");
        }
    }

    @Override
    public Optional<ItemResult<UserProfile>> findById(String id) {
        try {
            LegacyUserItemResponse response = legacyRestClient.get()
                    .uri("/api/users/{id}", id)
                    .retrieve()
                    .body(LegacyUserItemResponse.class);

            if (response == null) {
                return Optional.empty();
            }

            return Optional.of(new ItemResult<>(legacyUserMapper.toUserProfile(response), ServedBy.LEGACY));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw toLegacyException(ex);
        } catch (RestClientException ex) {
            throw new LegacySystemException(502, "调用旧系统详情接口失败: " + ex.getMessage(), "LEGACY_CLIENT_ERROR");
        }
    }

    @Override
    public ActiveUserCount countActiveUsers() {
        try {
            LegacyActiveUserCountResponse response = legacyRestClient.get()
                    .uri("/api/users/active-count")
                    .retrieve()
                    .body(LegacyActiveUserCountResponse.class);

            if (response == null) {
                throw new LegacySystemException(502, "旧系统返回空统计响应", "LEGACY_EMPTY_COUNT");
            }

            return new ActiveUserCount(response.activeUsers(), ServedBy.LEGACY);
        } catch (RestClientResponseException ex) {
            throw toLegacyException(ex);
        } catch (RestClientException ex) {
            throw new LegacySystemException(502, "调用旧系统统计接口失败: " + ex.getMessage(), "LEGACY_CLIENT_ERROR");
        }
    }

    private LegacySystemException toLegacyException(RestClientResponseException ex) {
        return new LegacySystemException(
                ex.getStatusCode().value(),
                "旧系统调用失败: HTTP " + ex.getStatusCode().value(),
                "LEGACY_HTTP_" + ex.getStatusCode().value()
        );
    }
}
