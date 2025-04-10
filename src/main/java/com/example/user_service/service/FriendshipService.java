package com.example.user_service.service;

import com.example.user_service.model.User;
import com.example.user_service.model.Friendship;
import com.example.user_service.model.UserNode;
import com.example.user_service.repository.FriendshipRepository;
import com.example.user_service.repository.GraphUserRepository;
import com.example.user_service.repository.RelationalUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final GraphUserRepository graphUserRepository;
    private final RelationalUserRepository relationalUserRepository;

    @Autowired
    public FriendshipService(
            FriendshipRepository friendshipRepository,
            GraphUserRepository graphUserRepository,
            RelationalUserRepository relationalUserRepository) {
        this.friendshipRepository = friendshipRepository;
        this.graphUserRepository = graphUserRepository;
        this.relationalUserRepository = relationalUserRepository;
    }

    @Transactional
    public void addFriend(String userEmail, String friendEmail) {
        sendFriendRequest(userEmail, friendEmail);
    }

    @Transactional
    public Friendship sendFriendRequest(String senderEmail, String receiverEmail) {

        // Check if both users exists
        Optional<User> sender = relationalUserRepository.findByEmail(senderEmail);
        Optional<User> receiver = relationalUserRepository.findByEmail(receiverEmail);

        if (sender.isEmpty() || receiver.isEmpty()) {
            throw new IllegalArgumentException("Sender or receiver not found in relational database");
        }

        // Check if there is already a friend request
        Optional<Friendship> existingRequest = friendshipRepository.findBySenderEmailAndReceiverEmail(senderEmail, receiverEmail);
        if (existingRequest.isPresent()) {
            throw new IllegalStateException("Friend request already exists in relational database");
        }

        // Check if the nodes exist in Neo4j
        Optional<UserNode> senderNode = graphUserRepository.findById(senderEmail);
        Optional<UserNode> receiverNode = graphUserRepository.findById(receiverEmail);

        // If one of the nodes does not exist, it will be created
        if (senderNode.isEmpty()) {
            UserNode newSenderNode = new UserNode(senderEmail, sender.get().getName());
            graphUserRepository.save(newSenderNode);
            }

        if (receiverNode.isEmpty()) {
            UserNode newReceiverNode = new UserNode(receiverEmail, receiver.get().getName());
            graphUserRepository.save(newReceiverNode);
        }

        // Create a new friend request
        Friendship friendship = new Friendship();
        friendship.setSenderEmail(senderEmail);
        friendship.setReceiverEmail(receiverEmail);
        friendship.setStatus("PENDING");

        return friendshipRepository.save(friendship);

    }

    @Transactional
    public Friendship acceptFriendRequest(Long friendshipId) {
        Optional<Friendship> friendshipOpt = friendshipRepository.findById(friendshipId);
        if (friendshipOpt.isEmpty()) {
            throw new IllegalArgumentException("Friendship not found");
        }

        Friendship friendship = friendshipOpt.get();

        if(!friendship.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Friendship status is not PENDING state");
        }

        friendship.setStatus("ACCEPTED");
        Friendship savedFriendship = friendshipRepository.save(friendship);

        // After accepting the request, create the relationship in Neo4j
        createNeo4jRelationship(friendship.getSenderEmail(), friendship.getReceiverEmail());

        return savedFriendship;
    }

    // Method for creating a friendship in Neo4j
    private void createNeo4jRelationship(String userEmail1, String userEmail2) {
        UserNode user1 = graphUserRepository.findById(userEmail1)
                .orElseThrow(() -> new IllegalArgumentException("User1 not found in graph database"));

        UserNode user2 = graphUserRepository.findById(userEmail2)
                .orElseThrow(() -> new IllegalArgumentException("User2 not found in graph database"));

        // Add each to the other's friends list
        user1.getFriends().add(user2);
        user2.getFriends().add(user1);

        // Save both nodes
        graphUserRepository.save(user1);
        graphUserRepository.save(user2);
    }

    @Transactional
    public Friendship rejectFriendRequest(Long friendshipId) {
        Optional<Friendship> friendshipOpt = friendshipRepository.findById(friendshipId);
        if (friendshipOpt.isEmpty()) {
            throw new IllegalArgumentException("Friendship not found");
        }

        Friendship friendship = friendshipOpt.get();

        if(!friendship.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Friendship status is not in PENDING state");
        }

        friendship.setStatus("REJECTED");
        return friendshipRepository.save(friendship);
    }

    public List<User> getFriendList(String userEmail) {
        // Search for all accepted friendships where the user is the sender or the recipient
        List<Friendship> sentFriendships = friendshipRepository.findBySenderEmailAndStatus(userEmail, "ACCEPTED");
        List<Friendship> receivedFriendships = friendshipRepository.findByReceiverEmailAndStatus(userEmail, "ACCEPTED");

        // Extract friends' emails
        List<String> friendEmails = sentFriendships.stream()
                .map(Friendship::getReceiverEmail)
                .collect(Collectors.toList());

        friendEmails.addAll(receivedFriendships.stream()
                .map(Friendship::getSenderEmail)
                .collect(Collectors.toList()));

        // Search for the corresponding users
        return relationalUserRepository.findAllByEmailIn(friendEmails);
    }
}
