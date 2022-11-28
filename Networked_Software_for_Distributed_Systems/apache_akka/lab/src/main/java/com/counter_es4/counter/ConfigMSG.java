package com.counter_es4.counter;

import akka.actor.ActorRef;

public class ConfigMSG {
    ActorRef ref;
    ConfigMSG(ActorRef val) {this.ref = val;}

    public ActorRef getconf() {return ref;}
}
