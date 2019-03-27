package matcher;

import com.google.common.base.Stopwatch;
import methods.ora.Analysis;
import methods.ora.AnalysisResult;
import methods.search.Search;
import methods.search.SearchResult;
import model.Error;
import model.InputType;
import model.Mapping;
import model.MatchType;
import org.apache.commons.cli.ParseException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static matcher.tools.FileHandler.createFile;
import static matcher.tools.FileHandler.readFile;

@Command(name = "PathwayMatcher",
        header = "@|green %n PathwayMatcher 1.9.0 %n |@",
        description = "Searches the input in the Pathways of Reactome and performs pathway analysis. Optionally creates the interaction networks.",
        footer = {"@|cyan If you like the project star it on github and follow me on twitter!|@",
                "@|cyan This project is created and maintained by Remko Popma (@remkopopma)|@",
                ""},
        version = "PathwayMatcher 1.9.0")
public class PathwayMatcher implements Runnable {

    private static final String separator = "\t";    // Column separator

    // Search and analysis parameters
    @Option(names = {"-t", "--inputType"}, required = true, description = "Input file type. %nValid values: ${COMPLETION-CANDIDATES}. %nDefault: ${DEFAULT-VALUE}")
    private InputType inputType;

    public InputType getInputType() {
        return inputType;
    }

    @Option(names = {"-T", "--topLevelPathways"}, description = "Show Top Level Pathways in the search result.")
    private Boolean showTopLevelPathways = false;

    @Option(names = {"-m", "--matchType"}, description = "Proteoform match criteria. %nValid values: ${COMPLETION-CANDIDATES}. %nDefault: ${DEFAULT-VALUE}")
    private MatchType matchType = MatchType.SUBSET;

    @Option(names = {"-r", "--range"}, description = "Ptm sites range of error")
    private static Long range = 0L;
    private static int populationSize = -1;

    // File parameters
    @Option(names = {"-i", "--input"}, required = true, description = "Input file with path")
    private static String input_path = "intput.txt";

    @Option(names = {"-o", "-output"}, description = "Path to directory to set the output files: search.csv, analysis.csv and networks files.")
    private static String output_path = "";

    // TODO: Create options for names of output files.

    @Option(names = {"-f", "--fasta"}, description = "Path and name of the fasta file containing the Proteins where to find the peptides.")
    private static String fasta_path = "";

    // Graph parameters
    @Option(names = {"-g", "--graph"}, description = "Create default connection graph: gene, protein or proteoform interaction network according on the input type.")
    private static boolean doDefaultGraph = false;

    @Option(names = {"-gg", "--graphGene"}, description = "Create gene connection graph")
    private static boolean doGeneGraph = false;

    @Option(names = {"-gp", "--graphProteoform"}, description = "Create proteoform connection graph")
    private boolean doUniprotGraph = false;

    @Option(names = {"-gu", "--graphUniprot"}, description = "Create protein connection graph")
    private static boolean doProteoformGraph = false;

    @Option(names = {"-v", "-version", "--version"}, versionHelp = true, description = "Show version information and exit")
    boolean versionInfoRequested;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help message and quits.")
    private boolean usageHelpRequested = false;

    public void callCommandLine(String args[]){
        new CommandLine(this).setCaseInsensitiveEnumValuesAllowed(true)
                .parseWithHandlers(new CommandLine.RunLast().useOut(System.out),
                        CommandLine.defaultExceptionHandler().useErr(System.err), args);
    }

    public static void main(String args[]) {
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
    }

    @Override
    public void run() {
        System.out.println("The working directory is: " + System.getProperty("user.dir"));

        BufferedWriter output_search;
        BufferedWriter output_analysis;
        SearchResult searchResult;
        AnalysisResult analysisResult;

        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            List<String> input = readFile(input_path);

            output_search = createFile(output_path, "search.tsv");
            Mapping mapping = new Mapping(inputType, showTopLevelPathways); // Load static structures needed for all the cases

            searchResult = Search.search(input, inputType, showTopLevelPathways, mapping,
                    matchType, range, fasta_path);
            searchResult.writeToFile(output_search, separator);
            output_search.close();

            output_analysis = createFile(output_path, "analysis.tsv");
            if (populationSize == -1) {
                setPopulationSize(mapping.getProteinsToReactions().keySet().size(), mapping.getProteoformsToReactions().keySet().size());
            }
            analysisResult = Analysis.analysis(searchResult, populationSize);
            analysisResult.writeToFile(output_analysis, inputType, separator);
            output_analysis.close();

            NetworkGenerator.writeGraphs(doGeneGraph, doUniprotGraph, doProteoformGraph,
                    inputType, searchResult, mapping, output_path);

            stopwatch.stop();
            Duration duration = stopwatch.elapsed();
            System.out.println("PathwayMatcher finished (" + duration.toMillis() / 1000 + "s)");

        } catch (IOException e) {
            if (e.getMessage().contains("network") || e.getMessage().contains("directory")) {
                System.out.println(e.getMessage());
            } else {
                System.out.println(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getMessage() + ": " +
                        output_path + "search.txt  " +
                        System.lineSeparator() +
                        output_path + "analysis.txt");
            }
            System.exit(Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getCode());
        }
    }

    private void setPopulationSize(int totalProteins, int totalProteoforms) {
        switch (inputType) {
            case GENE:
            case ENSEMBL:
            case UNIPROT:
            case RSID:
            case CHRBP:
            case VCF:
            case PEPTIDE:
                populationSize = totalProteins;
                break;
            case PROTEOFORM:
            case MODIFIEDPEPTIDE:
                populationSize = totalProteoforms;
                break;
            default:
                populationSize = 0;
                break;
        }
    }

    private static void setInputPath(String value) throws ParseException {
        if (value == null) {
            throw new ParseException("Missing required option: i");
        }

        input_path = value;
    }

    private static void setOutputPath(String value) {
        if (value == null) {
            output_path = "";
        } else {
            output_path = value.endsWith("/") ? value : value + "/";
        }
    }

    private void setFasta(String value) throws ParseException {
        switch (inputType) {
            case PEPTIDE:
            case MODIFIEDPEPTIDE:
                if (value == null) {
                    throw new ParseException("Missing required option: f");
                } else {
                    File f = new File(value);
                    if (!f.exists() || f.isDirectory()) {
                        System.out.println(Error.COULD_NOT_READ_FASTA_FILE.getMessage());
                        System.exit(Error.COULD_NOT_READ_FASTA_FILE.getCode());
                    }
                    fasta_path = value;
                }
        }
    }

    private void setDoGeneGraph(boolean value) {
        if (value) {
            doGeneGraph = true;
            return;
        }
        if (doDefaultGraph) {
            switch (inputType) {
                case GENE:
                    doGeneGraph = true;
                    return;
            }
        }
        doGeneGraph = false;
    }

    private void setDoUniprotGraph(boolean value) {
        if (value) {
            doUniprotGraph = true;
            return;
        }
        if (doDefaultGraph) {
            switch (inputType) {
                case UNIPROT:
                case ENSEMBL:
                case PEPTIDE:
                case VCF:
                case RSID:
                case CHRBP:
                    doUniprotGraph = true;
                    return;
            }
        }
        doUniprotGraph = false;
    }

    private void setDoProteoformGraph(boolean value) {
        if (value) {
            doProteoformGraph = true;
            return;
        }
        if (doDefaultGraph) {
            switch (inputType) {
                case PROTEOFORM:
                case MODIFIEDPEPTIDE:
                    doProteoformGraph = true;
                    return;
            }
        }
        doProteoformGraph = false;
    }


}



