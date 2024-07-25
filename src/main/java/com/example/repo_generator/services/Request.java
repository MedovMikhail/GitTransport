package com.example.repo_generator.services;

import com.example.repo_generator.dataDase.ProjectEntity;
import com.example.repo_generator.yaml.ApplicationLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Request {

    @Autowired
    private static ApplicationLoader application;

    public static List getRequest(String url, String accessToken, RestTemplate template) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<List> req;
        try {
            req = template.exchange(RequestEntity.get(new URI(String.format(url))).headers(headers).build(), List.class);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return req.getBody();
    }

    public static File getRepositoryFile(String name, File dir) {
        return Arrays.stream(dir.listFiles())
                .toList()
                .stream()
                .filter(r -> r.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static List<File> getRepositories(){
        File repo = new File(application.getPath());
        return Arrays.asList(repo.listFiles());
    }

    public static String postRequest(String url, String accessToken, ProjectEntity page, RestTemplate template){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<ProjectEntity> request = new HttpEntity<>(page, headers);
        ResponseEntity<ProjectEntity> req = template.exchange(url, HttpMethod.POST, request, ProjectEntity.class);
        return req.getBody().getName();
    }

    public static List<ProjectEntity> convertToProjectList(List<?> projectsList) {
        List<ProjectEntity> pageList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < projectsList.size(); i++){
            String jsonData = JSONObject.valueToString(projectsList.get(i));
            ProjectEntity projectEntity;
            try {
                projectEntity = mapper.readValue(jsonData, ProjectEntity.class);
                pageList.add(projectEntity);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return pageList;
    }
    public static String checkoutBranch(Git git, Ref ref) throws GitAPIException {
        String branchName = ref.getName().substring(ref.getName().lastIndexOf("/") + 1);
        try{
            git.checkout().setName(branchName).call();
        } catch (RefNotFoundException e){
            git.branchCreate().setName(branchName).setStartPoint(ref.getName()).call();
            git.checkout().setName(branchName).call();
        }
        return branchName;
    }
}
