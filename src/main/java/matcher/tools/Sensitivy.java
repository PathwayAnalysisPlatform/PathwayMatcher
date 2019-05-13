package matcher.tools;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.Files;
import methods.matching.ProteoformMatching;
import methods.search.Search;
import model.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.xerces.impl.xpath.regex.Match;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;

public class Sensitivy {

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

    static String getSimpleProteoformString(String phospholine) throws ParseException {
        String[] parts = phospholine.split(",");
        return parts[0] + ";" + getPTM(phospholine);
    }

    static void comparePhosphosites(String file_all_phosphosites, String file_reactome_phosphosites) throws IOException {
        // Check how many of the phosphosites appear in Reactome
        List<String> phosphosites = Files.readLines(new File(file_all_phosphosites), Charset.forName("ISO-8859-1"));
        List<String> phosphosites_reactome = Files.readLines(new File(file_reactome_phosphosites), Charset.forName("ISO-8859-1"));
        HashSet<String> set_reactome = new HashSet<>(phosphosites_reactome);
        HashSet<String> set_found = new HashSet<>();

        for (String str : phosphosites) {
            if (set_reactome.contains(str)) {
                set_found.add(str);
//                System.out.println(str);
            }
        }
        System.out.println("Found: " + set_found.size() + " of " + phosphosites.size());
    }

