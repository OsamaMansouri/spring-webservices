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
import java.util.List;

@RestController
@RequestMapping("/api-rest/categories")
public class CategoryRestController {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    // GET /api-rest/categories?page=&size=
    @GetMapping
    public ResponseEntity<Page<Category>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categories = categoryRepository.findAll(pageable);
        return ResponseEntity.ok(categories);
    }

    // GET /api-rest/categories/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api-rest/categories
    @PostMapping
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        category.setUpdatedAt(LocalDateTime.now());
        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api-rest/categories/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    existing.setCode(category.getCode());
                    existing.setName(category.getName());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(categoryRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api-rest/categories/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // GET /api-rest/categories/{id}/items (association inverse)
    @GetMapping("/{id}/items")
    public ResponseEntity<List<Item>> getCategoryItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Pageable pageable = PageRequest.of(page, size);
        List<Item> items = itemRepository.findByCategoryId(id, pageable).getContent();
        return ResponseEntity.ok(items);
    }
}

