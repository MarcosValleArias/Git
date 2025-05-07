package aiss.gitminer.controller;

import aiss.gitminer.model.Commit;
import aiss.gitminer.repository.CommitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gitminer/commits")
public class CommitController {

    @Autowired
    private CommitRepository commitRepository;

    @GetMapping
    public List<Commit> getAllCommits() {
        return commitRepository.findAll();
    }

    @GetMapping("/{id}")
    public Commit getCommitById(@PathVariable String id) {
        return commitRepository.findById(id).orElse(null);
    }
}
