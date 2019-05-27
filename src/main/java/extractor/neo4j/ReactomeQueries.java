package extractor.neo4j;

public interface ReactomeQueries {
    public static final String GET_MAP_GENES_TO_PROTEINS = "MATCH (ewas:EntityWithAccessionedSequence{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWITH re.identifier as protein, re.geneName as genes\nWHERE size(genes) > 0  \nUNWIND genes as gene\nRETURN DISTINCT gene, protein";

    public static final String GET_MAP_ENSEMBL_TO_PROTEINS = "MATCH (ewas:EntityWithAccessionedSequence{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nUNWIND re.otherIdentifier as ensembl\nWITH DISTINCT ensembl, re.identifier as uniprot\nWHERE ensembl STARTS WITH \"ENS\"\nRETURN ensembl, uniprot";

    public static final String GET_ALL_PROTEINS = "MATCH (pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\nRETURN DISTINCT re.identifier as protein";

    public static final String GET_COUNT_ALL_PROTEINS = "MATCH (pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\nRETURN count(DISTINCT re.identifier) as count";

    public static final String GET_ALL_PROTEINS_WITH_ISOFORMS = "MATCH (pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\n" +
            "RETURN DISTINCT (CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END) as Identifiers";

    public static final String GET_COUNT_ALL_PROTEINS_WITH_ISOFORMS = "MATCH (pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\n" +
            "RETURN count(DISTINCT CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END) as count";

    public static final String GET_ALL_REACTIONS = "MATCH (n:Reaction{speciesName:\"Homo sapiens\"}) \nRETURN DISTINCT n.stId as stId, n.displayName as displayName";

    public static final String GET_COUNT_ALL_REACTIONS = "MATCH (n:Reaction{speciesName:\"Homo sapiens\"}) RETURN count(DISTINCT n) as count";

    public static final String GET_MAP_PROTEINS_TO_REACTIONS = "MATCH (pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\nWITH pe, re\nOPTIONAL MATCH (r:Reaction{speciesName:\"Homo sapiens\"})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)\nRETURN DISTINCT re.identifier as protein, r.stId AS reaction";

    public static final String GET_MAP_PHYSICALENTITIES_TO_REACTIONS = "MATCH (r:Reaction{speciesName:\"Homo sapiens\"})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})\nRETURN DISTINCT pe.stId as physicalEntity, r.stId AS reaction";

    public static final String GET_ALL_PATHWAYS = "MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWITH DISTINCT  pe, re\nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWITH DISTINCT \n  pe,\n  re.identifier AS protein,\n  re.variantIdentifier AS isoform,\n  tm.coordinate as coordinate, \n  mod.identifier as type ORDER BY type, coordinate\nWITH DISTINCT   \n  pe,\n  protein,\n  isoform,\n  COLLECT(CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END + \":\" + type) AS ptms \n WITH DISTINCT   \n  pe,\n  protein,\n  COLLECT(CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END + ptms) as proteoform\n  MATCH (p:Pathway{speciesName:\"Homo sapiens\"})-[:hasEvent*]->(rle:Reaction{speciesName: \"Homo sapiens\"})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe)\nRETURN DISTINCT p.stId AS stId, p.displayName as displayName, count(DISTINCT rle) as numReactionsTotal, count(DISTINCT protein) as numEntitiesTotal, count(DISTINCT proteoform) as numProteoformsTotal\n  ORDER BY stId";

    public static final String GET_COUNT_ALL_PATHWAYS = "MATCH (p:Pathway{speciesName:\"Homo sapiens\"}) RETURN count(DISTINCT p) as count";

    public static final String GET_MAP_REACTIONS_TO_PATHWAYS = "MATCH (p:Pathway{speciesName:\"Homo sapiens\"})-[:hasEvent*]->(rle:Reaction{speciesName: \"Homo sapiens\"})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\nRETURN DISTINCT p.stId AS pathway, rle.stId as reaction";

    public static final String GEP_MAP_PATHWAYS_TO_TOPLEVELPATHWAYS = "MATCH (tlp:TopLevelPathway{speciesName:'Homo sapiens'})-[:hasEvent*]->(p:Pathway{speciesName:'Homo sapiens'})\n" +
            "RETURN DISTINCT p.stId AS pathway, tlp.stId as topLevelPathway";

    public static final String GET_MAP_PROTEOFORMS_TO_PHYSICALENTITIES = "MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWITH DISTINCT pe, re\nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWITH DISTINCT pe.stId AS physicalEntity,\n                re.identifier AS protein,\n                re.variantIdentifier AS isoform,\n                tm.coordinate as coordinate, \n                mod.identifier as type ORDER BY type, coordinate\nWITH DISTINCT physicalEntity,\n\t\t\t\tprotein,\n                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,\n                COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms\n                RETURN protein, isoform, ptms, collect(physicalEntity) as peSet\n                ORDER BY isoform, ptms";

