package com.example.mcp.domain.port.out;

import com.example.mcp.domain.common.ActiveUserCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.model.UserProfile;

import java.util.Optional;

/**
 * 新系统用户查询读接口。
 */
public interface NewUserQueryGateway {

    PageResult<UserProfile> search(String query, String role, int page, int size);

    Optional<ItemResult<UserProfile>> findById(String id);

    ActiveUserCount countActiveUsers();
}
