package com.ecommerce.orderservice.event;


// import org.springframework.context.ApplicationEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedEvent{
    private String orderNumber;
}