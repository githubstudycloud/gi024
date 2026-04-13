package com.example.mcp.application.service;

import com.example.mcp.application.config.MigrationFlagsProperties;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.PendingOrderCount;
import com.example.mcp.domain.common.ServedBy;
import com.example.mcp.domain.model.OrderRecord;
import com.example.mcp.domain.port.out.LegacyOrderGateway;
import com.example.mcp.domain.port.out.NewOrderQueryGateway;
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

class OrderQueryServiceTest {

    private LegacyOrderGateway legacyOrderGateway;
    private NewOrderQueryGateway newOrderQueryGateway;
    private MigrationFlagsProperties migrationFlagsProperties;

    @BeforeEach
    void setUp() {
        legacyOrderGateway = Mockito.mock(LegacyOrderGateway.class);
        newOrderQueryGateway = Mockito.mock(NewOrderQueryGateway.class);
        migrationFlagsProperties = new MigrationFlagsProperties();
    }

    @Test
    void shouldUseLegacyGatewayWhenFlagIsDisabled() {
        migrationFlagsProperties.setOrderSourceNew(false);
        OrderQueryService service = new OrderQueryService(
                legacyOrderGateway,
                newOrderQueryGateway,
                migrationFlagsProperties
        );

        PageResult<OrderRecord> expected = new PageResult<>(
                List.of(sampleOrder("legacy-order-1")),
                0,
                20,
                1,
                ServedBy.LEGACY
        );
        when(legacyOrderGateway.search("acme", null, 0, 20)).thenReturn(expected);

        PageResult<OrderRecord> actual = service.search("acme", null, 0, 20);

        assertThat(actual.servedBy()).isEqualTo(ServedBy.LEGACY);
        verify(legacyOrderGateway).search("acme", null, 0, 20);
    }

    @Test
    void shouldUseNewGatewayWhenFlagIsEnabled() {
        migrationFlagsProperties.setOrderSourceNew(true);
        OrderQueryService service = new OrderQueryService(
                legacyOrderGateway,
                newOrderQueryGateway,
                migrationFlagsProperties
        );

        when(newOrderQueryGateway.countPendingOrders()).thenReturn(new PendingOrderCount(5, ServedBy.NEW));

        PendingOrderCount result = service.countPendingOrders();

        assertThat(result.servedBy()).isEqualTo(ServedBy.NEW);
        verify(newOrderQueryGateway).countPendingOrders();
    }

    @Test
    void shouldThrowWhenQueryIsBlank() {
        OrderQueryService service = new OrderQueryService(
                legacyOrderGateway,
                newOrderQueryGateway,
                migrationFlagsProperties
        );

        assertThatThrownBy(() -> service.search(" ", null, 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("订单查询关键字不能为空");
    }

    @Test
    void shouldReturnDetailWhenOrderExists() {
        migrationFlagsProperties.setOrderSourceNew(true);
        OrderQueryService service = new OrderQueryService(
                legacyOrderGateway,
                newOrderQueryGateway,
                migrationFlagsProperties
        );
        when(newOrderQueryGateway.findById("o-1"))
                .thenReturn(Optional.of(new ItemResult<>(sampleOrder("o-1"), ServedBy.NEW)));

        ItemResult<OrderRecord> result = service.findById("o-1");

        assertThat(result.data().id()).isEqualTo("o-1");
        assertThat(result.servedBy()).isEqualTo(ServedBy.NEW);
    }

    private OrderRecord sampleOrder(String id) {
        return new OrderRecord(
                id,
                "SO-2026-0001",
                "Acme Corp",
                "PENDING",
                new BigDecimal("1999.90"),
                "CNY",
                Instant.parse("2026-04-01T00:00:00Z")
        );
    }
}
