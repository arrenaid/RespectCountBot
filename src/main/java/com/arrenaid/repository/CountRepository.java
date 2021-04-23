package com.arrenaid.repository;

import com.arrenaid.entity.Count;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountRepository extends JpaRepository<Count,Integer> {
    List<Count> getAllByChatId(int chatId);
    List<Count> findByNameContainingIgnoreCase(String name);
}
