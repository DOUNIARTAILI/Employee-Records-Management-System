package com.drtaili.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Integer id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User with id " + id + " does not exist!"));

        var userEmail = userRepository.findByEmail(user.getEmail());
        if (userEmail.isPresent()) {
            throw new IllegalStateException("Email already taken!");
        }

        existingUser.setFirstname(user.getFirstname());
        existingUser.setLastname(user.getLastname());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        existingUser.setRole(user.getRole());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Integer id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User with id " + id + " does not exist!"));
        userRepository.deleteById(id);
    }

}

