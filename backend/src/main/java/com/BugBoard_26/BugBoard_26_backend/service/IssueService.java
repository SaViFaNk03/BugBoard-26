package com.BugBoard_26.BugBoard_26_backend.service;

import com.BugBoard_26.BugBoard_26_backend.dto.IssueDTO;
import com.BugBoard_26.BugBoard_26_backend.model.Issue;
import com.BugBoard_26.BugBoard_26_backend.model.Status;
import com.BugBoard_26.BugBoard_26_backend.model.User;
import com.BugBoard_26.BugBoard_26_backend.model.Priority;
import com.BugBoard_26.BugBoard_26_backend.model.IssueType;
import com.BugBoard_26.BugBoard_26_backend.repository.IssueRepository;
import com.BugBoard_26.BugBoard_26_backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // RF - 2: Creazione Issue
    public IssueDTO createIssue(IssueDTO issueDTO) {
        Issue issue = new Issue();
        issue.setTitle(issueDTO.getTitle());
        issue.setDescription(issueDTO.getDescription());
        issue.setPriority(issueDTO.getPriority());
        issue.setStatus(issueDTO.getStatus());
        issue.setDeadline(issueDTO.getDeadline());
        issue.setDateResolved(issueDTO.getDateResolved());
        issue.setImageUrl(issueDTO.getImageUrl());
        issue.setType(issueDTO.getType());

        if (issueDTO.getReporterId() != null) {
            User reporter = userRepository.findById(issueDTO.getReporterId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Reporter non trovato con ID: " + issueDTO.getReporterId()));
            issue.setReporter(reporter);
        }

        if (issueDTO.getAssigneeId() != null) {
            User assignee = userRepository.findById(issueDTO.getAssigneeId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Assignee non trovato con ID: " + issueDTO.getAssigneeId()));
            issue.setAssignee(assignee);
        }

        return toDTO(issueRepository.save(issue));
    }

    // RF - 3: Visualizzazione Issue
    @Transactional(readOnly = true)
    public List<IssueDTO> getAllIssues() {
        return issueRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public IssueDTO getIssueById(Long issueId) {
        return toDTO(issueRepository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Issue non trovata con ID: " + issueId)));
    }

    @Transactional(readOnly = true)
    public List<IssueDTO> getIssuesFiltered(Status status, Priority priority, IssueType type,
            String sortBy, String sortDir) {
        // Campi ordinabili ammessi (whitelist per sicurezza)
        java.util.Set<String> allowed = java.util.Set.of("dateAdded", "deadline", "priority", "status", "title");
        String field = allowed.contains(sortBy) ? sortBy : "dateAdded";
        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();
        return issueRepository.findByFilters(status, priority, type, sort)
                .stream().map(this::toDTO).toList();
    }

    // RF - 11: Ricerca Issue
    public List<IssueDTO> searchIssues(String keyword) {
        return issueRepository
                .findByTitleContainingOrDescriptionContainingIgnoreCase(keyword, keyword)
                .stream().map(this::toDTO).toList();
    }

    // RF - 6: Modifica Issue & RF - 4: Assegnazione Issue
    @Transactional
    public IssueDTO updateIssue(Long issueId, IssueDTO issueDTO) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Issue non trovata con ID: " + issueId));

        boolean wasAlreadyDone = (issue.getStatus() == Status.DONE);

        issue.setTitle(issueDTO.getTitle());
        issue.setDescription(issueDTO.getDescription());
        issue.setPriority(issueDTO.getPriority());
        issue.setStatus(issueDTO.getStatus());
        issue.setDeadline(issueDTO.getDeadline());
        issue.setDateResolved(issueDTO.getDateResolved());
        issue.setImageUrl(issueDTO.getImageUrl());
        issue.setType(issueDTO.getType());

        // Assegnazione Utente (RF-4)
        if (issueDTO.getAssigneeId() != null) {
            User assignee = userRepository.findById(issueDTO.getAssigneeId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Assignee non trovato con ID: " + issueDTO.getAssigneeId()));
            issue.setAssignee(assignee);

            // Notifica a chi è stata assegnata l'issue
            notificationService.createNotification(
                    assignee,
                    "Ti è stata assegnata una nuova issue : \"" + issue.getTitle() + "\"",
                    issue.getId());
        } else {
            issue.setAssignee(null);
        }

        // Notifica al reporter quando l'issue viene chiusa
        if (issueDTO.getStatus() == Status.DONE && !wasAlreadyDone && issue.getReporter() != null) {
            notificationService.createNotification(
                    issue.getReporter(),
                    "La tua issue : \"" + issue.getTitle() + "\" è stata risolta e chiusa",
                    issue.getId());
        }

        return toDTO(issueRepository.save(issue));
    }

    // Cancellazione
    public void deleteIssue(Long issueId) {
        if (!issueRepository.existsById(issueId)) {
            throw new EntityNotFoundException("Issue non trovata con ID: " + issueId);
        }
        issueRepository.deleteById(issueId);
    }

    // Converte Issue entità → IssueDTO (con ID flat invece di oggetti annidati)
    private IssueDTO toDTO(Issue issue) {
        IssueDTO dto = new IssueDTO();
        dto.setId(issue.getId());
        dto.setTitle(issue.getTitle());
        dto.setDescription(issue.getDescription());
        dto.setPriority(issue.getPriority());
        dto.setStatus(issue.getStatus());
        dto.setDeadline(issue.getDeadline());
        dto.setDateResolved(issue.getDateResolved());
        dto.setImageUrl(issue.getImageUrl());
        dto.setType(issue.getType());
        dto.setDateAdded(issue.getDateAdded());
        if (issue.getAssignee() != null) {
            dto.setAssigneeId(issue.getAssignee().getId());
            dto.setAssigneeFullName(issue.getAssignee().getName() + " " + issue.getAssignee().getSurname());
        }
        if (issue.getReporter() != null) {
            dto.setReporterId(issue.getReporter().getId());
            dto.setReporterFullName(issue.getReporter().getName() + " " + issue.getReporter().getSurname());
        }
        return dto;
    }
}
