package model;

/**
 * Specifies the possible input types for PathwayMatcher as an enum
 */
public enum InputType {

    GENE,
    GENES,
    ENSEMBL,
    ENSEMBLS,
    UNIPROT,
    UNIPROTS,
    PROTEOFORM,
    PROTEOFORMS,
    PEPTIDE,
    PEPTIDES,
    MODIFIEDPEPTIDE,
    MODIFIEDPEPTIDES,
    VCF,
    RSID,
    RSIDS,
    CHRBP,
    CHRBPS,
    UNKNOWN;

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
}