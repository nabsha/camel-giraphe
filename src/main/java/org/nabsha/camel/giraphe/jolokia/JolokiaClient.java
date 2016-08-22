package org.nabsha.camel.giraphe.jolokia;

import org.apache.camel.model.ModelHelper;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.io.FileUtils;
import org.jolokia.client.BasicAuthenticator;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pExecRequest;
import org.jolokia.client.request.J4pResponse;
import org.jolokia.client.request.J4pSearchRequest;
import org.jolokia.client.request.J4pSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by nabeel on 20/08/16.
 */
public class JolokiaClient {

    public static final String M_BEAN_PATTERN = "org.apache.camel:context=*,type=routes,*";

    private static Logger logger = LoggerFactory.getLogger(JolokiaClient.class);

    private String url;
    private String username;
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public JolokiaClient(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Map<String, Map<String, RouteDefinition>> getAllRemoteRoutes(boolean doPersist) throws Exception {
        J4pClient j4pClient = J4pClient.url(this.url)
                .user(this.username)
                .password(this.password)
                .authenticator(new BasicAuthenticator().preemptive())
                .connectionTimeout(3000)
                .build();

        J4pSearchRequest searchRequest = new J4pSearchRequest(M_BEAN_PATTERN);
        J4pSearchResponse searchResponse = j4pClient.execute(searchRequest);

        Map<String, Map<String, RouteDefinition>> contextMap = new LinkedHashMap<>();

        for (ObjectName objectName : searchResponse.getObjectNames()) {
            Map<String, String> names = getNames(objectName.getCanonicalName());
            String contextName = names.get("context");
            String routeName = names.get("name");

            if (find(objectName.getCanonicalName())!= null)
            RouteDefinition modelFromXml = getRemoteRoute(j4pClient, objectName.getCanonicalName(), doPersist);

            if (contextMap.get(contextName) == null) {
                contextMap.put(contextName, new LinkedHashMap<String, RouteDefinition>());
            } else {
                contextMap.get(contextName).put(routeName, modelFromXml);
            }
        }

        return contextMap;
    }

    private Map<String, String> getNames(String canonicalName) {
        Map<String, String> namesMap = new LinkedHashMap<>();
        String[] toks = canonicalName.replace("org.apache.camel:","").split(",");
        for (String tok : toks) {
            if (tok.contains("=")) {
                String[] subToks = tok.split("=");
                namesMap.put(subToks[0].replace("\"",""), subToks[1].replace("\"",""));
            }
        }
        return namesMap;
    }

    public RouteDefinition getRemoteRoute(J4pClient j4pClient, String objectName, boolean doPersist) throws JAXBException, MalformedObjectNameException, J4pException, IOException {

        logger.info("Requesting route details for " + objectName + " from remote" + j4pClient);
        String xml = getXmlDump(j4pClient, objectName);
        if (doPersist) {
            store(objectName, xml);
        }
        return ModelHelper.createModelFromXml(null, xml, RouteDefinition.class);
    }

    private String find(String objectName) {
        Map<String, String> names = getNames(objectName);
        String filename = "cache/" + names.get("context") + "/" + names.get("name");
        String s = FileUtils.readFileToString(new File(filename), "UTF-8");
        return s;
    }

    private void store(String objectName, String xml) throws IOException {
        Map<String, String> names = getNames(objectName);
        String filename = "cache/" + names.get("context") + "/" + names.get("name");
        FileUtils.writeStringToFile(new File(filename), xml, "UTF-8");
    }

    private String getXmlDump(J4pClient j4pClient, String objectName) throws MalformedObjectNameException, J4pException {
        J4pExecRequest req = new J4pExecRequest(objectName, "dumpRouteAsXml");
        J4pResponse resp = j4pClient.execute(req);

        return resp.getValue().toString();
    }

}
