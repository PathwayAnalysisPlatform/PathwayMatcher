package extractor.neo4j;

import org.neo4j.driver.v1.*;

import java.util.List;
import java.util.Map;

public class ConnectionNeo4j {

    public static Driver driver;
    public static Session session;

    /**
     * InitializeNeo4j the connection to Neo4j.
     * This method needs that the command line arguments have been parsed and set, also the variables from the
     * configuration file.
     *
     * @return
     */
    public static void initializeNeo4j(String host, String username, String password) {

        if (username.length() > 0) {
            ConnectionNeo4j.driver = GraphDatabase.driver(host, AuthTokens.basic(username, password));
        } else {
            ConnectionNeo4j.driver = GraphDatabase.driver(host);
        }

        try {
            Session session = ConnectionNeo4j.driver.session();
            session.close();
            System.out.println("Connection established.");
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            System.out.println(" Unable to connect to \"" + host + "\", ensure the database is running and that there is a working network connection to it.");
            System.out.println(e);
        }
    }
    
    public static List<Record> query(String query) {
        ConnectionNeo4j.session = ConnectionNeo4j.driver.session();

        StatementResult queryResult = ConnectionNeo4j.session.run(query);

        ConnectionNeo4j.session.close();

        return queryResult.list();
    }

    public static List<Record> query(String query, Value parameters) {
        ConnectionNeo4j.session = ConnectionNeo4j.driver.session();

        StatementResult queryResult = ConnectionNeo4j.session.run(query, parameters);

        ConnectionNeo4j.session.close();

        return queryResult.list();
    }

    public static List<Record> query(String query, Map<String, Object> parameters) {
        ConnectionNeo4j.session = ConnectionNeo4j.driver.session();

        StatementResult queryResult = ConnectionNeo4j.session.run(query, parameters);

        ConnectionNeo4j.session.close();

        return queryResult.list();
    }

    public static int getSingleValue(String query, String attribute) {
        ConnectionNeo4j.session = ConnectionNeo4j.driver.session();
        StatementResult queryResult = ConnectionNeo4j.session.run(query);
        Record r = queryResult.single();
        ConnectionNeo4j.session.close();

        return r.get(attribute).asInt();
    }

    public static int getSingleValue(String query, String attribute, Value parameters) {
        ConnectionNeo4j.session = ConnectionNeo4j.driver.session();
        StatementResult queryResult = ConnectionNeo4j.session.run(query, parameters);
        Record r = queryResult.single();
        ConnectionNeo4j.session.close();

        return r.get(attribute).asInt();
    }
}
