package matcher.tools;

import methods.matching.ProteoformMatching;
import model.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.xerces.impl.xpath.regex.Match;
import picocli.CommandLine;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static matcher.tools.FileHandler.createFile;

@CommandLine.Command(version = "PathwayMatcher 1.9.1")
public class Sensitivity implements Runnable {

    public static boolean matchesAtLeastOne(Proteoform proteoform, HashSet<Proteoform> potentialProteoforms, ProteoformMatching matcher, Long range) {
        for (Proteoform potentialProteoform : potentialProteoforms) {
            if (matcher.matches(proteoform, potentialProteoform, range)) {
                return true;
            }
        }
        return false;
    }

    public static HashMap<MatchType, Double> calculatePercentagesMatchesAtLeastOne(HashSet<Proteoform> inputProteoforms, Mapping mapping, Long range) {
        HashMap<MatchType, Double> percentages = new HashMap<>(MatchType.values().length);

        for (MatchType matchType : MatchType.values()) {
            ProteoformMatching proteoformMatching = ProteoformMatching.getInstance(matchType);
            int cont = 0;
            double percentage = 0.0;

            // Count number of input proteoforms that matches at least one proteoform in the database
            for (Proteoform proteoform : inputProteoforms) {
                HashSet<Proteoform> potentialProteoforms = getPotentialProteoforms(proteoform, mapping, PotentialProteoformsType.ALL, false);
                if (matchesAtLeastOne(proteoform, potentialProteoforms, proteoformMatching, 5L)) {
                    cont++;
                }
            }

            percentage = (double) cont * 100.0 / inputProteoforms.size();
            percentages.put(matchType, percentage);
        }
        return percentages;
    }