    static HashSet<Proteoform> createProteoformList(String fileName) throws ParseException {
        HashSet<Proteoform> result = new HashSet<>();
        int row = 1;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();

            while (line != null) {
                result.add(ProteoformFormat.SIMPLE.getProteoform(getSimpleProteoformString(line), row));
                line = br.readLine();
                row++;
            }
        } catch (IOException e) {
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
            proteoform = ProteoformFormat.SIMPLE.getProteoform(getSimpleProteoformString(line), row);
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                row++;
                Proteoform currentProteoform = ProteoformFormat.SIMPLE.getProteoform(getSimpleProteoformString(line), row);
                if (proteoform.getUniProtAcc().equals(currentProteoform.getUniProtAcc())) {
                    proteoform.addPtm(currentProteoform.getPtms().get(0).getKey(), currentProteoform.getPtms().get(0).getValue());
                } else {
                    result.add(proteoform);
                    proteoform = ProteoformFormat.SIMPLE.getProteoform(getSimpleProteoformString(line), row);
                }
            }
            result.add(proteoform);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static HashSet<Proteoform> readProteoforms(String fileName) {
        HashSet<Proteoform> proteoforms = new HashSet<>();
        BufferedReader br;
        int row = 1;
        try {
            br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();
            proteoforms.add(ProteoformFormat.SIMPLE.getProteoform(line, row));
        } catch (
                IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return proteoforms;
    }

    static void writeEvaluation(HashMap<MatchType, Double> percentages, String fileName) {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(fileName));
            output.write("MatchType,Percentage,Category\n");
            for (Map.Entry<MatchType, Double> matchTypeEntry : percentages.entrySet()) {
                output.write(matchTypeEntry.getKey().toString() + "," + String.format("%.2f", matchTypeEntry.getValue()) + ",hit\n");
                output.write(matchTypeEntry.getKey().toString() + "," + String.format("%.2f", 100.0 - matchTypeEntry.getValue()) + ",miss\n");
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeEvaluation(Multimap<MatchType, Double> percentages, String fileName) {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(fileName));
            output.write("MatchType,Percentage,Category\n");
            for (Map.Entry<MatchType, Double> matchTypeEntry : percentages.entries()) {
                output.write(matchTypeEntry.getKey().toString() + "," + String.format("%.2f", matchTypeEntry.getValue()) + ",hit\n");
                output.write(matchTypeEntry.getKey().toString() + "," + String.format("%.2f", 100.0 - matchTypeEntry.getValue()) + ",miss\n");
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static HashMap<MatchType, Double> evaluateProteoforms(String inputFile, Mapping mapping) throws IOException {
        return evaluateProteoforms(readProteoforms(inputFile), mapping);
    }

    static HashSet<Proteoform> getProteinsWithMultipleProteoforms(Mapping mapping) {
        HashSet<Proteoform> result = new HashSet<>();

        for (String protein : mapping.getProteinsToProteoforms().keySet()) {
            if (mapping.getProteinsToProteoforms().get(protein).size() > 1) {
                for (Proteoform proteoform : mapping.getProteinsToProteoforms().get(protein)) {
                    if (proteoform.getPtms().size() > 0) {
                        result.add(proteoform);
                    }
                }
            }
        }

        return result;
    }

    public static Proteoform alterProteoform(Proteoform proteoform) {

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

    public static Collection<Proteoform> alterProteoforms(Collection<Proteoform> proteoforms) {

        Random rand = new Random();

        for (Proteoform proteoform : proteoforms) {
            int numPTMs = proteoform.getPtms().size();

            if (numPTMs > 0) {
                // Change one type
                int n = rand.nextInt(proteoform.getPtms().size());
                Pair<String, Long> ptm = proteoform.getPtms().get(n);
                proteoform.getPtms().remove(n);
                ptm = new MutablePair<>("00000", ptm.getRight());
                proteoform.getPtms().add(ptm);

                // Change one site
                n = rand.nextInt(proteoform.getPtms().size());
                Long coordinate = proteoform.getPtms().get(n).getValue();
                proteoform.getPtms().get(n).setValue(coordinate + 15);
            }
        }
        return proteoforms;
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

    /**
     * Calculates the percentage of potential proteoforms that were matched
     * using the input proteoforms with each type of matching.
     * <p>
     * Input: List of proteoforms to match to the possible potential proteoforms.
     * Output: File with percentages of proteoforms matched for each type of matching type
     *
     * @param inputProteoforms     Proteoforms to be matched
     * @param potentialProteoforms Options to match each single input proteoform
     * @throws IOException
     */
    static HashMap<MatchType, Double> evaluateProteoforms(HashSet<Proteoform> inputProteoforms, HashSet<Proteoform> potentialProteoforms) {
        HashSet<Proteoform> hitProteoforms = new HashSet<>();
        Long range = 0L;
        HashMap<MatchType, Double> percentages = new HashMap<>(7);

        for (MatchType matchType : MatchType.values()) {
            ProteoformMatching matcher = ProteoformMatching.getInstance(matchType);
            // Count the number of selected proteoforms
            for (Proteoform potentialProteoform : potentialProteoforms) {
                for (Proteoform inputProteoform : inputProteoforms) {
                    if (matcher.matches(inputProteoform, potentialProteoform, range)) {
                        hitProteoforms.add(potentialProteoform);
                        break;
                    }
                }
            }
            System.out.println("Finished matching with " + matchType.toString());
            // Write number to file
            double percentageCovered = hitProteoforms.size() * 100.0 / potentialProteoforms.size();
            percentages.put(matchType, percentageCovered);
        }
        return percentages;
    }

    /**
     * Calculates in multiple runs the percentage of potential proteoforms that were matched using the input proteoforms
     * for each matching type.
     * <p>
     * Input: Proteoforms to be matched and the possible potential proteoforms to match.
     * Output: File with percentages of proteoforms matched for each type of matching type in each run.
     *
     * @param sampleSizeInPercentage Percentage of proteins selected from all the proteins with multiple proteoforms
     * @param runs                   Number samples to be taken
     */
    static Multimap<MatchType, Double> evaluateProteoforms(HashSet<Proteoform> inputProteoforms,
                                                           HashSet<Proteoform> potentialProteoforms,
                                                           int runs,
                                                           double sampleSizeInPercentage) {
        Multimap<MatchType, Double> percentages = TreeMultimap.create();
        HashMap<MatchType, Double> runPercentages = new HashMap<>(7);

        for (int I = 0; I < runs; I++) {
            runPercentages = evaluateProteoforms(getProteoformSample(inputProteoforms, sampleSizeInPercentage), potentialProteoforms);
            for (Map.Entry<MatchType, Double> matchTypeDoubleEntry : runPercentages.entrySet()) {
                percentages.put(matchTypeDoubleEntry.getKey(), matchTypeDoubleEntry.getValue());
            }
            System.out.println("Finished evaluation " + I);
        }
        return percentages;
    }

    /**
     * Take a sample of proteins from the list and evaluate multiple times the percentage of potential proteoforms
     * * matched after altering the proteoforms.
     * <p>
     * Input: List of proteins to be sampled
     * Output: File with the total percentages of proteoforms matched for each type of matching type. It averages
     * the percentages of all runs.
     *
     * @param sampleSizeInPercentage Percentage of proteins selected from all the proteins with multiple proteoforms
     * @param runs                   Number samples to be taken
     */
    static HashMap<MatchType, Double> evaluateReactomeProteoformsAverage(HashSet<Proteoform> proteoforms,
                                                                         int runs,
                                                                         double sampleSizeInPercentage,
                                                                         PotentialProteoformsType potentialProteoformsType,
                                                                         Mapping mapping,
                                                                         Long range) {
        HashMap<MatchType, Double> percentages = new HashMap<>(7);
        HashMap<MatchType, Double> totalPercentages = new HashMap<>(7);
        int t = 1;
        for (MatchType matchType : MatchType.values()) {
            totalPercentages.put(matchType, 0.0);
        }

        for (int I = 0; I < runs; I++) {
            percentages = evaluateReactomeProteoforms(getProteoformSample(proteoforms, sampleSizeInPercentage), potentialProteoformsType, mapping, range);
            for (MatchType matchType : MatchType.values()) {
                totalPercentages.put(matchType, totalPercentages.get(matchType) + (percentages.get(matchType) - totalPercentages.get(matchType)) / t);
            }
            System.out.println("Finished evaluation " + I);
            t++;
        }
        return totalPercentages;
    }

    /**
     * Take a sample of proteins from the list and evaluate multiple times the percentage of potential proteoforms
     * matched after altering the proteoforms.
     *
     * @param proteoforms
     * @param runs
     * @param sampleSizeInPercentage
     * @param potentialProteoformsType
     * @param mapping
     * @return
     */
    static Multimap<MatchType, Double> evaluateReactomeProteoforms(HashSet<Proteoform> proteoforms,
                                                                   int runs,
                                                                   double sampleSizeInPercentage,
                                                                   PotentialProteoformsType potentialProteoformsType,
                                                                   Mapping mapping,
                                                                   Long range) {
        HashMap<MatchType, Double> percentages = new HashMap<>(7);
        Multimap<MatchType, Double> results = TreeMultimap.create();

        for (int I = 0; I < runs; I++) {
            percentages = evaluateReactomeProteoforms(getProteoformSample(proteoforms, sampleSizeInPercentage), potentialProteoformsType, mapping, range);
            for (MatchType matchType : MatchType.values()) {
                results.put(matchType, percentages.get(matchType));
            }
            System.out.println("Finished evaluation " + I);
        }
        return results;
    }

    static HashSet<Proteoform> getPotentialProteoforms(HashSet<Proteoform> proteoforms, Mapping mapping) {
        HashSet<Proteoform> potentialProteoforms = new HashSet<>();

        for (Proteoform proteoform : proteoforms) {
            potentialProteoforms.addAll(mapping.getProteinsToProteoforms().get(proteoform.getUniProtAcc()));
        }

        return potentialProteoforms;
    }

    static HashSet<Proteoform> getPotentialProteoforms(Proteoform proteoform, Mapping mapping, PotentialProteoformsType potentialProteoformsType) {
        HashSet<Proteoform> potentialProteoforms = new HashSet<>();
        switch (potentialProteoformsType) {
            case ORIGINAL:
                for (Proteoform potentialProteoform : mapping.getProteinsToProteoforms().get(proteoform.getUniProtAcc())) {
                    if (potentialProteoform.equals(proteoform) && potentialProteoform.getPtms().size() > 0) {
                        potentialProteoforms.add(potentialProteoform);
                    }
                }
                break;
            case OTHERS:
                for (Proteoform potentialProteoform : mapping.getProteinsToProteoforms().get(proteoform.getUniProtAcc())) {
                    if (!potentialProteoform.equals(proteoform) && potentialProteoform.getPtms().size() > 0) {
                        potentialProteoforms.add(potentialProteoform);
                    }
                }
                break;
            case ALL:
                for (Proteoform potentialProteoform : mapping.getProteinsToProteoforms().get(proteoform.getUniProtAcc())) {
                    if (potentialProteoform.getPtms().size() > 0) {
                        potentialProteoforms.add(potentialProteoform);
                    }
                }
                break;
        }
        return potentialProteoforms;
    }

    /**
     * Calculates the percentage of proteoforms matched using each type of matching type
     * <p>
     * Input: List of proteoforms to match with proteoforms in reactome
     * Output: File with percentages of proteoforms matched for each type of matching type
     *
     * @param inputProteoforms
     * @throws IOException
     */
    static HashMap<MatchType, Double> evaluateProteoforms(HashSet<Proteoform> inputProteoforms, Mapping mapping) {
        // Find total possible proteoforms available
        HashSet<Proteoform> potentialProteoforms = getPotentialProteoforms(inputProteoforms, mapping);
        HashSet<Proteoform> hitProteoforms = new HashSet<>();
        Long range = 0L;
        HashMap<MatchType, Double> percentages = new HashMap<>(7);

        for (MatchType matchType : MatchType.values()) {
            ProteoformMatching matcher = ProteoformMatching.getInstance(matchType);
            // Count the number of selected proteoforms and unselected
            for (Proteoform potentialProteoform : potentialProteoforms) {
                for (Proteoform inputProteoform : inputProteoforms) {
                    if (matcher.matches(inputProteoform, potentialProteoform, range)) {
                        hitProteoforms.add(potentialProteoform);
                    }
                }
            }
            System.out.println("Finished matching with " + matchType.toString());
            // Write number to file
            double percentageCovered = hitProteoforms.size() * 100.0 / potentialProteoforms.size();
            percentages.put(matchType, percentageCovered);
        }
        return percentages;
    }

    enum PotentialProteoformsType {
        ALL, ORIGINAL, OTHERS;
    }

    /**
     * For each matching type, gets the average percentage of potential proteoforms of each proteoform got matched, after
     * having altered the list of original proteoforms.
     * Potential proteoforms are the proteoforms with the same accession and with at least one modification.
     */
    static HashMap<MatchType, Double> evaluateReactomeProteoforms(HashSet<Proteoform> inputProteoforms,
                                                                  PotentialProteoformsType potentialProteoformsType,
                                                                  Mapping mapping, Long range) {
        HashMap<MatchType, Double> totalPercentages = new HashMap<>(7);
        HashMap<MatchType, ProteoformMatching> matchers = new HashMap();
        int t = 1;

        for (MatchType matchType : MatchType.values()) {
            matchers.put(matchType, ProteoformMatching.getInstance(matchType));
            totalPercentages.put(matchType, 0.0);
        }

        for (Proteoform proteoform : inputProteoforms) {
            HashSet<Proteoform> potentialProteoforms = getPotentialProteoforms(proteoform, mapping, potentialProteoformsType);
            if (potentialProteoforms.size() > 0) {
                Proteoform alteredProteoform = alterProteoform(proteoform);
                for (MatchType matchType : MatchType.values()) {
                    int matched = 0;
                    for (Proteoform potentialProteoform : potentialProteoforms) {
                        if (matchers.get(matchType).matches(alteredProteoform, potentialProteoform, range)) {
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
     * Receives a list of proteoforms and calculates the percentage of proteoforms in Reactome that got matched by any
     * proteoform in the input.
     */
    static void evaluateExternalProteoforms() {

    }

    public static void main(String args[]) throws IOException {

        int runs = 10;
        Long range = 5L;
        Mapping mapping = new Mapping(InputType.PROTEOFORM, false, "");
        String scriptPlotMatches = "src\\main\\r\\match.R";
        String scriptFacetMatches = "src\\main\\r\\facetMatches.R";

        /************* Sample matching percentage with the proteoforms of the proteins with multiple proteoforms ********************/

        // In this case: what percentage of proteoforms with the same accession were matched

        /* All mathes */
        String multiproteoformProteins_matchesFile = "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\resources\\sensitivity\\multiproteoformProteins_matchesFile.csv";
        String multiproteoformProteins_plotFile = "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\docs\\multiproteoformProteins.png";

        HashSet<Proteoform> proteoformsFromMultiproteoformProteins = getProteinsWithMultipleProteoforms(mapping);
        Multimap<MatchType, Double> percentages = evaluateReactomeProteoforms(
                proteoformsFromMultiproteoformProteins,
                runs, 10.0,
                PotentialProteoformsType.ALL, mapping, range);
        System.out.println("Calculated percentages");
        writeEvaluation(percentages, multiproteoformProteins_matchesFile);
        System.out.println("Writing evaluation");
//        Runtime.getRuntime().exec("Rscript --vanilla " + scriptPlotMatches + " " + multiproteoformProteins_matchesFile + " " + multiproteoformProteins_plotFile);
//        System.out.println("Plot finished\n\n");

        /* Matching only original proteoforms */
        String multiproteoformProteins_originalMatchesFile = "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\resources\\sensitivity\\multiproteoformProteins_originalMatchesFile.csv";
        String multiproteoformProteins_originalPlotFile = "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\docs\\multiproteoformProteinsOriginal.png";
        percentages = evaluateReactomeProteoforms(
                proteoformsFromMultiproteoformProteins,
                runs, 10.0,
                PotentialProteoformsType.ORIGINAL, mapping, range);
        System.out.println("Calculated percentages");
        writeEvaluation(percentages, multiproteoformProteins_originalMatchesFile);
        System.out.println("Writing evaluation");

        /* Matching only non-original proteoforms */
        String multiproteoformProteins_nonOriginalMatchesFile = "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\resources\\sensitivity\\multiproteoformProteins_nonOriginalMatchesFile.csv";
        String multiproteoformProteins_nonOriginalPlotFile = "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\docs\\multiproteoformProteinsNonOriginal.png";
        percentages = evaluateReactomeProteoforms(
                proteoformsFromMultiproteoformProteins,
                runs, 30.0,
                PotentialProteoformsType.OTHERS, mapping, range);
        System.out.println("Calculated percentages");
        writeEvaluation(percentages, multiproteoformProteins_nonOriginalMatchesFile);
        System.out.println("Writing evaluation");

        /* Sample matching percentage with the proteoforms from phosphosite dataset */

        // In this case: what percentage of the reference proteoforms got matched using the phosphoproteoforms

        // Create proteoform file from phosphosites

        System.out.println("Calculating percentages of phosphoproteoforms.");
        String file_phosphosites = "C:\\Users\\luisp\\OneDrive\\Documents\\phd\\Papers\\2018 PathwayMatcher\\GigaScience\\SensitivityAnalysis\\phosphosites.csv";
        String file_phosphosites_reactome = "C:\\Users\\luisp\\OneDrive\\Documents\\phd\\Papers\\2018 PathwayMatcher\\GigaScience\\SensitivityAnalysis\\phosphosites_reactome.csv";
        String phosphoproteoforms_matchesFile = "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\resources\\sensitivity\\phosphoproteoforms_matchesFile.csv";
        String phosphoproteoforms_plotFile = "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\docs\\phosphoproteoforms.png";
//        try {
//            HashSet<Proteoform> inputProteoforms = createProteoformList(file_phosphosites);
//            System.out.println("Created proteoform list");
//            percentages = evaluateProteoforms(inputProteoforms, mapping);
//            System.out.println("Calculated percentages");
//            writeEvaluation(percentages, phosphoproteoforms_matchesFile);
//            System.out.println("Writing evaluation");
//            Runtime.getRuntime().exec("Rscript --vanilla " + scriptPlotMatches + " " + phosphoproteoforms_matchesFile + " " + phosphoproteoforms_plotFile);
//            System.out.println("Plot finished\n\n");
//        } catch (ParseException e) {
//            System.out.println("Error creating the disaggregated proteoform list.");
//            e.printStackTrace();
//        }

        Runtime.getRuntime().exec("Rscript --vanilla " + scriptFacetMatches
                + " " + multiproteoformProteins_originalMatchesFile
                + " " + multiproteoformProteins_originalPlotFile
                + " " + multiproteoformProteins_nonOriginalMatchesFile
                + " " + multiproteoformProteins_nonOriginalPlotFile
                + " " + multiproteoformProteins_matchesFile
                + " " + multiproteoformProteins_plotFile
                + " " + phosphoproteoforms_matchesFile
                + " " + phosphoproteoforms_plotFile);


//        comparePhosphosites(file_phosphosites, file_phosphosites_reactome);
//
//        try {
//            List<Proteoform> proteoformList = createProteoformList(file_phosphosites);
//            Mapping mapping = new Mapping(InputType.PROTEOFORM, false, "");
//
//            // Compare proteoforms with one modification each
//            for(MatchType matchType : MatchType.values()){
////                Search.searchWithProteoform(input, mapping, showTopLevelPathways, matchType, range);
//            }
//        } catch (ParseException e) {
//            System.out.println("Error creating the disaggregated proteoform list.");
//            e.printStackTrace();
//        }
//

        /* Sample matching percentage with custom proteoforms in a separate file*/

//        String customProteoformsFile = "src\\test\\resources\\Proteoforms\\Simple\\proteoform.txt";
//        String customProteoforms_matchesFile = "src\\test\\resources\\sensitivity\\matchesCustom.csv";
//        String customProteoforms_plotFile = "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\docs\\multiproteoformProteins.png";
//        percentages = evaluateProteoforms(customProteoformsFile);
//        writeEvaluation(percentages, customProteoforms_matchesFile);
//        Runtime.getRuntime().exec("Rscript --vanilla " + scriptPlotMatches + " " + customProteoforms_matchesFile + " " + customProteoforms_plotFile);
//

    }
};
