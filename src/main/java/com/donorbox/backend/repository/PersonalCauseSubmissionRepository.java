package com.donorbox.backend.repository;

import com.donorbox.backend.entity.PersonalCauseSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PersonalCauseSubmissionRepository extends JpaRepository<PersonalCauseSubmission, Long> {
    
    List<PersonalCauseSubmission> findByStatus(PersonalCauseSubmission.SubmissionStatus status);
    
    List<PersonalCauseSubmission> findBySubmitterEmailOrderByCreatedAtDesc(String submitterEmail);
    
    @Query("SELECT p FROM PersonalCauseSubmission p WHERE p.status = :status ORDER BY p.createdAt ASC")
    List<PersonalCauseSubmission> findByStatusOrderByCreatedAtAsc(@Param("status") PersonalCauseSubmission.SubmissionStatus status);
    
    @Query("SELECT COUNT(p) FROM PersonalCauseSubmission p WHERE p.status = :status")
    long countByStatus(@Param("status") PersonalCauseSubmission.SubmissionStatus status);
    
    @Query("SELECT p FROM PersonalCauseSubmission p ORDER BY p.createdAt DESC")
    List<PersonalCauseSubmission> findAllOrderByCreatedAtDesc();
}
