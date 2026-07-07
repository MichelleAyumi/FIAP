package com.talkevents.cassandra.repositories;

import com.talkevents.cassandra.models.User;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserRepository extends CassandraRepository<User, UUID> {
}
