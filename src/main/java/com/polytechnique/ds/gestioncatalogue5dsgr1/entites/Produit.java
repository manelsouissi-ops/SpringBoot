package com.polytechnique.ds.gestioncatalogue5dsgr1.entites;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private double prix;

    @Column(name = "qte")
    private int quantite;

    @ManyToOne
    private Categorie categorie;
}