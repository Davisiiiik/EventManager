package com.terminuscraft.eventmanager.eventhandler;

public class EvmHandler {

    private static Event currentEvent;

    public static void setCurrentEvent(Event newEvent) {
        currentEvent = newEvent;
    }

    public static Event getCurrentEvent() {
        return currentEvent;
    }
}
