package com.sm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    User findByEmailEndsWith(String email);
    User findByEmail(String email);
    User findByUserId(String userId);
    @Query("select user from User user where user.email like %:emailDomain")
    List<User> findUsersWithEmailEndingWith(@Param("emailDomain") String emailDomain);
}
