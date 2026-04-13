package com.teastall.service;

import com.teastall.dto.BillingDtos.*;
import com.teastall.model.MenuItem;
import com.teastall.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    public List<MenuItem> getAllAvailableItems() {
        return menuItemRepository.findByAvailableTrueOrderByCategory();
    }

    public List<MenuItem> getAllItems() {
        return menuItemRepository.findAll();
    }

    public MenuItem getItemById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + id));
    }

    public MenuItem createItem(MenuItemRequest req) {
        MenuItem item = MenuItem.builder()
                .name(req.getName())
                .category(req.getCategory())
                .price(req.getPrice())
                .icon(req.getIcon() != null ? req.getIcon() : "🍵")
                .available(req.getAvailable() != null ? req.getAvailable() : true)
                .build();
        return menuItemRepository.save(item);
    }

    public MenuItem updateItem(Long id, MenuItemRequest req) {
        MenuItem item = getItemById(id);
        if (req.getName() != null) item.setName(req.getName());
        if (req.getCategory() != null) item.setCategory(req.getCategory());
        if (req.getPrice() != null) item.setPrice(req.getPrice());
        if (req.getIcon() != null) item.setIcon(req.getIcon());
        if (req.getAvailable() != null) item.setAvailable(req.getAvailable());
        return menuItemRepository.save(item);
    }

    public void deleteItem(Long id) {
        MenuItem item = getItemById(id);
        item.setAvailable(false);
        menuItemRepository.save(item);
    }

    public void initDefaultMenu() {
        if (menuItemRepository.count() > 0) return;
        List<MenuItem> defaults = List.of(
            MenuItem.builder().name("Masala Chai").category("Tea").price(15.0).icon("🍵").available(true).build(),
            MenuItem.builder().name("Ginger Tea").category("Tea").price(15.0).icon("🍵").available(true).build(),
            MenuItem.builder().name("Lemon Tea").category("Tea").price(18.0).icon("🍵").available(true).build(),
            MenuItem.builder().name("Cardamom Tea").category("Tea").price(18.0).icon("🍵").available(true).build(),
            MenuItem.builder().name("Special Chai").category("Tea").price(25.0).icon("🍵").available(true).build(),
            MenuItem.builder().name("Green Tea").category("Tea").price(20.0).icon("🍵").available(true).build(),
            MenuItem.builder().name("Filter Coffee").category("Coffee").price(20.0).icon("☕").available(true).build(),
            MenuItem.builder().name("Black Coffee").category("Coffee").price(18.0).icon("☕").available(true).build(),
            MenuItem.builder().name("Milk Coffee").category("Coffee").price(22.0).icon("☕").available(true).build(),
            MenuItem.builder().name("Biscuits").category("Snacks").price(10.0).icon("🍪").available(true).build(),
            MenuItem.builder().name("Bread Toast").category("Snacks").price(20.0).icon("🥐").available(true).build(),
            MenuItem.builder().name("Samosa").category("Snacks").price(15.0).icon("🧆").available(true).build(),
            MenuItem.builder().name("Poha").category("Snacks").price(30.0).icon("🥞").available(true).build(),
            MenuItem.builder().name("Cold Coffee").category("Cold").price(40.0).icon("🥤").available(true).build(),
            MenuItem.builder().name("Lassi").category("Cold").price(35.0).icon("🧋").available(true).build(),
            MenuItem.builder().name("Buttermilk").category("Cold").price(20.0).icon("🥛").available(true).build()
        );
        menuItemRepository.saveAll(defaults);
    }
}
