import {ManagedPageCPL} from "../settings/account-details/account-details.component";

export interface MessageBox {
  idMm: number;
  id_pc: string;
  idPc: string;
  name: string;
  link_to_profile:string;
  platform: string;
  id_sp:number;
  mediaUrl: string;
  identifier_on_platform: string;
}
export interface Message {
  id:number;
  message:string;
  fromPlatform:boolean;
  idMm:number;
  createdAt:Date;
}
export interface MessageBody {
  idMm: number;
  message: string;
  platform: string;
}

interface Profiles {
  page: ManagedPageCPL[];
}

export interface InboxDisplay {
  page: ManagedPageCPL;
  mothers: MessageBox[];
}
