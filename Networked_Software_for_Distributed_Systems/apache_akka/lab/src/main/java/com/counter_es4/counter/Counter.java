package com.counter_es4.counter;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.IOException;

public class Counter {

	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef client = sys.actorOf(ClientActor.props(), "client");
		final ActorRef server = sys.actorOf(ServerActor.props(), "server");

		// Send messages from multiple threads in parallel
		ConfigMSG conf1 = new ConfigMSG(server);
		client.tell(conf1, ActorRef.noSender());

		Wake wake = new Wake();
		Sleep sleep = new Sleep();

		NormalMSG m1 = new NormalMSG("111");
		client.tell(m1, ActorRef.noSender());

		NormalMSG m2 = new NormalMSG("222");
		NormalMSG m3 = new NormalMSG("333");
		NormalMSG m4 = new NormalMSG("444");

		client.tell(sleep, ActorRef.noSender());
		client.tell(m2, ActorRef.noSender());
		client.tell(m3, ActorRef.noSender());
		client.tell(wake, ActorRef.noSender());
		client.tell(m4, ActorRef.noSender());

		// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sys.terminate();

	}

}
