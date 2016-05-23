package ru.ifmo.kot.game.model;

import java.text.MessageFormat;

public class Edge implements Comparable<Edge> {

    static final String STRING_PATTERN = "{0} - {1}: {2}";

    private final int srcVrtxIndx;
    private final int dstVrtxIndx;
    private int weight;

    Edge(final int srcVrtxIndx, final int dstVrtxIndx, final int weight) {
        this.srcVrtxIndx = srcVrtxIndx;
        this.dstVrtxIndx = dstVrtxIndx;
        this.weight = weight;
    }

    public int anyVertexIndex() {
        return srcVrtxIndx;
    }

    public int otherVertexIndex(final int vrtxIndx) {
        if (vrtxIndx == srcVrtxIndx) {
            return dstVrtxIndx;
        }
        if (vrtxIndx == dstVrtxIndx) {
            return srcVrtxIndx;
        }
        return -1; //todo maybe replace with Exception
    }

    public int otherVertexIndex() {
        return dstVrtxIndx;
    }

    public int weight() {
        return weight;
    }

    @Override
    public int compareTo(Edge otherEdge) {
        if (weight < otherEdge.weight) {
            return -1;
        } else {
            if (weight > otherEdge.weight) {
                return +1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format(STRING_PATTERN, srcVrtxIndx, dstVrtxIndx, weight);
    }
}
