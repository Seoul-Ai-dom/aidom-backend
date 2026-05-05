package com.aidom.api.domain.visit.repository;

import com.aidom.api.domain.visit.entity.VisitHistory;
import com.aidom.api.domain.visit.enums.VisitStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitHistoryRepository extends JpaRepository<VisitHistory, Long> {

  @Query(
      "SELECT v FROM VisitHistory v JOIN FETCH v.facility JOIN FETCH v.child"
          + " WHERE v.user.id = :userId AND v.status <> :excludedStatus"
          + " ORDER BY v.createdAt DESC")
  Slice<VisitHistory> findByUserIdAndStatusNotWithDetails(
      @Param("userId") Long userId,
      @Param("excludedStatus") VisitStatus excludedStatus,
      Pageable pageable);

  @Query(
      "SELECT v FROM VisitHistory v JOIN FETCH v.facility JOIN FETCH v.child"
          + " WHERE v.user.id = :userId AND v.status = :status"
          + " ORDER BY v.createdAt DESC")
  Slice<VisitHistory> findByUserIdAndStatusWithDetails(
      @Param("userId") Long userId, @Param("status") VisitStatus status, Pageable pageable);

  @Query(
      "SELECT v FROM VisitHistory v JOIN FETCH v.facility JOIN FETCH v.child"
          + " WHERE v.user.id = :userId"
          + " AND v.visitDate BETWEEN :startDate AND :endDate"
          + " AND v.status <> :excludedStatus"
          + " ORDER BY v.visitDate DESC")
  Slice<VisitHistory> findByUserIdAndVisitDateBetweenAndStatusNotWithDetails(
      @Param("userId") Long userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("excludedStatus") VisitStatus excludedStatus,
      Pageable pageable);

  @Query(
      "SELECT v FROM VisitHistory v JOIN FETCH v.facility JOIN FETCH v.child"
          + " WHERE v.user.id = :userId"
          + " AND v.visitDate BETWEEN :startDate AND :endDate"
          + " AND v.status = :status"
          + " ORDER BY v.visitDate DESC")
  Slice<VisitHistory> findByUserIdAndVisitDateBetweenAndStatusWithDetails(
      @Param("userId") Long userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("status") VisitStatus status,
      Pageable pageable);

  @Query(
      "SELECT v FROM VisitHistory v JOIN FETCH v.facility JOIN FETCH v.child"
          + " WHERE v.user.id = :userId AND v.status IN :statuses"
          + " ORDER BY v.createdAt DESC")
  Slice<VisitHistory> findByUserIdAndStatusInWithDetails(
      @Param("userId") Long userId,
      @Param("statuses") List<VisitStatus> statuses,
      Pageable pageable);
}
