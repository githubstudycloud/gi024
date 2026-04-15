package com.example.multids.component.routing;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 基于线程上下文的数据源路由器。
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<Deque<String>> CURRENT = ThreadLocal.withInitial(ArrayDeque::new);

    public static void push(String dataSourceName) {
        CURRENT.get().push(dataSourceName);
    }

    public static void pop() {
        Deque<String> stack = CURRENT.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            CURRENT.remove();
        }
    }

    public static String current() {
        Deque<String> stack = CURRENT.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return current();
    }
}
