package org.nabsha.camel.giraphe;

import org.apache.camel.CamelContext;
import org.apache.camel.model.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Visitor {

    public abstract String visit(OnExceptionDefinition definition);

    public abstract String visit(FromDefinition fromDefinition);

	public abstract String visit(WhenDefinition whenDefinition);

    public abstract String  visit(OtherwiseDefinition definition);

    public abstract String  visit(ChoiceDefinition definition);

    public abstract String  visit(LogDefinition definition);

    public abstract String  visit(RouteDefinition definition);

    public abstract String visit(CamelContext camelContext);

    public abstract String  defaultVisit(Object object);


	public String visit(Object object) {
        String answer = null;
		try
		{
			Method downPolymorphic = this.getClass().getMethod("visit", new Class[] { object.getClass() });

			if (downPolymorphic == null) {
				answer = defaultVisit(object);
			} else {
				answer = (String)downPolymorphic.invoke(this, new Object[] {object});
			}
		}
		catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
		{
			answer = this.defaultVisit(object);
		}

        return answer;
	}
}