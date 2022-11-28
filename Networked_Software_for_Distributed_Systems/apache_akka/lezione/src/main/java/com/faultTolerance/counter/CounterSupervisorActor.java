package com.faultTolerance.counter;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import java.time.Duration;

public class CounterSupervisorActor extends AbstractActor {

	 // We specify what is the strategy to be applied. We are opting for a OneForOne strategy
    private static SupervisorStrategy strategy =
        new OneForOneStrategy(
            1, // Max no of retries  --> this strategy costumizes handle at most one fault every minute
            Duration.ofMinutes(1), // Within what time period
            DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.stop())
                .build());	// that fault is handled by matching the Exception representing the fault to the strategy that restart

    @Override
    public SupervisorStrategy supervisorStrategy() {
      return strategy;
    }

	public CounterSupervisorActor() {
	}

	@Override
	public Receive createReceive() {
		// Creates the child actor within the supervisor actor context
		return receiveBuilder()
		          .match(
		              Props.class,
		              props -> {
		                getSender().tell(getContext().actorOf(props), getSelf());
		              })
		          .build();
	}

	static Props props() {
		return Props.create(CounterSupervisorActor.class);
	}

}
