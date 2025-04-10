package com.example.user_service.controller;

import com.example.user_service.model.Friendship;
import com.example.user_service.model.User;
import com.example.user_service.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendship")
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    @PostMapping("/{userEmail}/add/{friendEmail}")
    public ResponseEntity<String> addFriend(@PathVariable String userEmail, @PathVariable String friendEmail) {
        friendshipService.addFriend(userEmail, friendEmail);
        return ResponseEntity.ok("Friendship request sent from " + userEmail + " to " + friendEmail);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Friendship> acceptFriendRequest(@PathVariable Long id) {
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Friendship> rejectFriendRequest(@PathVariable Long id) {
        return ResponseEntity.ok(friendshipService.rejectFriendRequest(id));
    }

    @GetMapping("/user/{email}")
    publict ResponseEntity<List<User>> getUserFriends(@PathVariable string email) {
        return ResponseEntity.ok(friendshipService.getFriendList(email));
    }
}
