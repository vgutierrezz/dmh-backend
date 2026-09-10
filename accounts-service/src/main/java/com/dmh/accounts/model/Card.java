package com.dmh.accounts.model;

import com.dmh.accounts.model.enums.TypeCard;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    Long number;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String expirationDate;

    @Column(nullable = false)
    String cvc;

    @Column(nullable = false)
    Long accountId;

    @Column(nullable = false)
    TypeCard type;
}
