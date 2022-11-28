package com.counter_es3.counter;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Counter {

	private static final int numThreads = 10;
	private static final int numMessages = 100;

	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef client = sys.actorOf(ClientActor.props(), "client");
		final ActorRef server = sys.actorOf(ServerActor.props(), "server");

		// Send messages from multiple threads in parallel
		ConfigMSG conf = new ConfigMSG(server);
		client.tell(conf, ActorRef.noSender());

		PutMSG m1 = new PutMSG("Marco", "Marcogmail");
		GetMSG m2 = new GetMSG("Marco");

		client.tell(m1, ActorRef.noSender());
		client.tell(m2, ActorRef.noSender());
/*
		for (int i = 0; i < numMessages; i++) {
			exec.submit(() -> counter.tell(new SingleMessage(1), ActorRef.noSender()));
			exec.submit(() -> counter.tell(new SimpleMessage(), ActorRef.noSender()));
			exec.submit(() -> counter.tell(new DecrementMessage(), ActorRef.noSender()));
		}
*/
		// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sys.terminate();

	}

}
