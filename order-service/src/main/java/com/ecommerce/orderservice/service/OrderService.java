package com.ecommerce.orderservice.service;

import java.util.Arrays;
// import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.ecommerce.orderservice.dto.InventoryResponse;

// import org.springframework.context.ApplicationEventPublisher;

import com.ecommerce.orderservice.dto.OrderLineItemsDto;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.event.OrderPlacedEvent;
// import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderLineItems;
import com.ecommerce.orderservice.repository.OrderRepository;

// import io.micrometer.observation.Observation;
// import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
// import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    // private final ObservationRegistry observationRegistry;
    // private final ApplicationEventPublisher applicationEventPublisher;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);


        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory", 
                    uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);



        if(allProductsInStock){
            orderRepository.save(order);
            kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
        }
        else{
            throw new IllegalArgumentException("Product is not in stock.");
        }

        // Observation inventoryServiceObservation = Observation.createNotStarted("inventory-service-lookup",
        //         this.observationRegistry);
        // inventoryServiceObservation.lowCardinalityKeyValue("call", "inventory-service");
        // return inventoryServiceObservation.observe(() -> {
        //     InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
        //             .uri("http://inventory-service/api/inventory",
        //                     uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
        //             .retrieve()
        //             .bodyToMono(InventoryResponse[].class)
        //             .block();

        //     boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
        //             .allMatch(InventoryResponse::isInStock);

        //     if (allProductsInStock) {
        //         orderRepository.save(order);
        //         // publish Order Placed Event
        //         applicationEventPublisher.publishEvent(new OrderPlacedEvent(this, order.getOrderNumber()));
        //         return "Order Placed";
        //     } else {
        //         throw new IllegalArgumentException("Product is not in stock, please try again later");
        //     }
        // });

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
