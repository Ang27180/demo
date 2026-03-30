package com.proyectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectojpa.demo.models.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
}
