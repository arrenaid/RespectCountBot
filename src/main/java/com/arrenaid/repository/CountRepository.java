package com.arrenaid.repository;

import com.arrenaid.entity.Count;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountRepository extends JpaRepository<Count,Integer> {
}
