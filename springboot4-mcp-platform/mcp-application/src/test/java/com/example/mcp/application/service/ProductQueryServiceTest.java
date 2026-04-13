package com.example.mcp.application.service;

import com.example.mcp.application.config.MigrationFlagsProperties;
import com.example.mcp.domain.common.ActiveProductCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.ServedBy;
import com.example.mcp.domain.model.ProductRecord;
import com.example.mcp.domain.port.out.LegacyProductGateway;
import com.example.mcp.domain.port.out.NewProductQueryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductQueryServiceTest {

    private LegacyProductGateway legacyProductGateway;
    private NewProductQueryGateway newProductQueryGateway;
    private MigrationFlagsProperties migrationFlagsProperties;

    @BeforeEach
    void setUp() {
        legacyProductGateway = Mockito.mock(LegacyProductGateway.class);
        newProductQueryGateway = Mockito.mock(NewProductQueryGateway.class);
        migrationFlagsProperties = new MigrationFlagsProperties();
    }

    @Test
    void shouldUseLegacyGatewayWhenFlagIsDisabled() {
        migrationFlagsProperties.setProductSourceNew(false);
        ProductQueryService service = new ProductQueryService(
                legacyProductGateway,
                newProductQueryGateway,
                migrationFlagsProperties
        );

        PageResult<ProductRecord> expected = new PageResult<>(
                List.of(sampleProduct("legacy-product-1")),
                0,
                20,
                1,
                ServedBy.LEGACY
        );
        when(legacyProductGateway.search("analytics", null, 0, 20)).thenReturn(expected);

        PageResult<ProductRecord> actual = service.search("analytics", null, 0, 20);

        assertThat(actual.servedBy()).isEqualTo(ServedBy.LEGACY);
        verify(legacyProductGateway).search("analytics", null, 0, 20);
    }

    @Test
    void shouldUseNewGatewayWhenFlagIsEnabled() {
        migrationFlagsProperties.setProductSourceNew(true);
        ProductQueryService service = new ProductQueryService(
                legacyProductGateway,
                newProductQueryGateway,
                migrationFlagsProperties
        );

        when(newProductQueryGateway.countActiveProducts()).thenReturn(new ActiveProductCount(9, ServedBy.NEW));

        ActiveProductCount result = service.countActiveProducts();

        assertThat(result.servedBy()).isEqualTo(ServedBy.NEW);
        verify(newProductQueryGateway).countActiveProducts();
    }

    @Test
    void shouldThrowWhenQueryIsBlank() {
        ProductQueryService service = new ProductQueryService(
                legacyProductGateway,
                newProductQueryGateway,
                migrationFlagsProperties
        );

        assertThatThrownBy(() -> service.search(" ", null, 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("商品查询关键字不能为空");
    }

    @Test
    void shouldReturnDetailWhenProductExists() {
        migrationFlagsProperties.setProductSourceNew(true);
        ProductQueryService service = new ProductQueryService(
                legacyProductGateway,
                newProductQueryGateway,
                migrationFlagsProperties
        );
        when(newProductQueryGateway.findById("p-1"))
                .thenReturn(Optional.of(new ItemResult<>(sampleProduct("p-1"), ServedBy.NEW)));

        ItemResult<ProductRecord> result = service.findById("p-1");

        assertThat(result.data().id()).isEqualTo("p-1");
        assertThat(result.servedBy()).isEqualTo(ServedBy.NEW);
    }

    private ProductRecord sampleProduct(String id) {
        return new ProductRecord(
                id,
                "PRD-2026-0001",
                "Analytics Suite",
                "SOFTWARE",
                new BigDecimal("4999.00"),
                "CNY",
                true,
                Instant.parse("2026-04-01T00:00:00Z")
        );
    }
}
