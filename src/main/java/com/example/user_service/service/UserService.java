package com.example.user_service.service;

import com.example.user_service.model.User;
import com.example.user_service.model.UserNode;
import com.example.user_service.repository.GraphUserRepository;
import com.example.user_service.repository.RelationalUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final RelationalUserRepository relationalUserRepository;
    private final GraphUserRepository graphUserRepository;

    @Autowired
    public UserService(RelationalUserRepository relationalUserRepository, GraphUserRepository graphUserRepository) {
        this.relationalUserRepository = relationalUserRepository;
        this.graphUserRepository = graphUserRepository;
    }

    @Transactional
    public User registerUser(User user) {
        // Check if the email is already registered
        if (relationalUserRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        // Save to relational database
        User savedUser = relationalUserRepository.save(user);

        // Also save in Neo4j to maintain consistency
        UserNode userNode = new UserNode(user.getEmail(), user.getName());
        graphUserRepository.save(userNode);

        return savedUser;
    }

    @Transactional
    public void deleteUser(String email) {
        //Delete from relational database
        Optional<User> user = relationalUserRepository.findByEmail(email);
        if (user.isPresent()) {
            relationalUserRepository.delete(user.get());
        }

        // Delete from Neo4j
        graphUserRepository.deleteById(email);
    }
}
