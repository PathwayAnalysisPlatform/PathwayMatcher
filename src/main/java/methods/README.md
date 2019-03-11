# Methods

This Java project which is a module of [PathwayMatcher](https://github.com/PathwayAnalysisPlatform/PathwayMatcher).
It defines the procedures to do proteoform matching, search for pathways and do pathway analysis.

## Proteoform matching

There are a set of rules to decide if a proteoform in the input sample for PathwayMatcher can be
considered the same proteoform in the reference data (Reactome). 

PathwayMatcher allows the user to select multiple modes depending on the needs:
* [superset](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#superset): the input proteoform PTMs are superset of the PTMs in the reference proteoform
* [subset](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#subset): the input proteoform PTMs are subset of the PTMs in the reference proteoform
* [one](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#one): the reference proteoform has at least one input PTM; or the reference has no PTMs
* [superset_no_types](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#superset): similar to _superset_ ignoring the modification types
* [subset_no_types](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#subset): : similar to _subset_ ignoring the modification types
* [one_no_types](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#one): similar to _one_ ignoring the modification types
* [strict](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#strict): the input and reference proteoforms must match exactly in all the attributes.

The [classes](https://github.com/PathwayAnalysisPlatform/Methods/tree/master/src/main/java/no/uib/pap/methods/matching) implementing that functionality are located [here](https://github.com/PathwayAnalysisPlatform/Methods/tree/master/src/main/java/no/uib/pap/methods/matching). For mode details on each of this modes please consult this [wiki](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching) page.

## Pathway search

The [procedure](https://github.com/PathwayAnalysisPlatform/Methods/blob/master/src/main/java/no/uib/pap/methods/search/Search.java) to find the set of pathways, with their respective reactions, that contain as participants at least one of the entities(genes, proteins, proteoforms...) in the input sample.

## Pathway analysis

The java [classes](https://github.com/PathwayAnalysisPlatform/Methods/tree/master/src/main/java/no/uib/pap/methods/analysis/ora) that define methods to decide which pathways are more statistically significant to the sample. The implemented method is Over Representation Analysis<sup>[\[1\]](#references)</sup>, assuming that the input list of entities (genes, proteins,...) are differentially expressed and were selected using a cut off threshold.

This set of classes can be extended to include other methods for pathway analysis
by following a similar structure with the same input and output data structures.

## References

\[1\] [García-Campos, M. A., Espinal-Enríquez, J. & Hernández-Lemus, E. Pathway Analysis: State of the Art. Frontiers in Physiology 6, doi:10.3389/fphys.2015.00383 (2015).](https://www.frontiersin.org/articles/10.3389/fphys.2015.00383/full)