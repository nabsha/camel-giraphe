package org.nabsha.camel.giraphe;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.ComponentResolver;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.util.List;

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

        Visitor visitor = new PlantUmlRouteDefinitionVisitorImpl();
        System.out.println(visitor.visit(context));
    }

}

