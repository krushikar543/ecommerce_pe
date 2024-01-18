package com.ecommerce.orderservice.controller;

public @interface CircuitBreaker {

    String name();

    String fallbackMethod();

}
