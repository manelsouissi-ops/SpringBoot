package com.polytechnique.ds.gestioncatalogue5dsgr1.controller;

import com.polytechnique.ds.gestioncatalogue5dsgr1.entites.Produit;
import com.polytechnique.ds.gestioncatalogue5dsgr1.service.IServiceProduit;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/produit")
public class ProduitController {

    IServiceProduit serviceProduit;

    @GetMapping("/all")
    public List<Produit> getAll()
    {
        return serviceProduit.getAllProducts();
    }
    @GetMapping("/{id}")
    public Produit getProduct(@PathVariable Long id)
    {
        return serviceProduit.getProduct(id);
    }
    /*@GetMapping("/search/{mc}")
    public List<Produit> serachProductsBKW2(@PathVariable String kw)
    {
        return serviceProduit.getProductsBMC(kw);
    }*/

    @GetMapping("/search")
    public List<Produit> serachProductsBKW(@RequestParam String kw)
    {
        return serviceProduit.getProductsBMC(kw);
    }

    @PostMapping("/add")
    public void addProduct(@RequestBody Produit p)
    {
        serviceProduit.addProduct(p);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id)
    {
        serviceProduit.deleteProduct(id);
    }

    @PutMapping("/update/{id}")
    public void updateProduct(@PathVariable Long id,@RequestBody Produit p)
    {
        serviceProduit.updateProduct(id,p);
    }




}
