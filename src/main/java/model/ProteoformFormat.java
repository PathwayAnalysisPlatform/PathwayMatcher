package model;

import com.google.common.base.CharMatcher;
import org.apache.commons.lang3.tuple.Pair;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Specifies the possible proteoform formats
 */
public enum ProteoformFormat {

    SIMPLE {
        public boolean matches(String str) {
            Matcher m = PATTERN_PROTEOFORM_SIMPLE.matcher(str);
            return m.matches();
        }

        @Override
        public String getString(model.Proteoform proteoform) {
            StringBuilder str = new StringBuilder();
            str.append(proteoform.getUniProtAccWithIsoform() + ";");

            int cont = 0;
            for (Pair<String, Long> ptm : proteoform.getPtms()) {
                if (cont > 0) {
                    str.append(",");
                }
                str.append(ptm.getKey() + ":" + model.Proteoform.interpretCoordinateFromLongToString(ptm.getValue()));
                cont++;
            }

            return str.toString();
        }

        /**
         * Interprets a matching line of a simple proteoform with optional expression
         * values.
         *
         * @param line
         * @param i
         * @return
         */
        @Override
        public Proteoform getProteoform(String line, int i) throws ParseException {
            Proteoform proteoform = new Proteoform("");
            StringBuilder protein = new StringBuilder();
            StringBuilder coordinate = null;
            StringBuilder mod = null;

            // Get the identifier
            // Read until end of line or semicolon
            int pos = 0;
            char c = line.charAt(pos);
            while (c != ';') {
                protein.append(c);
                pos++;
                if (pos == line.length())
                    break;
                c = line.charAt(pos);
            }
            pos++;
            if (protein.length() == 0) {
                Warning.sendWarning(Warning.INVALID_ROW, i);
                throw new ParseException(Warning.INVALID_ROW.getMessage(),
                        Warning.INVALID_ROW.getCode());
            }
            proteoform.setUniProtAcc(protein.toString());
            // Get ptms one by one
            // While there are characters

            while (pos < line.length()) {
                c = line.charAt(pos);
                while (!Character.isDigit(c)) {
                    if (pos + 1 < line.length()) {
                        c = line.charAt(++pos);
                    } else {
                        break;
                    }
                }
                coordinate = new StringBuilder();
                mod = new StringBuilder();
                // Read a ptm
                while (c != ':') {
                    mod.append(c);
                    pos++;
                    c = line.charAt(pos);
                }
                pos++;
                c = line.charAt(pos);
                while (Character.isDigit(c) || CharMatcher.anyOf("nulNUL").matches(c)) {
                    coordinate.append(c);
                    pos++;
                    if (pos == line.length())
                        break;
                    c = line.charAt(pos);
                }
                proteoform.addPtm(mod.toString(),
                        Proteoform.interpretCoordinateFromStringToLong(coordinate.toString()));
                if (c != ',') {
                    break;
                }
                pos++;
            }

            return proteoform;
        }

        @Override
        public boolean fastCheck(String line) {
            Matcher m = PATTERN_PROTEOFORM_SIMPLE.matcher(line);
            return m.find();
        }
    },
    PRO {
        /**
         * Format rules: - One proteoform per line - Consists of a sequence block and
         * optional modification blocks - The only mandatory part is the accession
         * number. - There are one or more optional modification blocks - Sequence
         * blocks consist of a UniProtKB accession with an optional isoform indicated by
         * a dash, followed by a comma. And an optional subsequence range separated with
         * a comma. - Each modification block is presented in order from the
         * N-terminal-most amino acid specified. - Within a modification block there are
         * one or more amino acids listed by type and position. - Multiple amino-acids
         * within a block are separated by forward slashes. - Positions of modification
         * are relative to the full length of the isoform. - Missing a subsequence
         * section indicates that the class encompasses either multiple species or
         * isoforms. - Missing modification blocks with a subsequence indicates that the
         * class is defined by subsequence only. - If there is a subsequence then the
         * comma separator is added, otherwise is not added. - If there is at least one
         * modification block, an extra comma separator is added - The line never ends
         * in comma. - There string ",," is never found. - NOTE: In our casse we will
         * only use the accession numbers and set of post translational modifications to
         * identify a particular proteoform, to make our analysis consistent with the
         * rest of the formats. - We allow the position to be null, so that it is also
         * consistent with the rest. - The missing coordinates are represented as "?" or
         * "null" or "NULL", never left blank.
         * <p>
         * The draft of the format is at: doi: 10.1093/nar/gkw1075
         */

        @Override
        public String getString(Proteoform proteoform) {
            StringBuilder str = new StringBuilder();

            // Print the protein accession
            str.append("UniProtKB:" + proteoform.getUniProtAccWithIsoform());

            // Print the subsequence range
            Long start = proteoform.getStartCoordinate();
            Long end = proteoform.getEndCoordinate();
            if (!(start == null && end == null)) {
                str.append("," + (start != null ? start : "?") + "-" + (end != null ? end : "?"));
            }

            HashMap<String, ArrayList<Long>> reorderedPtms = new HashMap<>();

            for (Pair<String, Long> ptm : proteoform.getPtms()) {
                if (!reorderedPtms.containsKey(ptm.getKey())) {
                    reorderedPtms.put(ptm.getLeft(), new ArrayList<Long>());
                }
                reorderedPtms.get(ptm.getLeft()).add(ptm.getRight());
            }

            if (proteoform.getPtms().size() > 0) {
                str.append(",");

                int M = 0;
                for (String mod : reorderedPtms.keySet()) {
                    if (M != 0) {
                        str.append("|");
                    }
                    int S = 0;
                    for (Long site : reorderedPtms.get(mod)) {
                        if (S != 0) {
                            str.append("/");
                        }
                        str.append(getResidue(mod) + "-" + Proteoform.interpretCoordinateFromLongToString(site));
                        S++;
                    }
                    str.append(",MOD:" + mod);
                    M++;
                }
            }
            return str.toString();
        }

        /**
         * Receives a trimmed line that has already been proved to follow the regex for
         * PRO Proteoform.
         * <p>
         * The proteoform consists of five attributes: uniprot accession, isoform, start
         * and end coordinates and post-translational modifications.
         *
         * @param line
         * @param i
         * @return
         */
        public Proteoform getProteoform(String line, int i) {

            Proteoform proteoform = new Proteoform("");
            StringBuilder protein = new StringBuilder();
            StringBuilder coordinate = null;
            List<Long> coordinateList = new ArrayList<>();
            StringBuilder mod = null;
            final int lineLength = line.length();

            int pos = 0;
            char c = line.charAt(pos);
            while (c != ':') { // Read the database name section "UniProtKB:"
                c = line.charAt(++pos);
            }
            c = line.charAt(++pos);
            while (true) { // Read the accession section
                protein.append(c);
                pos++;
                if (pos >= lineLength) {
                    break;
                }
                c = line.charAt(pos);
                if (c == ',' || c == ' ' || c == '\t') {
                    break;
                }
            } // The proteoform should come at least until here
            proteoform.setUniProtAcc(protein.toString());
            if (c == ',') {
                pos++; // Advance after the comma of the accession or out of the string
                if (pos < lineLength) { // If there are still characters
                    c = line.charAt(pos);

                    // Read the next piece of text until the next '-'
                    StringBuilder str = new StringBuilder();
                    while (c != '-') {
                        str.append(c);
                        c = line.charAt(++pos);
                    }

                    // If it is a start coordinate
                    if (org.apache.commons.lang3.StringUtils.isNumeric(str.toString()) || str.toString().equals("?")
                            || str.toString().toLowerCase().equals("null")) {
                        proteoform.setStringStartCoordinate(str.toString());
                        c = line.charAt(++pos);

                        // Read the endCoordinate
                        coordinate = new StringBuilder();
                        while (Character.isAlphabetic(c) || Character.isDigit(c) || c == '?') {
                            coordinate.append(c);
                            pos++;
                            if (pos >= lineLength) {
                                break;
                            }
                            c = line.charAt(pos);
                        }
                        proteoform.setStringEndCoordinate(coordinate.toString());
                        pos++;
                    }
                    // If it was a PTM modified residue
                    else {
                        proteoform.setStartCoordinate(null);
                        proteoform.setEndCoordinate(null);
                        pos -= str.toString().length();
                    }
                    // Here pos should e pointing at the comma or the next position after the end
                    if (pos < lineLength) {
                        if (line.charAt(pos - 1) != ',') {
                            return proteoform;
                        }
                    }
                    // Here the pos should point to the comma; either after the uniprot accession or
                    // the subsequence ranges.
                    while (pos < lineLength) { // Read the post-translational modifications section
                        c = line.charAt(pos); // While there are characters to read expect: \w{3}-\d+/PTM/PTM,MOD:#####

                        if (c == '|') {
                            c = line.charAt(++pos);
                            coordinateList = new ArrayList<>();
                        }
                        if (c == ' ' || c == '\t') {
                            break;
                        }
                        while (c != ',') {
                            while (c != '-') {
                                c = line.charAt(++pos);
                            }
                            c = line.charAt(++pos);
                            coordinate = new StringBuilder();
                            while (c != ',' && c != '/') {
                                coordinate.append(c);
                                c = line.charAt(++pos);
                            }
                            coordinateList.add(Proteoform.interpretCoordinateFromStringToLong(coordinate.toString()));
                        }
                        while (c != ':') { // Skip the "MOD:"
                            c = line.charAt(++pos);
                        }
                        mod = new StringBuilder();
                        for (int I = 0; I < 5; I++) {
                            mod.append(line.charAt(++pos));
                        }
                        for (Long site : coordinateList) {
                            proteoform.addPtm(mod.toString(), site);
                        }
                        pos++;
                    }
                }
            }

            return proteoform;
        }

        @Override
        public boolean matches(String line) {
            Matcher m = PATTERN_PROTEOFORM_PRO.matcher(line);
            return m.matches();
        }

        @Override
        public boolean fastCheck(String line) {
            Matcher m = PATTERN_PROTEOFORM_PRO.matcher(line);
            return m.find();
        }

        private String getResidue(String mod) {
            switch (mod) {
                case "00010":
                case "01631":
                    return "Ala";
                case "00092":
                case "00012":
                    return "Asn";
                case "00011":
                case "01632":
                    return "Arg";
                case "00113":
                case "00014":
                case "01635":
                case "00094":
                case "00798":
                    return "Cys";
                case "01637":
                    return "Gln";
                case "00015":
                case "00041":
                    return "Glu";
                case "01638":
                    return "Gly";
                case "00018":
                    return "His";
                case "00019":
                    return "Ile";
                case "01641":
                    return "Leu";
                case "00037":
                case "00130":
                case "00162":
                case "01148":
                case "01914":
                case "00083":
                case "01149":
                case "00064":
                case "00087":
                    return "Lys";
                case "00023":
                    return "Phe";
                case "00038":
                case "00039":
                case "01645":
                case "00024":
                    return "Pro";
                case "00046":
                case "01646":
                case "00025":
                    return "Ser";
                case "00047":
                case "00813":
                case "00026":
                    return "Thr";
                case "00027":
                case "01648":
                    return "Trp";
                case "00048":
                    return "Tyr";
                case "01650":
                    return "Val";
                default:
                    return "XXX";
            }
        }

    },
    NEO4J {

        // Example: """Q15303-2""","[""00048:1046"",""00048:1178"",""00048:1232""]"

        @Override
        public String getString(Proteoform proteoform) {
            try {
                StringBuilder str = new StringBuilder();
                boolean isFirst = true;

                str.append("\"\"\"" + proteoform.getUniProtAccWithIsoform() + "\"\"\"");
                str.append(",");
                if (proteoform.getPtms().size() > 0) {
                    str.append("\"");
                }
                str.append("[");

                for (Pair<String, Long> ptm : proteoform.getPtms()) {
                    if (!isFirst) {
                        str.append(',');
                    }
                    str.append("\"\"");
                    str.append(ptm.getLeft() + ":" + Proteoform.interpretCoordinateFromLongToString(ptm.getRight()));
                    str.append("\"\"");
                    isFirst = false;
                }

                str.append("]");
                if (proteoform.getPtms().size() > 0) {
                    str.append("\"");
                }
                return str.toString();
            } catch (NullPointerException e) {
                System.out.println(proteoform.toString(ProteoformFormat.NEO4J));
                System.out.println(e);
            }
            return null;
        }

        /**
         * This method receives a line that has been validated to follow the structure
         * of a simple proteoform with optional expression values.
         *
         * @param line
         * @param i
         * @return
         */
        @Override
        public Proteoform getProteoform(String line, int i) {

            Proteoform proteoform = new Proteoform("");
            StringBuilder protein = new StringBuilder();
            StringBuilder coordinate = null;
            StringBuilder mod = null;

            // Get the identifier
            int pos = 0;
            char c = line.charAt(pos);
            while (c != ',') {
                if (c != '\"') {
                    protein.append(c);
                }
                c = line.charAt(++pos);
            }
            proteoform.setUniProtAcc(protein.toString());
            // Here the pos points to the first comma
            c = line.charAt(++pos);
            // Get the PTMs
            while (c != ']') {
                if (Character.isDigit(c)) { // Found a digit of a PSI-MOD id
                    // Capture the 5 digits
                    mod = new StringBuilder();
                    for (int I = 0; I < 5; I++) {
                        mod.append(c);
                        c = line.charAt(++pos);
                    }
                    c = line.charAt(++pos);
                    // Capture the coordinate
                    coordinate = new StringBuilder();
                    while (c != ']' && c != ',') {
                        coordinate.append(c);
                        c = line.charAt(++pos);
                    }
                    // Add the PTM
                    proteoform.addPtm(mod.toString(), (coordinate.toString().toLowerCase().equals("null") ? null
                            : Long.valueOf(coordinate.toString())));
                }
                if (line.charAt(pos) != ']')
                    c = line.charAt(++pos);
            }

            return proteoform;
        }

        @Override
        public boolean matches(String line) {
            Matcher m = PATTERN_PROTEOFORM_NEO4J.matcher(line);
            return m.matches();
        }

        /**
         * Goes through the line to check if it contains at least a part that could
         * uniquely identify the input to a specific format.
         *
         * @param line to check
         * @return
         */
        @Override
        public boolean fastCheck(String line) {
            Matcher m = PATTERN_PROTEOFORM_NEO4J.matcher(line);
            return m.find();
        }

    };

