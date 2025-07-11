import { Component } from '@angular/core';
import {PostHeaderComponent} from "./post-header/post-header.component";
import {PostBodyComponent} from "./post-body/post-body.component";
import {LikeCommentComponent} from "./like-comment/like-comment.component";

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [
    PostHeaderComponent,
    PostBodyComponent,
    LikeCommentComponent
  ],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.css'
})
export class FeedComponent {

}
