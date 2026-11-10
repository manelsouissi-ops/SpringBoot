package com.polytechnique.ds.gestioncatalogue5dsgr1.tools;

import com.polytechnique.ds.gestioncatalogue5dsgr1.entites.Produit;
import com.polytechnique.ds.gestioncatalogue5dsgr1.repository.ProduitRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import com.polytechnique.ds.gestioncatalogue5dsgr1.entites.Categorie;
import com.polytechnique.ds.gestioncatalogue5dsgr1.repository.CategorieRepository;
import org.springframework.ai.tool.annotation.ToolParam;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

@Component

public class ProduitTools {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;
    private final Map<String, PendingProduct> pendingProducts =
            new ConcurrentHashMap<>();

    public ProduitTools(
            ProduitRepository produitRepository,
            CategorieRepository categorieRepository) {

        this.produitRepository = produitRepository;
        this.categorieRepository = categorieRepository;
    }

    @Tool(
            name = "getProduct",
            description = "retourne le produit à partir de son id"
    )
    public Produit getProduit(
            @ToolParam(description = "l'id du produit") Long id) {

        return produitRepository.findById(id).orElse(null);
    }

    @Tool(
            name = "getProducts",
            description = "retourner la liste de tous les produits existant dans ma base de donnée"
    )
    public List<Produit> getAllProduits() {

        return produitRepository.findAll();
    }
   // @Tool(
        //    name = "addProduct",
        //    description = "Ajoute un produit dans PostgreSQL uniquement après "
           //         + "confirmation explicite de l'utilisateur. "
            //        + "Nécessite un nom, un prix, une quantité et un ID de catégorie."
   // )
    public String addProduct(
            @ToolParam(description = "Nom du produit") String nom,
            @ToolParam(description = "Prix du produit") double prix,
            @ToolParam(description = "Quantité disponible") int quantite,
            @ToolParam(description = "ID de la catégorie") Long categorieId) {

        if (nom == null || nom.isBlank() || prix < 0 || quantite < 0) {
            return "Erreur : informations du produit invalides.";
        }

        if (categorieId == null) {
            return "Erreur : catégorie obligatoire.";
        }

        Categorie categorie = categorieRepository
                .findById(categorieId)
                .orElse(null);

        if (categorie == null) {
            return "Erreur : catégorie introuvable.";
        }

        Produit produit = new Produit();
        produit.setNom(nom);
        produit.setPrix(prix);
        produit.setQuantite(quantite);
        produit.setCategorie(categorie);

        Produit saved = produitRepository.save(produit);

        return "Produit ajouté avec succès. ID : " + saved.getId();
    }
    @Tool(
            name = "prepareProduct",
            description = "Prépare l'ajout d'un produit sans l'enregistrer. " +
                    "Utilise ce tool quand l'utilisateur demande d'ajouter un produit."
    )
    public String prepareProduct(
            @ToolParam(description = "Nom du produit") String nom,
            @ToolParam(description = "Prix du produit") double prix,
            @ToolParam(description = "Quantité du produit") int quantite,
            @ToolParam(description = "ID de la catégorie") Long categorieId) {

        if (nom == null || nom.isBlank() || prix < 0 || quantite < 0) {
            return "Erreur : informations invalides.";
        }

        if (categorieId == null ||
                !categorieRepository.existsById(categorieId)) {
            return "Erreur : catégorie introuvable.";
        }

        String reference = UUID.randomUUID().toString();

        pendingProducts.put(
                reference,
                new PendingProduct(nom, prix, quantite, categorieId)
        );

        return "Produit préparé, mais NON enregistré. " +
                "Référence : " + reference +
                ". Nom : " + nom +
                ", prix : " + prix +
                ", quantité : " + quantite +
                ", catégorie ID : " + categorieId +
                ". Demander à l'utilisateur de confirmer.";
    }
    public synchronized String confirmProduct(String reference) {

        if (reference == null || reference.isBlank()) {
            return "Erreur : référence obligatoire.";
        }

        PendingProduct pending = pendingProducts.remove(reference);

        if (pending == null) {
            return "Erreur : proposition introuvable ou déjà confirmée.";
        }

        return addProduct(
                pending.nom(),
                pending.prix(),
                pending.quantite(),
                pending.categorieId()
        );
    }
}