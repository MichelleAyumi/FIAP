package com.talkevents.mongodb.controllers;

import com.talkevents.mongodb.documents.Address;
import com.talkevents.mongodb.services.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService){
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<Address>> getAllAddresses(){
        return ResponseEntity.ok(addressService.getAll());
    }

    @PostMapping
    public ResponseEntity<Address> addAddress(@RequestBody Address address){
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.save(address));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable String id){
        return ResponseEntity.ok(addressService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable String id, @RequestBody Address address){
        var updatedAddress = addressService.update(id, address);

        if (updatedAddress == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable String id){
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
