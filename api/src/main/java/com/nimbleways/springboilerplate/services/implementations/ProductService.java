package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import com.nimbleways.springboilerplate.Utils.Helper;
import com.nimbleways.springboilerplate.enums.ProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    NotificationService notificationService;

    public void notifyDelay(int leadTime, Product p) {
        p.setLeadTime(leadTime);
        productRepository.save(p);
        notificationService.sendDelayNotification(leadTime, p.getName());
    }

    public void handleSeasonalProduct(Product p) {
        if (LocalDate.now().plusDays(p.getLeadTime()).isAfter(p.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(p.getName());
            p.setAvailable(0);
            productRepository.save(p);
        } else if (p.getSeasonStartDate().isAfter(LocalDate.now())) {
            notificationService.sendOutOfStockNotification(p.getName());
            productRepository.save(p);
        } else {
            notifyDelay(p.getLeadTime(), p);
        }
    }

    public void handleExpiredProduct(Product p) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else {
            notificationService.sendExpirationNotification(p.getName(), p.getExpiryDate());
            p.setAvailable(0);
            productRepository.save(p);
        }
    }

    public void processProduct(Product product) {
        if (product.getType().equals(ProductType.Normal.name())) {
            if (product.getAvailable() > 0) {
                product.setAvailable(product.getAvailable() - 1);
                productRepository.save(product);
            } else {
                int leadTime = product.getLeadTime();
                if (leadTime > 0) {
                    notifyDelay(leadTime, product);
                }
            }
        } else if (product.getType().equals(ProductType.SEASONAL.name())) {
            if (Helper.isWithinSeason(product.getSeasonStartDate(), product.getSeasonEndDate())
                    && product.getAvailable() > 0) {
                product.setAvailable(product.getAvailable() - 1);
                productRepository.save(product);
            } else {
                handleSeasonalProduct(product);
            }
        } else if (product.getType().equals(ProductType.EXPIRABLE.name())) {
            if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
                product.setAvailable(product.getAvailable() - 1);
                productRepository.save(product);
            } else {
                handleExpiredProduct(product);
            }
        }
    }
}