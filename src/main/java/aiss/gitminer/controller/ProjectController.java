package aiss.gitminer.controller;

import aiss.gitminer.model.*;
import aiss.gitminer.model.GITCLASS.*;
import aiss.gitminer.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gitminer/projects")
public class ProjectController {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ProjectRepository projectRepository;

    @PostMapping("/{owner}/{repo}")
    public ResponseEntity<Project> importProjectFromGitHub(@PathVariable String owner, @PathVariable String repo) {
        String url = "http://localhost:8081/api/github/projects/" + owner + "/" + repo;

        ProjectGITCOPY projectGITCOPY = restTemplate.getForObject(url, ProjectGITCOPY.class);

        if (projectGITCOPY == null) {
            return ResponseEntity.badRequest().build();
        }

        Project localProject = mapToLocalProject(projectGITCOPY);

        Project saved = projectRepository.save(localProject);

        return ResponseEntity.ok(saved);
    }

    private Project mapToLocalProject(ProjectGITCOPY projectGITCOPY) {
        Project project = new Project();
        project.setId(projectGITCOPY.getId());
        project.setName(projectGITCOPY.getName());
        project.setWebUrl(projectGITCOPY.getWebUrl());

        List<Commit> commits = projectGITCOPY.getCommits().stream()
                .map(this::mapToLocalCommit)
                .collect(Collectors.toList());
        project.setCommits(commits);

        List<Issue> issues = projectGITCOPY.getIssues().stream()
                .map(this::mapToLocalIssue)
                .collect(Collectors.toList());
        project.setIssues(issues);

        return project;
    }


    private Commit mapToLocalCommit(CommitGITCOPY commitGITCOPY) {
        Commit commit = new Commit();
        commit.setId(commitGITCOPY.getId());
        commit.setTitle(commitGITCOPY.getTitle());
        commit.setMessage(commitGITCOPY.getMessage());
        commit.setAuthorName(commitGITCOPY.getAuthorName());
        commit.setAuthorEmail(commitGITCOPY.getAuthorEmail());
        commit.setAuthoredDate(commitGITCOPY.getAuthoredDate());
        commit.setWebUrl(commitGITCOPY.getWebUrl());
        return commit;
    }


    private Issue mapToLocalIssue(IssueGITCOPY issueGITCOPY) {
        Issue issue = new Issue();
        issue.setId(issueGITCOPY.getId());
        issue.setTitle(issueGITCOPY.getTitle());
        issue.setState(issueGITCOPY.getState());
        issue.setCreatedAt(issueGITCOPY.getCreatedAt());
        issue.setUpdatedAt(issueGITCOPY.getUpdatedAt());
        return issue;
    }


    private User mapToLocalUser(UserGITCOPY gitUser) {
        User user = new User();

        user.setId(gitUser.getId());
        user.setUsername(gitUser.getUsername());
        user.setName(gitUser.getName());
        user.setAvatarUrl(gitUser.getAvatarUrl());
        user.setWebUrl(gitUser.getWebUrl());

        return user;
    }

    private Comment mapToLocalComment(CommentGITCOPY gitComment) {
        Comment comment = new Comment();

        comment.setId(gitComment.getId());
        comment.setBody(gitComment.getBody());
        comment.setCreatedAt(gitComment.getCreatedAt());
        comment.setUpdatedAt(gitComment.getUpdatedAt());

        if (gitComment.getAuthor() != null) {
            comment.setAuthor(mapToLocalUser(gitComment.getAuthor()));
        }

        return comment;
    }



    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable String id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
