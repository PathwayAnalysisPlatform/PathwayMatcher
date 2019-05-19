package methods.matching;

import model.Proteoform;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Matching type where the input proteoforms are matched with proteoforms, when the input contains all or more of the post translational modification of the reference proteoforms.
 */
public class ProteoformMatchingSuperset extends ProteoformMatching {

    public ProteoformMatchingSuperset(Boolean useTypes) {
        this.useTypes = useTypes;
    }

    public boolean matches(Long iC, Long rC, Long margin){
        if(iC != null){ if(iC == -1L) iC = null; }
        if(rC != null){ if(rC == -1L) rC = null; }
        if(iC != null && rC != null){
            if(iC != rC){
                if(Math.abs(iC-rC) > margin){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Boolean matches(Proteoform iP, Proteoform rP, Long margin) {

        // Check the uniprot accession, including the isoform matches
        if(iP.getUniProtAccWithIsoform() == null){
            throw new IllegalArgumentException();
        }

        if(rP.getUniProtAccWithIsoform() == null){
            throw new IllegalArgumentException();
        }

        if(!iP.getUniProtAccWithIsoform().equals(rP.getUniProtAccWithIsoform())){
            return false;
        }

        if (!matches(iP.getStartCoordinate(), rP.getStartCoordinate(), margin)) {
            return false;
        }

        if (!matches(iP.getEndCoordinate(), rP.getEndCoordinate(), margin)) {
            return false;
        }

        // All the reference PTMs should be in the inputs
        for (Pair<String, Long> rPtm : rP.getPtms()) {
            // If the rPtm is exatly in the input then it is ok
            // If the rPtm is not present then we check in a flexible way with the coordinate margin or type
            if (!iP.getPtms().contains(new MutablePair<>(rPtm.getKey(), rPtm.getValue()))) {
                boolean anyMatches = false;
                // Try to find the current reference ptm in the available input ptms
                for (Pair<String, Long> iPtm : iP.getPtms()) {
                    // Check type is equal if needed
                    if (!useTypes || rPtm.getLeft().equals(iPtm.getLeft())) {
                        // Check the coordinates are within the margin
                        if (matches(rPtm.getRight(), iPtm.getRight(), margin)) {
                            anyMatches = true;
                            break;
                        }
                    }
                }
                if (!anyMatches) {
                    return false;
                }
            }
        }
        return true;
    }
}
