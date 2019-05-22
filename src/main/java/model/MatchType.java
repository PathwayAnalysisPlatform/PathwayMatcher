package model;

public enum MatchType {

    STRICT,
    SUPERSET, SUPERSET_NO_TYPES,
    SUBSET, SUBSET_NO_TYPES,
    ONE, ONE_NO_TYPES,
    ACCESSION;

    public static boolean isValueOf(String value) {
        for (MatchType type : MatchType.values()) {
            if (type.toString().equals(value.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
