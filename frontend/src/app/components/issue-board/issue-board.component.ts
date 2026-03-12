import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { IssueService, PagedResponse } from '../../services/issue.service';
import { Issue, Status, Priority } from '../../models/issue.model';
import { NotificationBellComponent } from '../notification-bell/notification-bell.component';
import { ConfirmModal } from '../confirm-modal/confirm-modal';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-issue-board',
  standalone: true,
  imports: [CommonModule, RouterModule, NotificationBellComponent, ConfirmModal],
  templateUrl: './issue-board.component.html',
  styleUrls: ['./issue-board.component.css']
})
export class IssueBoardComponent implements OnInit {
  issues: Issue[] = [];
  status = Status;
  isAdmin: boolean = localStorage.getItem('role') === 'ADMIN';
  currentUserId: number = Number(localStorage.getItem('userID'));
  isLoading: boolean = true;

  // Paginazione
  currentPage: number = 0;
  totalPages: number = 0;
  pageSize: number = 10;

  constructor(
    private issueService: IssueService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toastService: ToastService
  ) { }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userID');
    localStorage.removeItem('role');
    this.router.navigate(['/login']);
  }

  // Filtri
  filterStatus: string = '';
  filterPriority: string = '';
  filterType: string = '';
  filterMine: boolean = false;
  sortBy: string = 'dateAdded';
  sortDir: string = 'desc';


  ngOnInit(): void {
    this.loadIssues();
  }

  loadIssues(): void {
    this.isLoading = true;
    this.issueService.getAllIssues(this.currentPage, this.pageSize).subscribe({
      next: (data: PagedResponse) => {
        this.issues = this.filterMine
          ? data.content.filter((i: Issue) => i.assigneeId === Number(localStorage.getItem('userID')))
          : data.content;
        this.totalPages = data.totalPages;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error:', err);
        this.isLoading = false;
        this.toastService.show('Errore nel caricamento dei ticket', 'error');
      }
    });
  }

  showConfirmDelete: boolean = false;
  issueToDelete: number | null = null;

  promptDeleteIssue(id: number | undefined): void {
    if (!id) return;
    this.issueToDelete = id;
    this.showConfirmDelete = true;
  }

  confirmDelete(): void {
    if (!this.issueToDelete) return;
    this.issueService.deleteIssue(this.issueToDelete).subscribe({
      next: () => {
        this.showConfirmDelete = false;
        this.issueToDelete = null;
        this.toastService.show('Ticket eliminato con successo', 'success');
        this.loadIssues();
      },
      error: (err) => {
        console.error(err);
        this.showConfirmDelete = false;
        this.toastService.show('Errore durante l\'eliminazione del ticket', 'error');
      }
    });
  }

  cancelDelete(): void {
    this.showConfirmDelete = false;
    this.issueToDelete = null;
  }

  deleteIssue(id: number | undefined): void {
    this.promptDeleteIssue(id);
  }
  applyFilters(): void {
    this.currentPage = 0; // reset alla prima pagina quando cambiano i filtri
    this.fetchFiltered();
  }

  fetchFiltered(): void {
    this.isLoading = true;
    const params: any = {};
    if (this.filterStatus) params['status'] = this.filterStatus;
    if (this.filterPriority) params['priority'] = this.filterPriority;
    if (this.filterType) params['type'] = this.filterType;
    params['sortBy'] = this.sortBy;
    params['sortDir'] = this.sortDir;
    this.issueService.getIssuesFiltered(params, this.currentPage, this.pageSize).subscribe({
      next: (data: PagedResponse) => {
        this.issues = this.filterMine
          ? data.content.filter((i: Issue) => i.assigneeId === Number(localStorage.getItem('userID')))
          : data.content;
        this.totalPages = data.totalPages;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Filter Error:', err);
        this.isLoading = false;
        this.toastService.show('Errore durante il filtraggio', 'error');
      }
    });
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    const hasFilters = this.filterStatus || this.filterPriority || this.filterType;
    hasFilters ? this.fetchFiltered() : this.loadIssues();
  }
  resetFilters(statusEl: HTMLSelectElement, priorityEl: HTMLSelectElement, typeEl: HTMLSelectElement,
    sortByEl: HTMLSelectElement, sortDirEl: HTMLSelectElement): void {
    this.filterStatus = '';
    this.filterPriority = '';
    this.filterType = '';
    this.sortBy = 'dateAdded';
    this.sortDir = 'desc';
    this.currentPage = 0;
    statusEl.value = '';
    priorityEl.value = '';
    typeEl.value = '';
    sortByEl.value = 'dateAdded';
    sortDirEl.value = 'desc';
    this.loadIssues();
  }
  getPriorityClass(priority: Priority): string {
    switch (priority) {
      case Priority.CRITICAL: return 'badge-critical';
      case Priority.HIGH: return 'badge-high';
      case Priority.MEDIUM: return 'badge-medium';
      case Priority.LOW: return 'badge-low';
      default: return 'badge-default';
    }
  }
  getStatusClass(status: Status): string {
    switch (status) {
      case Status.TODO: return 'status-todo';
      case Status.IN_PROGRESS: return 'status-progress';
      case Status.DONE: return 'status-done';
      case Status.DUPLICATE: return 'status-duplicate';
      default: return 'status-default';
    }
  }
  onSearch(keyword: string): void {
    if (!keyword.trim()) {
      this.loadIssues();
      return;
    }
    this.isLoading = true;
    this.issueService.searchIssues(keyword).subscribe({
      next: (data) => {
        this.issues = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err)
        this.isLoading = false;
        this.toastService.show('Errore durante la ricerca', 'error');
      }
    })
  }
  canEdit(issue: Issue): boolean {
    return this.isAdmin || issue.assigneeId === this.currentUserId;
  }
}