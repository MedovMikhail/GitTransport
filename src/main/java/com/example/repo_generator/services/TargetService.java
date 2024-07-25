package com.example.repo_generator.services;

import com.example.repo_generator.dataDase.ProjectEntity;
import com.example.repo_generator.exceptions.RepositoryNotFoundException;
import com.example.repo_generator.repository.ProjectRepository;
import com.example.repo_generator.yaml.ApplicationLoader;
import com.example.repo_generator.yaml.TargetLoader;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.example.repo_generator.services.Request.convertToProjectList;

@Component
public class TargetService implements ServerRequests {
    private final RestTemplate template = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    @Autowired
    private TargetLoader targetLoader;
    @Autowired
    private ApplicationLoader applicationLoader;
    @Override
    public List<ProjectEntity> getProjects() {
        String url = String.format("%s/user/repos",
                targetLoader.getApi());
        return convertToProjectList(
                Request.getRequest(url, targetLoader.getToken(), template)
        );
    }

    @Override
    public void updateProject(String name, ProjectRepository projectRepository) throws GitAPIException {
        File repository = getRepositoryFile(name);
        if (repository == null) throw new RepositoryNotFoundException();
        List<ProjectEntity> projects = getProjects();
        ProjectEntity repo = projects.stream()
                .filter(project -> Objects.equals(project.getName(), name))
                .findFirst()
                .orElse(null);
        ProjectEntity uploadRepo = projectRepository.findByName(name);
        uploadRepository(name, repo, uploadRepo, repository, projectRepository);
    }

    private void uploadRepository(String name, ProjectEntity repo, ProjectEntity uploadRepo, File repository, ProjectRepository projectRepository) throws GitAPIException {
        String url;
        if (repo == null){
            url = targetLoader.getPath() + "/" + targetLoader.getUserName() + "/" + Request.postRequest(String.format("%s/user/repos",
                    targetLoader.getApi()), targetLoader.getToken(), uploadRepo, template);
        }
        else{
            url = String.format("%s/%s/%s", targetLoader.getPath(), targetLoader.getUserName(), name);
        }
        try {
            Git git = Git.open(repository);
            Config config = git.getRepository().getConfig();
            config.setString("remote", "origin", "url", url + ".git");
            git.add();
            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(targetLoader.getUserName(), targetLoader.getToken()))
                    .setPushAll()
                    .call();
            git.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ProjectEntity projects = projectRepository.findByName(name);
        Map<String, String> pr = new HashMap<>();
        pr.put("visibility", uploadRepo.getVisibility());
        String properties = JSONObject.valueToString(pr);
        String url2 = String.format("%s/repos/%s/%s", targetLoader.getApi(), targetLoader.getUserName(), projects.getName());
        setProperties(url2, properties);
    }

    @Override
    public void synchronizeProjects(ProjectRepository projectRepository) throws GitAPIException {
        List<ProjectEntity> projectEntities = projectRepository.findAll();
        for (ProjectEntity projectEntity: projectEntities){
            updateProject(projectEntity.getName(), projectRepository);
        }
    }

    private File getRepositoryFile(String name) {
        File pathToRepositories = new File(applicationLoader.getPath());
        return Arrays.stream(pathToRepositories.listFiles())
                .filter(r -> r.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private void setProperties(String url, String properties){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(targetLoader.getToken());
        HttpEntity<String> request = new HttpEntity<>(properties, headers);
        template.exchange(url, HttpMethod.PATCH, request, ProjectEntity.class);
    }
}
