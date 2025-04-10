package com.example.user_service.controller;

import com.example.user_service.model.Friendship;
import com.example.user_service.model.User;
import com.example.user_service.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendship")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @Autowired
    private FriendshipController friendshipController;

    @PostMapping("/{userEmail}/add/{friendEmail}")
    public String addFriend(@PathVariable String userEmail, @PathVariable String friendEmail) {
        friendshipService.addFriend(userEmail, friendEmail);
        return "Friendship created between " + userEmail + " and " + friendEmail;
    }
}
