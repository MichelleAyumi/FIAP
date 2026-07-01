package com.talkevents.mongodb.services;

import com.talkevents.mongodb.documents.Address;
import com.talkevents.mongodb.documents.User;
import com.talkevents.mongodb.repositories.AddressRepository;
import com.talkevents.mongodb.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserService(UserRepository userRepository, AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user) {
        user.setAddress(resolveAddress(user.getAddress()));
        return userRepository.save(user);
    }

    public User update(String id, User user) {
        User userToUpdate = getUserById(id);

        if(userToUpdate == null) {
            return null;
        }

        userToUpdate.setName(user.getName());
        userToUpdate.setAge(user.getAge());
        userToUpdate.setAddress(resolveAddress(user.getAddress()));

        return userRepository.save(userToUpdate);
    }

    public User updateAddress(String id, Address address) {
        User userToUpdate = getUserById(id);

        if (userToUpdate == null) {
            return null;
        }

        userToUpdate.setAddress(resolveAddress(address));
        return userRepository.save(userToUpdate);
    }

    private Address resolveAddress(Address address) {
        if (address == null) {
            return null;
        }

        if (address.getId() != null) {
            return addressRepository.findById(address.getId())
                    .orElseGet(() -> addressRepository.save(address));
        }

        return addressRepository.save(address);
    }

    public void delete(String id) {
        userRepository.deleteById(id);
    }
}
