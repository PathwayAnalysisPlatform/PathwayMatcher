package matcher;

import com.google.common.base.Stopwatch;
import methods.ora.Analysis;
import methods.ora.AnalysisResult;
import methods.search.Search;
import methods.search.SearchResult;
import model.InputType;
import model.Mapping;
import model.MatchType;
import model.Error;
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

/**
 * Class to run the full PathwayMatcher. It calls the chosen commands from the command line interface.
 */
public class Main {

    public static CommandLine commandLine;

    // Parent command for matching
    @Command(name = "java -jar PathwayMatcher.jar",
            header = "@|green %n PathwayMatcher 1.9.0 %n |@",
            description = "Matches the input to reactions and pathways",
            footer = {"@|cyan %n If you like the project, star it on github. |@", ""},
            version = "Main 1.9.0",
            subcommands = {
                    MatchProteoforms.class,
                    MatchGenes.class,
                    MatchUniprot.class,
                    MatchEnsembl.class,
                    MatchVCF.class,
                    MatchChrBp.class,
                    MatchRsId.class,
                    MatchPeptides.class,
                    MatchModifiedPeptides.class,
                    CommandLine.HelpCommand.class
            }
    )
    static class PathwayMatcher implements Runnable {

        @Option(names = {"-v", "--version"}, versionHelp = true, description = "Show version information and exit")
        boolean versionInfoRequested;

        @Override
        public void run() {
        }
    }

    static abstract class MatchSubcommand implements Runnable {


        @Option(names = {"-i", "--input"}, required = true, description = "Input file with path")
        String input_path;

        public String getInput_path() {
            return input_path;
        }

        @Option(names = {"-o", "-output"}, description = "Path to directory to set the output files: search.csv, analysis.csv and networks files.")
        String output_path = "";

        @Option(names = {"-T", "--topLevelPathways"}, description = "Show Top Level Pathways in the search result.")
        boolean showTopLevelPathways = false;
        public boolean isShowTopLevelPathways() {
            return showTopLevelPathways;
        }

        @Option(names = {"-g", "--graph"}, description = "Create default connection graph according to input type.")
        boolean doDefaultGraph = false;
        public boolean isDoDefaultGraph() {
            return doDefaultGraph;
        }

        @Option(names = {"-gg", "--graphGene"}, description = "Create gene connection graph")
        boolean doGeneGraph = false;

        @Option(names = {"-gp", "--graphProteoform"}, description = "Create proteoform connection graph")
        boolean doUniprotGraph = false;

        @Option(names = {"-gu", "--graphUniprot"}, description = "Create protein connection graph")
        boolean doProteoformGraph = false;

        int populationSize = -1;
        List<String> input;
        InputType inputType;
        final String separator = "\t";    // Column separator
        Mapping mapping;
        BufferedWriter output_search;
        BufferedWriter output_analysis;
        SearchResult searchResult;
        AnalysisResult analysisResult;

        @Override
        public void run() {

        }

        protected void match() {
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                input = readFile(input_path);

                if (input != null) {
                    output_search = createFile(output_path, "search.tsv");
                    output_analysis = createFile(output_path, "analysis.tsv");

                    if (output_search != null && output_analysis != null) {
                        mapping = new Mapping(inputType, showTopLevelPathways); // Load static structures needed for all the cases

                        searchResult = search();
                        searchResult.writeToFile(output_search, separator);
                        output_search.close();

                        setPopulationSize();
                        analysisResult = Analysis.analysis(searchResult, populationSize);
                        analysisResult.writeToFile(output_analysis, inputType, separator);
                        output_analysis.close();

                        if (doDefaultGraph) {
                            setDoCorrespondingGraph();
                        }
                        NetworkGenerator.writeGraphs(doGeneGraph, doUniprotGraph, doProteoformGraph,
                                inputType, searchResult, mapping, output_path);

                        stopwatch.stop();
                        Duration duration = stopwatch.elapsed();
                        System.out.println("Main finished (" + duration.toMillis() / 1000 + "s)");
                    }
                }
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

        abstract SearchResult search();

        abstract void setPopulationSize();

        abstract void setDoCorrespondingGraph();
    }

