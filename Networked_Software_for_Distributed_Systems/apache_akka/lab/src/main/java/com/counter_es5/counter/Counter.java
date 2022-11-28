package com.counter_es5.counter;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.faultTolerance.counter.CounterActor;
import com.faultTolerance.counter.DataMessage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Counter {

	private static final int numThreads = 10;
	private static final int numMessages = 100;

	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef supervisor = sys.actorOf(SupervisorActor.props(), "supervisor");

		final ActorRef server;
		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
		try {
			// Asks the supervisor to create the child actor and returns a reference
			scala.concurrent.Future<Object> waitingForCounter = ask(supervisor, Props.create(ServerActor.class), 5000);
			server = (ActorRef) waitingForCounter.result(timeout, null);

			final ActorRef client = sys.actorOf(ClientActor.props(), "client");
			ConfigMSG conf = new ConfigMSG(server);
			client.tell(conf, ActorRef.noSender());

			PutMSG m1 = new PutMSG("AAA", "aaa");
			PutMSG m2 = new PutMSG("BBB", "bbb");
			PutMSG fail = new PutMSG("FAIL!", "xxx");
			PutMSG m3 = new PutMSG("CCC", "ccc");

			client.tell(m1, ActorRef.noSender());

			client.tell(m2, ActorRef.noSender());

			client.tell(fail, ActorRef.noSender());

			client.tell(m3, ActorRef.noSender());

			GetMSG g1 = new GetMSG("AAA");
			client.tell(g1, ActorRef.noSender());

			sys.terminate();

		} catch (TimeoutException | InterruptedException e1) {

			e1.printStackTrace();
		}
	}
}
