import { Component, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, ToastMessage } from '../../services/toast.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-toast',
  imports: [CommonModule],
  templateUrl: './toast.html',
  styleUrl: './toast.css',
})
export class Toast implements OnDestroy {
  toasts: ToastMessage[] = [];
  private sub: Subscription;

  constructor(
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {
    this.sub = this.toastService.toastState$.subscribe(toast => {
      this.toasts.push(toast);
      this.cdr.detectChanges(); // Force angular to update the view immediately

      setTimeout(() => {
        this.removeToast(toast);
      }, 4000);
    });
  }

  removeToast(toast: ToastMessage): void {
    this.toasts = this.toasts.filter(t => t !== toast);
  }

  ngOnDestroy(): void {
    if (this.sub) this.sub.unsubscribe();
  }
}
