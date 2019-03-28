package matcher;

import model.MatchType;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Class to run the full PathwayMatcher. It calls the chosen commands from the command line interface.
 */
public class Main {

    // Parent command for matching
    @Command(name = "java -jar PathwayMatcher.jar",
            header = "@|green %n PathwayMatcher 1.9.0 %n |@",
            description = "Matches the input to reactions and pathways",
            footer = {"@|cyan %n If you like the project star it on github. |@", ""},
            version = "Main 1.9.0",
            subcommands = {
                    CommandLine.HelpCommand.class,
                    MatchProteoforms.class,
                    MatchGenes.class,
                    MatchUniprot.class,
                    MatchEnsembl.class,
                    MatchVCF.class,
                    MatchChrBp.class,
                    MatchRsId.class,
                    MatchPeptides.class,
                    MatchModifiedPeptides.class
            }
    )
    static class PathwayMatcher implements Runnable {

        @Option(names = {"-v", "--version"}, versionHelp = true, description = "Show version information and exit")
        boolean versionInfoRequested;

        @Override
        public void run() {
        }
    }

    static class MatchSubcommand implements Runnable {

        @Option(names = {"-i", "--input"}, required = true, description = "Input file with path")
        String input_path = "intput.txt";

        @Option(names = {"-o", "-output"}, description = "Path to directory to set the output files: search.csv, analysis.csv and networks files.")
        String output_path = "";

        @Option(names = {"-T", "--topLevelPathways"}, description = "Show Top Level Pathways in the search result.")
        boolean showTopLevelPathways = false;

        @Option(names = {"-g", "--graph"}, description = "Create default connection graph: gene, protein or proteoform interaction network according on the input type.")
        boolean doDefaultGraph = false;

        @Option(names = {"-gg", "--graphGene"}, description = "Create gene connection graph")
        boolean doGeneGraph = false;

        @Option(names = {"-gp", "--graphProteoform"}, description = "Create proteoform connection graph")
        boolean doUniprotGraph = false;

        @Option(names = {"-gu", "--graphUniprot"}, description = "Create protein connection graph")
        boolean doProteoformGraph = false;

        @Override
        public void run() {

        }
    }

    @Command(name = "match-genes", description = "Match a list of gene names")
    static class MatchGenes extends MatchSubcommand{
        @Override
        public void run(){
            System.out.println("In match-genes");
        }
    }

    @Command(name = "match-proteins-uniprot", description = "Match a list of UniProt protein accessions")
    static class MatchUniprot extends MatchSubcommand {
        @Override
        public void run() {
            System.out.println("In match-uniprot");
        }
    }

    @Command(name = "match-proteins-ensembl", description = "Match a list of Ensembl protein identifiers")
    static class MatchEnsembl extends MatchSubcommand {
        @Override
        public void run() {
            System.out.println("In match-proteins-ensembl");
        }
    }

    @Command(name = "match-variants-vcf", description = "Match a list of genetic variants in VCF format")
    static class MatchVCF extends MatchSubcommand {
        @Override
        public void run() {
            System.out.println("In match-variants-vcf");
        }
    }

    @Command(name = "match-variants-chrbps", description = "Match a list of genetic variants as chromosome and base pairs")
    static class MatchChrBp extends MatchSubcommand {
        @Override
        public void run() {
            System.out.println("In match-variants-chrbps");
        }
    }

    @Command(name = "match-variants-rsids", description = "Match a list of genetic variants as RsIds")
    static class MatchRsId extends MatchSubcommand {
        @Override
        public void run() {
            System.out.println("In MatchRsId");
        }
    }

    static class MatchSubcommandWithModifications extends MatchSubcommand {
        @Option(names = {"-m", "--matchType"}, description = "Proteoform match criteria. %nValid values: ${COMPLETION-CANDIDATES}. %nDefault: ${DEFAULT-VALUE}", required = true)
        MatchType matchType = MatchType.SUBSET;

        @Option(names = {"-r", "--range"}, description = "Ptm sites range of error")
        Long range = 0L;
    }

    @Command(name = "match-proteoforms", description = "Match a list of proteoforms to reactions and pathways")
    static class MatchProteoforms extends MatchSubcommandWithModifications {
        @Override
        public void run() {
            System.out.println("In match-proteoforms");
        }
    }

    static class MatchSubcommandPeptides extends MatchSubcommand {
        @Option(names = {"-f", "--fasta"}, description = "Path and name of the fasta file containing the Proteins where to find the peptides.", required = true)
        String fasta_path = "";
    }

    @Command(name = "match-peptides", description = "Match a list of peptides to proteins and then to reactions and pathways")
    static class MatchPeptides extends MatchSubcommandPeptides {

        @Option(names = {"-f", "--fasta"}, description = "Path and name of the fasta file containing the Proteins where to find the peptides.", required = true)
        String fasta_path = "";

        @Override
        public void run() {
            System.out.println("In match-peptides");
        }
    }

    @Command(name = "match-peptides-modified", description = "Match a list of peptides with post translational modifications to proteoforms and then match to reactions and pathways")
    static class MatchModifiedPeptides extends MatchSubcommandWithModifications {

        @Option(names = {"-f", "--fasta"}, description = "Path and name of the fasta file containing the Proteins where to find the peptides.", required = true)
        String fasta_path = "";

