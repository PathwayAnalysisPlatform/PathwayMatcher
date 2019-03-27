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

import org.apache.commons.cli.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 *  Support class to run {@link extractor.psimod.OnthologyHttpClient} with command line arguments.
 *
 *  This class is not used needed for PathwayMatcher functionality, but serves as a support tool to consult the
 *  available standard modification types.
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class ExtractorPsiMod {

    public static void main(String args[]) {
        // Define and parse command line options
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        Option modId = new Option("m", "mod", true, "The PSIMOD id for the Post Translational Modification that will be used to select the proteins that can have that modification.");
        modId.setRequired(true);
        options.addOption(modId);

        Option output = new Option("o", "output", true, "Output file path and name");
        output.setRequired(true);
        options.addOption(output);

        Option config = new Option("c", "confPath", true, "config.txt file path and name");
        config.setRequired(false);
        options.addOption(config);

        Option host = new Option("h", "host", true, "Url of the Neo4j database with Reactome");
        host.setRequired(false);
        options.addOption(host);

        Option username = new Option("u", "username", true, "Username to access the database with Reactome");
        username.setRequired(false);
        options.addOption(username);

        Option password = new Option("p", "password", true, "Password related to the username provided to access the database with Reactome");
        password.setRequired(false);
        options.addOption(password);

        //Verify command line options
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

//        System.out.println("modId: " + cmd.getOptionValue("mod"));
//        System.out.println("output: " + cmd.getOptionValue("output"));
//        Conf.setEmptyMaps();
//        Conf.setDefaultNeo4jValues();
//        if (cmd.hasOption("confPath")) {
//            Conf.readConf();
//        }
//        if (cmd.hasOption("host")) {
//            Conf.setValue("host", cmd.getOptionValue("host"));
//        }
//        if (cmd.hasOption("username")) {
//            Conf.setValue("username", cmd.getOptionValue("username"));
//        }
//        if (cmd.hasOption("password")) {
//            Conf.setValue("password", cmd.getOptionValue("password"));
//        }
//
//        ConnectionNeo4j.driver = GraphDatabase.driver(
//                Conf.strMap.get(StrVars.host),
//                AuthTokens.basic(Conf.strMap.get(
//                        StrVars.username),
//                        Conf.strMap.get(StrVars.password)
//                )
//        );

        try {
            FileWriter resultFW = new FileWriter(cmd.getOptionValue("output"));

            // Access EBI fot the onthology to get the mod descendants
            List<String> modList = OnthologyHttpClient.getTermDescendants("mod", cmd.getOptionValue("mod"), "descendants");
            modList.add( cmd.getOptionValue("mod"));
//            List<String> modList = Arrays.asList("00818",  "00173",  "00170",  "00171",  "00172",  "00167",  "00168",  "00169",  "00491",  "00895",  "00152",  "00153",  "00154",  "01146",  "00226",  "01182",  "01668",  "00896",  "01164",  "00354",  "00355",  "00225",  "01611",  "00356",  "00357",  "00358",  "01804",  "00301",  "00302",  "01847",  "00151",  "00159",  "01475",  "01474",  "00176",  "00583",  "01362",  "01361",  "01363",  "00639",  "01308",  "01307",  "01309",  "00635",  "01588",  "01587",  "00640",  "01973",  "01972",  "00696",  "00043",  "01456",  "00890",  "00045",  "00044",  "01931",  "00227",  "01455",  "00046",  "01452",  "01451",  "00047",  "00048",  "00042",  "01606",  "00311",  "00797",  "00787",  "00788");
//            System.out.println("The list of related mods is:");
//            for (String mod : modList) {
//                System.out.println(mod);
//            }

            // Get list of proteins from Reactome
//            List<String> proteinList = ReactomeAccess.getProteinListByMods(modList);

//            // Write list to file
//            for (String protein : proteinList) {
//                resultFW.write(protein + "\n");
//            }

            resultFW.close();
        } catch (IOException ex) {
            System.out.println("Could not create the output file in the specified path.");
            //Logger.getLogger(ExtractorPsiMod.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static List<String> getTermDescendants() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
