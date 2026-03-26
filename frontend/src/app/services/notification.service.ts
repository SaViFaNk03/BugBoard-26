import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Notification } from '../models/notification.model';

import { API_BASE_URL } from '../app.constants';

@Injectable({
    providedIn: 'root'
})
export class NotificationService {
    private apiUrl = `${API_BASE_URL}/notifications`;

    // Oggetto per emettere eventi quando le notifiche vengono lette/aggiornate
    private notificationsUpdatedSource = new Subject<void>();
    notificationsUpdated$ = this.notificationsUpdatedSource.asObservable();

    constructor(private http: HttpClient) { }

    // Notifica ai componenti in ascolto che c'è stato un aggiornamento
    notifyUpdate() {
        this.notificationsUpdatedSource.next();
    }

    // Recupera tutte le notifiche dell'utente loggato
    getMyNotifications(): Observable<Notification[]> {
        return this.http.get<Notification[]>(`${this.apiUrl}/me?_t=${new Date().getTime()}`);
    }

    // Recupera il numero di notifiche non lette
    getUnreadCount(): Observable<{ count: number }> {
        return this.http.get<{ count: number }>(`${this.apiUrl}/me/unread-count?_t=${new Date().getTime()}`);
    }

    // Segna una notifica come letta
    markAsRead(id: number): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/${id}/read`, {}).pipe(
            tap(() => this.notifyUpdate())
        );
    }

    // Elimina una notifica
    deleteNotification(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
            tap(() => this.notifyUpdate())
        );
    }

    // Segna tutte come lette
    markAllAsRead(): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/me/read-all`, {}).pipe(
            tap(() => this.notifyUpdate())
        );
    }

    // Elimina tutte le notifiche dell'utente loggato
    deleteAllMyNotifications(): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/me/all`).pipe(
            tap(() => this.notifyUpdate())
        );
    }
}
