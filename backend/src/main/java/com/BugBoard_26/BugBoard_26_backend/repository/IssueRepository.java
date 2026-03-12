package com.BugBoard_26.BugBoard_26_backend.repository;

import com.BugBoard_26.BugBoard_26_backend.model.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.BugBoard_26.BugBoard_26_backend.model.Status;
import com.BugBoard_26.BugBoard_26_backend.model.Priority;
import com.BugBoard_26.BugBoard_26_backend.model.IssueType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssueRepository extends JpaRepository<Issue, Long> {

        // Metodo per trovare tutti i ticket di un utente
        List<Issue> findByAssigneeId(Long assigneeId);

        // Metodo per filtrare i ticket per stato
        List<Issue> findByStatus(Status status);

        // Metodo per filtrare i ticket per priorità
        List<Issue> findByPriority(Priority priority);

        // Metodo per filtrare i ticket per titolo o descrizione
        @Query("SELECT i FROM Issue i WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :description, '%'))")
        List<Issue> findByTitleContainingOrDescriptionContainingIgnoreCase(
                        @Param("title") String title,
                        @Param("description") String description);

        // Req 3: Filtri opzionali + ordinamento dinamico + paginazione
        @Query("SELECT i FROM Issue i WHERE " +
                        "(:status IS NULL OR i.status = :status) AND " +
                        "(:priority IS NULL OR i.priority = :priority) AND " +
                        "(:type IS NULL OR i.type = :type)")
        Page<Issue> findByFilters(
                        @Param("status") Status status,
                        @Param("priority") Priority priority,
                        @Param("type") IssueType type,
                        Pageable pageable);
}
