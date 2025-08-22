package com.donorbox.backend.repository;

import com.donorbox.backend.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    // Additional query methods if needed
}
