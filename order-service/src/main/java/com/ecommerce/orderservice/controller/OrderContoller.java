package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderContoller {
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest) {
        orderService.placeOrder(orderRequest);
        return "Order Placed Succesfully";
        // return CompletableFuture.supplyAsync(() -> orderService.placeOrder(orderRequest));
    }

    // public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException) {
    //     log.info("Cannot Place Order Executing Fallback logic");
    //     return CompletableFuture.supplyAsync(() -> "Oops! Something went wrong, please order after some time!");
    // }
}
