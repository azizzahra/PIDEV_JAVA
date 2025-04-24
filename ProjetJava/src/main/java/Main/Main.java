package Main;

import model.post;
import services.PostService;

public class Main
 {


    public static void main(String[] args) {


        DatabaseConnection.getInstance();
        PostService ps = new PostService();

        post p =new post("Quel système d’énergie verte recommandez-vous pour une ferme agricole ?",1,"Bonjour à tous, agricole plus autonome en énergie et réduire mon impact environnemental \uD83C\uDF0D. Je réfléchis à l’installation d’un système d’énergie renouvelable pour alimenter mes équipements agricoles (pompes d’irrigation, serres, éclairage, etc.).Entre panneaux solaires photovoltaïques, éoliennes rurales, et biogaz issu des déchets organiques, quelle solution vous semble la plus efficace et rentable sur le long terme ? ⚡\uD83C\uDF3FSi certains d’entre vous ont déjà installé un de ces systèmes, je serais ravi d’avoir vos retours d’expérience et conseils !","Matériel Agricole","NULL","2025-03-02",2,1);
        ps.add(p);

        ps.afficher();
    }
}
