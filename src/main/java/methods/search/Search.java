package methods.search;

import com.google.common.collect.Lists;
import methods.matching.ProteoformMatching;
import model.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static methods.search.PeptideMatcher.*;
import static model.Error.ERROR_INITIALIZING_PEPTIDE_MAPPER;
import static model.InputPatterns.*;
import static model.Warning.*;


/**
 * Methods to get the reactions and pathways using a list of entities of the accepted input types.
 * <p>
 * <p>These methods must fill the number of reactions and entities found in each pathway in the structure passed as parameter.
 * They also must fill in the set for the hit proteins and hit pathways.</p>
 */
public class Search {

//    public static SearchResult search(List<String> input, InputType inputType, boolean showTopLevelPathways, Mapping mapping) {
//        return Search.search(input, inputType, showTopLevelPathways, mapping, MatchType.SUPERSET, 0L, "");
//    }

    private static String removeUTF8BOM(String line) {
        if (line.startsWith("\uFEFF")) {
            line = line.substring(1);
        } else if (line.startsWith("ï»¿")) {
            line = line.substring(3);
        }
        return line;
    }

//    public static SearchResult search(List<String> input, InputType inputType, boolean showTopLevelPathways, Mapping mapping, MatchType matchType, Long range, String fastaFile, String mapping_path) {
//        input.replaceAll(String::trim);
//        input = Lists.transform(input, Search::removeUTF8BOM);
//        switch (inputType) {
//            case GENE:
//                return Search.searchWithGene(input, mapping, showTopLevelPathways);
//            case ENSEMBL:
//                return Search.searchWithEnsembl(input, mapping, showTopLevelPathways);
//            case UNIPROT:
//                return Search.searchWithUniProt(input, mapping, showTopLevelPathways);
//            case PROTEOFORM:
//                return Search.searchWithProteoform(input, mapping, showTopLevelPathways, matchType, range);
//            case RSID:
//                return Search.searchWithRsId(input, mapping, mapping_path, showTopLevelPathways);
//            case CHRBP:
//            case VCF:
//                return Search.searchWithChrBp(input, mapping, mapping_path, showTopLevelPathways);
//            case PEPTIDE:
//                return Search.searchWithPeptide(input, mapping, showTopLevelPathways, fastaFile);
//            case MODIFIEDPEPTIDE:
//                return Search.searchWithModifiedPeptide(input, mapping, showTopLevelPathways, matchType, range, fastaFile);
//            default:
//                System.out.println("Input inputType not supported.");
//                System.exit(1);
//                break;
//        }
//        return null;
//    }

    // Fills the hitProteins set to call the next method
    public static SearchResult searchWithUniProt(List<String> input, Mapping mapping, Boolean topLevelPathways) {

        SearchResult result = new SearchResult(InputType.UNIPROT, topLevelPathways);

        int row = 0;
        for (String protein : input) {
            row++;
            if (protein.contains("-")) {
                protein = protein.substring(0, protein.indexOf("-"));
            }

            if (!matches_Protein_Uniprot(protein)) {
                sendWarning(INVALID_ROW, row);
                continue;
            }
            result.getInputProteins().add(protein);
            if (mapping.getProteinsToNames().containsKey(protein)) {
                result.getMatchedProteins().add(protein);

                for (String reaction : mapping.getProteinsToReactions().get(protein)) {
                    result.getHitProteins().add(protein);

                    for (String pathwayStId : mapping.getReactionsToPathways().get(reaction)) {

                        Pathway pathway = mapping.getPathways().get(pathwayStId);
                        result.getHitPathways().add(mapping.getPathways().get(pathwayStId));
                        pathway.getReactionsFound().add(reaction);
                        pathway.getEntitiesFound().add(new Proteoform(protein));

                        String[] values = new String[7];
                        values[0] = protein;
                        values[1] = reaction;
                        values[2] = mapping.getReactions().get(reaction).getDisplayName();
                        values[3] = pathwayStId;
                        values[4] = pathway.getDisplayName();
                        values[5] = pathwayStId;
                        values[6] = pathway.getDisplayName();

                        if (topLevelPathways) {
                            if (mapping.getPathwaysToTopLevelPathways().get(pathwayStId).size() > 0) {
                                for (String topLevelPathway : mapping.getPathwaysToTopLevelPathways().get(pathwayStId)) {
                                    values[5] = topLevelPathway;
                                    values[6] = mapping.getPathways().get(topLevelPathway).getDisplayName();
                                    result.addRecord(values);
                                }
                            } else {
                                result.addRecord(values);
                            }
                        } else {
                            result.addRecord(Arrays.copyOfRange(values, 0, 5));
                        }
                    }
                }
            }

        }

        System.out.println("\nInput: " + result.getInputProteins().size() + " proteins");
        Double percentageProteins = (double) result.getMatchedProteins().size() * 100.0 / (double) result.getInputProteins().size();
        System.out.println("Matched: " + result.getMatchedProteins().size() + " proteins (" + new DecimalFormat("#0.00").format(percentageProteins) + "%)");
        percentageProteins = (double) result.getHitProteins().size() * 100.0 / (double) result.getInputProteins().size();
        System.out.println("Proteins mapping to reactions: " + result.getHitProteins().size() + " proteins (" + new DecimalFormat("#0.00").format(percentageProteins) + "%)");

        result.setStatus(new MessageStatus("Success", 0, 0, "", ""));

        return result;
    }

