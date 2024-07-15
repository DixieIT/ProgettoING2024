package com.example.progettoing;

public class Transition implements Comparable<Transition> {
    private final String currentState;
    private final String input;
    private final String nextState;

    public Transition(String currentState, String input, String nextState) {
        this.currentState = currentState;
        this.input = input;
        this.nextState = nextState;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getInput() {
        return input;
    }

    public String getNextState() {
        return nextState;
    }

    public int compareTo(Transition o) {
        int c = currentState.compareToIgnoreCase(o.currentState);
        if(c == 0) {
            c = input.compareToIgnoreCase(o.input);
            if(c == 0)
                c = nextState.compareToIgnoreCase(o.nextState);
        }
        return c;
    }
}