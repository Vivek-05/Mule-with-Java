package com.mule.example;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.mule.construct.Flow;

public class AddingTwoNames implements Callable {
	StringBuilder nameBuilder = new StringBuilder();
	String name = null;

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();
		String firstName = message.getProperty("firstName", PropertyScope.INVOCATION);
		String lastName = message.getProperty("lastName", PropertyScope.INVOCATION);
		nameBuilder.append(firstName);
		nameBuilder.append(lastName);
		name = nameBuilder.toString();
		MuleMessage muleMessage = new DefaultMuleMessage(name, eventContext.getMuleContext());
		muleMessage.setEncoding("UTF-8");
		/*
		 * invocation scoped property is simply a flow variable as it can only be
		 * accessed within a flow. You won’t be able to access when the message
		 * traverses to another flow as a result of an outbound endpoint.
		 */
		muleMessage.setProperty("name", name, PropertyScope.INVOCATION);
		muleMessage.setPayload(name);
		invokeMuleFlow(muleMessage, eventContext.getMuleContext(), "second-flow");
		return name;
	}

	public static MuleEvent invokeMuleFlow(MuleMessage muleMessage, MuleContext muleContext, String flowName)
			throws Exception {
		Flow flow = (Flow) muleContext.getRegistry().lookupFlowConstruct(flowName);
		MuleEvent muleEvent = new DefaultMuleEvent(muleMessage, MessageExchangePattern.REQUEST_RESPONSE, flow);
		return flow.process(muleEvent);
	}
}