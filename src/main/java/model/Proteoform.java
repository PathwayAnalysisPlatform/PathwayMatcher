package model;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.*;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class Proteoform implements Comparable<Proteoform>, Serializable {

    private String UniProtAcc; // The uniprot accession number including the optional isoform
    private Long startCoordinate; // The start coordinate of the protein subsequence
    private Long endCoordinate; // The end coordinate of the protein subsequence
    private List<Pair<String, Long>> ptms; // The list of post-translational modifications: PSI-MOD type ->
    // Sites set
    private Set<Snp> sourceSnpSet; // The genetic variants that lead to this proteoform
    // * This structure can not take "null" as a value, then when the coordinates
    // are null they are represented as -1.
    private static final long serialVersionUID = 1L;

    public Proteoform(String uniProtAcc) {
        UniProtAcc = uniProtAcc;
        startCoordinate = -1L;
        endCoordinate = -1L;
        ptms = new ArrayList<>();
    }

    public Proteoform(String uniProtAcc, List<Pair<String, Long>> ptms) {
        UniProtAcc = uniProtAcc;
        startCoordinate = -1L;
        endCoordinate = -1L;
        this.ptms = ptms;
    }

    public String getUniProtAcc() {
        if (UniProtAcc.contains("-")) {
            return UniProtAcc.substring(0, UniProtAcc.indexOf("-"));
        } else {
            return UniProtAcc;
        }
    }

    public String getUniProtAccWithIsoform() {
        return UniProtAcc;
    }

    public void setUniProtAcc(String uniProtAcc) {
        UniProtAcc = uniProtAcc;
    }

    /********************/
    public void setStartCoordinate(Long startCoordinate) {
        this.startCoordinate = startCoordinate == null ? -1L : startCoordinate;
    }

    public Long getStartCoordinate() {
        return this.startCoordinate == -1L ? null : this.startCoordinate;
    }

    public void setStringStartCoordinate(String coordinate) {
        this.startCoordinate = interpretCoordinateFromStringToLong(coordinate);
    }

    public String getStringStartCoordinate() {
        return interpretCoordinateFromLongToString(this.startCoordinate);
    }

    public static Long interpretCoordinateFromStringToLong(String s) {

        if (s == null) {
            return -1L;
        }
        if (s.length() == 0) {
            return -1L;
        }
        if (s.equals("?")) {
            return -1L;
        }
        if (s.toLowerCase().equals("null")) {
            return -1L;
        }
        return Long.valueOf(s);
    }

    public static String interpretCoordinateFromLongToString(Long l) {
        if (l == null || l == -1L) {
            return "null";
        } else {
            return l.toString();
        }
    }

    /***************************/

    public void setEndCoordinate(Long endCoordinate) {
        this.endCoordinate = endCoordinate == null ? -1L : endCoordinate;
    }

    public Long getEndCoordinate() {
        return this.endCoordinate == -1L ? null : this.endCoordinate;
    }

    public void setStringEndCoordinate(String coordinate) {
        this.endCoordinate = interpretCoordinateFromStringToLong(coordinate);
    }

    public String getStringEndCoordinate() {
        return interpretCoordinateFromLongToString(this.endCoordinate);
    }

    public List<Pair<String, Long>> getPtms() {
        return this.ptms;
    }

    // public List<Map.Entry<String, Long>> getPtmsSorted(){
    // List<Map.Entry<String, Long>> sortedPtms = new
    // ArrayList<>(this.ptms.entries());
    // Collections.sort(sortedPtms, (o1, o2) -> {
    // if (o1.getKey() != o2.getKey()){
    // return o1.getKey().compareTo(o2.getKey());
    // }
    // return (o1.getValue()).compareTo(o2.getValue());
    // });
    // return sortedPtms;
    // }

    public void setPtms(List<Pair<String, Long>> ptms) {
        this.ptms = ptms;
    }

    public Set<Snp> getSourceSnpSet() {
        return sourceSnpSet;
    }

    public void setSourceSnpSet(Set<Snp> sourceSnpSet) {
        this.sourceSnpSet = sourceSnpSet;
    }

    @Override
    public String toString() {
        return this.getUniProtAccWithIsoform();
    }

    public String toString(ProteoformFormat format) {
        if (format != null) {
            return format.getString(this);
        } else {
            return UniProtAcc + "," + startCoordinate + "-" + endCoordinate + "," + ptms.toString();
        }
    }

    public void addPtm(String modType, Long coordinate) {
        if (coordinate == null) {
            coordinate = -1L;
        }
        ptms.add(new MutablePair<>(modType, coordinate));
        Collections.sort(ptms);
    }

    @Override
    public int hashCode() {
        return UniProtAcc != null ? UniProtAcc.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Proteoform that = (Proteoform) obj;

        // noinspection RedundantIfStatement
        if (UniProtAcc != null ? !UniProtAcc.equals(that.UniProtAcc) : that.UniProtAcc != null)
            return false;

        if (startCoordinate != null ? !startCoordinate.equals(that.startCoordinate) : that.startCoordinate != null)
            return false;

        if (endCoordinate != null ? !endCoordinate.equals(that.endCoordinate) : that.endCoordinate != null)
            return false;

        // Verify the number of ptms is equal
        if (ptms != null ? ptms.size() != that.getPtms().size() : that.getPtms() != null)
            return false;

        // Verify the ptms are all equal
        for (Pair<String, Long> entry : ptms) {
            if (!that.getPtms().contains(new MutablePair<>(entry.getKey(), entry.getValue())))
                return false;
        }

        return true;
    }

    /**
     * Compares two proteoforms to decide how to order them.
     *
     * @param that The other proteoform
     * @return before = -1, equal = 0, after = 1
     */
    @Override
    public int compareTo(Proteoform that) {
        if (this.equals(that)) {
            return 0;
        }

        if (!this.UniProtAcc.equals(that.UniProtAcc)) {
            return this.UniProtAcc.compareTo(that.UniProtAcc);
        }

        // If both are not null
        if (this.startCoordinate != null && that.startCoordinate != null) {
            if (!this.startCoordinate.equals(that.startCoordinate)) {
                return this.startCoordinate.compareTo(that.startCoordinate);
            }
        }

        // If one is null
        if (this.startCoordinate == null && that.startCoordinate != null) {
            return -1;
        } else if (this.startCoordinate != null && that.startCoordinate == null) {
            return 1;
        }

        // If both are not null
        if (this.endCoordinate != null && that.endCoordinate != null) {
            if (!this.endCoordinate.equals(that.endCoordinate)) {
                return this.endCoordinate.compareTo(that.endCoordinate);
            }
        }

        // If one is null
        if (this.endCoordinate == null && that.endCoordinate != null) {
            return -1;
        } else if (this.endCoordinate != null && that.endCoordinate == null) {
            return 1;
        }

        // If they have different number of ptms
        if (this.ptms.size() != that.ptms.size()) {
            return Integer.compare(this.ptms.size(), that.ptms.size());
        }

        // If both have no ptms
        if (this.ptms.size() == 0) {
            return 0;
        }

        Iterator<Pair<String, Long>> itThis = this.getPtms().iterator();
        Iterator<Pair<String, Long>> itThat = that.getPtms().iterator();
        while (itThis.hasNext() && itThat.hasNext()) {
            Map.Entry<String, Long> thisPtm = itThis.next();
            Map.Entry<String, Long> thatPtm = itThat.next();

            if (!thisPtm.getKey().equals(thatPtm.getKey())) {
                return thisPtm.getKey().compareTo(thatPtm.getKey());
            }
            if (!thisPtm.getValue().equals(thatPtm.getValue())) {
                return thisPtm.getValue().compareTo(thatPtm.getValue());
            }
        }

        return 0;
    }
}