    /**
     * Expects one gene per line. Already trimmed.
     *
     * @param input            List of gene names as strings
     * @param mapping          Static map reference data
     * @param topLevelPathways Boolean to show top level pathways
     * @return
     */
    public static SearchResult searchWithGene(List<String> input, Mapping mapping, Boolean topLevelPathways) {

        SearchResult result = new SearchResult(InputType.GENE, topLevelPathways);

        for (String gene : input) {
            result.getInputGenes().add(gene);

            for (String protein : mapping.getGenesToProteins().get(gene)) {
                result.getMatchedProteins().add(protein);
                result.getMatchedGenes().add(gene);
                result.getInputProteins().add(protein);

                for (String reaction : mapping.getProteinsToReactions().get(protein)) {
                    result.getHitGenes().add(gene); // The genes that actually matched to some protein
                    result.getHitProteins().add(protein);

                    for (String pathwayStId : mapping.getReactionsToPathways().get(reaction)) {

                        Pathway pathway = mapping.getPathways().get(pathwayStId);
                        result.getHitPathways().add(mapping.getPathways().get(pathwayStId));
                        pathway.getReactionsFound().add(reaction);
                        pathway.getEntitiesFound().add(new Proteoform(protein));

                        String[] values = new String[8];
                        values[0] = gene;
                        values[1] = protein;
                        values[2] = reaction;
                        values[3] = mapping.getReactions().get(reaction).getDisplayName();
                        values[4] = pathwayStId;
                        values[5] = pathway.getDisplayName();
                        values[6] = pathwayStId;
                        values[7] = pathway.getDisplayName();

                        if (topLevelPathways) {
                            if (mapping.getPathwaysToTopLevelPathways().get(pathwayStId).size() > 0) {
                                for (String topLevelPathway : mapping.getPathwaysToTopLevelPathways().get(pathwayStId)) {
                                    values[6] = topLevelPathway;
                                    values[7] = mapping.getPathways().get(topLevelPathway).getDisplayName();
                                    result.addRecord(values);
                                }
                            } else {
                                result.addRecord(values);
                            }
                        } else {
                            result.addRecord(Arrays.copyOfRange(values, 0, 6));
                        }
                    }
                }
            }
        }

        System.out.println("\nInput: " + result.getInputGenes().size() + " genes");
        Double percentageGenes = (double) result.getMatchedGenes().size() * 100.0 / (double) input.size();
        System.out.println("Matched: " + result.getMatchedGenes().size() + " genes (" + new DecimalFormat("#0.00").format(percentageGenes) + "%), " + result.getHitProteins().size() + " proteins");
        percentageGenes = (double) result.getHitGenes().size() * 100.0 / (double) result.getInputGenes().size();
        System.out.println("Genes mapping to reactions: " + result.getHitGenes().size() + " genes (" + new DecimalFormat("#0.00").format(percentageGenes) + "%)");

        result.setStatus(new MessageStatus("Success", 0, 0, "", ""));

        return result;
    }

