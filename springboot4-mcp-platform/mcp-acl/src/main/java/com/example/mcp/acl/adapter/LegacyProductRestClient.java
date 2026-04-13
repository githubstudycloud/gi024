package com.example.mcp.acl.adapter;

import com.example.mcp.acl.config.LegacySystemProperties;
import com.example.mcp.acl.dto.LegacyActiveProductCountPayload;
import com.example.mcp.acl.dto.LegacyEnvelopeResponse;
import com.example.mcp.acl.dto.LegacyProductItemResponse;
import com.example.mcp.acl.dto.LegacyProductPagePayload;
import com.example.mcp.acl.mapper.LegacyProductMapper;
import com.example.mcp.domain.common.ActiveProductCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.ServedBy;
import com.example.mcp.domain.exception.LegacySystemException;
import com.example.mcp.domain.model.ProductRecord;
import com.example.mcp.domain.port.out.LegacyProductGateway;
import io.micrometer.observation.annotation.Observed;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Optional;

/**
 * 使用 RestClient 访问旧系统商品接口。
 */
@Component
@Observed(name = "legacy.product.gateway")
public class LegacyProductRestClient implements LegacyProductGateway {

    private final RestClient legacyRestClient;
    private final LegacyProductMapper legacyProductMapper;
    private final LegacySystemProperties legacySystemProperties;

    public LegacyProductRestClient(
            RestClient legacyRestClient,
            LegacyProductMapper legacyProductMapper,
            LegacySystemProperties legacySystemProperties
    ) {
        this.legacyRestClient = legacyRestClient;
        this.legacyProductMapper = legacyProductMapper;
        this.legacySystemProperties = legacySystemProperties;
    }

    @Override
    public PageResult<ProductRecord> search(String query, String category, int page, int size) {
        try {
            LegacyEnvelopeResponse<LegacyProductPagePayload> response = legacyRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(legacySystemProperties.getProduct().getSearchPath())
                                .queryParam("q", query)
                                .queryParam("pageNo", page + 1)
                                .queryParam("pageSize", size);
                        if (StringUtils.hasText(category)) {
                            uriBuilder.queryParam("category", category);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<LegacyEnvelopeResponse<LegacyProductPagePayload>>() {
                    });

            if (response == null || response.data() == null) {
                throw new LegacySystemException(502, "旧系统返回空商品分页响应", "LEGACY_EMPTY_PRODUCT_PAGE");
            }
            assertSuccess(response.code(), response.message(), "LEGACY_PRODUCT_PAGE_CODE_ERROR");

            List<ProductRecord> items = Optional.ofNullable(response.data().records())
                    .orElseGet(List::of)
                    .stream()
                    .map(legacyProductMapper::toProductRecord)
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
            throw new LegacySystemException(502, "调用旧系统商品分页接口失败: " + ex.getMessage(), "LEGACY_CLIENT_ERROR");
        }
    }

    @Override
    public Optional<ItemResult<ProductRecord>> findById(String id) {
        try {
            LegacyEnvelopeResponse<LegacyProductItemResponse> response = legacyRestClient.get()
                    .uri(legacySystemProperties.getProduct().getDetailPath(), id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<LegacyEnvelopeResponse<LegacyProductItemResponse>>() {
                    });

            if (response == null || response.data() == null) {
                return Optional.empty();
            }
            assertSuccess(response.code(), response.message(), "LEGACY_PRODUCT_DETAIL_CODE_ERROR");

            return Optional.of(new ItemResult<>(legacyProductMapper.toProductRecord(response.data()), ServedBy.LEGACY));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw toLegacyException(ex);
        } catch (RestClientException ex) {
            throw new LegacySystemException(502, "调用旧系统商品详情接口失败: " + ex.getMessage(), "LEGACY_CLIENT_ERROR");
        }
    }

    @Override
    public ActiveProductCount countActiveProducts() {
        try {
            LegacyEnvelopeResponse<LegacyActiveProductCountPayload> response = legacyRestClient.get()
                    .uri(legacySystemProperties.getProduct().getActiveCountPath())
                    .retrieve()
                    .body(new ParameterizedTypeReference<LegacyEnvelopeResponse<LegacyActiveProductCountPayload>>() {
                    });

            if (response == null || response.data() == null) {
                throw new LegacySystemException(502, "旧系统返回空商品统计响应", "LEGACY_EMPTY_PRODUCT_COUNT");
            }
            assertSuccess(response.code(), response.message(), "LEGACY_PRODUCT_COUNT_CODE_ERROR");

            return new ActiveProductCount(response.data().activeCount(), ServedBy.LEGACY);
        } catch (RestClientResponseException ex) {
            throw toLegacyException(ex);
        } catch (RestClientException ex) {
            throw new LegacySystemException(502, "调用旧系统商品统计接口失败: " + ex.getMessage(), "LEGACY_CLIENT_ERROR");
        }
    }

    private LegacySystemException toLegacyException(RestClientResponseException ex) {
        return new LegacySystemException(
                ex.getStatusCode().value(),
                "旧系统商品调用失败: HTTP " + ex.getStatusCode().value(),
                "LEGACY_PRODUCT_HTTP_" + ex.getStatusCode().value()
        );
    }

    private void assertSuccess(String code, String message, String legacyCode) {
        if (!legacySystemProperties.getSuccessCode().equals(code)) {
            throw new LegacySystemException(502, "旧系统商品返回业务失败: " + message, legacyCode);
        }
    }
}
