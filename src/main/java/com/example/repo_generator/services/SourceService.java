package com.example.repo_generator.services;

import com.example.repo_generator.dataDase.ProjectEntity;
import com.example.repo_generator.exceptions.RepositoryNotFoundException;
import com.example.repo_generator.repository.ProjectRepository;
import com.example.repo_generator.yaml.ApplicationLoader;
import com.example.repo_generator.yaml.SourceLoader;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.example.repo_generator.services.Request.convertToProjectList;
import static com.example.repo_generator.services.Request.getRepositoryFile;

@Component
public class SourceService implements ServerRequests {
    @Autowired
    private ApplicationLoader applicationLoader;
    private final RestTemplate template = new RestTemplate(new HttpComponentsClientHttpRequestFactory());;
    @Autowired
    private SourceLoader sourceLoader;
    @Override
    public List<ProjectEntity> getProjects() {
        String url = String.format("%s/users/%s/projects",
                sourceLoader.getApi(),
                sourceLoader.getUserId());
        return convertToProjectList(
                Request.getRequest(url, sourceLoader.getToken(), template)
        );
    }

    @Override
    public void updateProject(String name, ProjectRepository projectRepository) throws GitAPIException {
        List<ProjectEntity> projectEntities = getProjects();
        ProjectEntity projectEntity = projectEntities.stream()
                .filter(project -> project.getName().equals(name))
                .findFirst()
                .orElseThrow(RepositoryNotFoundException::new);
        checkProject(projectEntity, projectRepository);
    }

    @Override
    public void synchronizeProjects(ProjectRepository projectRepository) throws GitAPIException {
        List<ProjectEntity> pageList = getProjects();
        for (ProjectEntity project: pageList){
            checkProject(project, projectRepository);
        }
    }

    private File checkProject(ProjectEntity project, ProjectRepository projectRepository) throws GitAPIException {
        File pathToRepositories = new File(applicationLoader.getPath());
        File repository = getRepositoryFile(project.getName(), pathToRepositories);
        if (repository == null){
            repository = new File(pathToRepositories, project.getName());
            repository.mkdir();
            projectRepository.save(project);
            copyProject(repository, project);
        }
        else {
            CredentialsProvider cp = new UsernamePasswordCredentialsProvider(sourceLoader.getUserName(), sourceLoader.getToken());
            Git git;
            try {
                git = Git.open(repository);
                String branch;
                List<Ref> listRefs = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
                for (Ref ref: listRefs){
                    branch = Request.checkoutBranch(git, ref);
                    git.pull().setRemoteBranchName(branch).setCredentialsProvider(cp).call();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            git.close();
        }
        return repository;
    }

    private void copyProject(File repository, ProjectEntity project) throws GitAPIException{
        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(sourceLoader.getUserName(), sourceLoader.getToken());
        CloneCommand cloneRepository = Git.cloneRepository()
                .setCredentialsProvider(cp)
                .setURI(sourceLoader.getPath() + "/" + sourceLoader.getUserName() + "/" + project.getName())
                .setCloneAllBranches(true)
                .setDirectory(repository);
        Git git = cloneRepository.call();
        String branch;
        List<Ref> listRefs = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
        for (Ref ref: listRefs){
            branch = Request.checkoutBranch(git, ref);
            System.out.println(branch);
            git.pull().setRemoteBranchName(branch).setCredentialsProvider(cp).call();
        }
        git.close();
    }
}
