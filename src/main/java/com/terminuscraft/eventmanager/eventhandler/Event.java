package com.terminuscraft.eventmanager.eventhandler;

import com.terminuscraft.eventmanager.miscellaneous.Environment;

public class Event {
    private final String eventName;
    private boolean pvp;
    private boolean loadOnStartup;
    private Environment environment;

    public Event(String name) {
        this(name, false, false, Environment.NORMAL);
    }

    public Event(String name, boolean pvp, boolean loadOnStartup, Environment environment) {
        this.eventName = name;
        this.pvp = pvp;
        this.loadOnStartup = loadOnStartup;
        this.environment = environment;
    }

    public String getName() {
        return this.eventName;
    }

    public boolean isPvpEnabled() {
        return pvp;
    }

    public boolean shouldLoadOnStartup() {
        return loadOnStartup;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public void setLoadOnStartup(boolean loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public void setEnvironment(Environment env) {
        this.environment = env;
    }
}
