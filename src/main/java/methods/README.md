# Methods

This Java project which is a module of [PathwayMatcher](https://github.com/PathwayAnalysisPlatform/PathwayMatcher).
It defines the procedures to do proteoform matching, search for pathways, and conduct pathway analysis.

## Proteoform matching

Multiple rules allow tuning the matching of proteoforms between the input and the reference data of PathwayMatcher. 

* [superset](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#superset): the PTMs in the input proteoform are a superset of the PTMs in the reference proteoform
* [subset](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#subset): the PTMs in the input proteoform are a subset of the PTMs in the reference proteoform
* [one](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#one): the reference proteoform has at least one PTM of the input proteoform, or the reference has no PTMs
* [superset_no_types](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#superset): similar to _superset_ ignoring the modification types
* [subset_no_types](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#subset): similar to _subset_ ignoring the modification types
* [one_no_types](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#one): similar to _one_ ignoring the modification types
* [strict](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching#strict): the input and reference proteoforms match exactly for all attributes.

The [classes](https://github.com/PathwayAnalysisPlatform/Methods/tree/master/src/main/java/no/uib/pap/methods/matching) implementing these functionalities are located [here](https://github.com/PathwayAnalysisPlatform/Methods/tree/master/src/main/java/no/uib/pap/methods/matching). For mode details on each of this modes, please refer to our [wiki](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/Proteoform-matching).

## Pathway search

The [procedure](https://github.com/PathwayAnalysisPlatform/Methods/blob/master/src/main/java/no/uib/pap/methods/search/Search.java) to find the set of pathways, with their respective reactions, that contain as participants at least one of the entities (genes, proteins, or proteoforms) in the input sample.

## Pathway analysis

These [classes](https://github.com/PathwayAnalysisPlatform/Methods/tree/master/src/main/java/no/uib/pap/methods/analysis/ora) provide methods to assess which pathways are overrepresented in the results. The method implemented is an Over Representation Analysis<sup>[\[1\]](#references)</sup>, assuming that the entities provided as input are differentially abundant and were selected using a cut off threshold.

This set of classes can be extended to include other methods for pathway analysis by following a similar structure with the same input and output data structures. Please use our [issue tracker](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/issues) to suggest possible enhancements.

## References

\[1\] [García-Campos, M. A., Espinal-Enríquez, J. & Hernández-Lemus, E. Pathway Analysis: State of the Art. Frontiers in Physiology 6, doi:10.3389/fphys.2015.00383 (2015).](https://www.frontiersin.org/articles/10.3389/fphys.2015.00383/full)
