package com.talkevents.mongodb.repositories;

import com.talkevents.mongodb.documents.Address;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AddressRepository extends MongoRepository<Address, String> {
}
