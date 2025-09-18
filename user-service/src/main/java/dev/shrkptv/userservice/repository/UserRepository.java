package dev.shrkptv.userservice.repository;

import dev.shrkptv.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM users WHERE id = :id", nativeQuery = true)
    User findUserById(Long id);

    @Query(value = "SELECT u FROM User u WHERE u.email = :email")
    User findUserByEmail(String email);

    List<User> findUsersByIdIn(Collection<Long> ids);

    void deleteUserById(Long id);

}