    @Command(name = "match-genes", description = "Match a list of gene names")
    static class MatchGenes extends MatchSubcommand {
        @Override
        public void run() {
            inputType = InputType.GENE;
            match();
        }

        @Override
        SearchResult search() {
            return Search.searchWithGene(input, mapping, showTopLevelPathways);
        }

        @Override
        void setPopulationSize() {
            populationSize = mapping.getProteinsToReactions().keySet().size();
        }

        @Override
        void setDoCorrespondingGraph() {
            doGeneGraph = true;
        }
    }

    @Command(name = "match-uniprot", description = "Match a list of UniProt protein accessions")
    static class MatchUniprot extends MatchSubcommand {
        @Override
        public void run() {
            inputType = InputType.UNIPROT;
            match();
        }

        @Override
        SearchResult search() {
            return Search.searchWithUniProt(input, mapping, showTopLevelPathways);
        }

        @Override
        void setPopulationSize() {
            populationSize = mapping.getProteinsToReactions().keySet().size();
        }

        @Override
        void setDoCorrespondingGraph() {
            doUniprotGraph = true;
        }
    }

    @Command(name = "match-ensembl", description = "Match a list of Ensembl protein identifiers")
    static class MatchEnsembl extends MatchSubcommand {
        @Override
        public void run() {
            inputType = InputType.ENSEMBL;
            match();
        }

        @Override
        SearchResult search() {
            return Search.searchWithEnsembl(input, mapping, showTopLevelPathways);
        }

        @Override
        void setPopulationSize() {
            populationSize = mapping.getProteinsToReactions().keySet().size();
        }

        @Override
        void setDoCorrespondingGraph() {
            doUniprotGraph = true;
        }
    }

    @Command(name = "match-vcf", description = "Match a list of genetic variants in VCF format")
    static class MatchVCF extends MatchSubcommand {
        @Override
        public void run() {
            inputType = InputType.VCF;
            match();
        }

        @Override
        SearchResult search() {
            return Search.searchWithChrBp(input, mapping, showTopLevelPathways);
        }

        @Override
        void setPopulationSize() {
            populationSize = mapping.getProteinsToReactions().keySet().size();
        }

        @Override
        void setDoCorrespondingGraph() {
            doUniprotGraph = true;
        }
    }

    @Command(name = "match-chrbps", description = "Match a list of genetic variants as chromosome and base pairs")
    static class MatchChrBp extends MatchSubcommand {
        @Override
        public void run() {
            System.out.println("In match-variants-chrbps");
        }

        @Override
        SearchResult search() {
            return Search.searchWithChrBp(input, mapping, showTopLevelPathways);
        }

        @Override
        void setPopulationSize() {
            populationSize = mapping.getProteinsToReactions().keySet().size();
        }

        @Override
        void setDoCorrespondingGraph() {
            doUniprotGraph = true;
        }
    }

    @Command(name = "match-rsids", description = "Match a list of genetic variants as RsIds")
    static class MatchRsId extends MatchSubcommand {
        @Override
        public void run() {
            System.out.println("In MatchRsId");
        }

        @Override
        SearchResult search() {
            return Search.searchWithRsId(input, mapping, showTopLevelPathways);
        }

        @Override
        void setPopulationSize() {
            populationSize = mapping.getProteinsToReactions().keySet().size();
        }

        @Override
        void setDoCorrespondingGraph() {
            doUniprotGraph = true;
        }
    }

    static abstract class MatchSubcommandWithModifications extends MatchSubcommand {
        @Option(names = {"-m", "--matchType"}, description = "Proteoform match criteria. %nValid values: ${COMPLETION-CANDIDATES}. %nDefault: ${DEFAULT-VALUE}", required = true)
        MatchType matchType = MatchType.SUBSET;