    static final String GET_HIT_COUNT_BY_PROTEIN_PROTEOFORMS = "MATCH (p:Pathway{speciesName:\"Homo sapiens\"})-[:hasEvent*]->(rle:Reaction{speciesName: \"Homo sapiens\"})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt', identifier:\"P31749\"})\nWITH DISTINCT  p, rle, pe, re\nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWITH DISTINCT \n  p.stId as pathway,\n  rle.stId as reaction,\n  pe.stId as pe,\n  re.identifier AS protein,\n  re.variantIdentifier AS isoform,\n  tm.coordinate as coordinate, \n  mod.identifier as type ORDER BY type, coordinate\nWITH DISTINCT  pathway, reaction, \n  pe,\n  protein,\n  isoform,\n  COLLECT(CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END + \":\" + type) AS ptms \n WITH DISTINCT pathway, reaction, protein, COLLECT(CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END + ptms) as proteoform\n  RETURN  DISTINCT   count(pathway), count(reaction), protein, proteoform\n  ORDER BY proteoform";

    static final String GET_MAPPING_BY_PROTEIN_LIST = "MATCH (p:Pathway{speciesName:\"Homo sapiens\"})-[:hasEvent*]->(rle:Reaction{speciesName:\"Homo sapiens\"}),\n      (rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"}),\n      (pe)-[:referenceEntity]->(re:ReferenceEntity{databaseName:\"UniProt\"})\n      WHERE re.identifier IN [\"P01308\"]\nRETURN DISTINCT re.identifier, rle.stId, rle.displayName, p.stId, p.displayName\nORDER BY rle.stId";

    static final String GET_MAPPING_BY_PROTEOFORMS_BY_PROTEIN = "MATCH (p:Pathway{speciesName:\"Homo sapiens\"})-[:hasEvent*]->(r:Reaction{speciesName: \"Homo sapiens\"})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWITH DISTINCT p, r, pe, re\nWHERE re.identifier = \"O00186\"\nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWITH DISTINCT p, r, pe.stId AS physicalEntity,\n                re.identifier AS protein,\n                re.variantIdentifier AS isoform,\n                tm.coordinate as coordinate, \n                mod.identifier as type ORDER BY type, coordinate\nWITH DISTINCT p, r, physicalEntity,\n\t\t\t\tprotein,\n                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,\n                COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms\nRETURN DISTINCT p.stId as pathway, p.displayName, r.stId as reaction, r.displayName, (CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END + ptms) as proteoform       \n                ORDER BY proteoform";

    static final String GET_PROTEOFORMS_BY_ACCESSION = "MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWHERE re.identifier = \"P08123\"\nWITH DISTINCT pe, re\nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWITH DISTINCT pe, re, tm.coordinate as coordinate, mod.identifier as type \nORDER BY type, coordinate\nWITH DISTINCT pe, re, COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms\nRETURN DISTINCT re.identifier as accession, CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END as isoform, collect(DISTINCT pe.stId) as equivalentPe, ptms";

    static final String GET_NUMBER_PROTEOFORMS_BY_ACCESSION = "MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\n" +
            "WITH DISTINCT pe, re\n" +
            "OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\n" +
            "WITH DISTINCT pe, re, tm.coordinate as coordinate, mod.identifier as type \n" +
            "ORDER BY type, coordinate\n" +
            "WITH DISTINCT pe, re, COLLECT(CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END + \":\" + type) AS ptms\n" +
            "WITH DISTINCT re.identifier as accession, CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END as isoform, collect(DISTINCT pe.stId) as equivalentPe, ptms\n" +
            "WITH DISTINCT accession, {isoform: isoform, ptms: ptms} as proteoform\n" +
            "RETURN accession, count(proteoform) as num_proteoforms";

    static final String GET_TLPMAPPING_BY_PROTEOFORMS_BY_PROTEIN = "MATCH (p:Pathway{speciesName:\"Homo sapiens\"})-[:hasEvent*]->(rle:Reaction{speciesName: \"Homo sapiens\"})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWHERE re.identifier = \"P01308\"\nWITH DISTINCT p, rle, pe, re\nOPTIONAL MATCH (tlp:TopLevelPathway{speciesName:\"Homo sapiens\"})-[:hasEvent*]->(p:Pathway{speciesName:\"Homo sapiens\"})\nWITH DISTINCT CASE WHEN tlp IS NOT NULL THEN tlp ELSE p END as tlp, p, rle, pe, re\nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWITH DISTINCT tlp, p, rle, pe.stId AS physicalEntity,\n                re.identifier AS protein,\n                re.variantIdentifier AS isoform,\n                tm.coordinate as coordinate, \n                mod.identifier as type ORDER BY type, coordinate\nWITH DISTINCT tlp, p, rle, physicalEntity,\n\t\t\t\tprotein,\n                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,\n                COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms\nRETURN DISTINCT tlp.stId as tlp, p.stId as pathway, rle.stId as reaction, (CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END + ptms) as proteoform       \n                ORDER BY proteoform";

    static final String GET_GENES_BY_PATHWAY = "MATCH (pathway:Pathway{speciesName:\"Homo sapiens\"})-[:hasEvent*]->(rle:Reaction{speciesName:\"Homo sapiens\"}),\n      (rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWITH pathway, rle, re.identifier as protein, re.geneName as genes\nWHERE size(genes) > 0 AND pathway.stId IN [\"R-HSA-111995\", \"R-HSA-74749\"]\nUNWIND genes as gene\nWITH DISTINCT pathway, gene, protein\nRETURN DISTINCT pathway.stId, pathway.displayName, gene";

    static final String GET_PROTEINS_BY_PATHWAY = "MATCH (pathway:Pathway{speciesName:\"Homo sapiens\"})-[:hasEvent*]->(rle:Reaction{speciesName:\"Homo sapiens\"}),\n      (rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\n      WHERE pathway.stId IN [\"R-HSA-109703\",\"R-HSA-165160\"]\nRETURN DISTINCT pathway.stId, pathway.displayName, re.identifier";

    static final String GET_PROTEOFORMS_BY_PATHWAY = "MATCH (pathway:Pathway{speciesName:\"Homo sapiens\"})-[:hasEvent*]->(rle:Reaction{speciesName:\"Homo sapiens\"}),\n      (rle)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\n      WHERE pathway.stId IN [\"R-HSA-191647\", \"R-HSA-9032500\"]\nWITH DISTINCT pathway, rle, pe, re\nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWITH DISTINCT pathway, rle.stId as reaction, pe.stId AS physicalEntity,\n                re.identifier AS protein, re.variantIdentifier AS isoform,  tm.coordinate as coordinate, \n                mod.identifier as type \nORDER BY type, coordinate\nWITH DISTINCT pathway, reaction, physicalEntity, protein,\n                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,\n                COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms\nRETURN pathway.stId, pathway.displayName, reaction, isoform, ptms\nORDER BY isoform, ptms";

    // The name of the protein is independent of the isoforms. Therefore, all the isoforms will share the same name.
    static final String GET_PROTEIN_NAMES = "MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(ewas:PhysicalEntity{speciesName:'Homo sapiens'})\n" +
            "WHERE re.description IS NOT NULL\n" +
            "WITH re.identifier as identifier,collect(re.displayName) as displayNames, collect(re.description) as descriptions\n" +
            "RETURN identifier, head(displayNames) as displayName, head(descriptions) as description";

    static final String GET_REACTION_PARTICIPANTS = "MATCH p = (rle:Reaction{speciesName:'Homo sapiens'})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWITH *, relationships(p) as role\nWITH DISTINCT rle.stId as reaction, pe.stId as physicalEntity,re.identifier as protein, head(extract(x IN role | type(x))) as role ORDER BY role\nRETURN DISTINCT reaction, physicalEntity, protein, role\nORDER BY protein, reaction";

    // The members of a complex are only allocated in the hasComponent slot, but for the cases when members are sets, then also they are broken down with hasMember and hasCandidate relations
    static final String GET_COMPLEX_COMPONENTS = "MATCH p = (c:Complex{speciesName:'Homo sapiens'})-[:hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nRETURN DISTINCT c.stId as complex, pe.stId as physicalEntity, re.identifier as protein";

    static final String GET_PATH_FROM_COMPLEX_TO_PROTEIN = "MATCH p = (c:Complex{speciesName:'Homo sapiens', stId:'R-HSA-6811381'})-[:hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWHERE re.identifier = \"O00139\"\nRETURN c.stId as complex, extract(n IN nodes(p)| {id:n.stId, l:last(labels(n))}) AS steps, re.identifier as protein";

    // The components of a set are only allocated in the hasMember and hasCandidate slots, but for the cases when components or candidates are complexes, then also they are broken down with hasMember relation
    static final String GET_SET_MEMBERS_AND_CANDIDATES = "MATCH p = (s:EntitySet{speciesName:'Homo sapiens'})-[:hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nRETURN DISTINCT s.stId as set, pe.stId as physicalEntity, re.identifier as protein";

    static final String GET_MODIFICATIONS_ = "MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nRETURN DISTINCT mod.identifier as mod, mod.displayName as name, count(re) as count\nORDER BY count DESC";

    static final String GET_PTM_FREQUENCY_TABLE = "MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nRETURN DISTINCT mod.identifier as PSI_MOD_ID, mod.displayName as PSI_MOD_NAME, collect(DISTINCT re.identifier) as UNIPROT_ACCESSIONS, count(DISTINCT re.identifier) as UNIPROT_ACCESSIONS_COUNT\nORDER BY UNIPROT_ACCESSIONS_COUNT DESC";

    static final String GET_ALL_PROTEOFORMS = "MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWITH DISTINCT pe, re\nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWITH DISTINCT pe.stId AS physicalEntity,\n                re.identifier AS protein,\n                re.variantIdentifier AS isoform,\n                tm.coordinate as coordinate, \n                mod.identifier as type ORDER BY type, coordinate\nWITH DISTINCT physicalEntity,\n\t\t\t\tprotein,\n                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,\n                COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms\n                RETURN DISTINCT isoform, ptms\n                ORDER BY isoform, ptms";

    static final String GET_REACTION_PROTEOFORM_PARTICIPANTS_WITH_ROLE = "MATCH p = (rle:Reaction{speciesName: \"Homo sapiens\"})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWHERE rle.stId = \"R-HSA-419083\"\nWITH DISTINCT *, relationships(p) as role\nWITH DISTINCT rle, pe, re, head(extract(x IN role | type(x))) as role ORDER BY role\nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWITH DISTINCT rle, pe.stId AS physicalEntity,\n                re.identifier AS protein,\n                re.variantIdentifier AS isoform,\n                tm.coordinate as coordinate, \n                mod.identifier as type,\n                role\n                ORDER BY type, coordinate\nWITH DISTINCT rle, physicalEntity,\n\t\t\t\tprotein,\n                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,\n                COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms, role\nRETURN DISTINCT rle.stId as reaction, (CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END + ptms) as proteoform, role      \n                ORDER BY proteoform";

    static final String GET_REACTION_PROTEOFORM_PARTICIPANTS = "MATCH (p:Pathway{speciesName:\"Homo sapiens\"})-[:hasEvent*]->(rle:Reaction{speciesName: \"Homo sapiens\"})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWHERE rle.stId = \"R-HSA-419083\"\nWITH DISTINCT p, rle, pe, re\nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWITH DISTINCT p, rle, pe.stId AS physicalEntity,\n                re.identifier AS protein,\n                re.variantIdentifier AS isoform,\n                tm.coordinate as coordinate, \n                mod.identifier as type ORDER BY type, coordinate\nWITH DISTINCT p, rle, physicalEntity,\n\t\t\t\tprotein,\n                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,\n                COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms\nRETURN DISTINCT rle.stId as reaction, (CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END + ptms) as proteoform       \n                ORDER BY proteoform";

    static final String GET_COMPLEX_PROTEOFORM_COMPONENTS = "MATCH p = (c:Complex{speciesName:'Homo sapiens'})-[:hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWHERE c.stId = \"R-HSA-174138\"\nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWITH DISTINCT c.stId as complex,\n\t\t\t\tpe.stId AS physicalEntity,\n                re.identifier AS protein,\n                re.variantIdentifier AS isoform,\n                tm.coordinate as coordinate, \n                mod.identifier as type ORDER BY type, coordinate\nWITH DISTINCT complex, \n\t\t\t\tphysicalEntity, \n                protein,\n                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,\n                COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms\nRETURN DISTINCT complex, protein, (CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END + ptms) as proteoform";

    static final String GET_SET_PROTEOFORM_MEMBERS_AND_CANDIDATES = "MATCH p = (s:EntitySet{speciesName:'Homo sapiens'})-[:hasComponent|hasMember|hasCandidate*]->(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\n" +
            "WHERE s.stId = \"R-HSA-1008234\"\n" +
            "OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\n" +
            "WITH DISTINCT s.stId as set,\n" +
            "\t\t\t\tpe.stId AS physicalEntity,\n" +
            "                re.identifier AS protein,\n" +
            "                re.variantIdentifier AS isoform,\n" +
            "                tm.coordinate as coordinate, \n" +
            "                mod.identifier as type ORDER BY type, coordinate\n" +
            "WITH DISTINCT set, \n" +
            "\t\t\t\tphysicalEntity, \n" +
            "                protein,\n" +
            "                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,\n" +
            "                COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms\n" +
            "RETURN DISTINCT set, protein, (CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END + ptms) as proteoform";

    static final String GET_PROTEOFORMS_BY_PROTEIN = "MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\nWITH DISTINCT pe, re\nOPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWITH DISTINCT pe,\n                re.identifier AS protein,\n                re.variantIdentifier AS isoform,\n                tm.coordinate as coordinate, \n                mod.identifier as type ORDER BY type, coordinate\nWHERE protein = \"O00186\"\nWITH DISTINCT pe,\n\t\t\t\tprotein,\n                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,\n                COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms\nRETURN DISTINCT pe.displayName, isoform, ptms\n                ORDER BY isoform, ptms";

    static final String GET_NUM_PROTEOFORMS_PER_PROTEIN = "MATCH (pe:PhysicalEntity{speciesName:'Homo sapiens'})-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\n" +
            "WITH DISTINCT pe, re\n" +
            "OPTIONAL MATCH (pe)-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\n" +
            "WITH DISTINCT pe,\n" +
            "                re.identifier AS protein,\n" +
            "                re.variantIdentifier AS isoform,\n" +
            "                tm.coordinate as coordinate, \n" +
            "                mod.identifier as type ORDER BY type, coordinate\n" +
            "WITH DISTINCT pe,\n" +
            "\t\t\t\tprotein,\n" +
            "                CASE WHEN isoform IS NOT NULL THEN isoform ELSE protein END as isoform,\n" +
            "                COLLECT(type + \":\" + CASE WHEN coordinate IS NOT NULL THEN coordinate ELSE \"null\" END) AS ptms\n" +
            "WITH DISTINCT protein, {isoform: isoform, ptms: ptms} as proteoform\n" +
            "RETURN protein, count(proteoform) as num_proteoforms\n" +
            "ORDER BY num_proteoforms DESC";

    static final String GET_ALL_PHOSPHORYLATIONS = "MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\n" +
            "WHERE mod.displayName CONTAINS \"phospho\" OR mod.definition CONTAINS \"phospho\"\n" +
            "RETURN DISTINCT re.identifier as UNIPROT_ACCESSION, mod.displayName as modText, tm.coordinate as site,\n" +
            "CASE \n" +
            "WHEN mod.displayName CONTAINS \"serine\"\n" +
            "THEN \"S\"\n" +
            "WHEN mod.displayName CONTAINS \"threonine\"\n" +
            "THEN \"T\"\n" +
            "WHEN mod.displayName CONTAINS \"tyrosine\"\n" +
            "THEN \"Y\"\n" +
            "ELSE \"X\"\n" +
            "END\n" +
            "as PSI_MOD_ID\n" +
            "ORDER BY UNIPROT_ACCESSION";

    static final String GET_ALL_PHOSPHORYLATIONS_ANYTYPE_STY = "MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWHERE mod.displayName CONTAINS \"phospho\" AND (mod.definition CONTAINS \"serine\" OR mod.definition CONTAINS \"threonine\" OR mod.definition CONTAINS \"tyrosine\")\nRETURN DISTINCT re.identifier as UNIPROT_ACCESSION, mod.displayName as modText, tm.coordinate as site,\nCASE \nWHEN mod.displayName CONTAINS \"serine\"\nTHEN \"S\"\nWHEN mod.displayName CONTAINS \"threonine\"\nTHEN \"T\"\nWHEN mod.displayName CONTAINS \"tyrosine\"\nTHEN \"Y\"\nEND\nas PSI_MOD_ID\nORDER BY UNIPROT_ACCESSION";
    // 00046, 00047, 00048
    static final String GET_ALL_PHOSPHORYLATIONS_MAINTYPES_STY = "MATCH (re:ReferenceEntity{databaseName:'UniProt'})<-[:referenceEntity]-(pe:PhysicalEntity{speciesName:\"Homo sapiens\"})-[:hasModifiedResidue]->(tm:TranslationalModification)-[:psiMod]->(mod:PsiMod)\nWHERE mod.identifier in [\"00046\", \"00047\", \"00048\"]\nRETURN DISTINCT re.identifier as UNIPROT_ACCESSION, tm.coordinate as site,\nCASE \nWHEN mod.identifier = \"00046\"\nTHEN \"S\"\nWHEN mod.identifier = \"00047\"\nTHEN \"T\"\nELSE \"Y\"\nEND\nas PSI_MOD_ID\nORDER BY UNIPROT_ACCESSION";

}

