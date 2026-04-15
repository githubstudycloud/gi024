package com.example.multids.component.routing;

import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 读写分离数据源。
 */
public class ReadWriteSplittingDataSource extends AbstractDataSource {

    private final DataSource primary;
    private final List<DataSource> replicas;
    private final AtomicInteger roundRobin = new AtomicInteger(0);

    public ReadWriteSplittingDataSource(DataSource primary, List<DataSource> replicas) {
        this.primary = primary;
        this.replicas = List.copyOf(replicas);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return selectTarget().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return selectTarget().getConnection(username, password);
    }

    private DataSource selectTarget() {
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly() && !replicas.isEmpty()) {
            int index = Math.floorMod(roundRobin.getAndIncrement(), replicas.size());
            return replicas.get(index);
        }
        return primary;
    }
}
