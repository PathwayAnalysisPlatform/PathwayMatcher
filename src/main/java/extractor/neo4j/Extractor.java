package extractor.neo4j;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import model.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.driver.v1.Record;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static model.Error.ERROR_READING_VEP_TABLES;
import static model.Error.sendError;
import static org.apache.commons.io.FilenameUtils.separatorsToSystem;

/**
 * This module gathers reference biological data necessary to perform pathway search and analysis,
 * and creates static mapping files that are loaded during execution of PathwayMatcher.
 */
@Command(version = "PathwayMatcher 1.9.0")
public class Extractor implements Runnable {

    @Option(names = {"-u", "--user", "--username"}, description = "Username to log in to Neo4j")
    private static String username = "";

    @Option(names = {"-p", "--pass", "--password"}, description = "Password corresponding to the username for Neo4j.")
    private static String password = "";

    @Option(names = {"-d", "--directory"}, description = "Path to directory where vep tables are.")
    private static String vepFilesPath = "";

    @Option(names = {"-o", "--output"}, description = "Path to directory for the output files(static maps).")
    private static String outputPath = "";

    @Option(names = {"-v", "--version"}, versionHelp = true, description = "Show version information and exit")
    boolean versionInfoRequested;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help message and quits.")
    private boolean usageHelpRequested = false;

    // Specific classes are defined for proteoforms, reactions and pathways to have more than one attribute.
    // Objects like genes, sets, complexes and proteins don't need a separate class, because the
    // identifier is the only attribute used.

    private static ImmutableMap<String, Reaction> reactions; // Reaction stId to Reaction displayName
    private static ImmutableMap<String, Pathway> pathways; // Pathway stId to Pathway instance
    private static ImmutableMap<String, String> proteinsToNames; // Protein accession (UniProt) to name
    private static ImmutableSetMultimap<String, String> genesToProteins = null;
    private static ImmutableSetMultimap<String, String> ensemblToProteins = null;
    private static ImmutableSetMultimap<String, String> physicalEntitiesToReactions = null;
    private static ImmutableSetMultimap<String, Proteoform> physicalEntitiesToProteoforms = null;
    private static ImmutableSetMultimap<Proteoform, String> proteoformsToPhysicalEntities = null;
    private static ImmutableSetMultimap<String, String> proteinsToReactions = null;
    private static ImmutableSetMultimap<String, String> reactionsToPathways = null;
    private static ImmutableSetMultimap<String, String> pathwaysToTopLevelPathways = null;
    private static ImmutableSetMultimap<String, Proteoform> proteinsToProteoforms = null;
    private static ImmutableSetMultimap<Proteoform, String> proteoformsToReactions = null;
    private static ImmutableSetMultimap<String, String> proteinsToComplexes = null;
    private static ImmutableSetMultimap<String, String> complexesToProteins = null;
    private static ImmutableSetMultimap<String, String> setsToProteins = null;
    private static ImmutableSetMultimap<String, String> proteinsToSets = null;
    private static ImmutableSetMultimap<Proteoform, String> proteoformsToComplexes = null;
    private static ImmutableSetMultimap<String, Proteoform> complexesToProteoforms = null;
    private static ImmutableSetMultimap<Proteoform, String> proteoformsToSets = null;
    private static ImmutableSetMultimap<String, Proteoform> setsToProteoforms = null;

    private static ImmutableSetMultimap<String, String> rsIdsToProteins = null; // An array of multimaps, one for each chromosome. Added one extra to use natural 1-based numbering.
    private static ImmutableSetMultimap<Long, String> chrBpToProteins = null;

    private static final int rsidColumnIndex = 2;
    // private static final int ensemblColumnIndex = 4;
    private static final int swissprotColumnIndex = 5;
    // private static final int nearestGeneColumnIndex = 7;

    public static void main(String[] args) {
        CommandLine.run(new Extractor(), System.err, args);
    }

