package tools;

public record PendingProduct(
        String nom,
        double prix,
        int quantite,
        Long categorieId
) {
}
