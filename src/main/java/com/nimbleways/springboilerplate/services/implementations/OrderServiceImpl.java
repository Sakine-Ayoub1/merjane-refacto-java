package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.OrderService;
import com.nimbleways.springboilerplate.utils.Constantes;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class OrderServiceImpl implements OrderService {
    
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(ProductService productService,
                        ProductRepository productRepository,
                        OrderRepository orderRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public ProcessOrderResponse processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        order.getItems().forEach(this::processProduct);

        return new ProcessOrderResponse(order.getId());
    }

    private void processProduct(Product product) {
        switch (product.getType()) {
            case Constantes.NORMAL_PRODUCT -> handleNormalProduct(product);
            case Constantes.SEASONAL_PRODUCT -> handleSeasonalProduct(product);
            case Constantes.EXPIRED_PRODUCT -> checkIfProductIsExpired(product);
            default -> throw new IllegalArgumentException("Unknown product type: " + product.getType());
        }
    }

    private void handleNormalProduct(Product p) {
        if (p.getAvailable() > 0) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else if (p.getLeadTime() > 0) {
            productService.notifyDelay(p.getLeadTime(), p);
        }
    }

    private void handleSeasonalProduct(Product p) {
        LocalDate now = LocalDate.now();
        if (now.isAfter(p.getSeasonStartDate()) && now.isBefore(p.getSeasonEndDate()) && p.getAvailable() > 0) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else {
            productService.handleSeasonalProduct(p);
        }
    }

    private void checkIfProductIsExpired(Product p) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else {
            productService.handleExpiredProduct(p);
        }
    }
}