    /**
     * Expects one ensembl identifier per line, already trimmed.
     *
     * @param input            List of ensemble ids
     * @param mapping          Data structures with the static mapping to reactions and pathways
     * @param topLevelPathways
     * @return
     */
    public static SearchResult searchWithEnsembl(List<String> input, Mapping mapping, Boolean topLevelPathways) {

        SearchResult result = new SearchResult(InputType.ENSEMBL, topLevelPathways);
        int contHitEnsemble = 0;

        int row = 0;
        for (String ensembl : input) {
            row++;
            if (!matches_Protein_Ensembl(ensembl)) {
                sendWarning(INVALID_ROW, row);
                continue;
            }
            result.getInputEnsembl().add(ensembl);

            if (mapping.getEnsemblToUniprot().get(ensembl).size() > 0) {
                contHitEnsemble++;
            }

            for (String protein : mapping.getEnsemblToUniprot().get(ensembl)) {
                result.getInputProteins().add(protein);
                result.getMatchedEnsembl().add(ensembl);
                result.getMatchedProteins().add(protein);

                for (String reaction : mapping.getProteinsToReactions().get(protein)) {
                    result.getHitProteins().add(protein);
                    result.getHitEnsembl().add(ensembl);
                    for (String pathwayStId : mapping.getReactionsToPathways().get(reaction)) {

                        Pathway pathway = mapping.getPathways().get(pathwayStId);
                        result.getHitPathways().add(pathway);
                        pathway.getReactionsFound().add(reaction);
                        pathway.getEntitiesFound().add(new Proteoform(protein));

                        String[] values = new String[8];
                        values[0] = ensembl;
                        values[1] = protein;
                        values[2] = reaction;
                        values[3] = mapping.getReactions().get(reaction).getDisplayName();
                        values[4] = pathwayStId;
                        values[5] = pathway.getDisplayName();
                        values[6] = pathwayStId;
                        values[7] = pathway.getDisplayName();

                        if (topLevelPathways) {
                            if (mapping.getPathwaysToTopLevelPathways().get(pathwayStId).size() > 0) {
                                for (String topLevelPathway : mapping.getPathwaysToTopLevelPathways().get(pathwayStId)) {
                                    values[6] = topLevelPathway;
                                    values[7] = mapping.getPathways().get(topLevelPathway).getDisplayName();
                                    result.addRecord(values);
                                }
                            } else {
                                result.addRecord(values);
                            }
                        } else {
                            result.addRecord(Arrays.copyOfRange(values, 0, 6));
                        }
                    }
                }
            }
        }

        System.out.println("\nInput: " + result.getInputProteins().size() + " proteins");
        Double percentageProteins = (double) contHitEnsemble * 100.0 / (double) result.getInputProteins().size();
        System.out.println("Matched: " + contHitEnsemble + " proteins (" + new DecimalFormat("#0.00").format(percentageProteins) + "%)");

        result.setStatus(new MessageStatus("Success", 0, 0, "", ""));

        return result;
    }


    /**
     * Maps rsids to protein to reaction to pathways.
     * Usually only for rsids in a specific chromosome, but uses all the mapping contained at the imapRsIdsToProteins parameter.
     * Fills the hitPathways and the hitProteins from the parameter structures.
     *
     * @param input            Set of unique identifiers
     * @param mapping          Static structures for the pathway matching
     * @param topLevelPathways Flag if top level pathways should be used
     * @return Mapping from rsids to pathways, message errors
     */
    public static SearchResult searchWithRsId(List<String> input, Mapping mapping, Boolean topLevelPathways, String mapping_path) throws FileNotFoundException {

        SearchResult result = new SearchResult(InputType.RSID, topLevelPathways);

        int row = 0;
        for (String rsid : input) {
            row++;
            if (rsid.isEmpty()) {
                sendWarning(EMPTY_ROW, row);
                continue;
            }
            if (!matches_Rsid(rsid)) {
                sendWarning(INVALID_ROW, row);
                continue;
            }
            result.getInputRsid().add(rsid);
        }

        for (int chr = 1; chr <= 22; chr++) {
            for (String rsid : result.getInputRsid()) {
                for (String protein : mapping.getRsidsToProteins(chr, mapping_path).get(rsid)) {
                    result.getMatchedRsid().add(rsid);
                    result.getInputProteins().add(protein);
                    result.getMatchedProteins().add(protein);

                    for (String reaction : mapping.getProteinsToReactions().get(protein)) {
                        result.getHitProteins().add(protein);
                        result.getHitRsid().add(rsid);

                        for (String pathwayStId : mapping.getReactionsToPathways().get(reaction)) {

                            Pathway pathway = mapping.getPathways().get(pathwayStId);
                            result.getHitPathways().add(pathway);
                            pathway.getReactionsFound().add(reaction);
                            pathway.getEntitiesFound().add(new Proteoform(protein));

                            String[] values = new String[8];
                            values[0] = rsid;
                            values[1] = protein;
                            values[2] = reaction;
                            values[3] = mapping.getReactions().get(reaction).getDisplayName();
                            values[4] = pathwayStId;
                            values[5] = pathway.getDisplayName();
                            values[6] = pathwayStId;
                            values[7] = pathway.getDisplayName();

                            if (topLevelPathways) {
                                if (mapping.getPathwaysToTopLevelPathways().get(pathwayStId).size() > 0) {
                                    for (String topLevelPathway : mapping.getPathwaysToTopLevelPathways().get(pathwayStId)) {
                                        values[6] = topLevelPathway;
                                        values[7] = mapping.getPathways().get(topLevelPathway).getDisplayName();
                                        result.addRecord(values);
                                    }
                                } else {
                                    result.addRecord(values);
                                }
                            } else {
                                result.addRecord(Arrays.copyOfRange(values, 0, 6));
                            }
                        }
                    }
                }
            }
        }

        System.out.println("\nInput: " + result.getInputRsid().size() + " rsids");
        System.out.println("Found " + result.getHitProteins().size() + " proteins.");
        Double percentageSnps = (double) result.getMatchedRsid().size() * 100.0 / (double) result.getInputRsid().size();
        System.out.println("Matched: " + result.getMatchedRsid().size() + " snps (" + new DecimalFormat("#0.00").format(percentageSnps) + "%), " + result.getHitProteins().size() + " proteins");

        result.setStatus(new MessageStatus("Success", 0, 0, "", ""));

        return result;
    }

