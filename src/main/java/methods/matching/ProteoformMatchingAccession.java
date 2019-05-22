package methods.matching;

import model.Proteoform;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class ProteoformMatchingAccession extends ProteoformMatching {

    public ProteoformMatchingAccession() {

    }

    @Override
    public Boolean matches(Proteoform iP, Proteoform rP, Long margin) {

        // Check the uniprot accession, including the isoform matches
        if (iP.getUniProtAccWithIsoform() == null) {
            throw new IllegalArgumentException();
        }

        if (rP.getUniProtAccWithIsoform() == null) {
            throw new IllegalArgumentException();
        }

        if (!iP.getUniProtAcc().equals(rP.getUniProtAcc())) {
            return false;
        }

        return true;
    }
}
