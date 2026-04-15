package com.example.multids.testapp.service;

import com.example.multids.component.routing.UseDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 公共库字典查询服务。
 */
@Service
public class CountryDirectoryService {

    private final JdbcTemplate jdbcTemplate;

    public CountryDirectoryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @UseDataSource("public-shared")
    @Transactional(readOnly = true)
    public List<String> listCountryNames() {
        return jdbcTemplate.queryForList(
                "SELECT name FROM country_dict ORDER BY code",
                String.class
        );
    }
}
