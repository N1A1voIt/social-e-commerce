import {Component, OnInit} from '@angular/core';
import {PostHeaderComponent} from "./post-header/post-header.component";
import {PostBodyComponent} from "./post-body/post-body.component";
import {LikeCommentComponent} from "./like-comment/like-comment.component";
import {NgForOf, NgIf, TitleCasePipe} from "@angular/common";
import {HttpClient} from "@angular/common/http";

export interface Media {
  id: string | null;
  mediaUrl: string | null;
  idChild: string | number | null;
}

export interface Post {
  id: number;
  message: string | null;
  platform: string;
  username: string;
  medias: Media[] | null;
  childPosts: Post[] | null;
}

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [
    PostHeaderComponent,
    PostBodyComponent,
    LikeCommentComponent,
    NgForOf,
    TitleCasePipe,
    NgIf
  ],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.css'
})
export class FeedComponent implements OnInit {
  posts: Post[] = [];
  constructor(private http: HttpClient) { }
  ngOnInit() {
    // Your backend response data
    this.fetchPosts()
  }
  fetchPosts():void{
    const header = {
      'Authorization': `${localStorage.getItem('token')?.replace('Bearer ', '')}`
    };
    this.http.get<Post[]>('http://localhost:8080/api/posts',{headers:header}).subscribe({
      next: (response) => {
        this.posts = response;
      },
      error: (err) => {
        console.log(err);
      }
    })
  }


  isFacebookPost(post: Post): boolean {
    return post.childPosts !== null;
  }

  isInstagramPost(post: Post): boolean {
    return post.platform === "instagram";
    // return post.childPosts === null && post.medias !== null;
  }

  getPlatformLogo(platform: string): string {
    switch (platform.toLowerCase()) {
      case 'facebook':
        return 'assets/logos/facebook_logo.png';
      case 'instagram':
        return 'assets/logos/instagram_logo.png';
      default:
        return 'assets/logos/default_logo.png';
    }
  }

  getValidMedias(medias: Media[] | null): Media[] {
    if (!medias) return [];
    return medias.filter(media => media.mediaUrl !== null);
  }

  getFacebookPostMedias(post: Post): Media[] {
    if (!post.childPosts) return [];

    const allMedias: Media[] = [];
    post.childPosts.forEach(childPost => {
      if (childPost.medias) {
        allMedias.push(...childPost.medias.filter(media => media.mediaUrl !== null));
      }
    });
    return allMedias;
  }

  getGridClass(mediaCount: number): string {
    switch (mediaCount) {
      case 1:
        return 'grid-cols-1';
      case 2:
        return 'grid-cols-2';
      case 3:
        return 'grid-cols-2 grid-rows-2';
      case 4:
        return 'grid-cols-2 grid-rows-2';
      default:
        return 'grid-cols-2 grid-rows-2';
    }
  }

  getImageClass(mediaCount: number, index: number): string {
    if (mediaCount === 3 && index === 0) {
      return 'row-span-2';
    }
    return '';
  }

  protected readonly JSON = JSON;
}
