package methods.matching;

import model.Proteoform;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class ProteoformMatchingOne extends ProteoformMatching {

    public ProteoformMatchingOne(Boolean useTypes) {
        this.useTypes = useTypes;
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

        if(!matches(iP.getStartCoordinate(), rP.getStartCoordinate(), margin)){
            return false;
        }

        if(!matches(iP.getEndCoordinate(), rP.getEndCoordinate(), margin)){
            return false;
        }

        if(rP.getPtms().size() == 0){
            return true;
        }

        // At least one of the reference ptms should be in the input
        for(Pair<String, Long> rPtm : rP.getPtms()){
            if(iP.getPtms().contains(new MutablePair<String, Long>(rPtm.getKey(), rPtm.getValue()))){
                return true;
            }
            // Traverse all the possible input ptms
            for (Pair<String, Long> iPtm : iP.getPtms()) {
                // Check type is equal if needed
                if (!useTypes || rPtm.getKey().equals(iPtm.getKey())) {
                    // Check the coordinates are within the margin
                    if (matches(rPtm.getValue(), iPtm.getValue(), margin)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
