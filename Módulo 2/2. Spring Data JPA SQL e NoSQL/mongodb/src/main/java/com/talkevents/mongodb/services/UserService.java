package com.talkevents.mongodb.services;

import com.talkevents.mongodb.documents.User;
import com.talkevents.mongodb.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User update(String id, User user) {
        var userToUpdate = getUserById(id);

        if (userToUpdate != null) {

            userToUpdate.setName(user.getName());
            userToUpdate.setAge(user.getAge());
            return userRepository.save(userToUpdate);
        }

        return null;
    }

    public void delete(String id) {
        userRepository.deleteById(id);
    }
}
