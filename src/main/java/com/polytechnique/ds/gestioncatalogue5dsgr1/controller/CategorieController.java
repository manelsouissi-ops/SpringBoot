package com.polytechnique.ds.gestioncatalogue5dsgr1.controller;

import com.polytechnique.ds.gestioncatalogue5dsgr1.entites.Categorie;
import com.polytechnique.ds.gestioncatalogue5dsgr1.repository.CategorieRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/categorie")
public class CategorieController {

    private final CategorieRepository categorieRepository;

    @GetMapping("/all")
    public List<Categorie> getAll() {
        return categorieRepository.findAll();
    }
}