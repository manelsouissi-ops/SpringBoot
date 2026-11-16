
import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { finalize, TimeoutError } from 'rxjs';
import { CatalogueService } from './services/catalogue';

interface Message {
  type: 'user' | 'bot';
  texte: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  private catalogueService = inject(CatalogueService);

  question = '';
  chargement = signal(false);

  messages = signal<Message[]>([
    {
      type: 'bot',
      texte: 'Bonjour ! Je suis votre assistant boutique. Comment puis-je vous aider ?'
    }
  ]);

  envoyer(texte?: string) {
    const message = (texte ?? this.question).trim();

    if (!message || this.chargement()) return;

    this.messages.update(messages => [...messages, { type: 'user', texte: message }]);
    this.question = '';
    this.chargement.set(true);

    this.catalogueService.demander(message).pipe(
      finalize(() => this.chargement.set(false))
    ).subscribe({
      next: (reponse) => {
        this.messages.update(messages => [...messages, { type: 'bot', texte: reponse }]);
      },
      error: (error: unknown) => {
        this.messages.update(messages => [
          ...messages,
          { type: 'bot', texte: this.messageErreur(error) }
        ]);
      }
    });
  }

  afficherProduits() {
    if (this.chargement()) return;

    const message = 'Quels sont vos produits ?';
    this.messages.update(messages => [...messages, { type: 'user', texte: message }]);
    this.chargement.set(true);

    this.catalogueService.getProduits().pipe(
      finalize(() => this.chargement.set(false))
    ).subscribe({
      next: (produits) => {
        const texte = produits.length === 0
          ? 'Aucun produit disponible.'
          : produits.map(produit =>
            `${produit.nom} - ${produit.prix} DT - quantité : ${produit.quantite} - catégorie : ${produit.categorie.nom}`
          ).join('\n');
        this.messages.update(messages => [...messages, { type: 'bot', texte }]);
      },
      error: (error: unknown) => {
        this.messages.update(messages => [
          ...messages,
          { type: 'bot', texte: this.messageErreur(error) }
        ]);
      }
    });
  }

  private messageErreur(error: unknown): string {
    if (error instanceof TimeoutError) {
      return 'Le délai de réponse est dépassé. Veuillez réessayer.';
    }

    if (error instanceof HttpErrorResponse) {
      switch (error.status) {
        case 0:
          return 'Le backend Spring Boot est inaccessible.';
        case 404:
          return 'L’endpoint demandé est introuvable.';
        case 429:
          return 'La limite de requêtes Mistral est atteinte. Veuillez réessayer plus tard.';
        case 500:
          return 'Le backend a rencontré une erreur pendant le traitement.';
        default:
          return 'La requête n’a pas pu aboutir. Veuillez réessayer.';
      }
    }

    return 'La requête n’a pas pu aboutir. Veuillez réessayer.';
  }
}
