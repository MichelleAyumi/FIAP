package com.talkevents.cassandra.controllers;

import com.talkevents.cassandra.models.Address;
import com.talkevents.cassandra.services.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/address")
public class AddressController {
    private final AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity <List<Address>> getAllUsers(){
        return ResponseEntity.ok(addressService.getAll());
    }

    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody Address address){
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.save(address));
    }

    @GetMapping("/{id}")
    public ResponseEntity <Address> getAddressById(@PathVariable UUID id){
        var address = addressService.getById(id);

        if (address == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(address);
    }

    @PutMapping
    public ResponseEntity<Void> updateAddress(@RequestBody Address address){
        addressService.update(address);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID id){
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
