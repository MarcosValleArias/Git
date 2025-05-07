package aiss.gitminer.repository;

import aiss.gitminer.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, String> {
    List<Issue> findByState(String state);
}
