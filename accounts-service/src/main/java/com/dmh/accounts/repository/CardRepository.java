package com.dmh.accounts.repository;

import com.dmh.accounts.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
}
