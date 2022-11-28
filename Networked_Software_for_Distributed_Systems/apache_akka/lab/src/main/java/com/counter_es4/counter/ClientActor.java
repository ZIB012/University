package com.counter_es4.counter;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;

import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ClientActor extends AbstractActorWithStash {

	private ActorRef RefServer;

	private scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);


	public ClientActor() {}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Wake.class, this::WakeUp).match(Sleep.class, this::GoSleep)
				.match(NormalMSG.class, this::onMessage).match(ConfigMSG.class, this::onMessage).match(ReplyMSG.class, this::Reply).build();
	}

	void WakeUp(Wake msg)
	{
		RefServer.tell(msg, self());
		System.out.println("Messaggio di svegliarsi");
	}

	void GoSleep(Sleep msg)
	{
		RefServer.tell(msg, self());
		System.out.println("Messaggio di dormire");
	}
	void onMessage(NormalMSG msg)
	{
		RefServer.tell(msg, self());
	}

	void onMessage(ConfigMSG msg) {RefServer = msg.getconf();}

	void Reply(ReplyMSG msg) {System.out.println("Messaggio ricevuto " + msg.getname());}
	static Props props() {
		return Props.create(ClientActor.class);
	}

}
