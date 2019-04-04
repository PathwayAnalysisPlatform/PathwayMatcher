# Base Image
FROM anapsix/alpine-java

# Maintainer
MAINTAINER Luis Francisco Hernández Sánchez <Luis.Sanchez@uib.no>

# Metadata
LABEL base.image="ubuntu:16:04"
LABEL version="1.9.0"
LABEL software="PathwayMatcher"
LABEL software.version="20190310"
LABEL description="An open source mapper from omics data to Pathways and Reactions in Reactome"
LABEL website="https://github.com/PathwayAnalysisPlatform/PathwayMatcher"
LABEL documentation="https://github.com/PathwayAnalysisPlatform/PathwayMatcher/wiki"
LABEL license="Apache License 2.0: https://github.com/PathwayAnalysisPlatform/PathwayMatcher/blob/master/LICENSE.txt"
LABEL tags="Pathway Analysis, Biomolecular Pathways, Proteomics, Genetics"


ADD PathwayMatcher.jar /home/PathwayMatcher.jar

#This is the default command to be run
ENTRYPOINT ["java","-jar","/home/PathwayMatcher.jar"]

#This are the defailt arguments for the entrypoint command
CMD ["match-uniprot"]