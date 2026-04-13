package com.example.mcp.acl.adapter;

import com.example.mcp.acl.config.LegacySystemProperties;
import com.example.mcp.acl.dto.LegacyEnvelopeResponse;
import com.example.mcp.acl.dto.LegacyOrderItemResponse;
import com.example.mcp.acl.dto.LegacyOrderPagePayload;
import com.example.mcp.acl.dto.LegacyPendingOrderCountPayload;
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
    private final LegacySystemProperties legacySystemProperties;

    public LegacyOrderRestClient(
            RestClient legacyRestClient,
            LegacyOrderMapper legacyOrderMapper,
            LegacySystemProperties legacySystemProperties
    ) {
        this.legacyRestClient = legacyRestClient;
        this.legacyOrderMapper = legacyOrderMapper;
        this.legacySystemProperties = legacySystemProperties;
    }

    @Override
    public PageResult<OrderRecord> search(String query, String status, int page, int size) {
        try {
            LegacyEnvelopeResponse<LegacyOrderPagePayload> response = legacyRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(legacySystemProperties.getOrder().getSearchPath())
                                .queryParam("q", query)
                                .queryParam("pageNo", page + 1)
                                .queryParam("pageSize", size);
                        if (StringUtils.hasText(status)) {
                            uriBuilder.queryParam("status", status);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<LegacyEnvelopeResponse<LegacyOrderPagePayload>>() {
                    });

            if (response == null || response.data() == null) {
                throw new LegacySystemException(502, "旧系统返回空订单分页响应", "LEGACY_EMPTY_ORDER_PAGE");
            }
            assertSuccess(response.code(), response.message(), "LEGACY_ORDER_PAGE_CODE_ERROR");

            List<OrderRecord> items = Optional.ofNullable(response.data().records())
                    .orElseGet(List::of)
                    .stream()
                    .map(legacyOrderMapper::toOrderRecord)
                    .toList();

            return new PageResult<>(
                    items,
                    Math.max(response.data().pageNo() - 1, 0),
                    response.data().pageSize(),
                    response.data().totalCount(),
                    ServedBy.LEGACY
            );
        } catch (RestClientResponseException ex) {
            throw toLegacyException(ex);
        } catch (RestClientException ex) {
            throw new LegacySystemException(502, "调用旧系统订单分页接口失败: " + ex.getMessage(), "LEGACY_CLIENT_ERROR");
        }
    }

    @Override
    public Optional<ItemResult<OrderRecord>> findById(String id) {
        try {
            LegacyEnvelopeResponse<LegacyOrderItemResponse> response = legacyRestClient.get()
                    .uri(legacySystemProperties.getOrder().getDetailPath(), id)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<LegacyEnvelopeResponse<LegacyOrderItemResponse>>() {
                    });

            if (response == null || response.data() == null) {
                return Optional.empty();
            }
            assertSuccess(response.code(), response.message(), "LEGACY_ORDER_DETAIL_CODE_ERROR");

            return Optional.of(new ItemResult<>(legacyOrderMapper.toOrderRecord(response.data()), ServedBy.LEGACY));
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
            LegacyEnvelopeResponse<LegacyPendingOrderCountPayload> response = legacyRestClient.get()
                    .uri(legacySystemProperties.getOrder().getPendingCountPath())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<LegacyEnvelopeResponse<LegacyPendingOrderCountPayload>>() {
                    });

            if (response == null || response.data() == null) {
                throw new LegacySystemException(502, "旧系统返回空订单统计响应", "LEGACY_EMPTY_ORDER_COUNT");
            }
            assertSuccess(response.code(), response.message(), "LEGACY_ORDER_COUNT_CODE_ERROR");

            return new PendingOrderCount(response.data().pendingCount(), ServedBy.LEGACY);
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

    private void assertSuccess(String code, String message, String legacyCode) {
        if (!legacySystemProperties.getSuccessCode().equals(code)) {
            throw new LegacySystemException(502, "旧系统订单返回业务失败: " + message, legacyCode);
        }
    }
}
