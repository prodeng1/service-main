package ro.unibuc.prodeng.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.UserEntity;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    List<UserEntity> findByNameContainingIgnoreCase(String name);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPhone(String phone);
}
