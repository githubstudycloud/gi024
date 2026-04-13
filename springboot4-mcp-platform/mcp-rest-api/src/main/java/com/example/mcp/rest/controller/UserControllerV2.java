package com.example.mcp.rest.controller;

import com.example.mcp.domain.port.in.UserQueryUseCase;
import com.example.mcp.rest.dto.UserDetailResponse;
import com.example.mcp.rest.dto.UserPageResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户域 v2 REST 接口。
 */
@Validated
@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {

    private final UserQueryUseCase userQueryUseCase;

    public UserControllerV2(UserQueryUseCase userQueryUseCase) {
        this.userQueryUseCase = userQueryUseCase;
    }

    @GetMapping
    public UserPageResponse search(
            @RequestParam @NotBlank(message = "查询关键字不能为空") String query,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "页码不能小于 0") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "每页大小必须大于 0") int size
    ) {
        return UserPageResponse.from(userQueryUseCase.search(query, role, page, size));
    }

    @GetMapping("/{id}")
    public UserDetailResponse getById(@PathVariable String id) {
        return UserDetailResponse.from(userQueryUseCase.findById(id));
    }
}
