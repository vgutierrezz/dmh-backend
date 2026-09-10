package com.dmh.accounts.service;

import com.dmh.accounts.dto.ActivityResponse;
import com.dmh.accounts.model.Activity;
import com.dmh.accounts.model.enums.TransactionType;
import com.dmh.accounts.repository.ActivityRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;

    public List<ActivityResponse> findByAccountId(Long accountId) {
        return activityRepository.findByAccountId(accountId)
                .stream()
                .map(activity -> new ActivityResponse(
                        activity.getId(),
                        activity.getAmount().doubleValue(),
                        activity.getName(),
                        activity.getDated(),
                        activity.getType(),
                        activity.getOrigin(),
                        activity.getDestination()
                ))
                .toList();
    }

    public ActivityResponse createActivity(String origin, BigDecimal amount, Long accountId, String transactionName, String transactionType, String destionation) {
        var activity = new Activity();
        activity.setAmount(amount);
        activity.setName(transactionName);
        activity.setDated(java.time.LocalDateTime.now());
        activity.setType(transactionType.equals("DEPOSIT") ? TransactionType.DEPOSIT : TransactionType.TRANSFER);
        activity.setOrigin(origin);
        activity.setDestination(destionation);
        activity.setAccountId(accountId);

        activityRepository.save(activity);

        return new ActivityResponse(
                activity.getId(),
                activity.getAmount().doubleValue(),
                activity.getName(),
                activity.getDated(),
                activity.getType(),
                activity.getOrigin(),
                activity.getDestination()
        );
    }
}
