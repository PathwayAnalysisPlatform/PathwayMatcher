package methods.matching;

import model.Proteoform;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Matching type where the input proteoforms are matched with proteoforms, when all the ptms of the input proteoform are in the reference proteoforms.
 */
public class ProteoformMatchingSubset extends ProteoformMatching {

    public ProteoformMatchingSubset(Boolean useTypes) {
        this.useTypes = useTypes;
    }

    public boolean matches(Long iC, Long rC, Long margin) {
        if (iC != null) {
            if (iC == -1L) iC = null;
        }
        if (rC != null) {
            if (rC == -1L) rC = null;
        }
        if (iC != null && rC != null) {
            if (iC != rC) {
                if (Math.abs(iC - rC) > margin) {
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

        // All the input PTMs should be in the reference
        for (Pair<String, Long> iPtm : iP.getPtms()) {
            // If the ptm is exactly then everything keeps ok
            // If the ptm is not contained then we check in a flexible way with the coordinate margin and type
            if (!rP.getPtms().contains(new MutablePair<>(iPtm.getKey(), iPtm.getValue()))) {
                boolean anyMatches = false;
                // Try to find the current input ptm in all the available reference ptms
                for (Pair<String, Long> rPtm : rP.getPtms()) {
                    // If type is required then it must be equal
                    if (!useTypes || iPtm.getLeft().equals(rPtm.getLeft())) {
                        // Check the coordinate within the margin
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
