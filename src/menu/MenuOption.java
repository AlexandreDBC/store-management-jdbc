package menu;

public class MenuOption {
    private final String key;
    private final String value;

    public MenuOption(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return value;
    }
}
