package utils;

import java.util.concurrent.TimeUnit;

public class Utils {
    public static String availabilityToString(int availability) {
        if (availability < 0) throw new IllegalArgumentException("La disponibilité ne peut pas être négative.");
        if (availability == 0) return "En stock";
        return "Disponible sous " + availability + " jours";
    }

    public static String yearDayToDate(int jour) {
        String[] mois = {"janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre"};
        int[] joursParMois = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        int moisIndex = 0;
        // On soustrait les jours des mois successifs jusqu'au bon mois
        while (jour > joursParMois[moisIndex]) {
            jour -= joursParMois[moisIndex];
            moisIndex++;
        }
        return jour + " " + mois[moisIndex];
    }

    public static boolean isYearDayOfTheYearBetweenDays(int day, int start, int end) {
        return start <= end ? start <= day && day <= end : end <= day && day <= start;
    }

    public static void sleep(int seconds) throws InterruptedException {
        TimeUnit.SECONDS.sleep(seconds);
    }
}
