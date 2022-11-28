package com.counter_es2.counter;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class CounterActor extends AbstractActorWithStash {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(SingleMessage.class, this::onMessage).build();
	}

	void onMessage(SingleMessage msg) {
		if(msg.getValue() == 0)
		{
			++counter;
			System.out.println("Counter increased to " + counter);
			unstashAll();
		}
		else
		{
			if(counter==0)
			{
				stash();
			}
			else
			{
				--counter;
				System.out.println("Counter decreased to " + counter);
			}
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
