package dio.budgeting.domain;

public enum Category {
    FOOD("Alimentação"),
    HEALTH("Saúde"),
    TRANSPORT("Transporte"),
    HOUSING("Moradia"),
    LEISURE("Lazer"),
    OTHER("Outros");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
