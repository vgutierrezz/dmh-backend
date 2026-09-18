package com.dmh.accounts.repository;

import com.dmh.accounts.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // Métodos de verificación para asegurar la unicidad de los algoritmos
    boolean existsByCvu(String cvu);
    boolean existsByAlias(String alias);
    boolean existsByAliasIgnoreCaseAndIdNot(String alias, Long id);

    Optional<Account> findByUserId(Long userId);
    Optional<Account> findByCvu(String cvu);

    List<Account> findAll();

}