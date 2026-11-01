package service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record livraisonChecker() {
    private static final Set<String> TIER2 = new HashSet<>(Arrays.asList("albania", "andorra", "armenia", "austria", "azerbaijan", "belarus", "belgium", "bosnia and herzegovina", "bulgaria", "croatia", "cyprus", "czech republic", "denmark", "estonia", "finland", "france", "georgia", "germany", "greece", "hungary", "iceland", "ireland", "italy", "kosovo", "latvia", "liechtenstein", "lithuania", "luxembourg", "malta", "moldova", "monaco", "montenegro", "netherlands", "north macedonia", "norway", "poland", "portugal", "romania", "san marino", "serbia", "slovakia", "slovenia", "spain", "sweden", "switzerland", "ukraine", "united kingdom", "vatican city"));

    private static final Set<String> TIER1 = new HashSet<>(List.of("france"));

    public double getTax(String country, double poidscommande) {
        if (country == null) return 0;
        double tax = 0;
        String normalized = country.toLowerCase();

        if (TIER1.contains(normalized)) {
            tax += 3;
            tax += poidscommande / 5;
        } else if (TIER2.contains(normalized)) {
            tax += 10;
            tax += poidscommande / 2;
        } else {
            tax += 20;
            tax += poidscommande;
        }
        return tax;
    }

    public String getDateLivraison(String country, int delaiDisponibilite) {
        LocalDate dateLivraison = LocalDate.now();
        String normalized = country.toLowerCase();

        if (TIER1.contains(normalized)) return dateLivraison.plusDays(3 + delaiDisponibilite).toString();
        if (TIER2.contains(normalized)) return dateLivraison.plusDays(7 + delaiDisponibilite).toString();
        return dateLivraison.plusDays(10 + delaiDisponibilite).toString();
    }

}
