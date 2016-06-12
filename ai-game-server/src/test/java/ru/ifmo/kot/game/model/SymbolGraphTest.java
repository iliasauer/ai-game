package ru.ifmo.kot.game.model;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.ifmo.kot.game.util.BinaryRandom;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Double.*;
import static java.lang.Math.pow;
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
    }

    private List<int[]> distributeVerticesCoordinates(final int numberOfVertices) {
        return distributeVerticesCoordinates(numberOfVertices, 150);
    }

    private List<int[]> distributeVerticesCoordinates(final int verticesNum, final int step) {
        if(verticesNum < 0) {
            throw new IllegalArgumentException();
        }
        if(verticesNum == 0) {
            return Collections.emptyList();
        }
        final int numSqrt = ((int) (sqrt(verticesNum))) + 3;
        final int rowsNum = Math.max(2, numSqrt - 1);
        final int colsNum = numSqrt + 2;
        final int[][][] grid = new int[rowsNum][colsNum][2];
        int verticesCounter = 0;
        final List<int[]> verticesCoordinates = new ArrayList<>();
        for(int j = 0; j < colsNum; j++) {
            for(int i = 0; i < rowsNum; i++) {
                final int[] coords = grid[i][j];
                if(USUAL_RANDOM.nextBoolean()) {
                    coords[0] = j * step + USUAL_RANDOM.nextInt(step);
                    coords[1] = i * step + USUAL_RANDOM.nextInt(step);
                    verticesCoordinates.add(coords);
                    verticesCounter++;
                } else {
                    coords[0] = -1;
                }
            }
        }
        if(verticesCounter < verticesNum) {
            return distributeVerticesCoordinates(verticesNum, step);
        } else {
            return verticesCoordinates;
        }
    }

    private static class DstVrtxWeightPair
        implements Map.Entry<Integer, Integer> {

        private final int key;
        private final int value;

        DstVrtxWeightPair(final int key, final int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Integer getKey() {
            return key;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public Integer setValue(final Integer value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return key + ": " + value;
        }
    }

    private void collectMinWeights() {
        final int QUEUE_MAX_SIZE = 10;
        final Queue<Map.Entry<Integer, Integer>> dstWeights =
            new PriorityQueue<>(QUEUE_MAX_SIZE, Map.Entry.<Integer, Integer>comparingByValue().reversed());
        final Random random = new Random();
        IntStream.range(0, 1000).forEach(i -> {
            dstWeights.offer(new DstVrtxWeightPair(i, random.nextInt(100)));
            if(dstWeights.size() > QUEUE_MAX_SIZE) {
                dstWeights.poll();
            }
        });
        dstWeights.forEach(System.out:: println);
    }

    private int calculateWeight(final int[] coordinates1, final int[] coordinates2) {
        final int x = coordinates2[0] - coordinates1[0];
        final int y = coordinates2[1] - coordinates1[1];
        return (int) sqrt(sum(pow(x, 2), pow(y, 2)));
    }

    public void getCoordinates(final int vertexIndex) {
    }

    private void printGrid(final int[][][] grid) {
        Stream.of(grid).forEach(vector -> {
            Stream.of(vector).forEach(coords -> System.out.print(Arrays.toString(coords) + " \n"));
        });
    }

    public void printDistributedCoordinates(final List<int[]> verticesCoordinates) {
        IntStream.range(0, verticesCoordinates.size()).forEach(i -> {
            final int[] vertexCoords = verticesCoordinates.get(i);
            System.out.println("{\n");
            System.out.println(
                "\t\"data\": {\n\t\t\"id\": " + "\"v" + i + "\",\n\t\t\"name\": " + "\"city" + i +
                    "\"\n\t},");
            System.out.println(
                "\t\"position\": {\n\t\t\"x\": " + vertexCoords[0] + ",\n\t\t\"y\": " +
                    vertexCoords[1] + "\n\t\n}");
            System.out.println("},");
        });
    }

    @Test
    public void shouldDistributeCoordinates() {
        final List<int[]> verticesCoordinates = distributeVerticesCoordinates(37);
        printDistributedCoordinates(verticesCoordinates);
        //        System.out.println(calculateWeight(verticesCoordinates.get(0), verticesCoordinates.get(1)));
    }

    @Test
    public void shouldCollectMinWeights() {
        collectMinWeights();
    }
}