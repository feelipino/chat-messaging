package com.example.user_service.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

// Mapping "User" Node - Neo4J
@Node("User")
public class UserNode {

    @Id
    private String email;

    private String name;

    @Relationship(type= "FRIEND", direction = Relationship.Direction.OUTGOING) // Bidirectional
    private Set<UserNode> friends = new HashSet <>();

    public UserNode() {}

    public UserNode(String email, String name) {
        this.email = email;
        this.name = name;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<UserNode> getFriends() {
        return friends;
    }

    public void setFriends(Set<UserNode> friends) {
        this.friends = friends;
    }
}
