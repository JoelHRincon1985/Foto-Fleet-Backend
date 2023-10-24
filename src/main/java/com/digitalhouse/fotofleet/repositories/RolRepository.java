package com.digitalhouse.fotofleet.repositories;

import com.digitalhouse.fotofleet.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
}