    /**
     * Maps variants composed by [chr, bp] to protein to reaction to pathways.
     * Only maps a specific chromosome at a time, but uses all the mapping contained at the imapRsIdsToProteins parameter.
     * Fills the hitPathways and the hitProteins from the parameter structures.
     *
     * @param input            one chr and bp per line
     * @param mapping
     * @param topLevelPathways Flag if top level pathways should be used
     * @return Mapping from rsids to pathways, message errors
     */
    public static SearchResult searchWithChrBp(List<String> input, Mapping mapping, Boolean topLevelPathways, String mapping_path) throws FileNotFoundException {

        SearchResult result = new SearchResult(InputType.CHRBP, topLevelPathways);

        Snp snp = null;
        int row = 0;
        for (String line : input) {
            row++;
            if (line.isEmpty()) {
                sendWarning(EMPTY_ROW, row);
                continue;
            }
            if (line.startsWith("#")) {
                continue;
            }
            if (!matches_ChrBp(line) && !matches_Vcf_Record(line)) {
                sendWarning(INVALID_ROW, row);
                continue;
            }
            snp = getSnpFromChrBp(line);
            result.getInputChrBp().put(snp.getChr(), snp.getBp());
        }

        for (int chr : result.getInputChrBp().keySet()) {
            for (Long bp : result.getInputChrBp().get(chr)) {
                for (String protein : mapping.getChrBpToProteins(chr, mapping_path).get(bp)) {
                    result.getMatchedChrBp().put(chr, bp);
                    result.getInputProteins().add(protein);
                    result.getMatchedProteins().add(protein);

                    for (String reaction : mapping.getProteinsToReactions().get(protein)) {
                        result.getHitProteins().add(protein);
                        result.getHitChrBp().put(chr, bp);

                        for (String pathwayStId : mapping.getReactionsToPathways().get(reaction)) {

                            Pathway pathway = mapping.getPathways().get(pathwayStId);
                            result.getHitPathways().add(pathway);
                            pathway.getReactionsFound().add(reaction);
                            pathway.getEntitiesFound().add(new Proteoform(protein));

                            String[] values = new String[9];
                            values[0] = String.valueOf(chr);
                            values[1] = String.valueOf(bp);
                            values[2] = protein;
                            values[3] = reaction;
                            values[4] = mapping.getReactions().get(reaction).getDisplayName();
                            values[5] = pathwayStId;
                            values[6] = pathway.getDisplayName();
                            values[7] = pathwayStId;
                            values[8] = pathway.getDisplayName();

                            if (topLevelPathways) {
                                if (mapping.getPathwaysToTopLevelPathways().get(pathwayStId).size() > 0) {
                                    for (String topLevelPathway : mapping.getPathwaysToTopLevelPathways().get(pathwayStId)) {
                                        values[7] = topLevelPathway;
                                        values[8] = mapping.getPathways().get(topLevelPathway).getDisplayName();
                                        result.addRecord(values);
                                    }
                                } else {
                                    result.addRecord(values);
                                }
                            } else {
                                result.addRecord(Arrays.copyOfRange(values, 0, 7));
                            }
                        }
                    }
                }
            }
        }

        System.out.println("\nInput: " + result.getInputChrBp().entries().size() + " snps");
        System.out.println("Found " + result.getHitProteins().size() + " proteins.");
        Double percentageSnps = (double) result.getMatchedChrBp().entries().size() * 100.0 / (double) result.getInputChrBp().entries().size();
        System.out.println("Matched: " + result.getMatchedChrBp().entries().size() + " snps ("
                + new DecimalFormat("#0.00").format(percentageSnps) + "%), "
                + result.getHitProteins().size() + " proteins");

        result.setStatus(new MessageStatus("Success", 0, 0, "", ""));

        return result;
    }

