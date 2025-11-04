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
import java.util.ArrayList;
import java.util.List;

@Component
@Path("/categories")
public class CategoryJaxrsApi {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    // GET /categories?page=&size=
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Page<Category> getCategories(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return categoryRepository.findAll(pageable);
    }

    // GET /categories/{id}
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Category getCategory(@PathParam("id") Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    // POST /categories
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Category addCategory(Category category) {
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    // PUT /categories/{id}
    @PUT
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Category updateCategory(@PathParam("id") Long id, Category category) {
        Category existingCategory = categoryRepository.findById(id).orElse(null);
        if (existingCategory != null) {
            existingCategory.setCode(category.getCode());
            existingCategory.setName(category.getName());
            existingCategory.setUpdatedAt(LocalDateTime.now());
            return categoryRepository.save(existingCategory);
        }
        return null;
    }

    // DELETE /categories/{id}
    @DELETE
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void deleteCategory(@PathParam("id") Long id) {
        categoryRepository.deleteById(id);
    }

    // GET /categories/{id}/items (association inverse)
    @GET
    @Path("/{id}/items")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Item> getCategoryItems(@PathParam("id") Long id) {
        if (!categoryRepository.existsById(id)) {
            return new ArrayList<>();
        }
        return itemRepository.findByCategoryId(id, Pageable.unpaged()).getContent();
    }
}

