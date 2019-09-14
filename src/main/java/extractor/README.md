# Extractor Module

This is a command line program in Java, which is a module of [PathwayMatcher](https://github.com/PathwayAnalysisPlatform/PathwayMatcher).

This module gathers reference biological data necessary to perform pathway search and analysis, 
and creates static mapping files that are used during the execution of PathwayMatcher.

The extractor has two main components, one for the mapping of genetic variants, and the other 
to map proteins and proteoforms to pathways.

![Image of reference data extraction process](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/figures/wiki/extraction.png)

The mappings needed for the pathway search are:

![Image of static mappings for pathway search](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/figures/wiki/static_mappings_pathway_search.png)
* SNP --> Gene name
* SNP --> Protein
* Protein --> Proteoforms
* Protein --> Reactions
* Proteoform --> Reactions
* Reactions --> Pathways
* Pathways --> Top Level Pathways

The necessary mappings for the interaction networks are:
![Image of static mappings for protein interaction networks](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/figures/wiki/static_mappings_protein_interaction_networks.png)


![Image of static mappings for proteoform interaction networks](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki/figures/wiki/static_mappings_proteoform_interaction_networks.png)

## Genetic variants

* [VepFolderProcessor](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/blob/master/src/main/java/extractor/vep/VepFolderProcessor.java): Creates table files with the mapping of genetic variants to gene names and protein UniProt <sup>[\[1\]](#references)</sup> accessions using the Variant Effect Predictor <sup>[\[2\]](#references)</sup>.

#### Input:
No file is needed as input.

#### Output: 
Tables with the mapping from genetic variants to gene names and SwissProt entries (UniProt). One table for each chromosome: _1.gz, 2.gz,...,22.gz_

## Reactions and Pathways
* [Extractor](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/blob/master/src/main/java/extractor/neo4j/Extractor.java): Creates the mapping files to go from gene names, proteins and proteoforms to reactions and pathways of Reactome <sup>[\[3\]](#references)</sup>.

#### Set up requirements:
* Running instance of Neo4j with the Reactome [graph database](https://reactome.org/dev/graph-database) loaded.

#### Input:
Tables generated with [VepFolderProcessor](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/blob/master/src/main/java/extractor/vep/VepFolderProcessor.java): _1.gz, 2.gz,...,22.gz_ 

#### Output:
Serialized files ready to be used by [PathwayMatcher](https://github.com/PathwayAnalysisPlatform/PathwayMatcher):
* Entity lists:
  * _proteins.gz_
  * _reactions.gz_
  * _pathways.gz_
  
* Static mappings for pathway search:  
  * Pairs of chromosome and base pair to protein UniProt accessions: _chrBpToProteins1.gz,...,chrBpToProteins22.gz_
  * SNP rsIds to UniProt protein accessions: _rsIdsToProteins1.gz,..., rsIdsToProteins22.gz_
  * Gene names to UniProt protein accessions: _genesToProteins.gz_
  * Ensembl protein identifiers to UniProt protein accessions: _ensemblToProteins.gz_  
  * UniProt protein accessions to proteoforms: _proteinsToProteoforms.gz_
  * UniProt protein accessions to reactions: _proteinsToReactions.gz_   
  * Proteoforms to reactions: _proteoformsToReactions.gz_   
  * Pathways to [top level pathways](https://reactome.org/content/schema/TopLevelPathway): _pathwaysToTopLevelPathways.gz_
  
* Static mappings for interaction networks:
  * UniProt protein accessions to [complexes](https://reactome.org/content/schema/Complex) they can form: _proteinsToComplexes.gz_
  * UniProt protein accessions to [entity sets](https://reactome.org/content/schema/EntitySet): _proteinsToSets.gz_
  * Proteoforms to [complexes](https://reactome.org/content/schema/Complex): _proteoformsToComplexes.gz_
  * Proteoforms to [entity sets](https://reactome.org/content/schema/EntitySet): _proteoformsToSets.gz_

## Peptides

* [ExtractorPeptides](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/blob/master/src/main/java/extractor/peptides/ExtractorPeptides.java) This class gathers the 'Proteotypic Peptide' set from ProteomeTools<sup>[\[4\]](#references)</sup> in a single list file. 
    
    This is an extra command line application that was used as support during the development 
process of PathwayMatcher. It is not needed for the main functionality.

## Protein modifications 
 
* [ExtractorPsiMod](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/blob/master/src/main/java/extractor/psimod/ExtractorPsiMod.java): Http client application to gather the available modifications
from the PSI-MOD<sup>[\[5\]](#references)</sup> community standard for representation of protein modifications. 

    This is also an extra command line application not needed for the main functionality, but useful 
in case a user wants to get the list of available modifications programmatically.

# Running Extractor

Extractor is part of the [PathwayMatcher.jar](https://github.com/PathwayAnalysisPlatform/PathwayMatcher/releases) file. It can be executed with this command:
~~~~
java -cp PathwayMatcher.jar extractor.neo4j.Extractor -d path_to_vep_files/ -o vNN/
~~~~

It has the following command line arguments: 

| Argument | Description | Required
| --- | --- | -- |
| `-u,--user,--username <username>` | Username to log in to Neo4j | No |
| `-p,--pass,--password <password>` | Password corresponding to the username for Neo4j. | No |
| `-d,--directory <vepFilesPath>`| Path to directory where vep tables are | No |
| `-o,--output <file>` | Output file path and name | No |
| `-h, --help` | Print usage and available arguments | No |
| `-v, --version` | Print version of PathwayMatcher | No |

Note: The paths must end with the separator '/' (linux) or '\' (windows).
By default the input and output files are in the same location as the _PathwayMatcher.jar_ file. 

The VEP mapping files can be downloaded from the [MappingFiles](https://github.com/PathwayAnalysisPlatform/MappingFiles) repository in [this](https://github.com/PathwayAnalysisPlatform/MappingFiles/tree/master/Extractor) folder.

It is required to have Neo4j installed and running with the Reactome Graph database loaded. 

* Click [here](https://neo4j.com/docs/operations-manual/current/installation/) for instructions to [download](https://neo4j.com/download-center/#panel2-2) and [install Neo4j](https://neo4j.com/docs/operations-manual/current/installation/).
* [Reactome graph database](https://reactome.org/download-data).

 
 # References
 \[1\] [UniProt Consortium, T. UniProt: the universal protein knowledgebase. Nucleic acids research 46, 2699-2699, doi:10.1093/nar/gky092 %J Nucleic Acids Research (2018).
](https://academic.oup.com/nar/article/46/5/2699/4841658) <br>
\[2\] [McLaren, W. et al. The Ensembl Variant Effect Predictor. Genome Biology 17, 122, doi:10.1186/s13059-016-0974-4 (2016).
](https://genomebiology.biomedcentral.com/articles/10.1186/s13059-016-0974-4)<br>
\[3\] [Fabregat, A. et al. The Reactome Pathway Knowledgebase. Nucleic acids research 46, D649-d655, doi:10.1093/nar/gkx1132 (2018).](https://academic-oup-com.pva.uib.no/nar/article/46/D1/D649/4626770)<br>
 \[4\] [Desiere, et al., "The PeptideAtlas Project", Nucleic Acids Research, 2006, 34, D655-D658](http://www.peptideatlas.org/publications/PA_NAR_2006.pdf)<br>
 \[5\] [Montecchi-Palazzi, L. et al. The PSI-MOD community standard for representation of protein modification data. Nature Biotechnology 26, 864, doi:10.1038/nbt0808-864 (2008).
](https://www.nature.com/articles/nbt0808-864)
