package catalogue;

import utils.Utils;

import java.util.HashSet;
import java.util.stream.Collectors;

public record CatalogueProductItem(String name, String description, int availability, int seasonalityStart,
                                   int seasonalityEnd, HashSet<CatalogueVarianteProduct> variantes) {
    public String toShortString() {
        return name + " : " + Utils.availabilityToString(availability) + " du " + Utils.yearDayToDate(seasonalityStart) + " au " + Utils.yearDayToDate(seasonalityEnd);
    }

    public String toString() {
        String variantesToString = variantes.stream().map(v -> "└── " + v.toString()).collect(Collectors.joining("\n"));
        return name + " (" + description + ") - " + Utils.availabilityToString(availability) + " du " + Utils.yearDayToDate(seasonalityStart) + " au " + Utils.yearDayToDate(seasonalityEnd) + "\n" + variantesToString;
    }

    public HashSet<CatalogueVarianteProduct> getVariantes() {
        return variantes;
    }
}