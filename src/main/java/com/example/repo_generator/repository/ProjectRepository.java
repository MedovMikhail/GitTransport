package com.example.repo_generator.repository;

import com.example.repo_generator.dataDase.ProjectEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends CrudRepository<ProjectEntity, String> {
    ProjectEntity save(ProjectEntity projectEntity);
    void delete(ProjectEntity entity);
    ProjectEntity findByName(String name);
    List<ProjectEntity> findAll();
}
