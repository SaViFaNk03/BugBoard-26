package com.BugBoard_26.BugBoard_26_backend.controller;

import com.BugBoard_26.BugBoard_26_backend.dto.NotificationDTO;
import com.BugBoard_26.BugBoard_26_backend.model.User;
import com.BugBoard_26.BugBoard_26_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications/me — tutte le notifiche dell'utente loggato
    @GetMapping("/me")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getNotificationsForUser(currentUser.getId()));
    }

    // GET /api/notifications/me/unread-count — numero notifiche non lette
    @GetMapping("/me/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        long count = notificationService.countUnread(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // PUT /api/notifications/{id}/read — segna una notifica come letta
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/notifications/me/read-all — segna tutte come lette
    @PutMapping("/me/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/notifications/me/all — elimina tutte le notifiche dell'utente
    // loggato
    @DeleteMapping("/me/all")
    public ResponseEntity<Void> deleteAllMyNotifications(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        notificationService.deleteAllForUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/notifications/{id} — elimina una notifica
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