    @Override
    public void run() {

        //        System.out.println("The working directory is: " + System.getProperty("user.dir"));

        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", username, password);

        File directory = new File(outputPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        proteinsToReactions = getProteinsToReactions();
        System.out.println("Finished map proteins to reactions.");

        for (int chr = 1; chr <= 22; chr++) {
            rsIdsToProteins = getRsIdsToProteins(chr);
            System.out.println("Finished map rsids to proteins, chromosome " + chr);
        }

        for (int chr = 1; chr <= 22; chr++) {
            chrBpToProteins = getChrBpToProteins(chr);
            System.out.println("Finished map chrBp to proteins, chromosome " + chr);
        }

        genesToProteins = getGenesToProteinsReactome();
        System.out.println("Finished map genes to proteins.");

        ensemblToProteins = getEnsemblToProteins();
        System.out.println("Finished map ensembl to proteins.");

        proteoformsToReactions = getProteoformsToReactions();
        System.out.println("Finished map proteoforms to reactions.");

        reactionsToPathways = getReactonsToPathways();
        System.out.println("Finished map reactions to pathways.");

        pathwaysToTopLevelPathways = getPathwaysToTopLevelPathways();
        System.out.println("Finished map pathways to top level pathways.");

        proteinsToNames = getProteinNames();
        System.out.println("Finished getting the protein names.");

        proteinsToComplexes = getProteinsToComplexes();
        System.out.println("Finished map proteins to complexes.");

        complexesToProteins = getComplexesToProteins();
        System.out.println("Finished map of complexes to proteins.");

        proteoformsToComplexes = getProteoformsToComplexes();
        System.out.println("Finished map of proteoforms to complexes");

        complexesToProteoforms = getComplexesToProteoforms();
        System.out.println("Finished map of complexes to proteoforms");

        setsToProteins = getSetsToProteins();
        System.out.println("Finished map of sets to proteins.");

        proteinsToSets = getProteinsToSets();
        System.out.println("Finished map proteins to sets");

        setsToProteoforms = getSetsToProteoforms();
        System.out.println("Finished map sets to proteoforms");

        proteinsToProteoforms = getProteinsToProteoforms();
        System.out.println("Finished map proteins to proteoforms.");

        System.exit(0);
    }

    private static void getComplexComponents() {

        if (physicalEntitiesToProteoforms == null) {
            getPhysicalEntitiesToProteoforms();
        }

        ImmutableSetMultimap.Builder<String, String> builderComplexesToProteins = new ImmutableSetMultimap.Builder<>();
        ImmutableSetMultimap.Builder<String, String> builderProteinsToComplexes = new ImmutableSetMultimap.Builder<>();
        ImmutableSetMultimap.Builder<String, Proteoform> builderComplexesToProteoforms = new ImmutableSetMultimap.Builder<>();
        ImmutableSetMultimap.Builder<Proteoform, String> builderProteoformsToComplexes = new ImmutableSetMultimap.Builder<>();

        //Query the database to fill the data structure
        List<Record> resultList = ConnectionNeo4j.query(ReactomeQueries.GET_COMPLEX_COMPONENTS);
        for (Record record : resultList) {
            builderComplexesToProteins.put(record.get("complex").asString(), record.get("protein").asString());
            builderProteinsToComplexes.put(record.get("protein").asString(), record.get("complex").asString());

            for (Proteoform proteoform : physicalEntitiesToProteoforms.get(record.get("physicalEntity").asString())) {
                builderComplexesToProteoforms.put(record.get("complex").asString(), proteoform);
                builderProteoformsToComplexes.put(proteoform, record.get("complex").asString());
            }
        }

        complexesToProteins = builderComplexesToProteins.build();
        proteinsToComplexes = builderProteinsToComplexes.build();
        complexesToProteoforms = builderComplexesToProteoforms.build();
        proteoformsToComplexes = builderProteoformsToComplexes.build();

        storeSerialized(complexesToProteins, outputPath + "complexesToProteins.gz");
        storeSerialized(proteinsToComplexes, outputPath + "proteinsToComplexes.gz");
        storeSerialized(complexesToProteoforms, outputPath + "complexesToProteoforms.gz");
        storeSerialized(proteoformsToComplexes, outputPath + "proteoformsToComplexes.gz");
    }

    public static ImmutableSetMultimap<String, String> getComplexesToProteins() {
        if (complexesToProteins == null) {
            getComplexComponents();
        }

        return complexesToProteins;
    }


    public static ImmutableSetMultimap<String, String> getProteinsToComplexes() {
        if (proteinsToComplexes == null) {
            getComplexComponents();
        }
        return proteinsToComplexes;
    }

    public static ImmutableSetMultimap<String, Proteoform> getComplexesToProteoforms() {
        if (complexesToProteoforms == null) {
            getComplexComponents();
        }

        return complexesToProteoforms;
    }

    public static ImmutableSetMultimap<Proteoform, String> getProteoformsToComplexes() {
        if (proteoformsToComplexes == null) {
            getComplexComponents();
        }
        return proteoformsToComplexes;
    }

    public static ImmutableSetMultimap<String, String> getSetMembersAndCandidates() {
        if (physicalEntitiesToProteoforms == null) {
            getPhysicalEntitiesToProteoforms();
        }

        ImmutableSetMultimap.Builder<String, String> builderSetsToProteins = new ImmutableSetMultimap.Builder<>();
        ImmutableSetMultimap.Builder<String, String> builderProteinsToSets = new ImmutableSetMultimap.Builder<>();
        ImmutableSetMultimap.Builder<String, Proteoform> builderSetsToProteoforms = new ImmutableSetMultimap.Builder<>();
        ImmutableSetMultimap.Builder<Proteoform, String> builderProteoformsToSets = new ImmutableSetMultimap.Builder<>();

        //Query the database to fill the data structure
        List<Record> resultList = ConnectionNeo4j.query(ReactomeQueries.GET_SET_MEMBERS_AND_CANDIDATES);
        for (Record record : resultList) {
            builderSetsToProteins.put(record.get("set").asString(), record.get("protein").asString());
            builderProteinsToSets.put(record.get("protein").asString(), record.get("set").asString());
            for (Proteoform proteoform : physicalEntitiesToProteoforms.get(record.get("physicalEntity").asString())) {
                builderSetsToProteoforms.put(record.get("set").asString(), proteoform);
                builderProteoformsToSets.put(proteoform, record.get("set").asString());
            }
        }

        setsToProteins = builderSetsToProteins.build();
        proteinsToSets = builderProteinsToSets.build();
        setsToProteoforms = builderSetsToProteoforms.build();
        proteoformsToSets = builderProteoformsToSets.build();

        storeSerialized(setsToProteins, outputPath + "setsToProteins.gz");
        storeSerialized(proteinsToSets, outputPath + "proteinsToSets.gz");
        storeSerialized(setsToProteoforms, outputPath + "setsToProteoforms.gz");
        storeSerialized(proteoformsToSets, outputPath + "proteoformsToSets.gz");

        return setsToProteins;
    }

    public static ImmutableSetMultimap<String, String> getSetsToProteins() {
        if (setsToProteins == null) {
            getSetMembersAndCandidates();
        }

        return setsToProteins;
    }


    public static ImmutableSetMultimap<String, String> getProteinsToSets() {
        if (proteinsToSets == null) {
            getSetMembersAndCandidates();
        }
        return proteinsToSets;
    }

    public static ImmutableSetMultimap<String, Proteoform> getSetsToProteoforms() {
        if (setsToProteoforms == null) {
            getSetMembersAndCandidates();
        }

        return setsToProteoforms;
    }

    public static ImmutableSetMultimap<Proteoform, String> getProteoformsToSets() {
        if (proteoformsToSets == null) {
            getSetMembersAndCandidates();
        }
        return proteoformsToSets;
    }


    public static ImmutableSetMultimap<String, String> getRsIdsToProteins(int chr) {

        if (proteinsToReactions == null) {
            getProteinsToReactions();
        }

        ImmutableSetMultimap.Builder<String, String> builderRsIdsToProteins = ImmutableSetMultimap.builder();

        // Traverse all the vepTables

        System.out.println("Scanning vepTable for chromosome " + chr);
        try {
            BufferedReader br = getBufferedReaderForGzipFile(separatorsToSystem(vepFilesPath) + chr + ".gz");
            br.readLine(); // Read header line

            for (String line; (line = br.readLine()) != null; ) {

                Multimap<Snp, String> snpToSwissprotMap = getSNPAndSwissProtFromVep(line);

                for (Map.Entry<Snp, String> snpToSwissprotPair : snpToSwissprotMap.entries()) {
                    Snp snp = snpToSwissprotPair.getKey();
                    String protein = snpToSwissprotPair.getValue();

                    if (!protein.equals("NA")) {
                        if (proteinsToReactions.containsKey(protein)) {
                            builderRsIdsToProteins.put(snp.getRsid(), protein);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            if (vepFilesPath.length() == 0) {
                System.out.println("Tried reading vep tables at: " + System.getProperty("user.dir"));
            } else {
                System.out.println("Tried reading vep tables at: " + vepFilesPath);
            }
            sendError(ERROR_READING_VEP_TABLES, chr);
        }


        rsIdsToProteins = builderRsIdsToProteins.build();

        storeSerialized(rsIdsToProteins, outputPath + "rsIdsToProteins" + chr + ".gz");

        return rsIdsToProteins;
    }

    public static ImmutableSetMultimap<Long, String> getChrBpToProteins(int chr) {

        if (proteinsToReactions == null) {
            getProteinsToReactions();
        }

        ImmutableSetMultimap.Builder<Long, String> builderChrBpToProteins = ImmutableSetMultimap.builder();

        // Traverse all the vepTables

        System.out.println("Scanning vepTable for chromosome " + chr);
        try {
            BufferedReader br = getBufferedReaderForGzipFile(separatorsToSystem(vepFilesPath) + chr + ".gz");
            br.readLine(); // Read header line

            for (String line; (line = br.readLine()) != null; ) {

                Multimap<Snp, String> snpToSwissprotMap = getSNPAndSwissProtFromVep(line);

                for (Map.Entry<Snp, String> snpToSwissprotPair : snpToSwissprotMap.entries()) {
                    Snp snp = snpToSwissprotPair.getKey();
                    String protein = snpToSwissprotPair.getValue();

                    if (!protein.equals("NA")) {
                        if (proteinsToReactions.containsKey(protein)) {
                            builderChrBpToProteins.put(snp.getBp(), protein);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            sendError(ERROR_READING_VEP_TABLES, chr);
        }

        chrBpToProteins = builderChrBpToProteins.build();

        storeSerialized(chrBpToProteins, outputPath + "chrBpToProteins" + chr + ".gz");
        return chrBpToProteins;
    }

    public static ImmutableSetMultimap<String, Proteoform> getProteinsToProteoforms() {

        // Make sure the full list of proteoforms is loaded
        if (proteoformsToPhysicalEntities == null) {
            getPhysicalEntitiesToProteoforms();
        }

        ImmutableSetMultimap.Builder<String, Proteoform> builderProteinsToProteoforms = ImmutableSetMultimap.builder();

        //Traverse the list of proteoforms to get the protein accessions
        for (Proteoform proteoform : proteoformsToPhysicalEntities.keySet()) {
            builderProteinsToProteoforms.put(proteoform.getUniProtAcc(), proteoform);
        }

        proteinsToProteoforms = builderProteinsToProteoforms.build();
        storeSerialized(proteinsToProteoforms, outputPath + "proteinsToProteoforms.gz");

        return proteinsToProteoforms;
    }

    static ImmutableSetMultimap<String, Proteoform> getPhysicalEntitiesToProteoforms() {

        ImmutableSetMultimap.Builder<String, Proteoform> builderPhysicalEntitiesToProteoforms = ImmutableSetMultimap.builder();
        ImmutableSetMultimap.Builder<Proteoform, String> builderProteoformsToPhysicalEntities = ImmutableSetMultimap.builder();

        List<Record> resultList = ConnectionNeo4j.query(ReactomeQueries.GET_MAP_PROTEOFORMS_TO_PHYSICALENTITIES);
        for (Record record : resultList) {

            // Create the proteoform instance using the isoform and ptms.
            String isoform = record.get("isoform").asString();
            List<Pair<String, Long>> ptms = new ArrayList<>();

            for (Object ptm : record.get("ptms").asList()) {
                String[] parts = ptm.toString().split(":");
                String mod = parts[0];
                Long site = Proteoform.interpretCoordinateFromStringToLong(parts[1]);
                ptms.add(new MutablePair<>(mod, site));
            }

            Proteoform proteoform = new Proteoform(isoform, ptms);

            for (Object physicalEntity : record.get("peSet").asList()) {
                builderPhysicalEntitiesToProteoforms.put(physicalEntity.toString(), proteoform);
                builderProteoformsToPhysicalEntities.putAll(proteoform, physicalEntity.toString());
            }
        }

        physicalEntitiesToProteoforms = builderPhysicalEntitiesToProteoforms.build();
        proteoformsToPhysicalEntities = builderProteoformsToPhysicalEntities.build();

        return physicalEntitiesToProteoforms;
    }

    static ImmutableSetMultimap<Proteoform, String> getProteoformsToPhysicalEntities() {
        if (proteoformsToPhysicalEntities == null) {
            getPhysicalEntitiesToProteoforms();
        }
        return proteoformsToPhysicalEntities;
    }

    /**
     * Get list of reactions
     */
    public static ImmutableMap<String, Reaction> getReactions() {

        if (physicalEntitiesToProteoforms == null) {
            getPhysicalEntitiesToProteoforms();
        }

        ImmutableMap.Builder<String, Reaction> builderReactions = ImmutableMap.builder();

        // Fill the reactions stId and displayName
        List<Record> resultList = ConnectionNeo4j.query(ReactomeQueries.GET_ALL_REACTIONS);
        for (Record record : resultList) {
            Reaction reaction = new Reaction(record.get("stId").asString(), record.get("displayName").asString());
            builderReactions.put(record.get("stId").asString(), reaction);
        }

        reactions = builderReactions.build();

        // Fill the reaction participants
        resultList = ConnectionNeo4j.query(ReactomeQueries.GET_REACTION_PARTICIPANTS);
        for (Record record : resultList) {
            String role = record.get("role").asString().toUpperCase();
            if (!reactions.containsKey(record.get("reaction").asString())) {
                System.out.println("Missing reaction: " + record.get("reaction").asString());
            }
            reactions.get(record.get("reaction").asString()).addParticipant(record.get("protein").asString(), Role.valueOf(role));
            for (Proteoform proteoform : physicalEntitiesToProteoforms.get(record.get("physicalEntity").asString())) {
                reactions.get(record.get("reaction").asString()).addParticipant(proteoform, Role.valueOf(role));
            }
        }

        // Serialize list of reactions
        storeSerialized(reactions, outputPath + "reactions.gz");
        return reactions;
    }

    /**
     * Get list of pathways
     */
    public static ImmutableMap<String, Pathway> getPathways() {

        ImmutableMap.Builder<String, Pathway> builderPathways = ImmutableMap.builder();

        // Query the database and fill the data structure
        List<Record> resultList = ConnectionNeo4j.query(ReactomeQueries.GET_ALL_PATHWAYS);
        for (Record record : resultList) {

            // Get first the attributes that can be obtained directly
            Pathway pathway = new Pathway(record.get("stId").asString(), record.get("displayName").asString());
            pathway.setNumEntitiesTotal(record.get("numEntitiesTotal").asInt());
            pathway.setNumReactionsTotal(record.get("numReactionsTotal").asInt());
            pathway.setNumReactionsTotal(record.get("numProteoformsTotal").asInt());
            builderPathways.put(pathway.getStId(), pathway);
        }

        pathways = builderPathways.build();

        // Serialize pathways
        storeSerialized(pathways, outputPath + "pathways.gz");
        return pathways;
    }

    public static ImmutableSetMultimap<String, String> getGenesToProteinsReactome() {

        // Query the database and fill the data structure
        ImmutableSetMultimap.Builder<String, String> builderGenesToProteins = ImmutableSetMultimap.builder();
        List<Record> resultList = ConnectionNeo4j.query(ReactomeQueries.GET_MAP_GENES_TO_PROTEINS);
        for (Record record : resultList) {
            builderGenesToProteins.put(record.get("gene").asString(), record.get("protein").asString());
        }
        genesToProteins = builderGenesToProteins.build();

        storeSerialized(genesToProteins, outputPath + "genesToProteins.gz");
        return genesToProteins;
    }

    public static ImmutableSetMultimap<String, String> getEnsemblToProteins() {

        // Query the database and fill the data structure
        ImmutableSetMultimap.Builder<String, String> builderEnsemblToProteins = ImmutableSetMultimap
                .<String, String>builder();
        List<Record> resultList = ConnectionNeo4j.query(ReactomeQueries.GET_MAP_ENSEMBL_TO_PROTEINS);
        for (Record record : resultList) {
            builderEnsemblToProteins.put(record.get("ensembl").asString(), record.get("uniprot").asString());
        }
        ensemblToProteins = builderEnsemblToProteins.build();

        storeSerialized(ensemblToProteins, outputPath + "ensemblToProteins.gz");
        return ensemblToProteins;
    }

    /**
     * Get physical entities to reactions mapping
     */
    static ImmutableSetMultimap<String, String> getPhysicalEntitiesToReactions() {

        ImmutableSetMultimap.Builder<String, String> builderPhysicalEntitiesToReactions = ImmutableSetMultimap
                .<String, String>builder();

        List<Record> resultList = ConnectionNeo4j.query(ReactomeQueries.GET_MAP_PHYSICALENTITIES_TO_REACTIONS);
        for (Record record : resultList) {
            builderPhysicalEntitiesToReactions.put(
                    record.get("physicalEntity").asString(),
                    record.get("reaction").asString()
            );
        }

        physicalEntitiesToReactions = builderPhysicalEntitiesToReactions.build();
        return physicalEntitiesToReactions;
    }


    /**
     * Creates mapping from proteins to proteoforms and mapping from proteoforms to reactions.
     * Note that the proteins are identified by uniprot accession without the isoform, but the proteoforms include the isoform.
     *
     * @return
     */
    static ImmutableSetMultimap<Proteoform, String> getProteoformsToReactions() {
        if (physicalEntitiesToReactions == null) {
            getPhysicalEntitiesToReactions();
        }
        if (proteoformsToPhysicalEntities == null) {
            getPhysicalEntitiesToProteoforms();
        }

        ImmutableSetMultimap.Builder<Proteoform, String> builderProteoformsToReactions = ImmutableSetMultimap
                .builder();

        for (Map.Entry<Proteoform, String> proteoformToPhysicalEntity : proteoformsToPhysicalEntities.entries()) {

            for (String reaction : physicalEntitiesToReactions.get(proteoformToPhysicalEntity.getValue())) {
                builderProteoformsToReactions.put(proteoformToPhysicalEntity.getKey(), reaction);
            }
        }

        proteoformsToReactions = builderProteoformsToReactions.build();

        storeSerialized(proteoformsToReactions, outputPath + "proteoformsToReactions.gz");

        return proteoformsToReactions;
    }

    /**
     * Get mapping from proteins to reactions
     */
    static ImmutableSetMultimap<String, String> getProteinsToReactions() {

        if (reactions == null) {
            getReactions();
        }

        // Query the database and fill the data structure
        ImmutableSetMultimap.Builder<String, String> builderProteinsToReactions = ImmutableSetMultimap
                .builder();
        List<Record> resultList = ConnectionNeo4j.query(ReactomeQueries.GET_MAP_PROTEINS_TO_REACTIONS);

        for (Record record : resultList) {
            if (record.get("reaction").asString().equals("null")) {
                builderProteinsToReactions.putAll(record.get("protein").asString(), new ArrayList<String>());
            } else {
                builderProteinsToReactions.put(record.get("protein").asString(), record.get("reaction").asString());
            }
        }

        proteinsToReactions = builderProteinsToReactions.build();
        storeSerialized(proteinsToReactions, outputPath + "proteinsToReactions.gz");
        return proteinsToReactions;
    }

    public static ImmutableMap<String, String> getProteinNames() {
        ImmutableMap.Builder<String, String> proteinsBuilder = ImmutableMap.<String, String>builder();

        String name = "";
        String query = ReactomeQueries.GET_PROTEIN_NAMES;
        List<Record> resultList = ConnectionNeo4j.query(query);

        for (Record record : resultList) {
            for (Object entry : record.get("description").asList()) {
//                System.out.println("----" + entry.toString() + "---");
                name = record.get("description").asList().toString();
                String[] parts = name.split("(recommendedName:)|(alternativeName:)|(component recommendedName:)");
                if (parts.length <= 1) {
                    name = record.get("displayName").asString();
                } else {
                    name = parts[1].trim();
                }
                proteinsBuilder.put(record.get("identifier").asString(), name);
//                System.out.println(name + "\n");
                break;
            }
        }

        proteinsToNames = proteinsBuilder.build();
        storeSerialized(proteinsToNames, outputPath + "proteinsToNames.gz");
        return proteinsToNames;
    }

    /**
     * Get mapping from reactions to pathways
     */
    private static ImmutableSetMultimap<String, String> getReactonsToPathways() {

        if (reactions == null) {
            getReactions();
        }
        if (pathways == null) {
            getPathways();
        }

        // Query the database and fill the data structure
        ImmutableSetMultimap.Builder<String, String> builderReactionsToPathways = ImmutableSetMultimap
                .<String, String>builder();
        List<Record> resultList = ConnectionNeo4j.query(ReactomeQueries.GET_MAP_REACTIONS_TO_PATHWAYS);
        for (Record record : resultList) {
            builderReactionsToPathways.put(record.get("reaction").asString(), record.get("pathway").asString());
        }

        reactionsToPathways = builderReactionsToPathways.build();

        storeSerialized(reactionsToPathways, outputPath + "reactionsToPathways.gz");
        return reactionsToPathways;
    }

    /**
     * Get mapping from pathways to top level pathways
     */
    static ImmutableSetMultimap<String, String> getPathwaysToTopLevelPathways() {

        if (pathways.size() == 0) {
            getPathways();
        }

        // Query the database and fill the data structure
        ImmutableSetMultimap.Builder<String, String> builderPathwaysToTopLevelPathways = ImmutableSetMultimap
                .<String, String>builder();
        List<Record> resultList = ConnectionNeo4j.query(ReactomeQueries.GEP_MAP_PATHWAYS_TO_TOPLEVELPATHWAYS);
        for (Record record : resultList) {
            builderPathwaysToTopLevelPathways.put(record.get("pathway").asString(),
                    record.get("topLevelPathway").asString());
        }

        pathwaysToTopLevelPathways = builderPathwaysToTopLevelPathways.build();

        storeSerialized(pathwaysToTopLevelPathways, outputPath + "pathwaysToTopLevelPathways.gz");
        return pathwaysToTopLevelPathways;
    }

    public static Multimap<Snp, String> getSNPAndSwissProtFromVep(String line) {
        ImmutableSetMultimap.Builder<Snp, String> builder = ImmutableSetMultimap.<Snp, String>builder();
        String[] fields = line.split(" ");
        Integer chr = Integer.valueOf(fields[0]);
        Long bp = Long.valueOf(fields[1]);

        String[] rsids = fields[rsidColumnIndex].split(",");
        String[] uniprots = fields[swissprotColumnIndex].split(",");

        for (String rsid : rsids) {
            for (String uniprot : uniprots) {
                Snp snp = new Snp(chr, bp, rsid);
                builder.put(snp, uniprot);
            }
        }
        return builder.build();
    }

    static BufferedReader getBufferedReaderForGzipFile(String fileName) throws FileNotFoundException, IOException {

        InputStream fileStream = new FileInputStream(fileName);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, Charset.defaultCharset());
        return new BufferedReader(decoder);
    }

    static BufferedReader getBufferedReaderFromResource(String fileName) throws FileNotFoundException, IOException {

        BufferedReader br = null;
        InputStream fileStream = ClassLoader.getSystemResourceAsStream(fileName);
        Reader decoder = null;
        if (fileName.endsWith(".gz")) {
            InputStream gzipStream = new GZIPInputStream(fileStream);
            decoder = new InputStreamReader(gzipStream);
        } else {
            decoder = new InputStreamReader(fileStream);
        }
        br = new BufferedReader(decoder);

        return br;
    }

    private static void storeSerialized(Object obj, String fileName) {
        FileOutputStream fos = null;
        GZIPOutputStream gz;
        ObjectOutputStream oos;
        try {
            fos = new FileOutputStream(fileName);
            gz = new GZIPOutputStream(fos);
            oos = new ObjectOutputStream(gz);
            oos.writeObject(obj);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
