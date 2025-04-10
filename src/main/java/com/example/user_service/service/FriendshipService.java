package com.example.user_service.service;

import com.example.user_service.model.Friendship;
import com.example.user_service.model.User;
import com.example.user_service.model.UserNode;
import com.example.user_service.repository.FriendshipRepository;
import com.example.user_service.repository.GraphUserRepository;
import com.example.user_service.repository.RelationalUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Friendship sendFriendRequest(String senderEmail, String receiverEmail {
        // Check if both users exists
        Optional<UserNode> senderNode = graphUserRepository.findById(senderEmail);
        Optional<UserNode> receiverNode = graphUserRepository.findById(receiverEmail);

        if (senderNode.isEmpty() || receiverNode.isEmpty()) {
            throw new IllegalArgumentException("Sender or receiver not found");
        }

        // Create a new friend request
        Friendship friendship = new Friendship();
        friendship.setSenderEmail(senderEmail);
        friendship.setReceiverEmail(receiverEmail);
        friendship.setStatus("PENDING");

        return friendshipRepository.save(friendship);

    }

    public Friendship acceptFriendRequest(Long friendShipId) {
        Optional<Friendship> friendshipOpt = friendshipRepository.findById(friendshipId);
        if (friendshipOpt.isEmpty()) {
            throw new IllegalArgumentException("Friendship not found");
        }

        Friendship friendship = friendshipOpt.get();

        if(!friendship.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Friendship status is not PENDING state");
        }

        friendship.setStatus("ACCEPTED");
        return friendshipRepository.save(friendship);
    }

    public Friendship rejectFriendRequest(Long friendshipId) {
        return friendshipRepository.save(friendship);
    }

    public List<User> getFriendsList(String userEmail) {
        // Search for all accepted friendships where the user is the sender or the recipient
        List<Friendship> sentFriendships = friendshipRepository.findBySenderEmailAndStatus(userEmail, "ACCEPTED");
        List<Friendship> receivedFriendships = friendshipRepository.findByReceiverEmailAndStatus(userEmail, "ACCEPTED");

        // Extract friends' emails
        List<String> friendEmails = sentFriendships.stream()
                .map(Friendship::getReceiverEmail)
                .collect(Collector.toList());

        friendEmails.addAll(receivedFriendships.stream()
                .map(Friendship::getSenderEmail)
                .collect(Collector.toList()));

        // Search for the corresponding users
        return relationalUserRepository.findAllByEmailIn(friendEmails);
    }
}
