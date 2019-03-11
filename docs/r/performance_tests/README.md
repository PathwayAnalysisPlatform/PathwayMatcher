# Plots about proteins in Reactome

### Files

* Cypher queries: [queriesForStatistics.txt](https://github.com/LuisFranciscoHS/PathwayMatcher/blob/master/docs/queriesForStatistics.txt)
* Reactions and pathways mapped per protein: [HitsPerProtein.csv](https://github.com/LuisFranciscoHS/PathwayMatcher/blob/master/docs/resources/HitsPerProtein.csv.gz)
* Reactions and pathways mapped by each proteoform: [HitsPerProteoform.csv](https://github.com/LuisFranciscoHS/PathwayMatcher/blob/master/docs/resources/HitsPerProteoform.csv.gz)
* Reactions and pathways per protein/proteoform: [plotHits_v2.R](https://github.com/LuisFranciscoHS/PathwayMatcher/blob/master/docs/r/wiki_figures/plotHits_v2.R)
* Performance times: [times.csv](https://github.com/LuisFranciscoHS/PathwayMatcher/blob/master/docs/resources/times.csv)
* Performance plots: [makePerformancePlots.R](https://github.com/LuisFranciscoHS/PathwayMatcher/blob/master/docs/r/performance_tests/makePerformancePlots.R)

### Generate times

To generate the times use the class _PathwayMatcherSpeedTest_ inside _PathwayMatcher.jar_. To execute the class use the command:
~~~~
java -cp PathwayMatcher.jar no.uib.pap.pathwaymatcher.PathwayMatcherSpeedTest <parameters_file>
~~~~

The parameters file contains how many repetitions and sizes for each data type. The parameters file used is located at <PathwayMatcher_home>/resources/input/tests/ 
An example is:
~~~~
REPETITIONS	3
SAMPLE_SETS	30
WARMUP_OFFSET	1
ALL_PEPTIDES	resources/input/Peptides/AllPeptides.csv
ALL_PROTEINS	resources/input/Proteins/UniProt/uniprot-all.list
ALL_PROTEOFORMS	resources/input/ReactomeAllProteoformsSimple.csv
ALL_SNPS	extra/SampleDatasets/GeneticVariants/MoBa.csv
PROTEIN_SIZES	1	2000	4000	6000	8000	10000	12000	14000	16000	18000	20000
PROTEOFORM_SIZES	1	2000	4000	6000	8000	10000	12000	14000	16000	18000	20000
PEPTIDE_SIZES	1	20000	40000	60000	80000	100000	120000	140000	160000	180000	200000
SNPS_SIZES	200000	600000	100000 140000 1800000
~~~~

The class will generate a file called _times.csv_ It looks like this:
~~~~
Type,Sample,Size,ms,Repetition
UNIPROT,0,1,2166.983,1
UNIPROT,0,1,1197.578,2
UNIPROT,0,1,1536.749,3
...
PROTEOFORMS,9,20000,1783.052,2
PROTEOFORMS,9,20000,1722.111,3
PROTEOFORMS,9,20000,1797.966,4
PROTEOFORMS,9,20000,1713.655,5
...
PEPTIDES,18,200000,27280.150,4
PEPTIDES,18,200000,26605.677,5
PEPTIDES,19,1,22840.945,1
PEPTIDES,19,1,22743.989,2
...
RSIDS,0,1800000,55313.708,3
RSIDS,1,200000,22924.123,1
~~~~


