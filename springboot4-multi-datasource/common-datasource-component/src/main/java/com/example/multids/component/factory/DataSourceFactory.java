package com.example.multids.component.factory;

import com.example.multids.component.config.MultiDataSourceProperties.DataSourceEntry;

import javax.sql.DataSource;

/**
 * 数据源工厂接口。
 */
public interface DataSourceFactory {

    DataSource create(DataSourceEntry entry);
}
