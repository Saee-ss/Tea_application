package com.teastall.controller;

import com.teastall.dto.BillingDtos.*;
import com.teastall.model.MenuItem;
import com.teastall.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItem>>> getAvailableMenu() {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getAllAvailableItems()));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MenuItem>>> getAllMenu() {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getAllItems()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItem>> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getItemById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItem>> createItem(@RequestBody MenuItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Item created", menuService.createItem(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItem>> updateItem(
            @PathVariable Long id, @RequestBody MenuItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Item updated", menuService.updateItem(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        menuService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Item removed", null));
    }
}
