package com.faultTolerance.counter;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeoutException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class CounterSupervisor {

	public static final int NORMAL_OP = 0;
	public static final int FAULT_OP = -1;

	public static final int FAULTS = 1;

	public static void main(String[] args) {
		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

		// We set up the Actor System
		final ActorSystem sys = ActorSystem.create("System");
		// We create the supervisor actor
		final ActorRef supervisor = sys.actorOf(CounterSupervisorActor.props(), "supervisor");

		ActorRef counter;
		try {
			
			// Asks the supervisor to create the child actor and returns a reference
				// it's necessary because if the supervisor actor does not create the counter actor within its own local
					// context then there's no supervision relationship between counter and the supervisor counter
				// supervision happens because we are asking the supervisor actor to create the counter actor within its
					// local context and then we get a reference to the counter actor that the supervisor is supervising
					// by blocking on the future until we get the reference back (it happens immediately because there's no reason to wait).
			scala.concurrent.Future<Object> waitingForCounter = ask(supervisor, Props.create(CounterActor.class), 5000);
			// in counter we have a reference to the supervised actor that was instantiated in the context of the supervisor
			counter = (ActorRef) waitingForCounter.result(timeout, null);

			// we firstly tell to increment the counter by 1, then we generate a number of faults and then we ask the actor to increment the counter again
			counter.tell(new DataMessage(NORMAL_OP), ActorRef.noSender());

			for (int i = 0; i < FAULTS; i++)
				counter.tell(new DataMessage(FAULT_OP), ActorRef.noSender());

			counter.tell(new DataMessage(NORMAL_OP), ActorRef.noSender());
			// different choices of strategy:
				// restarting the actor --> counter = 1
				// resuming the actor --> counter = 2
				// stop the actor --> the message is not delivered (actor is not able anymore to receive any message)

			sys.terminate();

		} catch (TimeoutException | InterruptedException e1) {
		
			e1.printStackTrace();
		}

	}

}
