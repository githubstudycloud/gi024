package com.example.mcp.acl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 旧系统订单分页 data 节点。
 */
public record LegacyOrderPagePayload(
        @JsonProperty("records")
        List<LegacyOrderItemResponse> records,
        @JsonProperty("pageNo")
        int pageNo,
        @JsonProperty("pageSize")
        int pageSize,
        @JsonProperty("totalCount")
        long totalCount
) {
}
