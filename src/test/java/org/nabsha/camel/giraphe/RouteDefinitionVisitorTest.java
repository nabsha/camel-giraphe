package org.nabsha.camel.giraphe;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ModelHelper;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.nabsha.camel.giraphe.jolokia.JolokiaClient;
import org.nabsha.camel.giraphe.jolokia.JolokiaClientBuilder;
import org.nabsha.camel.giraphe.puml.RouteDefinitionPumlActivityImpl;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;

/**
 * Created by nabeel on 2/05/16.
 */
public class RouteDefinitionVisitorTest extends CamelTestSupport {

    @Override
    protected void doPreSetup() {
    }
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                onException(IllegalArgumentException.class)
                        .log("Exception occured")
                        .end();

                onException(NullPointerException.class)
                        .log("Exception occured")
                        .end();

                from("direct:test-inasdfasdfasdfasdfaposidufaposiduf aosidf oasdfasdfqwer sadf")
                        .log("before choice")
                        .choice()
                        .when(header("test"))
                        .log("UK message")
                        .when(header("test2"))
                        .choice()
                        .when(header("test"))
                        .log("UK message")
                        .log("Another UK message")
                        .endChoice()
                        .otherwise()
                        .log("Other message")
                        .endChoice()
                        .end()
                        .log("after choice");


                from("direct:test-in2")
                        .choice()
                        .when(header("test"))
                        .log("UK message")
                        .when(header("test2"))
                        .log("Another UK message")
                        .otherwise()
                        .log("Other message");
            }
        };
    }
    @Test
    public void testRoute() {
        System.out.println(context.getRouteDefinitions());
        CamelContext newContext = new DefaultCamelContext();


        for (RouteDefinition def: context.getRouteDefinitions()) {
            try {
                ModelHelper.createModelFromXml(newContext, ModelHelper.dumpModelAsXml(context, def), RouteDefinition.class);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        Visitor visitor = new RouteDefinitionPumlActivityImpl();
        System.out.println(visitor.visit(newContext));
    }

    @Test
    public void JolokiaGetList() throws Exception {

    }


    @Test
    public void readFromFile() throws JAXBException, IOException {
        String xml = IOUtils.toString(this.getClass().getResource("/xml/route1.xml"), "UTF-8");
        Utilities.generateActivityFromXml(xml, "route1.puml");
    }




}

