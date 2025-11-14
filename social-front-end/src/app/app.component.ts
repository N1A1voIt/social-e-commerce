import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {Router, RouterOutlet} from '@angular/router';
import {AccountDetailsComponent} from "./main/settings/account-details/account-details.component";
import {provideIcons} from "@ng-icons/core";
import {
  heroAdjustmentsHorizontal,
  heroArrowUturnRight, heroBellAlert,
  heroChartBarSquare, heroChatBubbleOvalLeftEllipsis,
  heroDocument, heroHeart, heroHome,
  heroPencilSquare,
  heroPlusCircle, heroShoppingBag, heroShoppingCart, heroUserCircle,heroCube,heroNewspaper,heroClipboardDocumentList,heroClock,heroArrowRightOnRectangle,
  heroXMark
} from "@ng-icons/heroicons/outline";
import {MenuComponent} from "./shared/menu/menu.component";
import {heroDocumentSolid} from "@ng-icons/heroicons/solid";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, AccountDetailsComponent, MenuComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  providers: [provideIcons({heroPencilSquare,heroPlusCircle,heroXMark,heroDocumentSolid,heroChartBarSquare,heroChatBubbleOvalLeftEllipsis,heroArrowUturnRight,heroClipboardDocumentList,heroClock,heroArrowRightOnRectangle,
  heroHome,heroHeart,heroBellAlert,heroUserCircle,heroShoppingBag,heroShoppingCart,heroAdjustmentsHorizontal,heroCube,heroNewspaper,})]
})
export class AppComponent implements OnInit {
  title = 'social-front-end';
  showMenu:boolean = false;
  constructor(public router:Router,) {
  }
  ngOnInit() {

  }
}
