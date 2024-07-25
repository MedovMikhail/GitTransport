package com.example.repo_generator.controller;

import com.example.repo_generator.dataDase.ProjectEntity;
import com.example.repo_generator.repository.ProjectRepository;
import com.example.repo_generator.services.SourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("source")
public class SourceController {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private SourceService sourceService;
    @GetMapping
    @Operation(summary = "Get repositories from source server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public List<ProjectEntity> getProjects(){
        return sourceService.getProjects();
    }
    @PutMapping("/update/{name}")
    @Operation(summary = "update local repository")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public void updateProject(@Parameter(description = "Repository name") @PathVariable String name) throws GitAPIException {
        sourceService.updateProject(name, projectRepository);
    }
    @PutMapping("/synchronize")
    public void synchronizeProjects() throws GitAPIException{
        sourceService.synchronizeProjects(projectRepository);
    }
}
