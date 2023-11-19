package com.digitalhouse.fotofleet.services;

import com.digitalhouse.fotofleet.dtos.RentalDto;
import com.digitalhouse.fotofleet.dtos.RentalResponseDto;
import com.digitalhouse.fotofleet.exceptions.BadRequestException;
import com.digitalhouse.fotofleet.exceptions.ResourceNotFoundException;
import com.digitalhouse.fotofleet.models.*;
import com.digitalhouse.fotofleet.repositories.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RentalService {
    private final RentalRepository rentalRepository;
    private final UserService userService;
    private final ProductService productService;
    private final StatusService statusService;
    private final RentalDetailService rentalDetailService;

    public Rental createRental(Rental rental) {
        return rentalRepository.save(rental);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<RentalResponseDto> addRentals(String jwt, List<RentalDto> rentalDtos) throws BadRequestException, ResourceNotFoundException {
        User user = userService.getUserByJwt(jwt);
        if (rentalDtos.size() > 20) throw new BadRequestException("El carrito de compra no puede exceder los 20 artículos");

        List<RentalResponseDto> rentalResponseDtos = new ArrayList<>();
        for (RentalDto rentalDto : rentalDtos) {
            Optional<Product> product = productService.getById(rentalDto.productId());
            if (product.isEmpty()) throw new ResourceNotFoundException("No existe el producto con ID " + rentalDto.productId() + " enviado en el listado");

            Optional<Status> status = statusService.getStatusByName("Pending");
            if (status.isEmpty()) throw new ResourceNotFoundException("No existe el estatus Pending, póngase en contacto con soporte");
            if (product.get().getStock() < rentalDto.quantity()) throw new BadRequestException("Excede el stock máximo de productos con ID " + product.get().getProductId());
            if (rentalDto.startDate().isAfter(rentalDto.endDate())) throw new BadRequestException("La fecha de inicio del alquiler no puede exceder la fecha de fin del mismo");
            if (Duration.between(rentalDto.startDate(), rentalDto.endDate()).toDays() > 90) throw new BadRequestException("El alquiler del producto no puede exceder los 3 meses");

            Integer daysRented = (int) Duration.between(rentalDto.startDate(), rentalDto.endDate()).toDays();
            Double rentalPrice = (product.get().getRentalPrice() * rentalDto.quantity()) * daysRented;

            Rental rental = createRental(new Rental(user, rentalDto.startDate(), rentalDto.endDate(), status.get()));
            RentalDetail rentalDetail = rentalDetailService.createRentalDetail(new RentalDetail(rental, product.get(), rentalDto.quantity(), rentalPrice, daysRented));

            rentalResponseDtos.add(new RentalResponseDto(rentalDetail.getDetailId(), rental.getRentalId(), product.get().getProductId(), rentalDetail.getQuantity(), rentalDetail.getRentalPrice(), rental.getStartDate(), rental.getEndDate(), status.get().getName()));
        }

        return rentalResponseDtos;
    }
}
