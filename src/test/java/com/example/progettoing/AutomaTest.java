package com.example.progettoing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

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
    /*
    @Test
    public void testAddTransition() {
        int result = automa.addTransition("q0", "a", "q1");
        assertEquals(-1, result);
        automa.addState();
        automa.addString("a");
        result = automa.addTransition("q0", "a", "q1");
        assertEquals(1, result);
        automa.printDelta();
    }

    @Test
    public void testClearAll() {
        automa.addString("a");
        automa.addState();
        automa.addFinalState();
        automa.addTransition("q0", "a", "q1");
        assertEquals(1, automa.getStateSize());
        assertEquals(0, automa.getFinalStateSize());
        assertTrue(automa.getSigma().isEmpty());
        assertTrue(automa.getDelta().isEmpty());
    }

    @Test
    public void testPrintQ() {
        assertEquals("q0", automa.printQ());
    }

    @Test
    public void testPrintF() {
        automa.addFinalState();
        assertEquals("q1", automa.printF());
    }

    @Test
    public void testPrintSigma() {
        automa.addString("a");
        automa.addString("b");
        assertEquals("a, b", automa.printSigma());
    }*/

    @Test
    public void testAddState() {
        automa.addState();
        assertEquals(2, automa.getStateSize());
        assertTrue(automa.getQ().contains("q1"));
    }
/*
    @Test
    public void testAddString() {
        automa.addString("c");
        assertTrue(automa.getSigma().contains("c"));
    }

    @Test
    public void testAddFinalState() {
        automa.addFinalState();
        assertEquals(2, automa.getStateSize());
        assertEquals(1, automa.getFinalStateSize());
    }

    @Test
    public void testIsStringAccepted() {
        // Without transitions, all should be rejected
        assertFalse(automa.isStringAccepted("a"));
        assertFalse(automa.isStringAccepted("b"));
        assertFalse(automa.isStringAccepted("aa"));
        assertFalse(automa.isStringAccepted("ab"));

        // Add states and transitions and test again
        automa.addState(); // q1
        automa.addState(); // q2
        automa.addString("a");

        automa.addTransition("q0", "a", "q1");
        automa.addTransition("q1", "a", "q2");
        automa.addFinalState(); // q3

        assertTrue(automa.isStringAccepted("a")); // q0 -> q1
        assertFalse(automa.isStringAccepted("aa")); // q0 -> q1 -> q2 (not final state)
        assertFalse(automa.isStringAccepted("b")); // no transition from q0 on b

        automa.addTransition("q2", "a", "q3");

        assertTrue(automa.isStringAccepted("aaa")); // q0 -> q1 -> q2 -> q3
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
        assertEquals("q1", delta.get("q0").get("a"));
    }*/
}
