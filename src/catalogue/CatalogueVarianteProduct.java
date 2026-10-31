package catalogue;

public record CatalogueVarianteProduct(int articleId, double sellPrice, boolean isPreCond, double weight) {
    public String toString() {
        return (isPreCond ? "Pré-conditionné en lot de " + weight + "kg pour " + sellPrice + "€" : "En vrac à " + sellPrice + "€ le kg");
    }
}