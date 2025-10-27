# Post Repost/Share Feature - Frontend Integration Guide

## Overview
This feature allows sellers to reuse/share existing posts to other social media platforms. When reposting, media files are automatically downloaded from the original platform (Facebook, Instagram, etc.) and uploaded to Supabase storage for long-term persistence.

## Setup Requirements

### 1. Environment Variables
Add the following to your `.env` file:

```bash
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_KEY=your-supabase-anon-or-service-key
```

### 2. Supabase Storage Bucket
Create a bucket named `post-media` in your Supabase project:
1. Go to Supabase Dashboard → Storage
2. Create a new bucket: `post-media`
3. Set it to **Public** (so URLs are accessible)
4. Configure CORS if needed for your domain

## API Endpoints

### 1. Get Repost Preview
**GET** `/api/posts/{postId}/repost-preview`

Preview what will be reposted without actually posting.

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:**
```json
{
  "postId": 123,
  "originalMessage": "Check out this amazing product!",
  "mediaCount": 3,
  "createdAt": "2025-01-15T10:30:00",
  "postType": "post"
}
```

### 2. Repost to New Platforms
**POST** `/api/posts/repost`

Repost an existing post to one or more platforms.

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "originalPostId": 123,
  "pagesIds": [
    {
      "pageId": "facebook_page_id_1",
      "platform": "facebook"
    },
    {
      "pageId": "instagram_page_id_1",
      "platform": "instagram"
    }
  ],
  "includeOriginalMessage": true,
  "additionalMessage": "Reposting this awesome content!",
  "scheduledUnixTime": null
}
```

**Request Parameters:**
- `originalPostId` (required): The ID of the post to repost
- `pagesIds` (required): Array of platforms/pages to share to
  - `pageId`: Platform-specific page identifier
  - `platform`: Platform name ("facebook", "instagram", etc.)
- `includeOriginalMessage` (optional): Whether to include the original post message (default: true)
- `additionalMessage` (optional): Additional text to append to the post
- `scheduledUnixTime` (optional): Unix timestamp in milliseconds for scheduling (null for immediate post)

**Response:**
```json
{
  "newPostId": 456,
  "originalPostId": 123,
  "publishedChildren": [
    {
      "childId": 789,
      "platform": "facebook",
      "postUrl": "https://www.facebook.com/12345678",
      "success": true,
      "errorMessage": null
    },
    {
      "childId": 790,
      "platform": "instagram",
      "postUrl": "https://www.instagram.com/p/ABC123",
      "success": true,
      "errorMessage": null
    }
  ],
  "message": "Post successfully reposted to 2 platform(s)",
  "createdAt": "2025-01-15T11:00:00",
  "isScheduled": false
}
```

## Frontend Implementation Examples

### React/TypeScript Example

```typescript
interface RepostArgs {
  originalPostId: number;
  pagesIds: Array<{
    pageId: string;
    platform: string;
  }>;
  includeOriginalMessage?: boolean;
  additionalMessage?: string;
  scheduledUnixTime?: number | null;
}

interface RepostResponse {
  newPostId: number;
  originalPostId: number;
  publishedChildren: Array<{
    childId: number;
    platform: string;
    postUrl: string;
    success: boolean;
    errorMessage?: string;
  }>;
  message: string;
  createdAt: string;
  isScheduled: boolean;
}

// Get preview before reposting
async function getRepostPreview(postId: number, token: string) {
  const response = await fetch(`/api/posts/${postId}/repost-preview`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (!response.ok) {
    throw new Error('Failed to fetch preview');
  }
  
  return await response.json();
}

// Repost to new platforms
async function repostPost(args: RepostArgs, token: string): Promise<RepostResponse> {
  const response = await fetch('/api/posts/repost', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(args)
  });
  
  if (!response.ok) {
    throw new Error('Failed to repost');
  }
  
  return await response.json();
}

