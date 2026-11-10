package com.polytechnique.ds.gestioncatalogue5dsgr1.tools;

public record PendingProduct(
        String nom,
        double prix,
        int quantite,
        Long categorieId
) {
}
