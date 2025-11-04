package com.example.Jaxrs.controller;

import com.example.Jaxrs.entity.Category;
import com.example.Jaxrs.entity.Item;
import com.example.Jaxrs.repository.CategoryRepository;
import com.example.Jaxrs.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api-rest/items")
public class ItemRestController {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // GET /api-rest/items?page=&size=
    // GET /api-rest/items?categoryId=...&page=&size=
    @GetMapping
    public ResponseEntity<Page<Item>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId) {
        Pageable pageable = PageRequest.of(page, size);
        
        // GET /api-rest/items?categoryId=...
        if (categoryId != null) {
            Page<Item> items = itemRepository.findByCategoryId(categoryId, pageable);
            return ResponseEntity.ok(items);
        }
        
        Page<Item> items = itemRepository.findAll(pageable);
        return ResponseEntity.ok(items);
    }

    // GET /api-rest/items/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        return itemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api-rest/items
    @PostMapping
    public ResponseEntity<Item> addItem(@RequestBody Item item) {
        // Ensure category is loaded
        if (item.getCategory() != null && item.getCategory().getId() != null) {
            Category category = categoryRepository.findById(item.getCategory().getId())
                    .orElse(null);
            if (category == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            item.setCategory(category);
        }
        item.setUpdatedAt(LocalDateTime.now());
        Item saved = itemRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api-rest/items/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        return itemRepository.findById(id)
                .map(existing -> {
                    existing.setSku(item.getSku());
                    existing.setName(item.getName());
                    existing.setPrice(item.getPrice());
                    existing.setStock(item.getStock());
                    existing.setUpdatedAt(LocalDateTime.now());
                    
                    // Update category if provided
                    if (item.getCategory() != null && item.getCategory().getId() != null) {
                        Category category = categoryRepository.findById(item.getCategory().getId())
                                .orElse(null);
                        if (category == null) {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).<Item>build();
                        }
                        existing.setCategory(category);
                    }
                    
                    Item saved = itemRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).<Item>build());
    }

    // DELETE /api-rest/items/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

