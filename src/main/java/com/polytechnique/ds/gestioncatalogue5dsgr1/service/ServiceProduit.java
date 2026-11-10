package com.polytechnique.ds.gestioncatalogue5dsgr1.service;

import com.polytechnique.ds.gestioncatalogue5dsgr1.entites.Produit;
import com.polytechnique.ds.gestioncatalogue5dsgr1.repository.ProduitRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class ServiceProduit implements IServiceProduit{

    //@Autowired
    private ProduitRepository produitRepository;

   /* public ServiceProduit(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }*/

    @Override
    public void addProduct(Produit p) {
        produitRepository.save(p);
    }

    @Override
    public void updateProduct(Long id, Produit p) {
        Produit pp=getProduct(id);
        if(pp!=null)
        {
            pp.setNom(p.getNom());
            pp.setPrix(p.getPrix());
            pp.setQuantite(p.getQuantite());
            pp.setCategorie(p.getCategorie());
            produitRepository.save(pp);
        }

    }

    @Override
    public void deleteProduct(Long id) {
            produitRepository.deleteById(id);
    }

    @Override
    public Produit getProduct(Long id) {
        return produitRepository.findById(id).orElse(null);
    }

    @Override
    public List<Produit> getAllProducts() {
        return produitRepository.findAll();
    }

    @Override
    public List<Produit> getProductsBMC(String mc) {
        return produitRepository.findByNomContains(mc);
    }
}
