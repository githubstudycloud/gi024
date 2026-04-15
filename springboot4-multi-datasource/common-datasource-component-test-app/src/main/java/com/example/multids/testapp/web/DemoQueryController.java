package com.example.multids.testapp.web;

import com.example.multids.testapp.service.CountryDirectoryService;
import com.example.multids.testapp.service.OrderStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 暴露最小查询接口，便于验证组件效果。
 */
@RestController
@RequestMapping("/api/demo")
public class DemoQueryController {

    private final CountryDirectoryService countryDirectoryService;
    private final OrderStatisticsService orderStatisticsService;

    public DemoQueryController(
            CountryDirectoryService countryDirectoryService,
            OrderStatisticsService orderStatisticsService
    ) {
        this.countryDirectoryService = countryDirectoryService;
        this.orderStatisticsService = orderStatisticsService;
    }

    @GetMapping("/countries")
    public List<String> listCountries() {
        return countryDirectoryService.listCountryNames();
    }

    @GetMapping("/orders/summary")
    public OrderSummaryResponse orderSummary() {
        return new OrderSummaryResponse(
                orderStatisticsService.countOrders(),
                orderStatisticsService.totalAmount()
        );
    }
}
