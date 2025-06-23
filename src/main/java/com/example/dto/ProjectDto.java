package com.example.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProjectDto {
    @JsonProperty("projectName")
    private String title;
    private String description;
}
