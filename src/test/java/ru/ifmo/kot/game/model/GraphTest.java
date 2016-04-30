package ru.ifmo.kot.game.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GraphTest {

    private static final int NUMBER_OF_VERTICES = 10;

    private Graph graph;

    @Before
    public void initGraph() {
        graph = new Graph(NUMBER_OF_VERTICES);
    }

    @Test
    public void verticesShouldBeConnected() throws Exception {
        final int srcVertexIndex = 1;
        final int dstVertexIndex = 3;
        graph.putEdge(srcVertexIndex, dstVertexIndex, 56);
        assertTrue(graph.areConnected(srcVertexIndex, dstVertexIndex));
    }

    @Test
    public void weightShouldBeEqualToSpecified() throws Exception {
        final int srcVertexIndex = 1;
        final int dstVertexIndex = 3;
        final int weight = 56;
        graph.putEdge(srcVertexIndex, dstVertexIndex, weight);
        assertEquals(weight, graph.getWeight(srcVertexIndex, dstVertexIndex));
    }


}