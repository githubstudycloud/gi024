package com.example.mcp.acl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 旧系统商品分页 data 节点。
 */
public record LegacyProductPagePayload(
        @JsonProperty("records")
        List<LegacyProductItemResponse> records,
        @JsonProperty("pageNo")
        int pageNo,
        @JsonProperty("pageSize")
        int pageSize,
        @JsonProperty("totalCount")
        long totalCount
) {
}
