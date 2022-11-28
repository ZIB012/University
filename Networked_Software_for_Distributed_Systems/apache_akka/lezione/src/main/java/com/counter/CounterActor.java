package com.counter;

import akka.actor.AbstractActor;
import akka.actor.Props;

// it's the only actor we have in this example
public class CounterActor extends AbstractActor {

	private int counter;  // single attribute initialized to 0 in the object constructor

	public CounterActor() {
		this.counter = 0;
	}

	// redefinition of the createReceive method: it specifies a single match() clause that looks at the type of message
	// 		with SimpleMessage.class, and whenever a message of that type is received by this actor, it triggers the execution
	// 		of the onMessage() method
	// SimpleMessage is an empty message structure (no fields inside the class, we use it only as a type of message
	// 													to trigger the matching in the only match clause)
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(SimpleMessage.class, this::onMessage).match(OtherMessage.class, this::onOtherMessage).build();
	}

	void onMessage(SimpleMessage msg) {
		++counter;
		System.out.println("Counter increased to " + counter);
	}

	void onOtherMessage(OtherMessage msg) {
		System.out.println("Riceived other type of message!");
	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}
