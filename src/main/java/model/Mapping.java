package model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.TreeMultimap;

import java.io.*;
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

    public Mapping(InputType inputType, boolean showTopLevelPathways, String mapping_path) throws FileNotFoundException {
        load(inputType, showTopLevelPathways, mapping_path);
    }

    public void load(InputType inputType, boolean showTopLevelPathways, String mapping_path) throws FileNotFoundException {
        switch (inputType) {
            case GENE:
                loadMapsForGenes(mapping_path);
                break;
            case ENSEMBL:
                loadMapsEnsembl(mapping_path);
                break;
            case UNIPROT:
                loadMapsForUniprot(mapping_path);
                break;
            case PEPTIDE:
                loadMapsForUniprot(mapping_path);
                break;
            case RSID:
                loadMapsForRsids(mapping_path);
                break;
            case CHRBP:
            case VCF:
                loadMapsChrBp(mapping_path);
                break;
            case PROTEOFORM:
                loadMapsForProteoforms(mapping_path);
                break;
            case MODIFIEDPEPTIDE:
                loadMapsForModifiedPeptides(mapping_path);
                break;
            default:
                break;
        }

        if (showTopLevelPathways) {
            pathwaysToTopLevelPathways = (ImmutableSetMultimap<String, String>) getSerializedObject(mapping_path, "pathwaysToTopLevelPathways.gz");
        }
    }

    private void loadMapsBasic(String mapping_path) throws FileNotFoundException {
        if (reactions.size() == 0) {
            reactions = (ImmutableMap<String, Reaction>) getSerializedObject(mapping_path, "reactions.gz");
        }
        if (pathways.size() == 0) {
            pathways = (ImmutableMap<String, Pathway>) getSerializedObject(mapping_path, "pathways.gz");
        }
        if (reactionsToPathways.size() == 0) {
            reactionsToPathways = (ImmutableSetMultimap<String, String>) getSerializedObject(mapping_path, "reactionsToPathways.gz");
        }
    }

    private void loadMapsForGenes(String mapping_path) throws FileNotFoundException {
        if (genesToProteins.size() == 0) {
            genesToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject(mapping_path, "genesToProteins.gz");
        }
        loadMapsForUniprot(mapping_path);   // reactions, pathways, proteinsToReactions, reactionsToPathways
    }

    private void loadMapsForUniprot(String mapping_path) throws FileNotFoundException {
        loadMapsBasic(mapping_path);
        if (proteinsToNames.size() == 0) {
            proteinsToNames = (ImmutableMap<String, String>) getSerializedObject(mapping_path, "proteinsToNames.gz");
        }
        if (proteinsToReactions.size() == 0) {
            proteinsToReactions = (ImmutableSetMultimap<String, String>) getSerializedObject(mapping_path, "proteinsToReactions.gz");
        }
    }

    private void loadMapsEnsembl(String mapping_path) throws FileNotFoundException {
        loadMapsForUniprot(mapping_path);
        if (ensemblToUniprot.size() == 0)
            ensemblToUniprot = (ImmutableSetMultimap<String, String>) getSerializedObject(mapping_path, "ensemblToProteins.gz");
    }

    private void loadMapsForRsids(String mapping_path) throws FileNotFoundException {
        loadMapsForUniprot(mapping_path);
    }

    public ImmutableSetMultimap<String, String> getRsidsToProteins(int chromosome, String mapping_path) throws FileNotFoundException {
        if (loadedChromosome != chromosome) {
            System.out.println("Loading data for chromosome " + chromosome);
            this.rsIdsToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject(mapping_path, "rsIdsToProteins" + chromosome + ".gz");
            loadedChromosome = chromosome;
        }
        return rsIdsToProteins;
    }

    private void loadMapsChrBp(String mapping_path) throws FileNotFoundException {
        loadMapsForUniprot(mapping_path);
    }

    public ImmutableSetMultimap<Long, String> getChrBpToProteins(int chromosome, String mapping_path) throws FileNotFoundException {
        if (loadedChromosome != chromosome) {
            System.out.println("Loading data for chromosome " + chromosome);
            this.chrBpToProteins = (ImmutableSetMultimap<Long, String>) getSerializedObject(mapping_path, "chrBpToProteins" + chromosome + ".gz");
            loadedChromosome = chromosome;
        }
        return chrBpToProteins;
    }

    public void loadMapsForProteoforms(String mapping_path) throws FileNotFoundException {
        loadMapsBasic(mapping_path);
        if (proteinsToProteoforms.size() == 0) {
            proteinsToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject(mapping_path, "proteinsToProteoforms.gz");
        }

        if (proteoformsToReactions.size() == 0) {
            proteoformsToReactions = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject(mapping_path, "proteoformsToReactions.gz");
        }
    }

    private void loadMapsForModifiedPeptides(String mapping_path) throws FileNotFoundException {
        loadMapsForProteoforms(mapping_path);
    }

    // Empty path means use resources.
    // Non empty path means search for the serialized file in the path
    public static Object getSerializedObject(String path, String fileName) throws FileNotFoundException {
        Object obj = null;
        InputStream inputStream;
        if (path.length() > 0) {
            if(!path.endsWith("/")){
                path += "./";
            }
            File file = new File(path + fileName);
            if (!file.exists()) {
                throw new FileNotFoundException("Could not find the file: " + fileName + " at the location: " + path);
            }
            inputStream = new FileInputStream(file);
        } else {
            if(ClassLoader.getSystemResource(fileName) == null){
                throw new FileNotFoundException("Could not find the file: " +  fileName + " in the jar file resources.");
            }
            inputStream = ClassLoader.getSystemResourceAsStream(fileName);
        }
        try {
            GZIPInputStream gis = new GZIPInputStream(inputStream);
            ObjectInputStream ois = new ObjectInputStream(gis);
            obj = ois.readObject();
            ois.close();
        } catch (Exception ex) {
            System.out.println("Error loading file: " + fileName);
            ex.printStackTrace();
        }
        return obj;
    }

    public void loadMapsForGeneNetwork(String mapping_path) throws FileNotFoundException {

        loadMapsForGenes(mapping_path); // genesToProteins, proteinsToReactions, reactions, pathways, reactionsToPathways

        //proteinsToGenes
        for (Map.Entry<String, String> entry : genesToProteins.entries()) {
            String gene = entry.getKey();
            String protein = entry.getValue();
            proteinsToGenes.put(protein, gene);
        }

        loadMapsForProteinNetwork(mapping_path); // proteinsToNames, proteinsToComplexes, complexesToProteins, proteinsToSets, setsToProteins
    }

    public void loadMapsForProteinNetwork(String mapping_path) throws FileNotFoundException {

        loadMapsForUniprot(mapping_path);  // proteinsToReactions, reactions, pathways, reactionsToPathways

        proteinsToNames = (ImmutableMap<String, String>) getSerializedObject(mapping_path, "proteinsToNames.gz");

        if (proteinsToComplexes.size() == 0) {
            proteinsToComplexes = (ImmutableSetMultimap<String, String>) getSerializedObject(mapping_path, "proteinsToComplexes.gz");
        }
        if (complexesToProteins.size() == 0) {
            complexesToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject(mapping_path, "complexesToProteins.gz");
        }
        if (setsToProteins.size() == 0) {
            setsToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject(mapping_path, "setsToProteins.gz");
        }

        if (proteinsToSets.size() == 0) {
            proteinsToSets = (ImmutableSetMultimap<String, String>) getSerializedObject(mapping_path, "proteinsToSets.gz");
        }
    }

    public void loadMapsForProteoformNetwork(String mapping_path) throws FileNotFoundException {

        loadMapsForProteoforms(mapping_path);

        proteinsToNames = (ImmutableMap<String, String>) getSerializedObject(mapping_path, "proteinsToNames.gz");

        if (proteoformsToComplexes.size() == 0) {
            proteoformsToComplexes = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject(mapping_path, "proteoformsToComplexes.gz");
        }
        if (complexesToProteoforms.size() == 0) {
            complexesToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject(mapping_path, "complexesToProteoforms.gz");
        }
        if (setsToProteoforms.size() == 0) {
            setsToProteoforms = (ImmutableSetMultimap<String, Proteoform>) getSerializedObject(mapping_path, "setsToProteoforms.gz");
        }
        if (proteoformsToSets.size() == 0) {
            proteoformsToSets = (ImmutableSetMultimap<Proteoform, String>) getSerializedObject(mapping_path, "proteoformsToSets.gz");
        }
    }
}
