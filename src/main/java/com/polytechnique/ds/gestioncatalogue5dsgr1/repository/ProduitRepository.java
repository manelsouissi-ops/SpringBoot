package com.polytechnique.ds.gestioncatalogue5dsgr1.repository;

import com.polytechnique.ds.gestioncatalogue5dsgr1.entites.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit,Long> {

    //derived Query
    List<Produit> findByNomContains(String mc);

    //jPQL
    @Query("select p from Produit p where p.nom like %:x%")
    List<Produit> rechercherParMotCle(@Param("x") String mc);
}
