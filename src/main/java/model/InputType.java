package model;

/**
 * Specifies the possible input types for Main as an enum
 */
public enum InputType {

    GENE,
    ENSEMBL,
    UNIPROT,
    PROTEOFORM,
    PEPTIDE,
    MODIFIEDPEPTIDE,
    VCF,
    RSID,
    CHRBP;

    public Iterable<InputType> getValues() {
        return this.getValues();
    }

    public static boolean isValueOf(String value) {
        for (InputType type : InputType.values()) {
            if (type.toString().equals(value.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() { return name(); }
}