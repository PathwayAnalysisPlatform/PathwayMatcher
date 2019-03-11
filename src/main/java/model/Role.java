package model;

public enum Role {

    CATALYSTACTIVITY("catalyst"),
    INPUT("input"),
    OUTPUT("output"),
    REGULATEDBY("regulator");

    private final String displayName;

    private Role(String name) {
        displayName = name;
    }

    public String toString() {
        return this.displayName;
    }
}
