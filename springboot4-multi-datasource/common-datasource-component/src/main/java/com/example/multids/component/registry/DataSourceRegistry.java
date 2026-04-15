package com.example.multids.component.registry;

import com.example.multids.component.config.MultiDataSourceProperties.Topology;
import com.example.multids.component.exception.UnknownDataSourceException;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源注册表。
 */
public class DataSourceRegistry {

    private final Map<String, DataSource> sources = new ConcurrentHashMap<>();
    private final Map<String, Topology> topologies = new ConcurrentHashMap<>();

    public void register(String name, DataSource dataSource, Topology topology) {
        if (sources.putIfAbsent(name, dataSource) != null) {
            throw new IllegalArgumentException("重复注册数据源：" + name);
        }
        topologies.put(name, topology);
    }

    public DataSource resolve(String name) {
        DataSource dataSource = sources.get(name);
        if (dataSource == null) {
            throw new UnknownDataSourceException(name);
        }
        return dataSource;
    }

    public Topology topologyOf(String name) {
        Topology topology = topologies.get(name);
        if (topology == null) {
            throw new UnknownDataSourceException(name);
        }
        return topology;
    }

    public Collection<String> allNames() {
        return Collections.unmodifiableSet(sources.keySet());
    }

    public Map<String, DataSource> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(sources));
    }
}
