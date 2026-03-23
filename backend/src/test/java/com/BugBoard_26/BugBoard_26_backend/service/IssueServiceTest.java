package com.BugBoard_26.BugBoard_26_backend.service;

import com.BugBoard_26.BugBoard_26_backend.model.*;
import com.BugBoard_26.BugBoard_26_backend.repository.IssueRepository;
import com.BugBoard_26.BugBoard_26_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test unitari per IssueService.
 *
 * Metodo testato:
 * - getIssuesFiltered(Status, Priority, IssueType,
 * String sortBy, String sortDir, int page, int size)
 *
 * Strategia: mock di repository e NotificationService.
 * Nessun contesto Spring necessario.
 */
@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private IssueService issueService;

    private Page<Issue> paginaVuota;

    @BeforeEach
    void setUp() {
        paginaVuota = new PageImpl<>(Collections.emptyList());
    }

    /**
     * EC1 – sortBy campo valido ("title"), sortDir "asc"
     * Il PageRequest deve usare "title" con ordinamento ASC.
     */
    @Test
    void getIssuesFiltered_sortByValido_usaCampoCorretto() {
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        when(issueRepository.findByFilters(any(), any(), any(), captor.capture()))
                .thenReturn(paginaVuota);

        issueService.getIssuesFiltered(null, null, null, "title", "asc", 0, 10);

        Sort.Order order = captor.getValue().getSort().getOrderFor("title");
        assertNotNull(order, "Il campo 'title' deve essere usato come sort field");
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    /**
     * EC2 – sortBy campo NON valido ("pippo")
     * Deve ricadere su "dateAdded" come fallback.
     */
    @Test
    void getIssuesFiltered_sortByNonValido_fallbackSuDateAdded() {
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        when(issueRepository.findByFilters(any(), any(), any(), captor.capture()))
                .thenReturn(paginaVuota);

        issueService.getIssuesFiltered(null, null, null, "pippo", "desc", 0, 10);

        Sort.Order order = captor.getValue().getSort().getOrderFor("dateAdded");
        assertNotNull(order, "Con sortBy non valido deve ricadere su 'dateAdded'");
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    /**
     * EC3 – sortDir = "ASC" (case insensitive), ordinamento atteso: ASC.
     */
    @Test
    void getIssuesFiltered_sortDirAscCaseInsensitive_usaAscending() {
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        when(issueRepository.findByFilters(any(), any(), any(), captor.capture()))
                .thenReturn(paginaVuota);

        issueService.getIssuesFiltered(null, null, null, "status", "ASC", 0, 5);

        Sort.Order order = captor.getValue().getSort().getOrderFor("status");
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    /**
     * EC4 – sortDir non riconosciuto ("xyz"), ordinamento atteso: DESC (default).
     */
    @Test
    void getIssuesFiltered_sortDirNonValido_usaDescending() {
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        when(issueRepository.findByFilters(any(), any(), any(), captor.capture()))
                .thenReturn(paginaVuota);

        issueService.getIssuesFiltered(null, null, null, "priority", "xyz", 0, 10);

        Sort.Order order = captor.getValue().getSort().getOrderFor("priority");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    /**
     * EC5 – Struttura della Map restituita:
     * deve contenere le 4 chiavi attese.
     */
    @Test
    void getIssuesFiltered_ritornaMapConChiaviCorrette() {
        when(issueRepository.findByFilters(any(), any(), any(), any()))
                .thenReturn(paginaVuota);

        Map<String, Object> result = issueService.getIssuesFiltered(null, null, null, "title", "asc", 0, 10);

        assertTrue(result.containsKey("content"));
        assertTrue(result.containsKey("totalPages"));
        assertTrue(result.containsKey("totalElements"));
        assertTrue(result.containsKey("currentPage"));
    }
}