    /*
     * Get the snp instance from a line with chromosome and base pair.
     * This method expects the line to be validated already
     */
    private static Snp getSnpFromChrBp(String line) {
        String[] fields = line.split("\\s");
        Integer chr = Integer.valueOf(fields[0]);
        if (fields[1].endsWith("L")) {
            fields[1] = fields[1].substring(0, fields[1].length() - 1);
        }
        Long bp = Long.valueOf(fields[1]);
        return new Snp(chr, bp);
    }

    public static SearchResult searchWithProteoform(List<String> input,
                                                    Mapping mapping,
                                                    Boolean topLevelPathways,
                                                    MatchType matchType,
                                                    Long range) {

        SearchResult result = new SearchResult(InputType.PROTEOFORM, topLevelPathways);
        ProteoformMatching matcher = ProteoformMatching.getInstance(matchType);
        assert matcher != null;

        int row = 0;
        for (String line : input) {
            row++;
            if (matches_Proteoform_Simple(line)) {
                try {
                    Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform(line, row);
                    result.getInputProteoforms().add(proteoform);
                } catch (ParseException e) {
                    sendWarning(INVALID_ROW, row);
                }
            } else {
                if (line.isEmpty())
                    sendWarning(EMPTY_ROW, row);
                else
                    sendWarning(INVALID_ROW, row);
            }
        }

        // For each proteoform in the input we try to find matches in the reference proteoforms
        for (Proteoform inputProteoform : result.getInputProteoforms()) {
            result.getInputProteins().add(inputProteoform.getUniProtAcc());

            for (Proteoform refProteoform : mapping.getProteinsToProteoforms().get(inputProteoform.getUniProtAcc())) {
                if (matcher.matches(inputProteoform, refProteoform, range)) {
                    result.getMatchedProteoforms().add(inputProteoform);
                    result.getMatchedProteins().add(inputProteoform.getUniProtAcc());
                    if (mapping.getProteoformsToReactions().get(refProteoform).size() > 0) {
                        result.getHitProteoforms().add(refProteoform);
                        result.getHitProteins().add(refProteoform.getUniProtAcc());
                    }
                }
            }
        }

        for (Proteoform hitProteoform : result.getHitProteoforms()) {
            for (String reaction : mapping.getProteoformsToReactions().get(hitProteoform)) {
                result.getHitProteoforms().add(hitProteoform);
                result.getHitProteins().add(hitProteoform.getUniProtAcc());

                for (String pathwayStId : mapping.getReactionsToPathways().get(reaction)) {

                    Pathway pathway = mapping.getPathways().get(pathwayStId);
                    result.getHitPathways().add(pathway);
                    pathway.getReactionsFound().add(reaction);
                    pathway.getEntitiesFound().add(hitProteoform);

                    String[] values = new String[8];
                    values[0] = hitProteoform.toString(ProteoformFormat.SIMPLE);
                    values[1] = hitProteoform.getUniProtAcc();
                    values[2] = reaction;
                    values[3] = mapping.getReactions().get(reaction).getDisplayName();
                    values[4] = pathwayStId;
                    values[5] = pathway.getDisplayName();
                    values[6] = pathwayStId;
                    values[7] = pathway.getDisplayName();

                    if (topLevelPathways) {
                        if (mapping.getPathwaysToTopLevelPathways().get(pathwayStId).size() > 0) {
                            for (String topLevelPathway : mapping.getPathwaysToTopLevelPathways().get(pathwayStId)) {
                                values[6] = topLevelPathway;
                                values[7] = mapping.getPathways().get(topLevelPathway).getDisplayName();
                                result.addRecord(values);
                            }
                        } else {
                            result.addRecord(values);
                        }
                    } else {
                        result.addRecord(Arrays.copyOfRange(values, 0, 6));
                    }
                }
            }
        }

        System.out.println("\nInput: " + result.getInputProteoforms().size() + " proteoforms, " + result.getInputProteins().size() + " proteins");
        Double percentageProteoforms = (double) result.getMatchedProteoforms().size() * 100.0 / (double) result.getInputProteoforms().size();
        Double percentageProteins = (double) result.getMatchedProteins().size() * 100.0 / (double) result.getInputProteins().size();
        System.out.println("Matched: " + result.getMatchedProteoforms().size() + " proteoforms(" + new DecimalFormat("#0.00").format(percentageProteoforms) + "%), "
                + result.getMatchedProteins().size() + " proteins (" + new DecimalFormat("#0.00").format(percentageProteins) + "%)");
        percentageProteoforms = (double) result.getHitProteoforms().size() * 100.0 / (double) result.getInputProteoforms().size();
        System.out.println("Proteoforms mapping to reactions: " + result.getHitProteoforms().size() + " proteoforms (" + new DecimalFormat("#0.00").format(percentageProteoforms) + "%)");

        result.setStatus(new MessageStatus("Success", 0, 0, "", ""));

        return result;
    }

