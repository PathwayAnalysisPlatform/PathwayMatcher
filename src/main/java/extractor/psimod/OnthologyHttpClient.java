/*
 * Copyright 2017 Luis Francisco Hernández Sánchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package extractor.psimod;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.*;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Application to get the PSIMOD modifications list and create the tree heirarchy.
 *
 *  This class is not used active part of PathwayMatcher, but serves as a support tool to consult the
 *  available standard modification types. This helped the decision process when developing PathwayMatcher.
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class OnthologyHttpClient {

    private static int onthologySize = 20;
    private static String pattern = "obo/MOD_"; //Pattern to find the ids of the mods in the Json file of the web service.

    public static void main(String[] args) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet getRequest = new HttpGet(
                    "http://www.ebi.ac.uk/ols/api/ontologies/mod/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FMOD_00861/descendants?size=2002");
            getRequest.addHeader("accept", "application/json");

            HttpResponse response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed to get the PSIMOD onthology : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

            String output;
            FileWriter fw = new FileWriter("./mod.json");
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
                fw.write(output);
            }

            fw.close();
            httpClient.close();

        } catch (ClientProtocolException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /*
    * try {
            result = "http://www.ebi.ac.uk/ols/api/ontologies/" + onthologyName + "/terms/" + URLEncoder.encode(URLEncoder.encode("htp://purl.obolibrary.org/obot/MOD_" + term, "ISO-8859-1"), "ISO-8859-1") + "/" + relation + "?size=" + onthologySize;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/

    private static String getOntologyDescription(String onthologyName) {

        String uri = createUri(onthologyName);
        BufferedReader br = getContentBufferedReader(uri);
        String queryContent = convertBufferedReader(br);

        return queryContent;
    }

    private static String convertBufferedReader(BufferedReader br) {
        String result = "";
        String line;
        try {
            while ((line = br.readLine()) != null) {
                result += line;
            }
        } catch (IOException ex) {
            Logger.getLogger(OnthologyHttpClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private static BufferedReader getContentBufferedReader(String uri) {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(uri);
            getRequest.addHeader("accept", "application/json");

            HttpResponse response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed to quety the PSIMOD onthology : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

            httpClient.getConnectionManager().shutdown();

            return br;

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getTermDescendants(String onthologyName, String term, String relation) {
        List<String> result = new ArrayList<>();
        String queryContent = "";

        onthologySize = getOntologySize(onthologyName);
        String uri = createUri(onthologyName, term, relation);
        //BufferedReader br = getContentBufferedReader(uri);
        //String queryContent = convertBufferedReader(br);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet(uri);
        getRequest.addHeader("accept", "application/json");

        HttpResponse response;
        try {
            response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed to get the PSIMOD onthology : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            String line;
            FileWriter fw = new FileWriter("./mod.json");

            while ((line = br.readLine()) != null) {
                fw.write(line);
                queryContent += line;
            }

            fw.close();
            httpClient.getConnectionManager().shutdown();

            char c;
            int matched = 0;
            String mod = "";
            int pos = 0;

            while (pos < queryContent.length()) {
                c = queryContent.charAt(pos);
                pos++;
                if (c == pattern.charAt(matched)) {
                    matched++;
                    if (matched == pattern.length()) {
                        for (int I = 0; I < 5; I++) {
                            mod += queryContent.charAt(pos);
                            pos++;
                        }
                        result.add(mod);
                        mod = "";
                        matched = 0;
                    }
                } else {
                    matched = 0;
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(OnthologyHttpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    private static String createUri(String onthologyName) {
        String result = "http://www.ebi.ac.uk/ols/api/ontologies/" + onthologyName;
        return result;
    }

    private static String createUri(String onthologyName, String term, String relation) {
        String result = "http://www.ebi.ac.uk/ols/api/ontologies/" + onthologyName + "/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FMOD_" + term + "/" + relation + "?size=" + onthologySize;
        return result;
    }

    private static int getOntologySize(String onthologyName) {
        String queryContent = getOntologyDescription(onthologyName);

        int startIndex = queryContent.indexOf("numberOfTerms") + 17;
        int endIndex = queryContent.indexOf(",", startIndex);
        String substr = queryContent.substring(startIndex, endIndex);
        int result = Integer.valueOf(substr);

        return result;
    }

    private enum relations {
        self, parents, hierarchicalParents, hierarchicalAncestors, ancestors, children, hierarchicalChildren, hierarchicalDescendants, descendants, jstree
    }
}
