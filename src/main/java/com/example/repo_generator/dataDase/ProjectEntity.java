package com.example.repo_generator.dataDase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class ProjectEntity {
    @Id
    private String name;
    private String id;
    private String visibility;
    protected ProjectEntity(){};
    public ProjectEntity(String name, String id, String visibility){
        this.name = name;
        this.id = id;
        this.visibility = visibility;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getVisibility() {
        return visibility;
    }
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
