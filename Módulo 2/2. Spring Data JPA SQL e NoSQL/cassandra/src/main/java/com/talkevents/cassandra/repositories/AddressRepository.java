package com.talkevents.cassandra.repositories;

import com.talkevents.cassandra.models.Address;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface AddressRepository extends CassandraRepository <Address, UUID> {
}
