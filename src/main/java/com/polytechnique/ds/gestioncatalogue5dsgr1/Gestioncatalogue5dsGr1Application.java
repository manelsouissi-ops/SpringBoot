package com.polytechnique.ds.gestioncatalogue5dsgr1;

import com.polytechnique.ds.gestioncatalogue5dsgr1.entites.Categorie;
import com.polytechnique.ds.gestioncatalogue5dsgr1.entites.Produit;
import com.polytechnique.ds.gestioncatalogue5dsgr1.repository.CategorieRepository;
import com.polytechnique.ds.gestioncatalogue5dsgr1.repository.ProduitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Gestioncatalogue5dsGr1Application {

    public static void main(String[] args) {
        SpringApplication.run(Gestioncatalogue5dsGr1Application.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(ProduitRepository produitRepository,
                                        CategorieRepository categorieRepository)
    {
        return arg->{
            Categorie c1=new Categorie(null,"informatique",null);
            Categorie c2=Categorie.builder()
                    .nom("electronique")
                    .build();
            categorieRepository.save(c1);
            categorieRepository.save(c2);

            produitRepository.save(
              Produit.builder()
                      .nom("pc")
                      .prix(1000)
                      .quantite(10)
                      .categorie(c1)
                      .build()
            );
            produitRepository.save(
                    Produit.builder()
                            .nom("souris")
                            .prix(10)
                            .quantite(10)
                            .categorie(c1)
                            .build()
            );
            produitRepository.save(
                    Produit.builder()
                            .nom("machine a laver")
                            .prix(1000)
                            .quantite(10)
                            .categorie(c2)
                            .build()
            );

        };

    }




}
