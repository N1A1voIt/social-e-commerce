import {ManagedPageCPL} from "../settings/account-details/account-details.component";

interface MessageBox {
  id_pc: string;
  name: string;
  link_to_profile:string;
  d_platform: string;
  id_sp:number;
  media_url: string;
  identifier_on_platform: string;
}
interface Profiles {
  page: ManagedPageCPL[];
}

export interface InboxDisplay {
  page: ManagedPageCPL;
  messages: MessageBox[];
}
