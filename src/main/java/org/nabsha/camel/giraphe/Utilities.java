package org.nabsha.camel.giraphe;

import org.apache.camel.model.ModelHelper;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.io.FileUtils;
import org.nabsha.camel.giraphe.puml.RouteDefinitionPumlActivityImpl;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by nabeel on 18/08/16.
 */
public class Utilities {
    public static String wrapPumlTags(String message) {
        return "@startuml\n " + message + "\n@enduml";
    }


    public static void generateActivityFromXml(String xml, String pathname) throws JAXBException, IOException {
        RouteDefinition modelFromXml = ModelHelper.createModelFromXml(null, xml, RouteDefinition.class);
        Visitor visitor = new RouteDefinitionPumlActivityImpl();

        FileUtils.writeStringToFile(new File(pathname), Utilities.wrapPumlTags(visitor.visit(modelFromXml)), Charset.defaultCharset());
    }

}
