package com.talkevents.mongodb.services;

import com.talkevents.mongodb.documents.Address;
import com.talkevents.mongodb.repositories.AddressRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {
    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }
    public List<Address> getAll(){
        return addressRepository.findAll();
    }

    public Address getById(String id){
        return addressRepository.findById(id).orElse(null);
    }

    public Address save(Address address) {
        return addressRepository.save(address);
    }

    public Address update(String id, Address address){
        var addressToUpdate = getById(id);

        if(addressToUpdate != null){
            addressToUpdate.setStreet(address.getStreet());
            addressToUpdate.setCity(address.getCity());
            addressToUpdate.setCountry(address.getCountry());
            return addressRepository.save(addressToUpdate);
        }

        return null;
    }

    public void delete(String id){
        addressRepository.deleteById(id);
    }
}
