package com.counter_es5.counter;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

public class ServerActor extends AbstractActorWithStash {

	HashMap<String, String> contacts;

	public ServerActor() {this.contacts = new HashMap<String, String>();}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(PutMSG.class, this::onMessage).
				match(GetMSG.class, this::onMessage).build();
	}

	void onMessage(PutMSG msg) throws Exception {
		if (msg.getname().equals("FAIL!"))
		{
			System.out.println("I am emulating a FAULT!");
			throw new Exception("Actor fault!");
		}
		else
		{
			contacts.put(msg.getname(), msg.getemail());
			System.out.println("Client added name = " + msg.getname() + " email = " + msg.getemail());
		}
	}

	void onMessage(GetMSG msg)
	{
		System.out.println("SERVER: Received query for name " + msg.getname());
		String name = msg.getname();
		String email = contacts.get(name);
		System.out.println("Email = " + email);
		ReplyMSG reply = new ReplyMSG(email);
		sender().tell(reply, self());;
	}
	static Props props() {
		return Props.create(ServerActor.class);
	}

}
