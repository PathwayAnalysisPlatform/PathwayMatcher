package methods.ora;

import model.InputType;
import model.MessageStatus;
import model.Pathway;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;

import static model.Error.ERROR_WITH_OUTPUT_FILE;
import static model.Error.sendError;

public class AnalysisResult {

    private Set<Pathway> hitPathways;
    private MessageStatus status;

    public Set<Pathway> getHitPathways() {
        return hitPathways;
    }

    public void setHitPathways(Set<Pathway> hitPathways) {
        this.hitPathways = hitPathways;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public AnalysisResult(Set<Pathway> hitPathways, MessageStatus status) {
        this.hitPathways = hitPathways;
        this.status = status;
    }

    public void writeToFile(BufferedWriter outputAnalysis, InputType inputType, String separator) {
        try {
            // Write headers of the file
            outputAnalysis.write("Pathway StId" + separator + "Pathway Name" + separator + "# Entities Found"
                    + separator + "# Entities Total" + separator + "Entities Ratio" + separator + "Entities P-Value"
                    + separator + "Significant" + separator + "Entities FDR" + separator + "# Reactions Found"
                    + separator + "# Reactions Total" + separator + "Reactions Ratio" + separator + "Entities Found"
                    + separator + "Reactions Found" + System.lineSeparator());

            // For each pathway
            for (Pathway pathway : hitPathways) {

                String line = String.join(separator,
                        pathway.getStId(),
                        String.join("", "\"", pathway.getDisplayName(), "\""),
                        Integer.toString(pathway.getEntitiesFound().size()),
                        Integer.toString(pathway.getNumEntitiesTotal()),
                        Double.toString(pathway.getEntitiesRatio()),
                        Double.toString(pathway.getPValue()),
                        (pathway.getPValue() < 0.05 ? "Yes" : "No"),
                        Double.toString(pathway.getEntitiesFDR()),
                        Integer.toString(pathway.getReactionsFound().size()),
                        Integer.toString(pathway.getNumReactionsTotal()),
                        Double.toString(pathway.getReactionsRatio()),
                        pathway.getEntitiesFoundString(inputType),
                        pathway.getReactionsFoundString());
                outputAnalysis.write(line);
                outputAnalysis.newLine();
            }

            outputAnalysis.close();

            System.out.println("Finished writing Analysis results.");

        } catch (IOException ex) {
            sendError(ERROR_WITH_OUTPUT_FILE);
        }
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
