package com.BugBoard_26.BugBoard_26_backend.service;

import com.BugBoard_26.BugBoard_26_backend.dto.NotificationDTO;
import com.BugBoard_26.BugBoard_26_backend.model.Notification;
import com.BugBoard_26.BugBoard_26_backend.model.User;
import com.BugBoard_26.BugBoard_26_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Crea e salva una nuova notifica per un utente
    public void createNotification(User user, String message, Long issueId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setIssueId(issueId);
        notificationRepository.save(notification);
    }

    // Recupera tutte le notifiche di un utente
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Conta le notifiche non lette di un utente
    @Transactional(readOnly = true)
    public Long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    // Segna una notifica come letta
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    // Elimina tutte le notifiche di un utente
    @Transactional
    public void deleteAllForUser(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(notifications);
    }

    // Segna tutte le notifiche di un utente come lette
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    // Elimina una notifica
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    private NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setMessage(n.getMessage());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setIssueId(n.getIssueId());
        return dto;
    }

}
