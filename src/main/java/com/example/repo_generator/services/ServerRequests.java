package com.example.repo_generator.services;

import com.example.repo_generator.dataDase.ProjectEntity;
import com.example.repo_generator.repository.ProjectRepository;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.List;

public interface ServerRequests {
    List<ProjectEntity> getProjects();
    void updateProject(String name, ProjectRepository projectRepository) throws GitAPIException;
    void synchronizeProjects(ProjectRepository projectRepository) throws GitAPIException;
}
