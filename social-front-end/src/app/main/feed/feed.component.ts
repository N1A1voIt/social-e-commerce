import {Component, OnInit} from '@angular/core';
import {PostHeaderComponent} from "./post-header/post-header.component";
import {PostBodyComponent} from "./post-body/post-body.component";
import {LikeCommentComponent} from "./like-comment/like-comment.component";
import {NgForOf, NgIf, TitleCasePipe} from "@angular/common";

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
export class FeedComponent implements OnInit{
  posts: Post[] = [];

  ngOnInit() {
    // Your backend response data
    this.posts = [
      {
        "id": 32,
        "message": "",
        "platform": "facebook",
        "username": "Pejy",
        "medias": null,
        "childPosts": [
          {
            "id": 87,
            "message": null,
            "platform": "facebook",
            "username": "Pejy",
            "medias": [
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523951476_122095802708962486_4305956395115047822_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=102&ccb=1-7&_nc_sid=6ee11a&_nc_eui2=AeE50e9fbDucH-I337NIJtTKDSyAOHI73DQNLIA4cjvcNFRn39qDQPk0QMgl7cVc-nCmiSG_Dc3ofxhBZ9oxXsE6&_nc_ohc=jyH-KxKrHSEQ7kNvwEDYoQs&_nc_oc=AdkL0l7e6YzuE-iF8_q2XNGqjR8ilDi6191bX2-nJsfaDtElN3Bh8UUw0ygP12ZZCY4&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=eRd9xeKGd3QoN7U7bKDjYQ&oh=00_AfSnR0YjbrOn5bcfsTiMFAuH38dP3SsR59LuxwwWtJp2hw&oe=688D336F",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523951476_122095802708962486_4305956395115047822_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=102&ccb=1-7&_nc_sid=6ee11a&_nc_eui2=AeE50e9fbDucH-I337NIJtTKDSyAOHI73DQNLIA4cjvcNFRn39qDQPk0QMgl7cVc-nCmiSG_Dc3ofxhBZ9oxXsE6&_nc_ohc=jyH-KxKrHSEQ7kNvwEDYoQs&_nc_oc=AdkL0l7e6YzuE-iF8_q2XNGqjR8ilDi6191bX2-nJsfaDtElN3Bh8UUw0ygP12ZZCY4&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=eRd9xeKGd3QoN7U7bKDjYQ&oh=00_AfSnR0YjbrOn5bcfsTiMFAuH38dP3SsR59LuxwwWtJp2hw&oe=688D336F",
                "idChild": null
              }
            ],
            "childPosts": null
          }
        ]
      },
      {
        "id": 33,
        "message": "Check schema",
        "platform": "facebook",
        "username": "Pejy",
        "medias": null,
        "childPosts": [
          {
            "id": 89,
            "message": null,
            "platform": "facebook",
            "username": "Pejy",
            "medias": [
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/524593674_122114957096932141_4503032692274847380_n.jpg?_nc_cat=106&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeHgOtJVcRVf9vNbL-I_9UV1IzWG_9nCjLwjNYb_2cKMvEhhUcPgXEWfJM7jh7C05Whz_--75EkcoxNu14uR-Mha&_nc_ohc=EvyRNaJOA0oQ7kNvwH5zJfo&_nc_oc=AdlXec5HRvuQlvgNq25gfXJ-jaTuFPmhhIQu0_xKb-vYl0lPZqCdB2FKwzDFoU9rCsc&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfTfVQkv13dBf3QImn4X-jK71YtRwy0KTK8DygO4oNujjw&oe=688D4FF9",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523094143_122114957210932141_4088153394920142544_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=103&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeEqfJlz6w8CNNkaiQkeKo5W4GMLpxuf32XgYwunG5_fZYKvUX64CdNknXiipxUzvBTcSBgPkNh1Yza9rKB24aZx&_nc_ohc=XfJT-JDHsokQ7kNvwEHMChD&_nc_oc=Adl7YA2jic_0A_KuP5tVfkmopYLHXIcpYksPCez_-lpclTl0BFtEST8vIN1GeDQDLTA&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfQNXAZRYDpRYo42qQN7n6UUcjSbjYuf0c9IsYufmIs6Eg&oe=688D32BE",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523146264_122114957204932141_6003498343021502635_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=102&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeHX2ThUV-ClD8tjPqomKA0Yj6XWZXI-TaiPpdZlcj5NqAUJSfU_GgE-hkCrcLQAkFrD-6WGQ0j4KDt6vHpUP4dR&_nc_ohc=QZd-JftkpXAQ7kNvwHUxMOT&_nc_oc=Adnh3cWWPSF0GoutRy4ZY7xokTG9REuq3xTnXElgEBnCzRD8hk1kHtgD8RNTghOqaUg&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfR27CfiuHKNpuRz4RZ_lz5o3DVJHJjqEoJO3eZVpxyvXA&oe=688D2BDD",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/524593674_122114957096932141_4503032692274847380_n.jpg?_nc_cat=106&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeHgOtJVcRVf9vNbL-I_9UV1IzWG_9nCjLwjNYb_2cKMvEhhUcPgXEWfJM7jh7C05Whz_--75EkcoxNu14uR-Mha&_nc_ohc=EvyRNaJOA0oQ7kNvwH5zJfo&_nc_oc=AdlXec5HRvuQlvgNq25gfXJ-jaTuFPmhhIQu0_xKb-vYl0lPZqCdB2FKwzDFoU9rCsc&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfTfVQkv13dBf3QImn4X-jK71YtRwy0KTK8DygO4oNujjw&oe=688D4FF9",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523094143_122114957210932141_4088153394920142544_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=103&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeEqfJlz6w8CNNkaiQkeKo5W4GMLpxuf32XgYwunG5_fZYKvUX64CdNknXiipxUzvBTcSBgPkNh1Yza9rKB24aZx&_nc_ohc=XfJT-JDHsokQ7kNvwEHMChD&_nc_oc=Adl7YA2jic_0A_KuP5tVfkmopYLHXIcpYksPCez_-lpclTl0BFtEST8vIN1GeDQDLTA&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfQNXAZRYDpRYo42qQN7n6UUcjSbjYuf0c9IsYufmIs6Eg&oe=688D32BE",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523146264_122114957204932141_6003498343021502635_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=102&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeHX2ThUV-ClD8tjPqomKA0Yj6XWZXI-TaiPpdZlcj5NqAUJSfU_GgE-hkCrcLQAkFrD-6WGQ0j4KDt6vHpUP4dR&_nc_ohc=QZd-JftkpXAQ7kNvwHUxMOT&_nc_oc=Adnh3cWWPSF0GoutRy4ZY7xokTG9REuq3xTnXElgEBnCzRD8hk1kHtgD8RNTghOqaUg&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfR27CfiuHKNpuRz4RZ_lz5o3DVJHJjqEoJO3eZVpxyvXA&oe=688D2BDD",
                "idChild": null
              }
            ],
            "childPosts": null
          }
        ]
      },
      {
        "id": 34,
        "message": "Test 2",
        "platform": "facebook",
        "username": "Pejy",
        "medias": null,
        "childPosts": [
          {
            "id": 93,
            "message": null,
            "platform": "facebook",
            "username": "Pejy",
            "medias": [
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/514283989_122097453008932141_507726957567822948_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=108&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeHl1Kmh1etlQLeXjB5iwpp7klmvHx3u-0WSWa8fHe77RdTTI-lX_jVVGqfpnP__yIUS9k50V7cyyh1GdxAhoAJF&_nc_ohc=jGYntNg8iKcQ7kNvwGbh2uW&_nc_oc=AdkvhWcYQKYqkh_4wpezMLfyfBrFWdPrBac3RTJe0PFHJb2KhcW3ty5fZCDIS7ckAzU&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfSYsLs-2OWknTz6CymmSl9mxhFudVColhZYaimI2a-KPg&oe=688D22C4",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/515104056_122097453248932141_9146316789992363867_n.jpg?_nc_cat=105&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeGEI7T_imj4FEGKbf6b1l7uWYI0FrzAUzdZgjQWvMBTN_qgAiloDa4QwCzCf6mEtejnHfZtTiLFassKAOh9JhaV&_nc_ohc=2OXps6FlOA0Q7kNvwHcMfhx&_nc_oc=Adnx3ikKU27YG1xRywpvKSlAuiM3j1cUrV-Jk2yP6DpT-gaO9MNAUJPhHnyNmRdaFZg&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfRuzhsiDwul119NczWVTSRd8G2Zf2a1IG3kkeM4GwRyHQ&oe=688D20C4",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/514191692_122097453296932141_7770825472985379872_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=106&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeFLEXdWOk6RqXORRuiViBreMrVl600dcq0ytWXrTR1yrWJYjFbcrbcI7IpqvmxQ3tByFb9eERMZVu-t5jUSrChO&_nc_ohc=oNWzSGnVMT4Q7kNvwEa5hcA&_nc_oc=AdnhsNqpCZsln8e259EWQwGtodStvR9xm7qPhxnW4FYlC0_Zr6yHSgoR1kC3m92vuXE&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfSrYVIiKuok38vlOTWNPpGpzbTSA42iaf_ZJsJVGyoUFw&oe=688D2466",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/514368772_122097453242932141_3505335620424002402_n.jpg?_nc_cat=111&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeE-IR1y132gVoLWiF4TQPRUwUC5ap3lq7TBQLlqneWrtO3HyIQ-nipMZrAQithFebd-zlBsJGXnN-vh_k8mU7Z1&_nc_ohc=rVPCitFdkQYQ7kNvwGwcLIF&_nc_oc=Adme8hb0voBcFhM0OEIQSmB48Hh6bGSfEhmLOnS5vp21r-DeIJ9vOX-BMI3eSzYxSkY&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfRYP6v5pGroxD3Vzd_KShrjZgSG-SanvNePDC0OxGRbuA&oe=688D4ED1",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/514283989_122097453008932141_507726957567822948_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=108&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeHl1Kmh1etlQLeXjB5iwpp7klmvHx3u-0WSWa8fHe77RdTTI-lX_jVVGqfpnP__yIUS9k50V7cyyh1GdxAhoAJF&_nc_ohc=jGYntNg8iKcQ7kNvwGbh2uW&_nc_oc=AdkvhWcYQKYqkh_4wpezMLfyfBrFWdPrBac3RTJe0PFHJb2KhcW3ty5fZCDIS7ckAzU&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfSYsLs-2OWknTz6CymmSl9mxhFudVColhZYaimI2a-KPg&oe=688D22C4",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/515104056_122097453248932141_9146316789992363867_n.jpg?_nc_cat=105&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeGEI7T_imj4FEGKbf6b1l7uWYI0FrzAUzdZgjQWvMBTN_qgAiloDa4QwCzCf6mEtejnHfZtTiLFassKAOh9JhaV&_nc_ohc=2OXps6FlOA0Q7kNvwHcMfhx&_nc_oc=Adnx3ikKU27YG1xRywpvKSlAuiM3j1cUrV-Jk2yP6DpT-gaO9MNAUJPhHnyNmRdaFZg&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfRuzhsiDwul119NczWVTSRd8G2Zf2a1IG3kkeM4GwRyHQ&oe=688D20C4",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/514191692_122097453296932141_7770825472985379872_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=106&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeFLEXdWOk6RqXORRuiViBreMrVl600dcq0ytWXrTR1yrWJYjFbcrbcI7IpqvmxQ3tByFb9eERMZVu-t5jUSrChO&_nc_ohc=oNWzSGnVMT4Q7kNvwEa5hcA&_nc_oc=AdnhsNqpCZsln8e259EWQwGtodStvR9xm7qPhxnW4FYlC0_Zr6yHSgoR1kC3m92vuXE&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfSrYVIiKuok38vlOTWNPpGpzbTSA42iaf_ZJsJVGyoUFw&oe=688D2466",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/514368772_122097453242932141_3505335620424002402_n.jpg?_nc_cat=111&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeE-IR1y132gVoLWiF4TQPRUwUC5ap3lq7TBQLlqneWrtO3HyIQ-nipMZrAQithFebd-zlBsJGXnN-vh_k8mU7Z1&_nc_ohc=rVPCitFdkQYQ7kNvwGwcLIF&_nc_oc=Adme8hb0voBcFhM0OEIQSmB48Hh6bGSfEhmLOnS5vp21r-DeIJ9vOX-BMI3eSzYxSkY&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfRYP6v5pGroxD3Vzd_KShrjZgSG-SanvNePDC0OxGRbuA&oe=688D4ED1",
                "idChild": null
              }
            ],
            "childPosts": null
          }
        ]
      },
      {
        "id": 35,
        "message": "Test Graph API",
        "platform": "facebook",
        "username": "Pejy",
        "medias": null,
        "childPosts": [
          {
            "id": 98,
            "message": null,
            "platform": "facebook",
            "username": "Pejy",
            "medias": [
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/514338184_122097415406932141_5467583372442251496_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=107&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeFXF9zwY6TQt21TaowVu3tE5MDn5rfPxhHkwOfmt8_GEX9nLm0_fdT-upAfuGFaX_FDIr0npUxDXNW4rUed5Gjk&_nc_ohc=G7xOHxfq764Q7kNvwFolTQX&_nc_oc=AdnLmuJZ1mqd-k-q0gu3MKRa2vVFZqSDWxApukLyYJLxXDF5KJz67mnNPtRlmLoBa3A&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfQKpJvl3boiu-vsu4DLqFdLSFDHz5r6-wVoknaV9OE1TA&oe=688D3263",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/514338184_122097415406932141_5467583372442251496_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=107&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeFXF9zwY6TQt21TaowVu3tE5MDn5rfPxhHkwOfmt8_GEX9nLm0_fdT-upAfuGFaX_FDIr0npUxDXNW4rUed5Gjk&_nc_ohc=G7xOHxfq764Q7kNvwFolTQX&_nc_oc=AdnLmuJZ1mqd-k-q0gu3MKRa2vVFZqSDWxApukLyYJLxXDF5KJz67mnNPtRlmLoBa3A&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfQKpJvl3boiu-vsu4DLqFdLSFDHz5r6-wVoknaV9OE1TA&oe=688D3263",
                "idChild": null
              }
            ],
            "childPosts": null
          }
        ]
      },
      {
        "id": 36,
        "message": "",
        "platform": "facebook",
        "username": "Pejy",
        "medias": null,
        "childPosts": [
          {
            "id": 100,
            "message": null,
            "platform": "facebook",
            "username": "Pejy",
            "medias": [
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/513913904_122097414722932141_2992619681762329114_n.jpg?_nc_cat=101&ccb=1-7&_nc_sid=6ee11a&_nc_eui2=AeEsHsitPvn2v-pOPQHLqD_r5OfkYAtzl5Tk5-RgC3OXlCoHmonK9snKwNos0TSm4ZbhUMwPj6mpUs-OitohR_6_&_nc_ohc=1sSiP1FT1UwQ7kNvwFIYhIZ&_nc_oc=AdnsqWmqdzg__BTp8ycmbmS2JPwjVczTRj_mkdvsBv1LURJStgJ9EMmLFqjlXa12LzI&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfQrG7Xe-W3e3DLtNbAz_mJRuS8F-AnZyQnnoH5UyQjt2w&oe=688D23CF",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/513913904_122097414722932141_2992619681762329114_n.jpg?_nc_cat=101&ccb=1-7&_nc_sid=6ee11a&_nc_eui2=AeEsHsitPvn2v-pOPQHLqD_r5OfkYAtzl5Tk5-RgC3OXlCoHmonK9snKwNos0TSm4ZbhUMwPj6mpUs-OitohR_6_&_nc_ohc=1sSiP1FT1UwQ7kNvwFIYhIZ&_nc_oc=AdnsqWmqdzg__BTp8ycmbmS2JPwjVczTRj_mkdvsBv1LURJStgJ9EMmLFqjlXa12LzI&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=scyGj1BgozyEWbtVzKlfLQ&oh=00_AfQrG7Xe-W3e3DLtNbAz_mJRuS8F-AnZyQnnoH5UyQjt2w&oe=688D23CF",
                "idChild": null
              }
            ],
            "childPosts": null
          }
        ]
      },
      {
        "id": 37,
        "message": "Ity dia multiple",
        "platform": "instagram",
        "username": "busin_ess_123",
        "medias": [
          {
            "id": null,
            "mediaUrl": "https://scontent.cdninstagram.com/v/t51.82787-15/523473184_17844131958541128_7861740099854992222_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=103&ccb=1-7&_nc_sid=18de74&_nc_eui2=AeFWZAvfK5vAZULtZk3_a3F1qi8Mh8uuZliqLwyHy65mWI0YSYlvC_cXiNZHfD-SOahnQwGXa4fR-iFnWwRI2_Ba&_nc_ohc=27e7rfTe6fMQ7kNvwG5wapx&_nc_oc=Adnwxe0g6Nb__XJ0UlCMK_8h1690u_x2ydAEGSC3tC3aIjiFZmZkUbzO7V9eyKl-Tpk&_nc_zt=23&_nc_ht=scontent.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=j6Utss25lbPBMjpaoGXMCw&oh=00_AfQt_rtvfiOX7gVSTNO0uWqr9KuVlk3GhLVGE1uk_heeow&oe=688D4DE5",
            "idChild": 102
          },
          {
            "id": null,
            "mediaUrl": "https://scontent.cdninstagram.com/v/t51.82787-15/522702985_17844131955541128_7435726395178942533_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=100&ccb=1-7&_nc_sid=18de74&_nc_eui2=AeG1zWFse043u2a-mpbK0bS2m0OSeU9-vC2bQ5J5T368LUYngj2mwePyTAgLiMxs4BH1lAuHR-XpQ9ZdYorwItwc&_nc_ohc=oSdcyGMLQEYQ7kNvwFNCrZR&_nc_oc=AdnwAsRpliU00stcJ8SDZkiaezSqQuRjkRdviad5VRLRm9Goqep9lhJIrmCaFsgZgOk&_nc_zt=23&_nc_ht=scontent.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=j6Utss25lbPBMjpaoGXMCw&oh=00_AfRGHHP6c0AqNOgm3J4unVAjfgPxPBWNf-dUTlzFKMeaYA&oe=688D4F96",
            "idChild": 102
          },
          {
            "id": null,
            "mediaUrl": "https://scontent.cdninstagram.com/v/t51.82787-15/523217590_17844131973541128_1551903219632748630_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=107&ccb=1-7&_nc_sid=18de74&_nc_eui2=AeEo9yEdFxBZ7NQiU5YesZnMxrbTr-OBB6zGttOv44EHrLzw1xnAXleAsymQC3kmRJQ_p3bmxINxVqaCayS4-nb-&_nc_ohc=7vgco9O64MkQ7kNvwFstyCX&_nc_oc=AdmHkjANo-L7aXPaJaRs00K6EMwq6c4k8LA7WRTK8wVikvPhmzvwyuVrPUVPtyyNgdQ&_nc_zt=23&_nc_ht=scontent.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=j6Utss25lbPBMjpaoGXMCw&oh=00_AfT_6TIwSWO8cYE0_eo0xciymJlrEqolK_01ZGI7VnJ31A&oe=688D39B9",
            "idChild": 102
          },
          {
            "id": null,
            "mediaUrl": "https://scontent.cdninstagram.com/v/t51.82787-15/524198791_17844131964541128_6144843531135810076_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=103&ccb=1-7&_nc_sid=18de74&_nc_eui2=AeGcxZIz53aFbRzKQ5Sk4V1z3CcPoTkT-7HcJw-hORP7sfyDxPpEUVv_pfHdKuQv4mK0ufrMSwi5rQQHnV04BBoi&_nc_ohc=bSIrFnuIaA8Q7kNvwF1D4co&_nc_oc=Adlw00z1R_Sy0uGr7ArMdKfBwnnf8urdmR1W2KtPrs5ET-dn5PJQfQsmWY44gwFjcco&_nc_zt=23&_nc_ht=scontent.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=j6Utss25lbPBMjpaoGXMCw&oh=00_AfTdvtkgrbM0H2_Pmpbe9J2cFLmYmg2yrf2F1Tr_Fgl7Kw&oe=688D4256",
            "idChild": 102
          }
        ],
        "childPosts": null
      },
      {
        "id": 38,
        "message": "Caption",
        "platform": "instagram",
        "username": "busin_ess_123",
        "medias": [
          {
            "id": null,
            "mediaUrl": "https://scontent.cdninstagram.com/v/t51.82787-15/525234042_17844131883541128_4960162907688548777_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=104&ccb=1-7&_nc_sid=18de74&_nc_eui2=AeHZEthH5ME01SkqDIbEu3efbc9g9bCAwXNtz2D1sIDBc1yrwahXELjocw5GnX2VSQcKV9W14x-1zfNDnIVzsQOz&_nc_ohc=l7kTL2JavGQQ7kNvwH-BbiN&_nc_oc=AdmCXa9juhuurpXlEOSjAMIFL7z_5GebTtvQsq1fhqFFe1m7gxRTrTjXO_hxhJiJC80&_nc_zt=23&_nc_ht=scontent.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=j6Utss25lbPBMjpaoGXMCw&oh=00_AfSVQDW_Midp9LZ9wWiSw4LKOzKTF3Kh5RmOMFptZguf6w&oe=688D1FF4",
            "idChild": 103
          }
        ],
        "childPosts": null
      },
      {
        "id": 39,
        "message": "",
        "platform": "instagram",
        "username": "busin_ess_123",
        "medias": [
          {
            "id": null,
            "mediaUrl": "https://scontent.cdninstagram.com/v/t51.82787-15/523881965_17844118293541128_6138618720886930231_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=105&ccb=1-7&_nc_sid=18de74&_nc_eui2=AeEj2J15JoCBwmnkrhsiewGrxEKXiVleKTPEQpeJWV4pM6Kh6gzxVK2Ua8gHvWV5hOdTV2rAuqAt7y20yUJXGfXa&_nc_ohc=2c-_rqekZlIQ7kNvwGRM-5K&_nc_oc=Adl3NXHtXRCzv4DKm-lbW9RMQvz4swVHbLsN-iGc1uGUKgVuuay6p7tu3TuMnwSTntc&_nc_zt=23&_nc_ht=scontent.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=j6Utss25lbPBMjpaoGXMCw&oh=00_AfSWzTjMVeUVGpCRGa-qggaz0IngIH15XlLLcYC2XV80sQ&oe=688D2705",
            "idChild": 104
          }
        ],
        "childPosts": null
      },
      {
        "id": 31,
        "message": "Publication",
        "platform": "facebook",
        "username": "Pejy",
        "medias": null,
        "childPosts": [
          {
            "id": 84,
            "message": null,
            "platform": "facebook",
            "username": "Pejy",
            "medias": [
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523703713_122095803398962486_3236942814440566761_n.jpg?_nc_cat=102&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeFGJxE6M6ytfnOCXY_DzukxoPkjLnwzTFqg-SMufDNMWpUrECyvCrAYXFSnGJLFWQPi8y4R3KZcEbLx6lwKNaHv&_nc_ohc=LTlclTqvL8gQ7kNvwE2vogw&_nc_oc=Adlog49CJPdEJMUC5yzcd4GTA8q4Iq5hOGZXQOftOaMzMXvR_F7x7zYgAyGSPfOUYzc&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=eRd9xeKGd3QoN7U7bKDjYQ&oh=00_AfTDhfrvqXMXV2HLg-fdxhEeP9082fDhIQueFf6Gtgp9Xg&oe=688D3906",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523978053_122095803368962486_5439436496908595273_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=106&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeE-h_UYA00ZRf6UY61FMfhBoQRWsY6syuShBFaxjqzK5EoSv_pAnzs7iprKPKbPGwnntRElH6FZRtxiYKdHHo7B&_nc_ohc=sF0hpkRDTQAQ7kNvwENO0pT&_nc_oc=AdlhHmV_n8ajyMvBdYcv3Hfei4cR87yQmOdbu6XsfeFKVQSFFctxgk_5axrjfCLm2CU&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=eRd9xeKGd3QoN7U7bKDjYQ&oh=00_AfTv-2Kq0GbJydlTvfWpH60MbYip1wqfbpuHpfkRldP1kw&oe=688D3559",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523703713_122095803398962486_3236942814440566761_n.jpg?_nc_cat=102&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeFGJxE6M6ytfnOCXY_DzukxoPkjLnwzTFqg-SMufDNMWpUrECyvCrAYXFSnGJLFWQPi8y4R3KZcEbLx6lwKNaHv&_nc_ohc=LTlclTqvL8gQ7kNvwE2vogw&_nc_oc=Adlog49CJPdEJMUC5yzcd4GTA8q4Iq5hOGZXQOftOaMzMXvR_F7x7zYgAyGSPfOUYzc&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=eRd9xeKGd3QoN7U7bKDjYQ&oh=00_AfTDhfrvqXMXV2HLg-fdxhEeP9082fDhIQueFf6Gtgp9Xg&oe=688D3906",
                "idChild": null
              },
              {
                "id": null,
                "mediaUrl": "https://scontent.ftnr2-2.fna.fbcdn.net/v/t39.30808-6/523978053_122095803368962486_5439436496908595273_n.jpg?stp=dst-jpg_p720x720_tt6&_nc_cat=106&ccb=1-7&_nc_sid=833d8c&_nc_eui2=AeE-h_UYA00ZRf6UY61FMfhBoQRWsY6syuShBFaxjqzK5EoSv_pAnzs7iprKPKbPGwnntRElH6FZRtxiYKdHHo7B&_nc_ohc=sF0hpkRDTQAQ7kNvwENO0pT&_nc_oc=AdlhHmV_n8ajyMvBdYcv3Hfei4cR87yQmOdbu6XsfeFKVQSFFctxgk_5axrjfCLm2CU&_nc_zt=23&_nc_ht=scontent.ftnr2-2.fna&edm=AKIiGfEEAAAA&_nc_gid=eRd9xeKGd3QoN7U7bKDjYQ&oh=00_AfTv-2Kq0GbJydlTvfWpH60MbYip1wqfbpuHpfkRldP1kw&oe=688D3559",
                "idChild": null
              }
            ],
            "childPosts": null
          }
        ]
      }
    ];
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