        @Option(names = {"-r", "--range"}, description = "Ptm sites range of error")
        Long range = 0L;
    }

    @Command(name = "match-proteoforms", description = "Match a list of proteoforms to reactions and pathways")
    static class MatchProteoforms extends MatchSubcommandWithModifications {
        @Override
        public void run() {
            inputType = InputType.PROTEOFORM;
            match();
        }

        @Override
        SearchResult search() {
            return Search.searchWithProteoform(input, mapping, showTopLevelPathways, matchType, range);
        }

        @Override
        void setPopulationSize() {
            populationSize = mapping.getProteoformsToReactions().keySet().size();
        }

        @Override
        void setDoCorrespondingGraph() {
            doProteoformGraph = true;
        }
    }

    static abstract class MatchSubcommandPeptides extends MatchSubcommand {
        @Option(names = {"-f", "--fasta"}, description = "Path and name of the fasta file containing the Proteins where to find the peptides.", required = true)
        String fasta_path = "";

        protected boolean isValidFasta(String value) throws CommandLine.ParameterException {

            if (value == "" || value == null) {
                throw new CommandLine.ParameterException(new CommandLine(this), "Missing required option '--fasta=<fasta_path>' when the input type is PEPTIDE or MODIFIEDPEPTIDE");
            } else {
                File f = new File(value);
                if (!f.exists() || f.isDirectory()) {
                    System.out.println(Error.COULD_NOT_READ_FASTA_FILE.getMessage());
                    return false;
                }
            }
            return true;
        }
    }

    @Command(name = "match-peptides", description = "Match a list of peptides")
    static class MatchPeptides extends MatchSubcommandPeptides {

        @Option(names = {"-f", "--fasta"}, description = "Path and name of the fasta file containing the Proteins where to find the peptides.", required = true)
        String fasta_path = "";

        @Override
        public void run() {
            inputType = InputType.PEPTIDE;
            if (isValidFasta(fasta_path)) {
                match();
            }
        }

        @Override
        SearchResult search() {
            return Search.searchWithPeptide(input, mapping, showTopLevelPathways, fasta_path);
        }

        @Override
        void setPopulationSize() {
            populationSize = mapping.getProteinsToReactions().keySet().size();
        }

        @Override
        void setDoCorrespondingGraph() {
            doUniprotGraph = true;
        }
    }

    @Command(name = "match-peptides-modified", description = "Match a list of peptides with post translational modifications")
    static class MatchModifiedPeptides extends MatchSubcommandPeptides {

        @Option(names = {"-m", "--matchType"}, description = "Proteoform match criteria. %nValid values: ${COMPLETION-CANDIDATES}. %nDefault: ${DEFAULT-VALUE}", required = true)
        MatchType matchType = MatchType.SUBSET;

        @Option(names = {"-r", "--range"}, description = "Ptm sites range of error")
        Long range = 0L;

        @Override
        public void run() {
            inputType = InputType.MODIFIEDPEPTIDE;
            if (isValidFasta(fasta_path)) {
                match();
            }
        }

        @Override
        SearchResult search() {
            return Search.searchWithModifiedPeptide(input, mapping, showTopLevelPathways, matchType, range, fasta_path);
        }

        @Override
        void setPopulationSize() {
            populationSize = mapping.getProteoformsToReactions().keySet().size();
        }

        @Override
        void setDoCorrespondingGraph() {
            doProteoformGraph = true;
        }
    }

    public static void main(String[] args) {
        commandLine = new CommandLine(new PathwayMatcher());
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        commandLine.parseWithHandlers(new CommandLine.RunAll().useOut(System.out), CommandLine.defaultExceptionHandler().useErr(System.err), args);

        if (args.length == 0) {
            commandLine.usage(System.out);
        }
    }
}



