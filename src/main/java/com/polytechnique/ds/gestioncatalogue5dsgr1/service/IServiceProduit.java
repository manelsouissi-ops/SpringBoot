package com.polytechnique.ds.gestioncatalogue5dsgr1.service;

import com.polytechnique.ds.gestioncatalogue5dsgr1.entites.Produit;

import java.util.List;

public interface IServiceProduit {

    void addProduct(Produit p);
    void updateProduct(Long id,Produit p);
    void deleteProduct(Long id);
    Produit getProduct(Long id);
    List<Produit> getAllProducts();
    List<Produit> getProductsBMC(String mc);
}
