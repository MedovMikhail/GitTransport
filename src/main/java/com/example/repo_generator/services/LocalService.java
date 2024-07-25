package com.example.repo_generator.services;

import com.example.repo_generator.yaml.ApplicationLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class LocalService {
    @Autowired
    private ApplicationLoader application;
    public List<Map<String, String>> getRepositories(){
        File dir = new File(application.getPath());
        List<Map<String, String>> repos = new ArrayList<>();
        for (File repo: dir.listFiles()){
            repos.add(new HashMap<>(){{put("name",repo.getName());}});
        }
        return repos;
    }
}