    public static void createTableMatchesAtLeastOne(HashSet<Proteoform> inputProteoforms, Mapping mapping, Long range, String path, String fileName) {
        if (inputProteoforms.size() == 0) {
            return;
        }
        try {
            BufferedWriter bufferedWriterOutput = createFile(path, fileName);
            HashMap<MatchType, ProteoformMatching> matchers = new HashMap<>();

            bufferedWriterOutput.write("PROTEOFORM");
            for (MatchType matchType : MatchType.values()) {
                bufferedWriterOutput.write("\t" + matchType.toString());
                matchers.put(matchType, ProteoformMatching.getInstance(matchType));
            }
            bufferedWriterOutput.write("\n");

            for (Proteoform proteoform : inputProteoforms) {
                bufferedWriterOutput.write(proteoform.toString(ProteoformFormat.SIMPLE));
                for (MatchType matchType : MatchType.values()) {
                    HashSet<Proteoform> potentialProteoforms = getPotentialProteoforms(proteoform, mapping, PotentialProteoformsType.ALL, false);
                    bufferedWriterOutput.write((matchesAtLeastOne(proteoform, potentialProteoforms, matchers.get(matchType), 5L) ? "\t1" : "\t0"));
                }
                bufferedWriterOutput.write("\n");
            }

            bufferedWriterOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    enum PotentialProteoformsType {
        ALL, ORIGINAL, OTHERS;
    }

    static String getModType(String residue) {
        switch (residue) {
            case "S":
                return "00046";
            case "T":
                return "00047";
            case "Y":
                return "00048";
            default:
                return "00000";
        }
    }

    static String getPTM(String phospholine) throws ParseException, NumberFormatException {
        String[] parts = phospholine.split(",");
        if (Integer.parseInt(parts[1]) <= 0) {
            throw new ParseException("Invalid site. Should be a positive integer.", 1);
        }
        return getModType(parts[2]) + ":" + parts[1];
    }

    static String getSimpleProteoformStringFromPhosphoModification(String phospholine) throws ParseException {
        String[] parts = phospholine.split(",");
        return parts[0] + ";" + getPTM(phospholine);
    }

    static HashSet<Proteoform> getProteoformSample(HashSet<Proteoform> proteoforms, double sampleSizeInPercentage) {

        List<Proteoform> proteoformList = new ArrayList<>(proteoforms);
        HashSet<Proteoform> result = new HashSet<>();

        int num_elements = (int) sampleSizeInPercentage * proteoforms.size() / 100;

        Random rand = new Random();
        for (int I = 0; I < num_elements; I++) {
            int n = rand.nextInt(proteoformList.size());
            result.add(proteoformList.get(n));
            proteoformList.remove(n);
        }

        return result;
    }

    static HashSet<Proteoform> createProteoformList(String fileName) {
        HashSet<Proteoform> result = new HashSet<>();
        int row = 1;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();

            while (line != null) {
                result.add(ProteoformFormat.SIMPLE.getProteoform(getSimpleProteoformStringFromPhosphoModification(line), row));
                line = br.readLine();
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    static List<Proteoform> createProteoformListAggregated(String fileName) throws ParseException {
        List<Proteoform> result = new ArrayList<>();
        int row = 1;
        Proteoform proteoform = new Proteoform("");
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();
            proteoform = ProteoformFormat.SIMPLE.getProteoform(getSimpleProteoformStringFromPhosphoModification(line), row);
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                row++;
                Proteoform currentProteoform = ProteoformFormat.SIMPLE.getProteoform(getSimpleProteoformStringFromPhosphoModification(line), row);
                if (proteoform.getUniProtAcc().equals(currentProteoform.getUniProtAcc())) {
                    proteoform.addPtm(currentProteoform.getPtms().get(0).getKey(), currentProteoform.getPtms().get(0).getValue());
                } else {
                    result.add(proteoform);
                    proteoform = ProteoformFormat.SIMPLE.getProteoform(getSimpleProteoformStringFromPhosphoModification(line), row);
                }
            }
            result.add(proteoform);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static HashSet<Proteoform> getModifiedProteoformsOfProteinsWithMultipleProteoforms(Mapping mapping) {
        HashSet<String> proteinsWithMultipleProteoforms = new HashSet<>();
        HashSet<Proteoform> result = new HashSet<>();
        for (String protein : mapping.getProteinsToProteoforms().keySet()) {
            if (mapping.getProteinsToProteoforms().get(protein).size() > 1) {
                for (Proteoform proteoform : mapping.getProteinsToProteoforms().get(protein)) {
                    if (proteoform.getPtms().size() > 0) {
                        result.add(proteoform);
                        proteinsWithMultipleProteoforms.add(protein);
                    }
                }
            }
        }
        return result;
    }

    static void writeEvaluation(Map<MatchType, Double> percentages, String path, String fileName) {
        try {
            BufferedWriter bufferedWriterOutput = createFile(path, fileName);
            bufferedWriterOutput.write("MatchType,Percentage\n");
            for (Map.Entry<MatchType, Double> matchTypeEntry : percentages.entrySet()) {
                bufferedWriterOutput.write(matchTypeEntry.getKey().toString() + "," + String.format("%.2f", matchTypeEntry.getValue()) + "\n");
            }
            bufferedWriterOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeEvaluation(List<Pair<MatchType, Double>> percentages, String path, String fileName) {
        try {
            BufferedWriter bufferedWriterOutput = createFile(path, fileName);
            bufferedWriterOutput.write("MatchType,Percentage\n");
            for (Pair<MatchType, Double> matchTypeEntry : percentages) {
                bufferedWriterOutput.write(matchTypeEntry.getKey().toString() + "," + String.format("%.2f", matchTypeEntry.getValue()) + "\n");
            }
            bufferedWriterOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeEvaluationSeparated(List<Pair<MatchType, Double>> percentagesOriginal,
                                         List<Pair<MatchType, Double>> percentagesOthers,
                                         String path, String fileName) {
        try {
            BufferedWriter bufferedWriterOutput = createFile(path, fileName);
            bufferedWriterOutput.write("MatchType,Percentage,Category\n");
            for (Pair<MatchType, Double> matchTypeEntry : percentagesOriginal) {
                bufferedWriterOutput.write(
                        matchTypeEntry.getKey().toString()
                                + "," + String.format("%.2f", matchTypeEntry.getValue())
                                + "," + PotentialProteoformsType.ORIGINAL.toString()
                                + "\n");
            }
            for (Pair<MatchType, Double> matchTypeEntry : percentagesOthers) {
                bufferedWriterOutput.write(
                        matchTypeEntry.getKey().toString()
                                + "," + String.format("%.2f", matchTypeEntry.getValue())
                                + "," + PotentialProteoformsType.OTHERS.toString()
                                + "\n");
            }
            bufferedWriterOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the proteoforms sharing the same protein accessions, that fullfill the requirements
     * in the parameters
     *
     * @param inputProteoform          The original proteoform used to find the potential proteoforms
     * @param mapping                  Static maps to go from proteins to proteoforms and see their modifications
     * @param potentialProteoformsType ALL proteoforms with the same accession, the ORIGINAL proteoform
     *                                 or OTHER proteoforms that are not the original but share the accession
     * @return Set of potential proteoforms
     */
    static HashSet<Proteoform> getPotentialProteoforms(Proteoform inputProteoform,
                                                       Mapping mapping,
                                                       PotentialProteoformsType potentialProteoformsType,
                                                       boolean onlyModified) {
        HashSet<Proteoform> potentialProteoforms = new HashSet<>();
        switch (potentialProteoformsType) {
            case ORIGINAL:
                if (!onlyModified || inputProteoform.getPtms().size() > 0) {
                    potentialProteoforms.add(inputProteoform);
                }
                break;
            case OTHERS:
                for (Proteoform potentialProteoform : mapping.getProteinsToProteoforms().get(inputProteoform.getUniProtAcc())) {
                    if (!potentialProteoform.equals(inputProteoform) && (!onlyModified || potentialProteoform.getPtms().size() > 0)) {
                        potentialProteoforms.add(potentialProteoform);
                    }
                }
                break;
            case ALL:
                for (Proteoform potentialProteoform : mapping.getProteinsToProteoforms().get(inputProteoform.getUniProtAcc())) {
                    if (!onlyModified || potentialProteoform.getPtms().size() > 0) {
                        potentialProteoforms.add(potentialProteoform);
                    }
                }
                break;
        }
        return potentialProteoforms;
    }

    /**
     * For proteoforms with
     *
     * @param proteoform
     * @return
     */
    public static Proteoform alterProteoform(Proteoform proteoform) {

        Proteoform newProteoform = new Proteoform(proteoform.getUniProtAcc());
        int numPTMs = proteoform.getPtms().size();

        if (numPTMs > 0) {
            int cont = 0;
            for (Pair<String, Long> ptm : proteoform.getPtms()) {
                if (cont == 0) {
                    newProteoform.addPtm("00000", (ptm.getValue() == null ? 5 : ptm.getValue() + 5));
                } else if (cont == 1) {
                    newProteoform.addPtm(ptm.getKey(), (ptm.getValue() == null ? 5 : ptm.getValue() + 5));
                } else {
                    newProteoform.addPtm(ptm.getKey(), ptm.getValue());
                }
                cont++;
            }
        }

        return newProteoform;
    }

    public static Proteoform alterProteoformSimple(Proteoform proteoform) {

        Proteoform newProteoform = new Proteoform(proteoform.getUniProtAcc());
        Random rand = new Random();
        int numPTMs = proteoform.getPtms().size();

        if (numPTMs > 0) {

            for (Pair<String, Long> ptm : proteoform.getPtms()) {
                newProteoform.addPtm(ptm.getKey(), ptm.getValue());
            }
            // Change one type
            int n = rand.nextInt(newProteoform.getPtms().size());
            Pair<String, Long> ptm = newProteoform.getPtms().get(n);
            newProteoform.getPtms().remove(n);
            ptm = new MutablePair<>("00000", ptm.getRight());
            newProteoform.addPtm(ptm.getKey(), ptm.getValue());

            // Change one site
            n = rand.nextInt(newProteoform.getPtms().size());
            Long coordinate = newProteoform.getPtms().get(n).getValue();
            newProteoform.getPtms().get(n).setValue(coordinate + 5);
        }

        return newProteoform;
    }

    /**
     * Calculate the percentage of modified candidate proteoforms for the input list that got matched for each matching criteria.
     *
     * @param inputProteoforms The list of proteoforms to match
     * @param sampleSize       Percentage of the input proteoforms to match
     * @param mapping          Static maps to go from proteins to proteoforms and see their modifications
     * @param range            Margin of error for the modification sites
     * @param alterProteoforms If the proteoforms in the input should be altered or not
     * @return Map with one percentage for each matching criteria
     */
    public static HashMap<MatchType, Double> calculateOneRunPercentages(
            HashSet<Proteoform> inputProteoforms,
            double sampleSize,
            Mapping mapping,
            PotentialProteoformsType potentialProteoformsType,
            boolean onlyModified,
            Long range,
            boolean alterProteoforms) {
        HashMap<MatchType, Double> totalPercentages = new HashMap<>(8);
        HashMap<MatchType, ProteoformMatching> matchers = new HashMap();
        int t = 1;

        if (onlyModified) {
            inputProteoforms = (HashSet<Proteoform>) inputProteoforms.stream()
                    .filter(proteoform -> proteoform.getPtms().size() > 0)
                    .collect(Collectors.toSet());
        }

        for (MatchType matchType : MatchType.values()) {
            matchers.put(matchType, ProteoformMatching.getInstance(matchType));
            totalPercentages.put(matchType, 0.0);
        }

        HashSet<Proteoform> proteoformSample = getProteoformSample(inputProteoforms, sampleSize);
        for (Proteoform proteoform : proteoformSample) {
            HashSet<Proteoform> potentialProteoforms = getPotentialProteoforms(proteoform, mapping,
                    potentialProteoformsType, onlyModified);
            if (potentialProteoforms.size() > 0) {
                for (MatchType matchType : MatchType.values()) {
                    int matched = 0;
                    for (Proteoform potentialProteoform : potentialProteoforms) {
                        if (matchers.get(matchType).matches((alterProteoforms) ? alterProteoform(proteoform) : proteoform, potentialProteoform, range)) {
                            matched++;
                        }
                    }
                    double percentage = matched * 100.0 / (double) potentialProteoforms.size();
                    totalPercentages.put(matchType, totalPercentages.get(matchType) + (percentage - totalPercentages.get(matchType)) / t);
                }
                t++;
            }
        }
        return totalPercentages;
    }

    /**
     * Take a sample of proteins from the list and evaluate multiple times the percentage of potential proteoforms
     * matched after altering the proteoforms.
     */
    public static List<Pair<MatchType, Double>> getAllRunsPercentages(HashSet<Proteoform> inputProteoforms,
                                                                      double sampleSize,
                                                                      Mapping mapping,
                                                                      PotentialProteoformsType potentialProteoformsType,
                                                                      boolean onlyModified,
                                                                      Long range,
                                                                      boolean alterProteoforms,
                                                                      int runs) {
        HashMap<MatchType, Double> oneRunPercentages = new HashMap<>(8);
        List<Pair<MatchType, Double>> allRunsPercentages = new ArrayList<>(runs * 8);

        for (int I = 0; I < runs; I++) {
            oneRunPercentages = calculateOneRunPercentages(inputProteoforms, sampleSize,
                    mapping, potentialProteoformsType, onlyModified, range, alterProteoforms);
            for (MatchType matchType : MatchType.values()) {
                allRunsPercentages.add(new MutablePair<>(matchType, oneRunPercentages.get(matchType)));
            }
        }
        return allRunsPercentages;
    }

    /**
     * The sensitivity class calculates the matching proteoforms for sets of proteoforms.
     */
    public static void main(String args[]) {
        CommandLine.run(new Sensitivity(), System.err, args);
    }

    Mapping mapping = null;
    String scriptPlotMatches = "src\\main\\r\\plotMatches.R";
    String scriptPlotPercentages = "src\\main\\r\\plotPercentages.R";
    String scriptPlotPercentagesSeparated = "src\\main\\r\\plotPercentagesSeparated.R";

    String matchesFile1 = "matchesFile1.csv";
    String plotFile1 = "plotMatches1.png";
    String matchesFile2 = "matchesFile2.csv";
    String plotFile2 = "plotMatches2.png";
    String percentagesFileMultiproteoforms = "percentagesFileMultiproteoforms.csv";
    String plotMultiproteoform = "plotMultiproteoforms.png";
    String phosphositesFile = "phosphosites.csv";
    String percentagesFilePhosphoproteoforms = "percentagesFilePhosphoproteoforms.csv";
    String plotPhosphoproteoforms = "plotPhosphoproteoforms.csv";
    String percentagesPhosphoproteoformsMatchAtLeastOne = "percentagesPhosphoproteoformsMatchAtLeastOne.csv";
    String tablePhosphoproteoformsMatchAtLeastOne = "tablePhosphoproteoformsMatchAtLeastOne.tsv";

    @CommandLine.Option(names = {"--resourcesPath"}, required = true, description = "Path for the output files with the proteoform matches and percentages")
    private static String resourcesPath = "";

    @CommandLine.Option(names = {"--plotsPath"}, required = true, description = "Path for plots")
    private static String plotsPath = "";

    @CommandLine.Option(names = {"--proteoform1"}, required = false, description = "Proteoform 1 string in simple format")
    private static String proteoform1 = "P01308;00087:53,00798:31,00798:43";

    @CommandLine.Option(names = {"--proteoform2"}, required = false, description = "Proteoform 2 string in simple format")
    private static String proteoform2 = "O43318;00047:184,00047:187";

    @Override
    public void run() {

        try {
            mapping = new Mapping(InputType.PROTEOFORM, false, "");
        } catch (FileNotFoundException e) {
            System.out.println("Could not load the static mapping.");
            e.printStackTrace();
        }

        if (!resourcesPath.endsWith("/")) {
            resourcesPath += "/";
        }

        // Phosphorylation dataset
        try {
            // Phosphorylation dataset: percentage experimental phosphosites that, when made into a proteoform, matched
            // to a proteoform in the database for each matching type
            HashSet<Proteoform> inputProteoforms = createProteoformList(resourcesPath + "\\" + phosphositesFile);
            // Filter the list of phosphosites to only those in proteins in Reactome
//            inputProteoforms = (HashSet<Proteoform>) inputProteoforms.stream()
//                    .filter(proteoform -> matchesAtLeastOne(
//                            proteoform,
//                            getPotentialProteoforms(proteoform, mapping, PotentialProteoformsType.ALL, false),
//                            ProteoformMatching.getInstance(MatchType.ACCESSION),
//                            5L))
//                    .collect(Collectors.toSet());
            HashMap<MatchType, Double> percentages = calculatePercentagesMatchesAtLeastOne(inputProteoforms, mapping, 5L);
            writeEvaluation(percentages, resourcesPath, percentagesPhosphoproteoformsMatchAtLeastOne);
            createTableMatchesAtLeastOne(inputProteoforms, mapping, 5L, resourcesPath, tablePhosphoproteoformsMatchAtLeastOne);

            // Phosphorylation dataset: calculate the match percentages for the candidate proteoform of the same accession
            List<Pair<MatchType, Double>> allRunsPercentages = getAllRunsPercentages(inputProteoforms,
                    100.0, mapping, PotentialProteoformsType.ALL, false, 5L,
                    false, 1);
            writeEvaluation(allRunsPercentages, resourcesPath, percentagesFilePhosphoproteoforms);
            Runtime.getRuntime().exec("Rscript --vanilla " + scriptPlotPercentages + " " + (resourcesPath + "\\" + percentagesFilePhosphoproteoforms) + " " + (plotsPath + "\\" + plotPhosphoproteoforms));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Plot single proteoform matches
        try {
            HashSet<Proteoform> inputProteoforms = new HashSet<>();
            inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform(proteoform1));
            List<Pair<MatchType, Double>> percentagesOriginal = getAllRunsPercentages(inputProteoforms, 100.0,
                    mapping, PotentialProteoformsType.ORIGINAL, true, 5L, false, 1);
            List<Pair<MatchType, Double>> percentagesOthers = getAllRunsPercentages(inputProteoforms, 100.0,
                    mapping, PotentialProteoformsType.OTHERS, true, 5L, false, 1);
            writeEvaluationSeparated(percentagesOriginal, percentagesOthers, resourcesPath, matchesFile1);
            Runtime.getRuntime().exec("Rscript --vanilla " + scriptPlotMatches + " " + (resourcesPath + "\\" + matchesFile1) + " " + (plotsPath + "\\" + plotFile1));
        } catch (ParseException e) {
            System.out.println("Proteoform 1 is invalid.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            HashSet<Proteoform> inputProteoforms = new HashSet<>();
            inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform(proteoform2));
            List<Pair<MatchType, Double>> percentagesOriginal = getAllRunsPercentages(inputProteoforms, 100.0,
                    mapping, PotentialProteoformsType.ORIGINAL, true, 5L, false, 1);
            List<Pair<MatchType, Double>> percentagesOthers = getAllRunsPercentages(inputProteoforms, 100.0,
                    mapping, PotentialProteoformsType.OTHERS, true, 5L, false, 1);
            writeEvaluationSeparated(percentagesOriginal, percentagesOthers, resourcesPath, matchesFile2);
            Runtime.getRuntime().exec("Rscript --vanilla " + scriptPlotMatches + " " + (resourcesPath + "\\" + matchesFile2) + " " + (plotsPath + "\\" + plotFile2));
        } catch (ParseException e) {
            System.out.println("Proteoform 2 is invalid.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Reactome proteins with multiple proteoforms: calculate match percentages for original and other proteoforms
        try {
            HashSet<Proteoform> inputProteoforms = getModifiedProteoformsOfProteinsWithMultipleProteoforms(mapping);
            List<Pair<MatchType, Double>> percentagesOriginal = getAllRunsPercentages(inputProteoforms, 10.0,
                    mapping, PotentialProteoformsType.ORIGINAL, true, 5L, true, 10);
            List<Pair<MatchType, Double>> percentagesOthers = getAllRunsPercentages(inputProteoforms, 10.0,
                    mapping, PotentialProteoformsType.OTHERS, true, 5L, true, 10);
            writeEvaluationSeparated(percentagesOriginal, percentagesOthers, resourcesPath, percentagesFileMultiproteoforms);
            Runtime.getRuntime().exec("Rscript --vanilla " + scriptPlotPercentagesSeparated + " " + (resourcesPath + "\\" + percentagesFileMultiproteoforms) + " " + (plotsPath + "\\" + plotMultiproteoform));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

};
