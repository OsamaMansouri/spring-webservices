package com.example.Jaxrs.controller;

import com.example.Jaxrs.entity.Category;
import com.example.Jaxrs.entity.Item;
import com.example.Jaxrs.repository.CategoryRepository;
import com.example.Jaxrs.repository.ItemRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Path("/items")
public class ItemJaxrsApi {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // GET /items?page=&size=
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Page<Item> getItems(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("categoryId") Long categoryId) {
        Pageable pageable = PageRequest.of(page, size);
        
        // GET /items?categoryId=...
        if (categoryId != null) {
            return itemRepository.findByCategoryId(categoryId, pageable);
        }
        
        return itemRepository.findAll(pageable);
    }

    // GET /items/{id}
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Item getItem(@PathParam("id") Long id) {
        return itemRepository.findById(id).orElse(null);
    }

    // POST /items
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Item addItem(Item item) {
        // Ensure category is loaded
        if (item.getCategory() != null && item.getCategory().getId() != null) {
            Category category = categoryRepository.findById(item.getCategory().getId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            item.setCategory(category);
        }
        item.setUpdatedAt(LocalDateTime.now());
        return itemRepository.save(item);
    }

    // PUT /items/{id}
    @PUT
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Item updateItem(@PathParam("id") Long id, Item item) {
        Item existingItem = itemRepository.findById(id).orElse(null);
        if (existingItem != null) {
            existingItem.setSku(item.getSku());
            existingItem.setName(item.getName());
            existingItem.setPrice(item.getPrice());
            existingItem.setStock(item.getStock());
            existingItem.setUpdatedAt(LocalDateTime.now());
            
            // Update category if provided
            if (item.getCategory() != null && item.getCategory().getId() != null) {
                Category category = categoryRepository.findById(item.getCategory().getId())
                        .orElseThrow(() -> new NotFoundException("Category not found"));
                existingItem.setCategory(category);
            }
            
            return itemRepository.save(existingItem);
        }
        return null;
    }

    // DELETE /items/{id}
    @DELETE
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void deleteItem(@PathParam("id") Long id) {
        itemRepository.deleteById(id);
    }
}


