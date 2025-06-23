package com.example.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.dto.ProjectDto;
import com.example.dto.UserProfileDto;
import com.example.entity.Project;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        return userRepository.save(user);
    }

    public User authenticate(String email, String password) {
        return userRepository.findByEmail(email)
            .filter(u -> u.getPassword().equals(password))
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
    
    
    private List<Project> mapToEntityProjects(List<ProjectDto> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                   .map(dto -> new Project(dto.getTitle(), dto.getDescription()))
                   .collect(Collectors.toList());
    }

    private List<ProjectDto> mapToDtoProjects(List<Project> projects) {
        if (projects == null) return Collections.emptyList();
        return projects.stream()
                       .map(p -> {
                           ProjectDto dto = new ProjectDto();
                           dto.setTitle(p.getTitle());
                           dto.setDescription(p.getDescription());
                           return dto;
                       })
                       .collect(Collectors.toList());
    }

    
    
    

    public UserProfileDto getProfileDto(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return mapToDto(user);
    }

    public UserProfileDto updateProfileDto(String email, UserProfileDto dto) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
        if (dto.getEducation() != null) user.setEducation(dto.getEducation());
        if (dto.getExp() != null) user.setExp(dto.getExp());

        if (user.getRole() == Role.JOB_SEEKER) {
            if (dto.getSkills() != null) user.setSkills(dto.getSkills());
            if (dto.getProjects() != null) user.setProjects(mapToEntityProjects(dto.getProjects()));
        } else if (user.getRole() == Role.EMPLOYER) {
            if (dto.getCompanyName() != null) user.setCompanyName(dto.getCompanyName());
            if (dto.getCompanyWebsite() != null) user.setCompanyWebsite(dto.getCompanyWebsite());
            if (dto.getCompanyDescription() != null) user.setCompanyDescription(dto.getCompanyDescription());
            if (dto.getDesignation() != null) user.setDesignation(dto.getDesignation());
        }

        userRepository.save(user);
        return mapToDto(user);
    }


 
    private UserProfileDto mapToDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setEducation(user.getEducation());
        dto.setExp(user.getExp());


        if (user.getRole() == Role.JOB_SEEKER) {
            dto.setSkills(user.getSkills());
            dto.setProjects(mapToDtoProjects(user.getProjects()));
        } else if (user.getRole() == Role.EMPLOYER) {
            dto.setCompanyName(user.getCompanyName());
            dto.setCompanyWebsite(user.getCompanyWebsite());
            dto.setCompanyDescription(user.getCompanyDescription());
            dto.setDesignation(user.getDesignation());
        }
		return dto;
    }
}