    public static SearchResult searchWithPeptide(List<String> input,
                                                 Mapping mapping,
                                                 Boolean topLevelPathways,
                                                 String fastaFile) {

        SearchResult result = new SearchResult(InputType.PEPTIDE, topLevelPathways);

        // Note: In this function the duplicate protein identifiers are removed by
        // adding the whole input list to a set.
        if (!initializePeptideMapper(fastaFile)) {
            System.out.println(ERROR_INITIALIZING_PEPTIDE_MAPPER.getMessage());
            System.exit(ERROR_INITIALIZING_PEPTIDE_MAPPER.getCode());
        }

        int row = 0;
        for (String line : input) {
            row++;
            if (matches_Peptide(line)) {
                for (String protein : getPeptideMapping(line)) {
                    result.getMatchedProteins().add(protein.contains("-") ? protein.substring(0, protein.indexOf("-")) : protein);
                }
            } else {
                if (line.isEmpty())
                    sendWarning(EMPTY_ROW, row);
                else
                    sendWarning(INVALID_ROW, row);
            }
        }

        return searchWithUniProt(new ArrayList<>(result.getMatchedProteins()), mapping, topLevelPathways);
    }

    public static SearchResult searchWithModifiedPeptide(List<String> input,
                                                         Mapping mapping,
                                                         Boolean topLevelPathways,
                                                         MatchType matchType,
                                                         Long margin,
                                                         String fastaFile) {

        List<String> correctedInput = new ArrayList<>();

        // Note: In this function the duplicate protein identifiers are removed by
        // adding the whole input list to a set.
        if (!initializePeptideMapper(fastaFile)) {
            System.out.println(ERROR_INITIALIZING_PEPTIDE_MAPPER.getMessage());
            System.exit(ERROR_INITIALIZING_PEPTIDE_MAPPER.getCode());
        }

        int row = 0;
        for (String line : input) {
            row++;
            if (matches_Peptite_And_Mod_Sites(line)) {
                try {
                    Proteoform tempProteoform = ProteoformFormat.SIMPLE.getProteoform(line);
                    for (Pair<String, Integer> pair : getPeptideMappingWithIndex(tempProteoform.getUniProtAcc())) {

                        String uniprot = pair.getLeft();
                        int index = pair.getRight();
                        Proteoform correctProteoform = new Proteoform(uniprot);

                        //Correct the positions of the PTMs
                        for (Pair<String, Long> ptm : tempProteoform.getPtms()) {
                            correctProteoform.addPtm(ptm.getLeft(), ptm.getValue() + index);
                        }
                        correctedInput.add(correctProteoform.toString(ProteoformFormat.SIMPLE));
                    }
                } catch (ParseException e) {
                    sendWarning(INVALID_ROW, row);
                }
            } else {
                if (line.isEmpty())
                    sendWarning(EMPTY_ROW, row);
                else
                    sendWarning(INVALID_ROW, row);
            }
        }

        return searchWithProteoform(correctedInput, mapping, topLevelPathways, matchType, margin);
    }
}
