import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { NotificationBellComponent } from '../notification-bell/notification-bell.component';

@Component({
  selector: 'app-header',
  imports: [CommonModule, RouterModule, NotificationBellComponent],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header implements OnInit, OnDestroy {
  isAdmin: boolean = false;
  show: boolean = false;
  private sub: any;

  constructor(private router: Router) {
    this.sub = this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.show = this.router.url !== '/login';
        this.isAdmin = localStorage.getItem('role') === 'ADMIN';
      }
    });
  }

  ngOnInit(): void {
    this.show = this.router.url !== '/login';
    this.isAdmin = localStorage.getItem('role') === 'ADMIN';
  }

  ngOnDestroy(): void {
    if (this.sub) this.sub.unsubscribe();
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userID');
    localStorage.removeItem('role');
    this.router.navigate(['/login']);
  }
}
