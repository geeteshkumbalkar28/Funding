package com.donorbox.backend.repository;

import com.donorbox.backend.entity.Cause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface CauseRepository extends JpaRepository<Cause, Long> {

    @Query("SELECT COALESCE(SUM(d.amount), 0) " +
           "FROM Donation d " +
           "WHERE d.cause.id = :causeId AND d.status = 'SUCCESS'")
    BigDecimal calculateTotalDonationsForCause(Long causeId);
}
