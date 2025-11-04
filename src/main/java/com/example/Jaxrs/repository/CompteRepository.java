package com.example.Jaxrs.repository;

import com.example.Jaxrs.entity.Compte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompteRepository extends JpaRepository<Compte, Long> {
}
