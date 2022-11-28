package com.counter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Counter {

	private static final int numThreads = 10;
	private static final int numMessages = 100;

	public static void main(String[] args) {

		// we initialize the ActorSystem
		final ActorSystem sys = ActorSystem.create("System");
		// We create a single counter actor
		final ActorRef counter = sys.actorOf(CounterActor.props(), "counter");

		// Send messages from multiple threads in parallel
		// It is an ExecutorService --> it's a regular Java feature that uses a number of threads to send messages in
		// 														parallel to the counter actor using the tell() method
		final ExecutorService exec = Executors.newFixedThreadPool(numThreads);

		for (int i = 0; i < numMessages; i++) {
			exec.submit(() -> counter.tell(new SimpleMessage(), ActorRef.noSender()));	// noSender() is the identifier
			// we are not explicitly identify the actor because it's the only one in this case

			exec.submit(() -> counter.tell(new OtherMessage(), ActorRef.noSender()));
			// if I run the code adding only the sending of OtherMessage, it says that there is a message that was
			// unhandled because the actor is not prepared to handle this type of messages, the reason it that in the
			// createReceive() method, the only match clause exists for the SimpleMessage --> the actor doesn't know how
			// to handle messages that are not of type SimpleMessage
		}
		
		// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		exec.shutdown();
		sys.terminate();

	}

}
