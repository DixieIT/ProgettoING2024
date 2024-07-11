package com.example.progettoing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AutomaTest {

    private Automa automa;

    @BeforeEach
    public void setUp() {
        String q_0 = "q0";
        String[] Q = {"q0"};
        String[] F = {};
        String[] Sigma = {};
        String[][] Delta = {}; // Delta vuoto

        automa = new Automa(q_0, Q, F, Sigma, Delta);
    }

    @Test
    public void testAddTransition() {
        // Adding a transition to a non-existent state should fail
        int result = automa.addTransition("q0", "a", "q1");
        assertEquals(-1, result, "Transition should not be added because 'q1' does not exist");

        // Add state and string, then add transition
        automa.addState();
        automa.addString("a");
        result = automa.addTransition("q0", "a", "q1");
        assertEquals(1, result, "Transition should be added successfully");

        automa.printDelta();
    }

    @Test
    public void testClearAll() {
        // Set up the automa with states, final states, and transitions
        automa.addString("a");
        automa.addState();
        automa.addFinalState();
        automa.addTransition("q0", "a", "q1");

        // Clear all data and verify
        automa.clearAll();
        assertEquals(1, automa.getStateSize(), "Should have only the initial state after clearAll");
        assertEquals(0, automa.getFinalStateSize(), "Should have no final states after clearAll");
        assertTrue(automa.getSigma().isEmpty(), "Sigma should be empty after clearAll");
        assertTrue(automa.getDelta().isEmpty(), "Delta should be empty after clearAll");
    }

    @Test
    public void testPrintQ() {
        automa.addState();
        assertEquals("q0, q1", automa.printQ(), "States should be 'q0, q1'");
    }

    @Test
    public void testPrintF() {
        automa.addFinalState();
        assertEquals("q1", automa.printF(), "Final states should be 'q1'");
    }

    @Test
    public void testPrintSigma() {
        automa.addString("a");
        automa.addString("b");
        assertEquals("a, b", automa.printSigma(), "Sigma should be 'a, b'");
    }

    @Test
    public void testPrintDelta() {
        // Add transitions and print Delta
        automa.addState(); // q1
        automa.addString("a");

        automa.addTransition("q0", "a", "q1");
        automa.printDelta();

        // Verify the contents of Delta
        Map<String, Map<String, String>> delta = automa.getDelta();
        assertEquals("q1", delta.get("q0").get("a"), "Delta should map 'q0' on 'a' to 'q1'");
    }

    @Test
    public void testAddState() {
        automa.addState();
        assertEquals(2, automa.getStateSize(), "There should be 2 states after adding one");
        assertTrue(automa.getQ().contains("q1"), "State 'q1' should be present in the state set");
    }

    @Test
    public void testAddString() {
        automa.addString("c");
        assertTrue(automa.getSigma().contains("c"), "Sigma should contain 'c'");
    }

    @Test
    public void testAddFinalState() {
        automa.addFinalState();
        assertEquals(2, automa.getStateSize(), "There should be 2 states after adding a final state");
        assertEquals(1, automa.getFinalStateSize(), "There should be 1 final state");
    }

    @Test
    public void testIsStringAccepted() {
        // Add states
        automa.addState(); // q1
        automa.addState(); // q2
        automa.addFinalState(); //q3
        automa.addFinalState(); // q4

        // Add strings to Sigma
        automa.addString("aab");
        automa.addString("aba");
        automa.addString("abbb");
        automa.addString("abb");
        automa.addString("bb");
        automa.addString("a");
        automa.addString("b");

        // Add transitions
        automa.addTransition("q0", "aab", "q1");
        automa.addTransition("q0", "abbb", "q2");

        automa.addTransition("q1", "b", "q2");
        automa.addTransition("q1", "a", "q3");

        automa.addTransition("q2", "aba", "q1");
        automa.addTransition("q2", "b", "q3");

        automa.addTransition("q3", "abb", "q3");
        automa.addTransition("q3", "bb", "q4");

        automa.addTransition("q4", "a", "q1");
        automa.addTransition("q4", "b", "q2");

        // Test cases
        // Case 1: aababbbbb (Accepted)
        assertTrue(automa.isStringAccepted("aababbbb"), "String 'aababbbb' should be accepted (q3 is final)");

        // Case 2: abbbaba (Rejected)
        assertFalse(automa.isStringAccepted("abbbaba"), "String 'abbbaba' should be rejected (q2 is not final)");

        // Case 3: abbbabaaba (Rejected)
        assertFalse(automa.isStringAccepted("abbbabaaba"), "String 'abbbabaaba' should be rejected (no path for 'abbbabaaba')");
    }
}
