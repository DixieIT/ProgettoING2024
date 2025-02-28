package com.example.progettoing;

import java.util.*;

public class Automa {

    private String q_0;
    private ArrayList<String> Q;
    private ArrayList<String> F;
    private Set<String> Sigma;
    private Map<String, Map<String, String>> delta;

    public Automa(String q_0, String[] Q, String[] F, String[] Sigma, String[][] Delta) {
        this.q_0 = q_0;
        this.Q = new ArrayList<>(Arrays.asList(Q));
        this.F = new ArrayList<>(Arrays.asList(F));
        this.Sigma = new HashSet<>(Arrays.asList(Sigma));
        this.delta = new HashMap<>();

        // Convert Delta array to delta map
        for (int i = 0; i < Q.length; i++) {
            Map<String, String> transitions = new HashMap<>();
            for (int j = 0; j < Sigma.length; j++) {
                if (j < Delta[i].length) {
                    transitions.put(Sigma[j], Delta[i][j]);
                }
            }
            delta.put(Q[i], transitions);
        }
    }

    public int addTransition(String currentState, String input, String nextState) {
        if (!Q.contains(nextState)){
            return -1;
        }
        if (!Sigma.contains(input)){
            return -1;
        }
        if(!Q.contains(currentState)){
            return -1;
        }

        delta.computeIfAbsent(currentState, k -> new HashMap<>()).put(input, nextState);
        return 1;
    }

    public String printQ() {
        StringBuilder sb = new StringBuilder();
        for (String state : Q) {
            sb.append(state).append(", ");
        }
        if (!Q.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    public String printF() {
        StringBuilder sb = new StringBuilder();
        for (String state : F) {
            sb.append(state).append(", ");
        }
        if (!F.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    public String printSigma(){
        StringBuilder sb = new StringBuilder();
        for (String string : Sigma){
            sb.append(string).append(", ");

        }
        if (!Sigma.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }

    public void printDelta() {
        System.out.println("Transition Function Delta:");
        for (String state : Q) {
            Map<String, String> transitions = delta.get(state);
            if (transitions != null) { // Check if transitions is not null
                for (Map.Entry<String, String> entry : transitions.entrySet()) {
                    System.out.println("Delta(" + state + ", " + entry.getKey() + ") = " + entry.getValue());
                }
            }
        }
    }

    public void addState() {
        String newState = "q" + Q.size();
        Q.add(newState);
        // Add the new state to the delta map with empty transitions
        delta.put(newState, new HashMap<>());
    }

    public void addString(String newString) {
        Sigma.add(newString);
    }

    public void addFinalState() {
        String newState = "q" + Q.size();
        F.add(newState);
        Q.add(newState);
    }

    public int getStateSize() {
        return Q.size();
    }

    public int getFinalStateSize() {
        return F.size();
    }

    public ArrayList<String> getQ(){
        return Q;
    }
    public ArrayList<String> getF(){
        return F;
    }
    public Set<String> getSigma(){
        return Sigma;
    }

    public Map<String, Map<String, String>> getDelta() { return delta;}

    public boolean isStringAccepted(String input) {
        String currentState = q_0;
        int pos = 0;

        while (pos < input.length()) {
            String longestMatch = null;
            int longestMatchLength = 0;

            // Try to find the longest matching string in the transitions
            for (int length = input.length() - pos; length > 0; length--) {
                String subStr = input.substring(pos, pos + length);
                if (delta.get(currentState) != null && delta.get(currentState).containsKey(subStr)) {
                    longestMatch = subStr;
                    longestMatchLength = length;
                    break; // Exit the loop once the longest match is found
                }
            }

            if (longestMatch == null) {
                return false;
            }


            currentState = delta.get(currentState).get(longestMatch);
            pos += longestMatchLength;
        }

        // Check if the final state is an accepting state
        return F.contains(currentState);
    }

}
