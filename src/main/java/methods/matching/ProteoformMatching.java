package methods.matching;

import model.MatchType;
import model.Proteoform;

public abstract class ProteoformMatching {

	Boolean useTypes;
	public abstract Boolean matches(Proteoform iP, Proteoform rP, Long margin);

	public boolean matches(Long iC, Long rC, Long margin) {
		if (iC != null) {
			if (iC == -1L)
				iC = null;
		}
		if (rC != null) {
			if (rC == -1L)
				rC = null;
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

	public static ProteoformMatching getInstance(MatchType matchType){
		ProteoformMatching matcher = null;
		switch (matchType) {
			case SUPERSET:
				matcher = new ProteoformMatchingSuperset(true);
				break;
			case SUPERSET_NO_TYPES:
				matcher = new ProteoformMatchingSuperset(false);
				break;
			case SUBSET:
				matcher = new ProteoformMatchingSubset(true);
				break;
			case SUBSET_NO_TYPES:
				matcher = new ProteoformMatchingSubset(false);
				break;
			case ONE:
				matcher = new ProteoformMatchingOne(true);
				break;
			case ONE_NO_TYPES:
				matcher = new ProteoformMatchingOne(false);
				break;
			case STRICT:
				matcher = new ProteoformMatchingStrict();
				break;
			case ACCESSION:
				matcher = new ProteoformMatchingAccession();
				break;
		}
		return matcher;
	}

}
