package com.digitalhouse.fotofleet.repositories;

import com.digitalhouse.fotofleet.models.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Integer> {
}
