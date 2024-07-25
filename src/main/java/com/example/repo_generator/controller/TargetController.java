package com.example.repo_generator.controller;

import com.example.repo_generator.dataDase.ProjectEntity;
import com.example.repo_generator.repository.ProjectRepository;
import com.example.repo_generator.services.TargetService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("target")
public class TargetController {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TargetService targetService;
    @GetMapping
    public List<ProjectEntity> getProjects(){
        return targetService.getProjects();
    }

    @PutMapping("/update/{name}")
    public void updateProject(@PathVariable String name) throws GitAPIException {
        targetService.updateProject(name, projectRepository);
    }

    @PutMapping("/synchronize")
    public void synchronizeProjects() throws GitAPIException{
        targetService.synchronizeProjects(projectRepository);
    }
}
