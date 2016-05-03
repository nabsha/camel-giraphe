package org.nabsha.camel.giraphe;

import org.apache.camel.CamelContext;
import org.apache.camel.model.*;
import org.apache.camel.model.dataformat.SyslogDataFormat;

import java.util.List;

public class PlantUmlRouteDefinitionVisitorImpl extends Visitor {


    @Override
    public String visit(OnExceptionDefinition definition) {
        StringBuilder answer = new StringBuilder();
        answer.append(activity(definition.getShortName()));
        processOutputs(answer, definition.getOutputs());

        return answer.toString();
    }

    @Override
    public String visit(FromDefinition definition) {
        StringBuilder answer = new StringBuilder();
        answer.append(activity("from[" + definition.getEndpointUri() + "]"));

        return answer.toString();
    }

    private String message(String msg) {
        return msg + System.lineSeparator();
    }

    private String activity(String msg) {
        return newLine(":" + msg + ";" + System.lineSeparator());
    }

    /**
     * This code has been taken from org.apache.camel.model.WhenDefinition#description() as it was package private
     * @param definition
     * @return
     */
    protected String description(ExpressionNode definition) {
        StringBuilder sb = new StringBuilder();
        if (definition.getExpression() != null) {
            String language = definition.getExpression().getLanguage();
            if (language != null) {
                sb.append(language).append("{");
            }
            sb.append(definition.getExpression().getLabel());
            if (language != null) {
                sb.append("}");
            }
        }
        return sb.toString();
    }

    @Override
    public String visit(WhenDefinition definition) {
        StringBuilder answer  = new StringBuilder();

        // only first when should get "if", rest should get elseif
        if (!((ChoiceDefinition)definition.getParent()).getWhenClauses().get(0).equals(definition)) {
            answer.append("elseif (");
        } else {
            answer.append("if (");
        }
        answer.append(definition.getShortName() + "[" + newLine(description(definition)) + "]" +" ) then (yes)\n");

        processOutputs(answer, definition.getOutputs());
        return answer.toString();
    }

    private String newLine(String description) {
        StringBuilder answer = new StringBuilder();
        String[] strings = description.split("(?<=\\G.{25})");
        for (String string : strings) {
            answer.append(string + System.lineSeparator());
        }
        return answer.toString();
    }

    @Override
    public String visit(OtherwiseDefinition definition) {
        StringBuilder answer = new StringBuilder("else (" + definition.getShortName() + ")\n");
        processOutputs(answer, definition.getOutputs());
        return answer.toString();
    }

    @Override
    public String visit(ChoiceDefinition definition) {
        StringBuilder answer = new StringBuilder();
        processOutputs(answer, definition.getOutputs());
        answer.append(message("endif"));
        return answer.toString();
    }

    @Override
    public String visit(LogDefinition definition) {
        return activity(definition.getShortName() + "[" + definition.getMessage() + "]");
    }

    @Override
    public String visit(RouteDefinition routeDefinition) {
        StringBuilder answer = new StringBuilder();

        answer.append(message("start"));

        answer.append(message("partition " + routeDefinition.getId() + " {"));
        for (FromDefinition fromDefinition : routeDefinition.getInputs()) {
            answer.append(this.visit(fromDefinition));
        }
        answer.append(message("split"));

        for (ProcessorDefinition processorDefinition : routeDefinition.getOutputs()) {
            answer.append(this.visit(processorDefinition));
            if (processorDefinition instanceof OnExceptionDefinition)
                answer.append(message("split again"));
        }
        answer.append(message("end split"));

        answer.append(message("}"));

        answer.append(message("stop"));
        return answer.toString();
    }

    @Override
    public String visit(CamelContext camelContext) {
        StringBuilder answer = new StringBuilder();

        answer.append(message("@startuml"));
        for (RouteDefinition routeDefinition : camelContext.getRouteDefinitions()) {
            answer.append(this.visit(routeDefinition));
        }

        answer.append(message("@enduml"));
        return answer.toString();
    }


    private String processOutputs(StringBuilder answer, List<ProcessorDefinition<?>> processorDefinitions) {

        for (ProcessorDefinition processorDefinition : processorDefinitions) {
            answer.append(this.visit(processorDefinition));
        }
        return answer.toString();
    }

    @Override
    public String defaultVisit(Object object) {

        if (object instanceof ProcessorDefinition) {
            ProcessorDefinition processorDefinition = (ProcessorDefinition) object;
            StringBuilder answer = new StringBuilder();
            answer.append(activity(processorDefinition.getShortName() + "[" + processorDefinition.getDescriptionText() + "]"));
            processOutputs(answer, processorDefinition.getOutputs());
            return answer.toString();
        }

        return " not implemented " + object.getClass().getSimpleName();

    }
}
