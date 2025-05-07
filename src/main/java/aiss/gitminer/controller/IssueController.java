package aiss.gitminer.controller;

import aiss.gitminer.model.Issue;
import aiss.gitminer.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gitminer/issues")
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;

    @GetMapping
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    @GetMapping("/{id}")
    public Issue getIssueById(@PathVariable String id) {
        return issueRepository.findById(id).orElse(null);
    }

    @GetMapping("/status/{state}")
    public List<Issue> getIssuesByState(@PathVariable String state) {
        return issueRepository.findByState(state);
    }
}