        @Override
        public void run() {
            System.out.println("In match-peptides-modified");
//            System.out.println(matchType);
//            new CommandLine(new MatchModifiedPeptides()).usage(System.out);
        }
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new PathwayMatcher());
        cmd.setCaseInsensitiveEnumValuesAllowed(true);
        cmd.parseWithHandlers(new CommandLine.RunAll().useOut(System.out), CommandLine.defaultExceptionHandler().useErr(System.err), args);

        if (args.length == 0) {
            cmd.usage(System.out);
        }
    }

    private static int populationSize = -1;
    private static final String separator = "\t";    // Column separator


    // TODO: Create options for names of output files.

//    public void run() {
////        System.out.println("The working directory is: " + System.getProperty("user.dir"));
//
//        BufferedWriter output_search;
//        BufferedWriter output_analysis;
//        SearchResult searchResult;
//        AnalysisResult analysisResult;
//
//        Stopwatch stopwatch = Stopwatch.createStarted();
//
//        validateFasta(fasta_path);
//
//        try {
//            List<String> input = readFile(input_path);
//
//            if (input != null) {
//                output_search = createFile(output_path, "search.tsv");
//                output_analysis = createFile(output_path, "analysis.tsv");
//
//                if (output_search != null && output_analysis != null) {
//                    Mapping mapping = new Mapping(inputType, showTopLevelPathways); // Load static structures needed for all the cases
//
//                    searchResult = Search.search(input, inputType, showTopLevelPathways, mapping,
//                            matchType, range, fasta_path);
//                    searchResult.writeToFile(output_search, separator);
//                    output_search.close();
//
//                    if (populationSize == -1) {
//                        setPopulationSize(mapping.getProteinsToReactions().keySet().size(), mapping.getProteoformsToReactions().keySet().size());
//                    }
//                    analysisResult = Analysis.analysis(searchResult, populationSize);
//                    analysisResult.writeToFile(output_analysis, inputType, separator);
//                    output_analysis.close();
//
//                    NetworkGenerator.writeGraphs(doGeneGraph, doUniprotGraph, doProteoformGraph,
//                            inputType, searchResult, mapping, output_path);
//
//                    stopwatch.stop();
//                    Duration duration = stopwatch.elapsed();
//                    System.out.println("Main finished (" + duration.toMillis() / 1000 + "s)");
//                }
//            }
//        } catch (IOException e) {
//            if (e.getMessage().contains("network") || e.getMessage().contains("directory")) {
//                System.out.println(e.getMessage());
//            } else {
//                System.out.println(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getMessage() + ": " +
//                        output_path + "search.txt  " +
//                        System.lineSeparator() +
//                        output_path + "analysis.txt");
//            }
//            System.exit(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getCode());
//        }
//    }

//    private void setPopulationSize(int totalProteins, int totalProteoforms) {
//        switch (inputType) {
//            case GENE:
//            case ENSEMBL:
//            case UNIPROT:
//            case RSID:
//            case CHRBP:
//            case VCF:
//            case PEPTIDE:
//                populationSize = totalProteins;
//                break;
//            case PROTEOFORM:
//            case MODIFIEDPEPTIDE:
//                populationSize = totalProteoforms;
//                break;
//            default:
//                populationSize = 0;
//                break;
//        }
//    }

//    private static void setInputPath(String value) throws ParseException {
//        if (value == null) {
//            throw new ParseException("Missing required option: i");
//        }
//
//        input_path = value;
//    }

//    private static void setOutputPath(String value) {
//        if (value == null) {
//            output_path = "";
//        } else {
//            output_path = value.endsWith("/") ? value : value + "/";
//        }
//    }
//
//    private void validateFasta(String value) throws CommandLine.ParameterException {
//        switch (inputType) {
//            case PEPTIDE:
//            case MODIFIEDPEPTIDE:
//                if (value == "" || value == null) {
//                    throw new CommandLine.ParameterException(new CommandLine(this), "Missing required option '--fasta=<fasta_path>' when the input type is PEPTIDE or MODIFIEDPEPTIDE");
//                } else {
//                    File f = new File(value);
//                    if (!f.exists() || f.isDirectory()) {
//                        System.out.println(Error.COULD_NOT_READ_FASTA_FILE.getMessage());
//                        System.exit(Error.COULD_NOT_READ_FASTA_FILE.getCode());
//                    }
//                    fasta_path = value;
//                }
//        }
//    }
//
//    private void setDoGeneGraph(boolean value) {
//        if (value) {
//            doGeneGraph = true;
//            return;
//        }
//        if (doDefaultGraph) {
//            switch (inputType) {
//                case GENE:
//                    doGeneGraph = true;
//                    return;
//            }
//        }
//        doGeneGraph = false;
//    }
//
//    private void setDoUniprotGraph(boolean value) {
//        if (value) {
//            doUniprotGraph = true;
//            return;
//        }
//        if (doDefaultGraph) {
//            switch (inputType) {
//                case UNIPROT:
//                case ENSEMBL:
//                case PEPTIDE:
//                case VCF:
//                case RSID:
//                case CHRBP:
//                    doUniprotGraph = true;
//                    return;
//            }
//        }
//        doUniprotGraph = false;
//    }
//
//    private void setDoProteoformGraph(boolean value) {
//        if (value) {
//            doProteoformGraph = true;
//            return;
//        }
//        if (doDefaultGraph) {
//            switch (inputType) {
//                case PROTEOFORM:
//                case MODIFIEDPEPTIDE:
//                    doProteoformGraph = true;
//                    return;
//            }
//        }
//        doProteoformGraph = false;
//    }


}



