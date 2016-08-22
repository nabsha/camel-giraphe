package org.nabsha.camel.giraphe.puml;

import org.apache.camel.CamelContext;
import org.apache.camel.model.*;
import org.apache.camel.model.dataformat.SyslogDataFormat;
import org.nabsha.camel.giraphe.Visitor;

import java.util.List;

public class RouteDefinitionPumlActivityImpl extends Visitor {


    private static boolean isFirstWhen = true;

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
        if (msg.contains("?")) {
            msg = msg.substring(0, msg.indexOf("?"));
        }

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
        if (isFirstWhen) {
            answer.append("if (");
            isFirstWhen = false;
        } else {
            answer.append("elseif (");
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
        isFirstWhen =true;
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

        for (RouteDefinition routeDefinition : camelContext.getRouteDefinitions()) {
            answer.append(this.visit(routeDefinition));
        }

        return answer.toString();
    }

    private String processOutputs(StringBuilder answer, List<ProcessorDefinition<?>> processorDefinitions, Object... args) {

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

            String descriptionText = processorDefinition.getLabel();
            answer.append(activity(processorDefinition.getShortName() + "[" + descriptionText + "]"));
            processOutputs(answer, processorDefinition.getOutputs());
            return answer.toString();
        }

        return " not implemented " + object.getClass().getSimpleName();

    }
}
