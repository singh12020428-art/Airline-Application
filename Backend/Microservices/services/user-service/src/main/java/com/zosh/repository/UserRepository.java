package com.zosh.repository;

import com.zosh.enums.UserRole;
import com.zosh.enums.UserStatus;
import com.zosh.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    long countByRoleAndStatus(UserRole role, UserStatus status);
}
