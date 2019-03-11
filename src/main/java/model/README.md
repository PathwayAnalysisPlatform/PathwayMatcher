# Model

This Java project which is a module of [PathwayMatcher](https://github.com/PathwayAnalysisPlatform/PathwayMatcher).
It defines the set of classes needed to model the real entities handled in Pathway matcher.

The classes used to model the data handled are:
* [Reaction](https://github.com/PathwayAnalysisPlatform/Model/blob/master/src/main/java/no/uib/pap/model/Reaction.java)
* [Pathway](https://github.com/PathwayAnalysisPlatform/Model/blob/master/src/main/java/no/uib/pap/model/Pathway.java)
* [Proteoform](https://github.com/PathwayAnalysisPlatform/Model/blob/master/src/main/java/no/uib/pap/model/Proteoform.java)
* [ProteoformFormat](https://github.com/PathwayAnalysisPlatform/Model/blob/master/src/main/java/no/uib/pap/model/ProteoformFormat.java)
* [Snp](https://github.com/PathwayAnalysisPlatform/Model/blob/master/src/main/java/no/uib/pap/model/Snp.java)
* [Role](https://github.com/PathwayAnalysisPlatform/Model/blob/master/src/main/java/no/uib/pap/model/Role.java)

This classes also define the rules and logic to compare instances of this classes and decide if they
are the same object.

The module also contains a set of classes that support the functionality of PathwayMatcher itself,
rather than modeling biological objects:
* InputPatterns
* Mapping
* MatchType
* MessageStatus
* Error
* Warning

