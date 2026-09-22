package com.dmh.accounts.repository;

import com.dmh.accounts.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository  extends JpaRepository<Activity, Long> {
    List<Activity> findByAccountIdOrderByDatedDesc(Long accountId);
}
