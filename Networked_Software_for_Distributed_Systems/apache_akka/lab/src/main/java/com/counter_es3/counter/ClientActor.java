package com.counter_es3.counter;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.TimeoutException;

public class ClientActor extends AbstractActorWithStash {

	private ActorRef RefServer;

	private scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);


	public ClientActor() {}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(GetMSG.class, this::onMessage).match(PutMSG.class, this::onMessage)
				.match(ReplyMSG.class, this::onMessage).match(ConfigMSG.class, this::onMessage).build();
	}

	void onMessage(GetMSG msg)
	{
		scala.concurrent.Future<Object> waitingForReply = Patterns.ask(RefServer, msg, 5000);
		try {
			ReplyMSG reply = (ReplyMSG) waitingForReply.result(timeout, null);
			if (reply.getemail()!=null) {
				System.out.println("CLIENT: Received reply, email is " + reply.getemail());
			} else {
				System.out.println("CLIENT: Received reply, no email found!");
			}
		} catch (TimeoutException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void onMessage(PutMSG msg)
	{
		RefServer.tell(msg, self());
	}
	void onMessage(ReplyMSG msg) {
		System.out.println("email received: " + msg.getemail());
	}

	void onMessage(ConfigMSG msg) {RefServer = msg.getconf();}

	static Props props() {
		return Props.create(ClientActor.class);
	}

}
