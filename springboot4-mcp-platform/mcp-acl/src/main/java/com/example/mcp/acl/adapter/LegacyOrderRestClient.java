package com.example.mcp.acl.adapter;

import com.example.mcp.acl.dto.LegacyOrderItemResponse;
import com.example.mcp.acl.dto.LegacyOrderPageResponse;
import com.example.mcp.acl.dto.LegacyPendingOrderCountResponse;
import com.example.mcp.acl.mapper.LegacyOrderMapper;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.PendingOrderCount;
import com.example.mcp.domain.common.ServedBy;
import com.example.mcp.domain.exception.LegacySystemException;
import com.example.mcp.domain.model.OrderRecord;
import com.example.mcp.domain.port.out.LegacyOrderGateway;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Optional;

/**
 * 使用 RestClient 访问旧系统订单接口。
 */
@Component
@Observed(name = "legacy.order.gateway")
public class LegacyOrderRestClient implements LegacyOrderGateway {

    private final RestClient legacyRestClient;
    private final LegacyOrderMapper legacyOrderMapper;

    public LegacyOrderRestClient(RestClient legacyRestClient, LegacyOrderMapper legacyOrderMapper) {
        this.legacyRestClient = legacyRestClient;
        this.legacyOrderMapper = legacyOrderMapper;
    }

    @Override
    public PageResult<OrderRecord> search(String query, String status, int page, int size) {
        try {
            LegacyOrderPageResponse response = legacyRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/orders")
                                .queryParam("q", query)
                                .queryParam("page", page)
                                .queryParam("size", size);
                        if (StringUtils.hasText(status)) {
                            uriBuilder.queryParam("status", status);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(LegacyOrderPageResponse.class);

            if (response == null) {
                throw new LegacySystemException(502, "旧系统返回空订单分页响应", "LEGACY_EMPTY_ORDER_PAGE");
            }

            List<OrderRecord> items = Optional.ofNullable(response.data())
                    .orElseGet(List::of)
                    .stream()
                    .map(legacyOrderMapper::toOrderRecord)
                    .toList();

            return new PageResult<>(items, response.page(), response.size(), response.total(), ServedBy.LEGACY);
        } catch (RestClientResponseException ex) {
            throw toLegacyException(ex);
        } catch (RestClientException ex) {
            throw new LegacySystemException(502, "调用旧系统订单分页接口失败: " + ex.getMessage(), "LEGACY_CLIENT_ERROR");
        }
    }

    @Override
    public Optional<ItemResult<OrderRecord>> findById(String id) {
        try {
            LegacyOrderItemResponse response = legacyRestClient.get()
                    .uri("/api/orders/{id}", id)
                    .retrieve()
                    .body(LegacyOrderItemResponse.class);

            if (response == null) {
                return Optional.empty();
            }

            return Optional.of(new ItemResult<>(legacyOrderMapper.toOrderRecord(response), ServedBy.LEGACY));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw toLegacyException(ex);
        } catch (RestClientException ex) {
            throw new LegacySystemException(502, "调用旧系统订单详情接口失败: " + ex.getMessage(), "LEGACY_CLIENT_ERROR");
        }
    }

    @Override
    public PendingOrderCount countPendingOrders() {
        try {
            LegacyPendingOrderCountResponse response = legacyRestClient.get()
                    .uri("/api/orders/pending-count")
                    .retrieve()
                    .body(LegacyPendingOrderCountResponse.class);

            if (response == null) {
                throw new LegacySystemException(502, "旧系统返回空订单统计响应", "LEGACY_EMPTY_ORDER_COUNT");
            }

            return new PendingOrderCount(response.pendingOrders(), ServedBy.LEGACY);
        } catch (RestClientResponseException ex) {
            throw toLegacyException(ex);
        } catch (RestClientException ex) {
            throw new LegacySystemException(502, "调用旧系统订单统计接口失败: " + ex.getMessage(), "LEGACY_CLIENT_ERROR");
        }
    }

    private LegacySystemException toLegacyException(RestClientResponseException ex) {
        return new LegacySystemException(
                ex.getStatusCode().value(),
                "旧系统订单调用失败: HTTP " + ex.getStatusCode().value(),
                "LEGACY_ORDER_HTTP_" + ex.getStatusCode().value()
        );
    }
}
