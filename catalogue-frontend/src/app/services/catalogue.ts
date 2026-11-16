
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, timeout } from 'rxjs';

const REQUEST_TIMEOUT_MS = 30_000;
const CONVERSATION_STORAGE_KEY = 'catalogue-conversation-id';

export interface Produit {
  id: number;
  nom: string;
  prix: number;
  quantite: number;
  categorie: {
    id: number;
    nom: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class CatalogueService {

  private http = inject(HttpClient);
  private readonly conversationId = this.getConversationId();

  // Envoyer une question à Mistral AI
  demander(message: string): Observable<string> {
    const params = new HttpParams()
      .set('message', message)
      .set('conversationId', this.conversationId);

    return this.http.get('/agent', {
      params,
      responseType: 'text'
    }).pipe(timeout(REQUEST_TIMEOUT_MS));
  }

  // Récupérer tous les produits
  getProduits(): Observable<Produit[]> {
    return this.http.get<Produit[]>('/produit/all').pipe(timeout(REQUEST_TIMEOUT_MS));
  }

  private getConversationId(): string {
    if (typeof sessionStorage === 'undefined') {
      return crypto.randomUUID();
    }

    const existingId = sessionStorage.getItem(CONVERSATION_STORAGE_KEY);
    if (existingId) {
      return existingId;
    }

    const newId = crypto.randomUUID();
    sessionStorage.setItem(CONVERSATION_STORAGE_KEY, newId);
    return newId;
  }
}
