package com.example.Subscription.Tracker.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;           // 訂閱名稱 (如 Netflix)
    private Double price;          // 金額
    private String currency;       // 幣別
    private LocalDate nextBillingDate; // 下次扣款日
    private String period;         // 週期 (MONTHLY/YEARLY)
    private Integer noticeDays;    // 提前幾天提醒
}