package com.aidom.api.domain.review.entity;

import com.aidom.api.domain.facility.entity.Facility;
import com.aidom.api.domain.user.entity.User;
import com.aidom.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "id", column = @Column(name = "review_id"))
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal ratingOverall;

    @Column(precision = 2, scale = 1)
    private BigDecimal ratingSafety;

    @Column(precision = 2, scale = 1)
    private BigDecimal ratingCleanliness;

    @Column(precision = 2, scale = 1)
    private BigDecimal ratingManagement;

    @Column(precision = 2, scale = 1)
    private BigDecimal ratingKindness;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    private Review(
            User user,
            Facility facility,
            BigDecimal ratingOverall,
            BigDecimal ratingSafety,
            BigDecimal ratingCleanliness,
            BigDecimal ratingManagement,
            BigDecimal ratingKindness,
            String content) {
        this.user = user;
        this.facility = facility;
        this.ratingOverall = ratingOverall;
        this.ratingSafety = ratingSafety;
        this.ratingCleanliness = ratingCleanliness;
        this.ratingManagement = ratingManagement;
        this.ratingKindness = ratingKindness;
        this.content = content;
    }
}
