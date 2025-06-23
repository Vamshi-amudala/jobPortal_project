package com.example.repository;

import com.example.entity.Job;
import com.example.entity.JobApplication;
import com.example.entity.JobStatus;
import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

  
	
    List<Job> findByEmployer(User employer);


    List<Job> findByStatus(JobStatus status);

    List<Job> findByLocation(String location);

    List<Job> findByTitle(String title);

    List<Job> findByCompany(String company);

   
    @Query(value = "SELECT * FROM job ORDER BY CAST(exp AS UNSIGNED) DESC", nativeQuery = true)
    List<Job> findAllOrderByExperienceDesc();

    @Query(value = "SELECT * FROM job ORDER BY CAST(exp AS UNSIGNED) ASC", nativeQuery = true)
    List<Job> findAllOrderByExperienceAsc();

    @Query(value = "SELECT * FROM job ORDER BY CAST(salary AS UNSIGNED) DESC", nativeQuery = true)
    List<Job> findAllOrderBySalaryDesc();

    @Query(value = "SELECT * FROM job ORDER BY CAST(salary AS UNSIGNED) ASC", nativeQuery = true)
    List<Job> findAllOrderBySalaryAsc();
   


}
