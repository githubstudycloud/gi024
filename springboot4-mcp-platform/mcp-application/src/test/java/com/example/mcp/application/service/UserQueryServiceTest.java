package com.example.mcp.application.service;

import com.example.mcp.application.config.MigrationFlagsProperties;
import com.example.mcp.domain.common.ActiveUserCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.ServedBy;
import com.example.mcp.domain.model.UserProfile;
import com.example.mcp.domain.port.out.LegacyUserGateway;
import com.example.mcp.domain.port.out.NewUserQueryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserQueryServiceTest {

    private LegacyUserGateway legacyUserGateway;
    private NewUserQueryGateway newUserQueryGateway;
    private MigrationFlagsProperties migrationFlagsProperties;

    @BeforeEach
    void setUp() {
        legacyUserGateway = Mockito.mock(LegacyUserGateway.class);
        newUserQueryGateway = Mockito.mock(NewUserQueryGateway.class);
        migrationFlagsProperties = new MigrationFlagsProperties();
    }

    @Test
    void shouldUseLegacyGatewayWhenFlagIsDisabled() {
        migrationFlagsProperties.setUserSourceNew(false);
        UserQueryService service = new UserQueryService(
                legacyUserGateway,
                newUserQueryGateway,
                migrationFlagsProperties
        );

        PageResult<UserProfile> expected = new PageResult<>(
                List.of(sampleUser("legacy-1")),
                0,
                20,
                1,
                ServedBy.LEGACY
        );
        when(legacyUserGateway.search("alice", null, 0, 20)).thenReturn(expected);

        PageResult<UserProfile> actual = service.search("alice", null, 0, 20);

        assertThat(actual.servedBy()).isEqualTo(ServedBy.LEGACY);
        verify(legacyUserGateway).search("alice", null, 0, 20);
    }

    @Test
    void shouldUseNewGatewayWhenFlagIsEnabled() {
        migrationFlagsProperties.setUserSourceNew(true);
        UserQueryService service = new UserQueryService(
                legacyUserGateway,
                newUserQueryGateway,
                migrationFlagsProperties
        );

        when(newUserQueryGateway.countActiveUsers()).thenReturn(new ActiveUserCount(3, ServedBy.NEW));

        ActiveUserCount result = service.countActiveUsers();

        assertThat(result.servedBy()).isEqualTo(ServedBy.NEW);
        verify(newUserQueryGateway).countActiveUsers();
    }

    @Test
    void shouldThrowWhenQueryIsBlank() {
        UserQueryService service = new UserQueryService(
                legacyUserGateway,
                newUserQueryGateway,
                migrationFlagsProperties
        );

        assertThatThrownBy(() -> service.search("  ", null, 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("查询关键字不能为空");
    }

    @Test
    void shouldReturnDetailWhenUserExists() {
        migrationFlagsProperties.setUserSourceNew(true);
        UserQueryService service = new UserQueryService(
                legacyUserGateway,
                newUserQueryGateway,
                migrationFlagsProperties
        );
        when(newUserQueryGateway.findById("u-1"))
                .thenReturn(Optional.of(new ItemResult<>(sampleUser("u-1"), ServedBy.NEW)));

        ItemResult<UserProfile> result = service.findById("u-1");

        assertThat(result.data().id()).isEqualTo("u-1");
        assertThat(result.servedBy()).isEqualTo(ServedBy.NEW);
    }

    private UserProfile sampleUser(String id) {
        return new UserProfile(
                id,
                "alice",
                "Alice",
                "alice@example.com",
                "ADMIN",
                true,
                Instant.parse("2026-04-01T00:00:00Z")
        );
    }
}
