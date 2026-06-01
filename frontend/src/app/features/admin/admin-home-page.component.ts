import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

interface AdminLink {
  path: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-admin-home-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './admin-home-page.component.html',
  styleUrl: './admin-page.component.scss'
})
export class AdminHomePageComponent {
  readonly links: AdminLink[] = [
    {
      path: '/admin/users',
      title: 'Users',
      description: 'Grant or revoke admin rights and review registered players.'
    },
    {
      path: '/admin/matches',
      title: 'Matches',
      description: 'Create, edit, and lock fixtures.'
    },
    {
      path: '/admin/results',
      title: 'Results',
      description: 'Enter final scores and knockout qualifiers.'
    },
    {
      path: '/admin/recalculate-scores',
      title: 'Scores',
      description: 'Rebuild awarded points and leaderboard totals.'
    },
    {
      path: '/admin/config',
      title: 'Configurations',
      description: 'View effective server configuration and integration status.'
    }
  ];
}
