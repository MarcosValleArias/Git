package aiss.gitminer.controller;

import aiss.gitminer.model.*;
import aiss.gitminer.model.GITCLASS.*;
import aiss.gitminer.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gitminer/projects")
public class ProjectController {

    private final RestTemplate restTemplate;

    @Autowired
    public ProjectController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    ProjectRepository projectRepository;

    @PostMapping("/{owner}/{repo}")
    public ResponseEntity<Project> importProjectFromGitHub(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "2") int sinceCommits,
            @RequestParam(defaultValue = "20") int sinceIssues,
            @RequestParam(defaultValue = "2") int maxPages) {
        String url = "http://localhost:8081/api/github/projects/" + owner + "/" + repo;
        ProjectGITCOPY projectGITCOPY = restTemplate.getForObject(url, ProjectGITCOPY.class);

        if (projectGITCOPY == null) {
            return ResponseEntity.badRequest().build();
        }
        Project localProject = mapToLocalProject(projectGITCOPY, sinceCommits, sinceIssues, maxPages);
        Project saved = projectRepository.save(localProject);
        return ResponseEntity.ok(saved);
    }

    private Project mapToLocalProject(ProjectGITCOPY projectGITCOPY, int sinceCommits, int sinceIssues, int maxPages) {
        Project project = new Project();
        project.setId(projectGITCOPY.getId());
        project.setName(projectGITCOPY.getName());
        project.setWebUrl(projectGITCOPY.getWebUrl());

        List<Commit> commits = projectGITCOPY.getCommits().stream()
                .filter(commit -> isWithinDays(commit.getAuthoredDate(), sinceCommits))
                .limit(maxPages * 30)
                .map(this::mapToLocalCommit)
                .collect(Collectors.toList());
        project.setCommits(commits);

        List<Issue> issues = projectGITCOPY.getIssues().stream()
                .filter(issue -> isWithinDays(issue.getUpdatedAt(), sinceIssues))
                .limit(maxPages * 30)
                .map(this::mapToLocalIssue)
                .collect(Collectors.toList());
        project.setIssues(issues);

        return project;
    }

    private boolean isWithinDays(String date, int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        try {
            Date commitDate = sdf.parse(date);
            Date currentDate = new Date();
            long diffInMillis = currentDate.getTime() - commitDate.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            return diffInDays <= days;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
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
        List<Comment> commentsL = new java.util.ArrayList<>();
        issue.setId(issueGITCOPY.getId());
        issue.setTitle(issueGITCOPY.getTitle());
        issue.setState(issueGITCOPY.getState());
        issue.setCreatedAt(issueGITCOPY.getCreatedAt());
        issue.setUpdatedAt(issueGITCOPY.getUpdatedAt());
        issue.setDescription(issueGITCOPY.getDescription());
        issue.setClosedAt(issueGITCOPY.getClosedAt());
        issue.setAuthor(mapToLocalUser(issueGITCOPY.getAuthor()));
        issue.setAssignee(mapToLocalUser(issueGITCOPY.getAssignee()));
        issue.setVotes(issueGITCOPY.getVotes());
        for (CommentGITCOPY c : issueGITCOPY.getComments()) {
            Comment n = mapToLocalComment(c);
            commentsL.add(n);
        }
        issue.setComments(commentsL);
        issue.setLabels(issueGITCOPY.getLabels());
        return issue;
    }

    private User mapToLocalUser(UserGITCOPY gitUser) {
        if (gitUser == null) {
            return null;
        }

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
