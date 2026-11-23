package tools;


import entites.Categorie;
import entites.Produit;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import repository.CategorieRepository;
import repository.ProduitRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    @McpTool(
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
            @McpToolParam(description = "Nom du produit") String nom,
            @McpToolParam(description = "Prix du produit") double prix,
            @McpToolParam(description = "Quantité disponible") int quantite,
            @McpToolParam(description = "ID de la catégorie") Long categorieId) {

        if (nom == null || nom.isBlank() || !Double.isFinite(prix) || prix <= 0 || quantite < 0) {
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

    @McpTool(
            name = "prepareProduct",
            description = "Prépare l'ajout d'un produit sans l'enregistrer. " +
                    "Utilise ce tool quand l'utilisateur demande d'ajouter un produit."
    )
    public String prepareProduct(
            @McpToolParam(description = "Nom du produit") String nom,
            @McpToolParam(description = "Prix du produit") double prix,
            @McpToolParam(description = "Quantité du produit") int quantite,
            @McpToolParam(description = "ID de la catégorie") Long categorieId) {

        if (nom == null || nom.isBlank() || !Double.isFinite(prix) || prix <= 0 || quantite < 0) {
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

        PendingProduct pending = pendingProducts.get(reference);

        if (pending == null) {
            return "Erreur : proposition introuvable ou déjà confirmée.";
        }

        String result = addProduct(
                pending.nom(),
                pending.prix(),
                pending.quantite(),
                pending.categorieId()
        );

        if (!result.startsWith("Erreur :")) {
            pendingProducts.remove(reference, pending);
        }

        return result;
    }

    @McpTool(
            name = "getStatistiques",
            description = "Fournir des statistiques détaillées sur notre catalogue de produits, notamment le nombre total de produits, le prix moyen, ainsi que les produits les plus chers et les moins chers."
    )
    public Map<String, Object> getStatistiques() {

        List<Produit> liste = produitRepository.findAll();

        Map<String, Object> map = new HashMap<>();

        // Nombre total de produits
        map.put("nombre de produits total", liste.size());

        // Prix moyen
        map.put("prix moyen", liste.stream()
                .mapToDouble(p -> p.getPrix())
                .average()
                .orElse(0));

        // Produit le plus cher
        map.put("produit le plus cher", liste.stream()
                .max((p1, p2) -> Double.compare(p1.getPrix(), p2.getPrix()))
                .orElse(null));

        // Produit le moins cher
        map.put("produit le moins cher", liste.stream()
                .min((p1, p2) -> Double.compare(p1.getPrix(), p2.getPrix()))
                .orElse(null));

        return map;
    }

    public Produit createProduct(

            @McpToolParam(description = "Nom du produit", required = true)
            String nom,

            @McpToolParam(description = "Prix en dinars, strictement positif", required = true)
            double prix,

            @McpToolParam(description = "Quantité en stock", required = true)
            int quantite,

            @McpToolParam(description = "Nom de la catégorie existante", required = true)
            String nomCategorie) {

        // 1. Validation
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom du produit est obligatoire.");
        }

        if (!Double.isFinite(prix) || prix <= 0) {
            throw new IllegalArgumentException("Le prix doit être strictement positif.");
        }

        if (quantite < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative.");
        }

        if (nomCategorie == null || nomCategorie.isBlank()) {
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        }

        // 2. Rechercher une catégorie existante
        Categorie categorie = categorieRepository.findAll()
                .stream()
                .filter(c -> c.getNom().equalsIgnoreCase(nomCategorie.trim()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Catégorie introuvable.")
                );

        // 3. Créer le produit
        Produit nouveauProduit = new Produit();

        nouveauProduit.setNom(nom);
        nouveauProduit.setPrix(prix);
        nouveauProduit.setQuantite(quantite);
        nouveauProduit.setCategorie(categorie);

        // 4. Enregistrer dans PostgreSQL
        return produitRepository.save(nouveauProduit);
    }





}