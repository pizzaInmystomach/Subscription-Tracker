package com.example.Subscription.Tracker.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private AppUser user;

    private String name;           // 訂閱名稱 (如 Netflix)
    private String email;          // 提醒信箱
    private Double price;          // 金額
    private String currency;       // 幣別
    private LocalDate nextBillingDate; // 下次扣款日
    private String period;         // 週期 (MONTHLY/YEARLY)
    private Integer noticeDays;    // 提前幾天提醒
    private String status;         // ACTIVE/CANCELED/PAYMENT_FAILED/PENDING_DETAILS
    private String lastStatusNote; // 上次狀態說明
    private LocalDate lastStatusAt; // 上次狀態更新日期
}
