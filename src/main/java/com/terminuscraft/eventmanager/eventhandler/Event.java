package com.terminuscraft.eventmanager.eventhandler;

public class Event {
    private final String eventName;

    public Event(String name) {
        this.eventName = name;
    }

    public String getName() {
        return this.eventName;
    }
}
