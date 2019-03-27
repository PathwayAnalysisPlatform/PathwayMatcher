package model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.TreeMultimap;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class Mapping {
    private ImmutableMap<String, String> proteinsToNames = ImmutableMap.of();  // Protein accession and name pairs
    private ImmutableMap<String, Reaction> reactions = ImmutableMap.of();
    private ImmutableMap<String, Pathway> pathways = ImmutableMap.of();
    private ImmutableSetMultimap<String, String> rsIdsToProteins = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<Long, String> chrBpToProteins = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<String, String> genesToProteins = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<String, String> ensemblToUniprot = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<String, Proteoform> proteinsToProteoforms = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<Proteoform, String> proteoformsToReactions = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<String, String> proteinsToReactions = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<String, String> reactionsToPathways = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<String, String> pathwaysToTopLevelPathways = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<String, String> proteinsToComplexes = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<String, String> complexesToProteins = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<String, String> setsToProteins = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<String, String> proteinsToSets = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<Proteoform, String> proteoformsToComplexes = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<String, Proteoform> complexesToProteoforms = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<String, Proteoform> setsToProteoforms = ImmutableSetMultimap.of();
    private ImmutableSetMultimap<Proteoform, String> proteoformsToSets = ImmutableSetMultimap.of();
    private TreeMultimap<String, String> proteinsToGenes = TreeMultimap.create();
    private int loadedChromosome = 0;

    public ImmutableMap<String, String> getProteinsToNames() {
        return proteinsToNames;
    }

    public ImmutableMap<String, Reaction> getReactions() {
        return reactions;
    }

    public ImmutableMap<String, Pathway> getPathways() {
        return pathways;
    }

    public ImmutableSetMultimap<String, String> getGenesToProteins() {
        return genesToProteins;
    }

    public ImmutableSetMultimap<String, String> getEnsemblToUniprot() {
        return ensemblToUniprot;
    }

    public ImmutableSetMultimap<String, Proteoform> getProteinsToProteoforms() {
        return proteinsToProteoforms;
    }

    public ImmutableSetMultimap<Proteoform, String> getProteoformsToReactions() {
        return proteoformsToReactions;
    }

    public ImmutableSetMultimap<String, String> getProteinsToReactions() {
        return proteinsToReactions;
    }

    public ImmutableSetMultimap<String, String> getReactionsToPathways() {
        return reactionsToPathways;
    }

    public ImmutableSetMultimap<String, String> getPathwaysToTopLevelPathways() {
        return pathwaysToTopLevelPathways;
    }

    public ImmutableSetMultimap<String, String> getProteinsToComplexes() {
        return proteinsToComplexes;
    }

    public ImmutableSetMultimap<String, String> getComplexesToProteins() {
        return complexesToProteins;
    }

    public ImmutableSetMultimap<String, String> getSetsToProteins() {
        return setsToProteins;
    }

    public ImmutableSetMultimap<String, String> getProteinsToSets() {
        return proteinsToSets;
    }

    public ImmutableSetMultimap<Proteoform, String> getProteoformsToComplexes() {
        return proteoformsToComplexes;
    }

    public ImmutableSetMultimap<String, Proteoform> getComplexesToProteoforms() {
        return complexesToProteoforms;
    }

    public ImmutableSetMultimap<String, Proteoform> getSetsToProteoforms() {
        return setsToProteoforms;
    }

    public ImmutableSetMultimap<Proteoform, String> getProteoformsToSets() {
        return proteoformsToSets;
    }

    public TreeMultimap<String, String> getProteinsToGenes() {
        return proteinsToGenes;
    }

    public Mapping(InputType inputType, boolean showTopLevelPathways) {
        load(inputType, showTopLevelPathways);
    }

    public void load(InputType inputType, boolean showTopLevelPathways) {
        switch (inputType) {
            case GENE:
                loadMapsForGenes();
                break;
            case ENSEMBL:
                loadMapsEnsembl();
                break;
            case UNIPROT:
                loadMapsForUniprot();
                break;
            case PEPTIDE:
                loadMapsForUniprot();
                break;
            case RSID:
                loadMapsForRsids();
                break;
            case CHRBP:
            case VCF:
                loadMapsChrBp();
                break;
            case PROTEOFORM:
                loadMapsForProteoforms();
                break;
            case MODIFIEDPEPTIDE:
                loadMapsForModifiedPeptides();
                break;
            default:
                break;
        }

        if (showTopLevelPathways) {
            pathwaysToTopLevelPathways = (ImmutableSetMultimap<String, String>) getSerializedObject("pathwaysToTopLevelPathways.gz");
        }
    }

    private void loadMapsBasic() {
        if (reactions.size() == 0) {
            reactions = (ImmutableMap<String, Reaction>) getSerializedObject("reactions.gz");
        }
        if (pathways.size() == 0) {
            pathways = (ImmutableMap<String, Pathway>) getSerializedObject("pathways.gz");
        }
        if (reactionsToPathways.size() == 0) {
            reactionsToPathways = (ImmutableSetMultimap<String, String>) getSerializedObject("reactionsToPathways.gz");
        }
    }

    private void loadMapsForGenes() {
        if (genesToProteins.size() == 0) {
            genesToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("genesToProteins.gz");
        }
        loadMapsForUniprot();   // reactions, pathways, proteinsToReactions, reactionsToPathways
    }

    private void loadMapsForUniprot() {
        loadMapsBasic();
        if (proteinsToNames.size() == 0) {
            proteinsToNames = (ImmutableMap<String, String>) getSerializedObject("proteinsToNames.gz");
        }
        if (proteinsToReactions.size() == 0) {
            proteinsToReactions = (ImmutableSetMultimap<String, String>) getSerializedObject("proteinsToReactions.gz");
        }
    }

    private void loadMapsEnsembl() {
        loadMapsForUniprot();
        if (ensemblToUniprot.size() == 0)
            ensemblToUniprot = (ImmutableSetMultimap<String, String>) getSerializedObject("ensemblToProteins.gz");
    }

    private void loadMapsForRsids() {
        loadMapsForUniprot();
    }

    public ImmutableSetMultimap<String, String> getRsidsToProteins(int chromosome) {
        if (loadedChromosome != chromosome) {
            System.out.println("Loading data for chromosome " + chromosome);
            this.rsIdsToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("rsIdsToProteins" + chromosome + ".gz");
            loadedChromosome = chromosome;
        }
        return rsIdsToProteins;
    }

    private void loadMapsChrBp() {
        loadMapsForUniprot();
    }

    public ImmutableSetMultimap<Long, String> getChrBpToProteins(int chromosome) {
        if (loadedChromosome != chromosome) {
            System.out.println("Loading data for chromosome " + chromosome);
            this.chrBpToProteins = (ImmutableSetMultimap<Long, String>) getSerializedObject("chrBpToProteins" + chromosome + ".gz");
            loadedChromosome = chromosome;
        }
        return chrBpToProteins;
    }

    private void loadMapsForProteoforms() {
        loadMapsBasic();
        if (proteinsToProteoforms.size() == 0) {
            proteinsToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject("proteinsToProteoforms.gz");
        }

        if (proteoformsToReactions.size() == 0) {
            proteoformsToReactions = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject("proteoformsToReactions.gz");
        }
    }

    private void loadMapsForModifiedPeptides() {
        loadMapsForProteoforms();
    }

    public static Object getSerializedObject(String fileName) {
        Object obj = null;
        try {
            InputStream fileStream = ClassLoader.getSystemResourceAsStream(fileName);
            GZIPInputStream gis = new GZIPInputStream(fileStream);
            ObjectInputStream ois = new ObjectInputStream(gis);
            obj = ois.readObject();
            ois.close();

        } catch (Exception ex) {
            System.out.println("Error loading file: " + fileName);
            ex.printStackTrace();
        }
        return obj;
    }

    public void loadMapsForGeneNetwork() {

        loadMapsForGenes(); // genesToProteins, proteinsToReactions, reactions, pathways, reactionsToPathways

        //proteinsToGenes
        for (Map.Entry<String, String> entry : genesToProteins.entries()) {
            String gene = entry.getKey();
            String protein = entry.getValue();
            proteinsToGenes.put(protein, gene);
        }

        loadMapsForProteinNetwork(); // proteinsToNames, proteinsToComplexes, complexesToProteins, proteinsToSets, setsToProteins
    }

    public void loadMapsForProteinNetwork() {

        loadMapsForUniprot();  // proteinsToReactions, reactions, pathways, reactionsToPathways

        proteinsToNames = (ImmutableMap<String, String>) getSerializedObject("proteinsToNames.gz");

        if (proteinsToComplexes.size() == 0) {
            proteinsToComplexes = (ImmutableSetMultimap<String, String>) getSerializedObject("proteinsToComplexes.gz");
        }
        if (complexesToProteins.size() == 0) {
            complexesToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("complexesToProteins.gz");
        }
        if (setsToProteins.size() == 0) {
            setsToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("setsToProteins.gz");
        }

        if (proteinsToSets.size() == 0) {
            proteinsToSets = (ImmutableSetMultimap<String, String>) getSerializedObject("proteinsToSets.gz");
        }
    }

    public void loadMapsForProteoformNetwork() {

        loadMapsForProteoforms();

        proteinsToNames = (ImmutableMap<String, String>) getSerializedObject("proteinsToNames.gz");

        if (proteoformsToComplexes.size() == 0) {
            proteoformsToComplexes = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject("proteoformsToComplexes.gz");
        }
        if (complexesToProteoforms.size() == 0) {
            complexesToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject("complexesToProteoforms.gz");
        }
        if (setsToProteoforms.size() == 0) {
            setsToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject("setsToProteoforms.gz");
        }
        if (proteoformsToSets.size() == 0) {
            proteoformsToSets = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject("proteoformsToSets.gz");
        }
    }
}
