package org.nabsha.camel.giraphe;

import org.apache.camel.model.RouteDefinition;
import org.apache.commons.io.FileUtils;
import org.nabsha.camel.giraphe.jolokia.JolokiaClient;
import org.nabsha.camel.giraphe.jolokia.JolokiaClientBuilder;
import org.nabsha.camel.giraphe.puml.RouteDefinitionPumlActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * Created by nabeel on 20/08/16.
 */
public class GiraphGenerator {
    JolokiaClient client;
    Visitor visitor;
    Logger logger = LoggerFactory.getLogger(GiraphGenerator.class);

    public void generateActivityFromRoutes() throws Exception {
        visitor = new RouteDefinitionPumlActivityImpl();


        client = new JolokiaClientBuilder()
                .setUrl("http://svicf0000054np.nbndc.local:8181/hawtio/jolokia")
                .setUsername("nabeelshaheen")
                .setPassword("HE55-ixk-548")
                .createJolokiaClient();

        logger.info("Requesting routes from " + client.getUrl());

        Map<String, Map<String, RouteDefinition>> allRemoteRoutes = client.getAllRemoteRoutes(true);
        for (String contextName : allRemoteRoutes.keySet()) {
            for (String route : allRemoteRoutes.get(contextName).keySet()) {
                String answer = visitor.visit(allRemoteRoutes.get(contextName).get(route));


            }
            String answer = visitor.visit(allRemoteRoutes.get(contextName));
            String filename = contextName.substring(contextName.indexOf("name=")).replace("\"","");
            logger.info("Writing response to file " + filename);

            FileUtils.writeStringToFile(new File(filename), answer, "UTF-8");
        }
    }
}