// Usage example
const handleRepost = async () => {
  try {
    // First, get preview
    const preview = await getRepostPreview(123, userToken);
    console.log('Preview:', preview);
    
    // Then repost
    const result = await repostPost({
      originalPostId: 123,
      pagesIds: [
        { pageId: 'fb_page_123', platform: 'facebook' },
        { pageId: 'ig_page_456', platform: 'instagram' }
      ],
      includeOriginalMessage: true,
      additionalMessage: 'Sharing this again!'
    }, userToken);
    
    console.log('Reposted successfully:', result);
    
    // Check for any failures
    const failures = result.publishedChildren.filter(c => !c.success);
    if (failures.length > 0) {
      console.warn('Some platforms failed:', failures);
    }
  } catch (error) {
    console.error('Repost failed:', error);
  }
};
```

### Angular Example

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RepostService {
  private apiUrl = '/api/posts';

  constructor(private http: HttpClient) {}

  getRepostPreview(postId: number, token: string): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.get(`${this.apiUrl}/${postId}/repost-preview`, { headers });
  }

  repost(args: RepostArgs, token: string): Observable<RepostResponse> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    
    return this.http.post<RepostResponse>(`${this.apiUrl}/repost`, args, { headers });
  }
}

// Component usage
export class PostRepostComponent {
  constructor(private repostService: RepostService) {}

  onRepost(postId: number, selectedPages: any[]) {
    const token = localStorage.getItem('authToken');
    
    this.repostService.repost({
      originalPostId: postId,
      pagesIds: selectedPages,
      includeOriginalMessage: true,
      additionalMessage: 'Check this out again!'
    }, token!).subscribe({
      next: (response) => {
        console.log('Success:', response);
        alert(response.message);
      },
      error: (error) => {
        console.error('Error:', error);
        alert('Failed to repost');
      }
    });
  }
}
```

## UI/UX Recommendations

### 1. Repost Button
Add a "Repost" or "Share Again" button to each post card:
```html
<button onclick="openRepostModal(postId)">
  <i class="share-icon"></i> Repost
</button>
```

### 2. Repost Modal/Dialog
The repost modal should include:

- **Preview Section**: Show original post content and media count
- **Platform Selection**: Checkboxes or multi-select for platforms
- **Message Options**:
  - Toggle: "Include original message"
  - Textarea: "Add additional message"
- **Schedule Option**: Date/time picker (optional)
- **Submit Button**: "Repost Now" or "Schedule Repost"

```html
<div class="repost-modal">
  <h3>Repost to Other Platforms</h3>
  
  <div class="preview">
    <p><strong>Original Message:</strong> {{ originalMessage }}</p>
    <p>{{ mediaCount }} media files</p>
  </div>
  
  <div class="platform-selection">
    <h4>Select Platforms:</h4>
    <label><input type="checkbox" value="facebook"> Facebook Page 1</label>
    <label><input type="checkbox" value="instagram"> Instagram Account 1</label>
  </div>
  
  <div class="message-options">
    <label>
      <input type="checkbox" checked> Include original message
    </label>
    <textarea placeholder="Add additional message (optional)"></textarea>
  </div>
  
  <div class="schedule-option">
    <label>
      <input type="checkbox"> Schedule for later
    </label>
    <input type="datetime-local" />
  </div>
  
  <button onclick="submitRepost()">Repost Now</button>
</div>
```

### 3. Success/Error Handling
Show detailed feedback:
```typescript
function handleRepostResponse(response: RepostResponse) {
  const successes = response.publishedChildren.filter(c => c.success);
  const failures = response.publishedChildren.filter(c => !c.success);
  
  if (successes.length > 0) {
    showSuccessMessage(`Posted to ${successes.length} platform(s)`);
  }
  
  if (failures.length > 0) {
    failures.forEach(failure => {
      showErrorMessage(`Failed to post to ${failure.platform}: ${failure.errorMessage}`);
    });
  }
}
```

## Media Handling Details

### How Media is Processed
1. When you repost, the backend:
   - Downloads media from the original platform URL (Facebook/Instagram CDN)
   - Uploads each file to Supabase storage bucket `post-media`
   - Generates a unique filename (UUID + extension)
   - Returns a public Supabase URL
   - Uses this Supabase URL when posting to new platforms

2. **Benefits**:
   - Media persists even if original platform deletes it
   - Faster access from Supabase CDN
   - You own the media files
   - No external dependencies on platform CDNs

3. **Fallback**: If Supabase upload fails, the system falls back to using the original platform URL

## Error Codes

| Status Code | Meaning |
|-------------|---------|
| 200 | Success |
| 401 | Unauthorized - invalid or missing token |
| 404 | Post not found or doesn't belong to seller |
| 400 | Invalid request (e.g., missing required fields) |
| 500 | Server error |

## Testing Checklist

- [ ] Preview shows correct post data
- [ ] Can select multiple platforms
- [ ] Original message toggle works
- [ ] Additional message appends correctly
- [ ] Immediate repost works
- [ ] Scheduled repost works
- [ ] Error handling for failed platforms
- [ ] Success message shows correct count
- [ ] Media loads from Supabase URLs

## Notes

- Reposting creates a new `Post` record with type "repost" or "scheduled_repost"
- Media files are permanently stored in Supabase (consider cleanup strategy)
- Scheduled posts use Unix timestamp in milliseconds
- Each platform failure is logged but doesn't stop others

## Support

If you encounter issues:
1. Check browser console for errors
2. Verify Supabase bucket is public and accessible
3. Ensure environment variables are set correctly
4. Check backend logs for detailed error messages

