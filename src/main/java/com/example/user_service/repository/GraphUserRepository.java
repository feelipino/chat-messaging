package com.example.user_service.repository;

import com.example.user_service.model.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface GraphUserRepository extends Neo4jRepository<UserNode, String> {

}
