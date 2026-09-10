package com.dmh.accounts.repository;

import com.dmh.accounts.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByIdAndAccountUserId(
            Long cardId,
            Long userId
    );
    List<Card> findByAccountId(Long accountId);
}
