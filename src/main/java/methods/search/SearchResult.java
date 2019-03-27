package methods.search;

import com.google.common.collect.TreeMultimap;
import model.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

import static model.Error.ERROR_WITH_OUTPUT_FILE;
import static model.Error.sendError;

public class SearchResult {
    private Set<Pathway> hitPathways = new TreeSet<>();
    private Set<Proteoform> hitProteoforms = new HashSet<>(); // Reference proteoforms that match input proteoforms

    private Set<String> inputProteins = new HashSet<>(); // Valid input protein accessions. These may not be in the reference data
    private Set<String> matchedProteins = new TreeSet<>(); // Proteins that are selected to search and exist in the reference data
    private Set<String> hitProteins = new TreeSet<>(); // Proteins that map at least to a reaction

    private Set<String> inputEnsembl = new HashSet<>(); // Valid input protein accessions. These may not be in the reference data
    private Set<String> matchedEnsembl = new TreeSet<>(); // Proteins that are selected to search and exist in the reference data
    private Set<String> hitEnsembl = new TreeSet<>(); // Proteins that map at least to a reaction

    private Set<String> matchedGenes = new TreeSet<>(); // Genes that are selected to search and exist in the reference data
    private Set<String> hitGenes = new TreeSet<>(); // These are in the reference data
    private Set<String> inputGenes = new TreeSet<>(); // Valid input genes. May or may not be in reference database.

    private Set<String> hitRsid = new HashSet<>();
    private Set<String> inputRsid = new HashSet<>();
    private Set<String> matchedRsid = new HashSet<>();

    private TreeMultimap<Integer, Long> hitChrBp = TreeMultimap.create();
    private TreeMultimap<Integer, Long> inputChrBp = TreeMultimap.create();
    private TreeMultimap<Integer, Long> matchedChrBp = TreeMultimap.create();


    private Set<Proteoform> inputProteoforms = new HashSet<>(); // These may not be in the reference data
    private Set<Proteoform> matchedProteoforms = new HashSet<>(); // Proteoforms in the input that had a matched reference proteoform

    private List<String> headers = new ArrayList<>();
    private List<String[]> records = new ArrayList<>();

    MessageStatus status;

    public Set<Pathway> getHitPathways() {
        return hitPathways;
    }

    public Set<String> getHitGenes() {
        return hitGenes;
    }

    public Set<String> getMatchedProteins() {
        return matchedProteins;
    }

    public Set<String> getHitProteins() {
        return hitProteins;
    }

    public Set<Proteoform> getHitProteoforms() {
        return hitProteoforms;
    }

    public Set<String> getInputProteins() {
        return inputProteins;
    }

    public Set<Proteoform> getInputProteoforms() {
        return inputProteoforms;
    }

    public Set<Proteoform> getMatchedProteoforms() {
        return matchedProteoforms;
    }

    void addRecord(String[] record) {
        this.records.add(record);
    }

    public Set<String> getInputGenes() {
        return inputGenes;
    }

    void setStatus(MessageStatus status) {
        this.status = status;
    }

    Set<String> getHitRsid() {
        return hitRsid;
    }

    Set<String> getInputRsid() {
        return inputRsid;
    }

    Set<String> getMatchedRsid() {
        return matchedRsid;
    }

    TreeMultimap<Integer, Long> getHitChrBp() {
        return hitChrBp;
    }

    TreeMultimap<Integer, Long> getInputChrBp() {
        return inputChrBp;
    }

    TreeMultimap<Integer, Long> getMatchedChrBp() {
        return matchedChrBp;
    }

    public Set<String> getInputEnsembl() {
        return inputEnsembl;
    }

    public Set<String> getMatchedEnsembl() {
        return matchedEnsembl;
    }

    public Set<String> getHitEnsembl() {
        return hitEnsembl;
    }

    public Set<String> getMatchedGenes() {
        return matchedGenes;
    }

    SearchResult(InputType inputType, boolean showTopLevelPathways) {

        switch (inputType) {
            case PROTEOFORM:
            case MODIFIEDPEPTIDE:
                this.headers.add("PROTEOFORM");
                break;
            case GENE:
                this.headers.add("GENE");
                break;
            case ENSEMBL:
                this.headers.add("ENSEMBL");
                break;
            case RSID:
                this.headers.add("RSID");
                break;
            case CHRBP:
            case VCF:
                this.headers.add("CHROMOSOME");
                this.headers.add("BASE_PAIR");
                break;
            default:
                break;
        }

        this.headers.add("UNIPROT");
        this.headers.add("REACTION_STID");
        this.headers.add("REACTION_DISPLAY_NAME");
        this.headers.add("PATHWAY_STID");
        this.headers.add("PATHWAY_DISPLAY_NAME");

        if (showTopLevelPathways) {
            this.headers.add("TOP_LEVEL_PATHWAY_DISPLAY_NAME");
        }
    }

    public void writeToFile(BufferedWriter bw, String separator) {
        try {
            for (String header : this.headers) {
                bw.write(header + separator);
            }
            bw.newLine();

            for (String[] r : records) {
                for (int i = 0; i < r.length; i++) {
                    if (i > 0) {
                        bw.write(separator);
                    }
                    bw.write(r[i]);
                }
                bw.newLine();
            }
        } catch (IOException ex) {
            sendError(ERROR_WITH_OUTPUT_FILE);
        }
        System.out.println("Finished writing Matching results.");
    }

    public void calculateMatchedGenes(Mapping mapping) {
        for (String protein : matchedProteins) {
            matchedGenes.addAll(mapping.getProteinsToGenes().get(protein));
        }
    }

    public void calculateMatchedProteoforms(Mapping mapping) {
        for (String protein : matchedProteins) {
            matchedProteoforms.addAll(mapping.getProteinsToProteoforms().get(protein));
        }
    }

    public boolean containsPathwayByStid(String pathway_stid) {
        for(Pathway pathway : this.hitPathways) {
            if(pathway.getStId().equals(pathway_stid)) {
                return true;
            }
        }
        return false;
    }

    public Pathway getHitPathwayByStid(String stid){
        for(Pathway pathway : this.hitPathways) {
            if(pathway.getStId().equals(stid)) {
                return pathway;
            }
        }
        return null;
    }

}
