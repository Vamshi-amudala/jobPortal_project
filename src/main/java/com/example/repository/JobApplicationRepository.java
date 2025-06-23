package com.example.repository;

import com.example.entity.JobApplication;
import org.springframework.transaction.annotation.Transactional;
import com.example.entity.User;
import com.example.entity.ApplicationStatus;
import com.example.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByApplicant(User user);
    List<JobApplication> findByJob(Job job);
    List<JobApplication> findByApplicantAndStatus(User applicant, ApplicationStatus status);
    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.employer = :employer")
    List<JobApplication> findByEmployer(@Param("employer") User employer);
 
    boolean existsByApplicantAndJob(User applicant, Job job);

    Optional<JobApplication> findByApplicantAndJob(User applicant, Job job);

    @Modifying
    @Transactional
    @Query("DELETE FROM JobApplication ja WHERE ja.job = :job")
    void deleteByJob(@Param("job") Job job);


}
