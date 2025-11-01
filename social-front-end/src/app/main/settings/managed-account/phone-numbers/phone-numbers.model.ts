export interface PhoneNumber {
  id?: number;
  phoneNumber: string;
  associatedName: string;
  idPm: number;
  idMp: number;
  paymentName?: string;
  idSpn?: number;
}

export interface PhoneNumberPayload {
  idSpn: number;
  idPm: number;
  idMp: number;
}

