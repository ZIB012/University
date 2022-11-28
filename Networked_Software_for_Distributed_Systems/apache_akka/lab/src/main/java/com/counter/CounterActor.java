package com.counter;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class CounterActor extends AbstractActor {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(SingleMessage.class, this::onMessage).match(SimpleMessage.class, this::onMessage).
				match(DecrementMessage.class, this::onMessage).build();
	}

	void onMessage(SingleMessage msg) {
		if(msg.getValue() == 0)
		{
			++counter;
			System.out.println("Counter increased to " + counter);
		}
		else
		{
			--counter;
			System.out.println("Counter decreased to " + counter);
		}
	}

	void onMessage(SimpleMessage msg)
	{
		++counter;
		System.out.println("Counter increased to " + counter);
	}

	void onMessage(DecrementMessage msg)
	{
		--counter;
		System.out.println("Counter decreased to " + counter);
	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}
