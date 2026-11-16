import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { App } from './app';
import { CatalogueService } from './services/catalogue';

class CatalogueServiceStub {
  demanderCalls = 0;
  getProduitsCalls = 0;

  demander() {
    this.demanderCalls++;
    return of('Réponse IA');
  }

  getProduits() {
    this.getProduitsCalls++;
    return of([
      {
        id: 1,
        nom: 'PC',
        prix: 1000,
        quantite: 10,
        categorie: { id: 1, nom: 'informatique' }
      }
    ]);
  }
}

describe('App', () => {
  let catalogueService: CatalogueServiceStub;

  beforeEach(async () => {
    catalogueService = new CatalogueServiceStub();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: CatalogueService, useValue: catalogueService }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render title', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Assistant Boutique IA');
  });

  it('loads products directly without sending an AI request', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;

    app.afficherProduits();
    fixture.detectChanges();

    expect(catalogueService.getProduitsCalls).toBe(1);
    expect(catalogueService.demanderCalls).toBe(0);
    expect(app.messages().at(-1)?.texte).toContain('PC');
    expect(app.messages().at(-1)?.texte).toContain('informatique');
    expect(app.chargement()).toBe(false);
  });
});
