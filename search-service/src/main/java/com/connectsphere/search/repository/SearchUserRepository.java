package com.connectsphere.search.repository;

import com.connectsphere.search.model.SearchUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchUserRepository extends JpaRepository<SearchUser, Long> {
    List<SearchUser> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrderByIndexedAtDesc(String username, String fullName);
}
