package com.example.Jaxrs.repository;

import com.example.Jaxrs.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(collectionResourceRel = "items", path = "items")
public interface ItemRepository extends JpaRepository<Item, Long> {
    @RestResource(path = "byCategory", rel = "byCategory")
    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);
}

