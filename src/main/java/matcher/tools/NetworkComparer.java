package matcher.tools;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;

public class NetworkComparer {

    static void comparePhosphosites(String file_experimental_phosphosites, String file_reactome_phosphosites) throws IOException {
        // Check how many of the phosphosites appear in Reactome
        List<String> phosphosites = Files.readLines(new File(file_experimental_phosphosites), Charset.forName("ISO-8859-1"));
        List<String> phosphosites_reactome = Files.readLines(new File(file_reactome_phosphosites), Charset.forName("ISO-8859-1"));
        HashSet<String> set_reactome = new HashSet<>(phosphosites_reactome);
        HashSet<String> set_found = new HashSet<>();

        // Remove header lines
        phosphosites.remove(0);
        phosphosites_reactome.remove(0);

        for (String str : phosphosites) {
            if (set_reactome.contains(str)) {
                set_found.add(str);
            }
        }
        System.out.println("Found: " + set_found.size() + " of " + phosphosites.size());
    }

    static void compareAccessions(String file_experimental_phosphosites, String file_reactome_phosphosites) throws IOException {
        List<String> phosphosites = Files.readLines(new File(file_experimental_phosphosites), Charset.forName("ISO-8859-1"));
        List<String> phosphosites_reactome = Files.readLines(new File(file_reactome_phosphosites), Charset.forName("ISO-8859-1"));
        HashSet<String> accessions_reactome = new HashSet<>();
        HashSet<String> accessions_experimental = new HashSet<>();
        int cont = 0;

        // Remove header lines
        phosphosites.remove(0);
        phosphosites_reactome.remove(0);

        for (String line : phosphosites_reactome) {
            String[] parts = line.split(",");
            accessions_reactome.add(parts[0]);
        }

        for (String line : phosphosites) {
            String[] parts = line.split(",");
            accessions_experimental.add(parts[0]);
        }

        for (String accession : accessions_experimental) {
            if (accessions_reactome.contains(accession)) {
                cont++;
            }
        }
        System.out.println("Found: " + cont + " experimental accessions in Reactome out of " + accessions_experimental.size());
    }

    public static void main(String args[]) {

        String experimental_phosphosites_file = args[0];
        String reactome_phosphosites_file = args[1];
        try {
            comparePhosphosites(experimental_phosphosites_file, reactome_phosphosites_file);
            compareAccessions(experimental_phosphosites_file, reactome_phosphosites_file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
