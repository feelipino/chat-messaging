package com.example.user_service.repository;

import com.example.user_service.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findBySenderEmailAndReceiverEmail(String senderEmail, String receiverEmail);

    List<Friendship> findBySenderEmailAndStatus(String senderEmail, String status);

    List<Friendship> findByReceiverEmailAndStatus(String receiverEmail, String status);
}
