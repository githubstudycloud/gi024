package com.example.multids.component.exception;

/**
 * 未知数据源异常。
 */
public class UnknownDataSourceException extends IllegalArgumentException {

    public UnknownDataSourceException(String dataSourceName) {
        super("未找到名为 %s 的数据源".formatted(dataSourceName));
    }
}
