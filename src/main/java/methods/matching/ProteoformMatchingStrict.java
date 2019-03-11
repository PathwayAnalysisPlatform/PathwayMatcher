package methods.matching;

import model.Proteoform;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Map;

public class ProteoformMatchingStrict extends ProteoformMatching {

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

        if (rP.getPtms().size() != iP.getPtms().size()) {
            return false;
        }

        // All the reference PTMs should be exactly in the input
        for (Map.Entry<String, Long> rPtm : rP.getPtms()) {
            if (!iP.getPtms().contains(new MutablePair<>(rPtm.getKey(), rPtm.getValue()))) {
                return false;
            }
        }

        // All the input PTMs should be exactly in the reference
        for (Map.Entry<String, Long> iPtm : iP.getPtms()) {
            if (!rP.getPtms().contains(new MutablePair<>(iPtm.getKey(), iPtm.getValue()))) {
                return false;
            }
        }

        return true;
    }

}
