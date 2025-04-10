package com.example.user_service.repository;

import com.example.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelationalUserRepository extends JpaRepository<User, Long> {

    // Method to search for users in a mailing list
    List<User> findAllByEmailIn(List<String> emails);
}
