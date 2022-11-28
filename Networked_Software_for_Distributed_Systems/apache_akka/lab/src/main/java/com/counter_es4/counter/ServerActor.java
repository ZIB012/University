package com.counter_es4.counter;

import akka.actor.AbstractActorWithStash;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;

public class ServerActor extends AbstractActorWithStash {

	private ActorRef RefClient;

	public ServerActor() {}

	@Override
	public Receive createReceive() {
		return awake();
	}

	private final Receive awake() {
		return receiveBuilder().match(NormalMSG.class, this::onMessage).match(Sleep.class, this::GoSleep).build();
	}

	private final Receive sleepy() {
		return receiveBuilder().match(NormalMSG.class, this::Aside).match(Wake.class, this::WakeUp).build();
	}

	void WakeUp(Wake msg)
	{
		System.out.println("Il server si è svegliato");
		getContext().become(awake());
		unstashAll();
	}

	void GoSleep(Sleep msg)
	{
		System.out.println("Il server va a dormire");
		getContext().become(sleepy());
	}

	void onMessage(NormalMSG msg)
	{
		ReplyMSG reply = new ReplyMSG(msg.getname());
		sender().tell(reply, self());
	}

	void Aside (NormalMSG msg)
	{
		System.out.println("SERVER: Setting aside msg...");
		stash();
	}

	static Props props() {
		return Props.create(ServerActor.class);
	}

}