    public static ProteoformFormat checkFormat(String input) {
        if (ProteoformFormat.PRO.fastCheck(input)) {
            return PRO;
        } else if (ProteoformFormat.SIMPLE.fastCheck(input)) {
            return SIMPLE;
        }
        return NEO4J;
    }

    private static final String PROTEOFORM_SIMPLE = "([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})([-]\\d{1,2})?;(\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll]))?(,\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll]))*";
    private static final Pattern PATTERN_PROTEOFORM_SIMPLE = Pattern.compile(PROTEOFORM_SIMPLE);

    public static final String PROTEOFORM_NEO4J = "\\\"{3}?([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})([-]\\d{1,2})?(\\\"{3})?,(-?\\d+|[Nn][Uu][Ll][Ll])?,(-?\\d+|[Nn][Uu][Ll][Ll])?,\\\"?\\[(\\\"\\\"\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll])\\\"\\\"(,\\\"\\\"\\d{5}:(\\d{1,11}|[Nn][Uu][Ll][Ll])\\\"\\\")*)?\\]\\\"?";
    private static final Pattern PATTERN_PROTEOFORM_NEO4J = Pattern.compile(PROTEOFORM_NEO4J);

    private static final String COORDINATE = "(\\d{1,11}|([Nn][Uu][Ll][Ll])|\\?)";
    private static final String PROTEOFORM_PRO = "UniProtKB:([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})([-]\\d{1,2})?(,"
            + COORDINATE + "-" + COORDINATE
            + ")?(,\\p{Alpha}{3}-(\\d{1,11}|[Nn][Uu][Ll][Ll])(\\/\\p{Alpha}{3}-(\\d{1,11}|([Nn][Uu][Ll][Ll])))*,MOD:\\d{5}(\\|\\p{Alpha}{3}-(\\d{1,11}|[Nn][Uu][Ll][Ll])(\\/\\p{Alpha}{3}-(\\d{1,11}|[Nn][Uu][Ll][Ll]))*,MOD:\\d{5})*)?";
    private static final Pattern PATTERN_PROTEOFORM_PRO = Pattern.compile(PROTEOFORM_PRO);

    public abstract String getString(Proteoform proteoform);

    public Proteoform getProteoform(String line) throws ParseException {
        return getProteoform(line, 0);
    }

    public abstract Proteoform getProteoform(String line, int i) throws ParseException;

    /**
     * Verifies that the string completely matches the format.
     *
     * @param line The text of the proteoform
     */
    public abstract boolean matches(String line);

    /**
     * Goes through the line to check if it contains at least a part that could
     * uniquely identify the input to a specific format.
     *
     * @param line The text of the proteoform
     */
    public abstract boolean fastCheck(String line);

}