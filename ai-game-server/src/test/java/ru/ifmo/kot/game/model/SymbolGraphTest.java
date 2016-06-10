package ru.ifmo.kot.game.model;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.ifmo.kot.game.util.BinaryRandom;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import static java.lang.Math.sqrt;

/**
 Created on 10.06.16.
 */
public class SymbolGraphTest {

    private static Random USUAL_RANDOM;
    private static BinaryRandom BINARY_RANDOM;

    @BeforeClass
    public static void setUpGenerators()
    throws Exception {
        USUAL_RANDOM = new Random();
        BINARY_RANDOM = new BinaryRandom(0.5);
    }

    private int[][][] createGrid(final int numberOfVertices) {
        return createGrid(numberOfVertices, 10);
    }

    private int[][][] createGrid(final int verticesNum, final int step) {
        if(verticesNum < 0) {
            throw new IllegalArgumentException();
        }
        if(verticesNum == 0) {
            return new int[0][0][0]; //todo implement
        }
        final int numSqrt = ((int) (sqrt(verticesNum))) + 3;
        final int rowsNum = Math.max(2, numSqrt - 1);
        final int colsNum = numSqrt + 2;
        final int[][][] grid = new int[rowsNum][colsNum][2];
        int verticesCounter = 0;
        for (int i = 0; i < rowsNum; i++) {
            for (int j = 0; j < colsNum; j++) {
                final int[] coords = grid[i][j];
                if(BINARY_RANDOM.nextBoolean()) {
                    coords[0] = j * step + USUAL_RANDOM.nextInt(step);
                    coords[1] = -(i * step + USUAL_RANDOM.nextInt(step));
                    verticesCounter++;
                } else {
                    coords[0] = -1;
                }
            }
        }
        if (verticesCounter < verticesNum) {
            System.out.println("Bad grid");
        } else {
            System.out.println("Good grid");
        }
        return grid;
    }

    private void printGrid(final int[][][] grid) {
        Stream.of(grid)
            .forEach(vector -> {
                Stream.of(vector).forEach(coords -> System.out.print(Arrays.toString(coords) + " "));
                System.out.println();
                });
    }

    @Test
    public void shouldPrintSomeGrid()
    throws Exception {
        printGrid(createGrid(37));
    }
}