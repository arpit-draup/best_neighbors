package com.maxdemarzi;

import com.maxdemarzi.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.Collections.addAll;

public class Procedures {


    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;

    private static final Pair<ArrayList<Node>, Double> DUMMY_PAIR = Pair.of(new ArrayList<Node>(), 99999.99);


    @Procedure(name = "com.maxdemarzi.best_neighbors", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.best_neighbors(Node start, Number n, Number hops,Number direction)")
    public Stream<SimplifiedWeightedPathResult> bestNeighbors(@Name("start") Node start, @Name("n") Number n, @Name("hops") Number hops,@Name("direction") Number direction) throws IOException {
        HashMap<Node, Pair<ArrayList<Node>, Double>> nodePathMap = new HashMap<>();

        // First Hop
        Direction DEFAULT_DIRECTION = Direction.OUTGOING;
        if(direction.intValue() == 1)
            DEFAULT_DIRECTION = Direction.INCOMING;

        for(Relationship r : start.getRelationships(DEFAULT_DIRECTION,RelationshipTypes.to)) {
            Node next = r.getOtherNode(start);
            Double weight = nodePathMap.getOrDefault(next, DUMMY_PAIR).other();
            Double cost = ((Number)r.getProperty("count", 0.0)).doubleValue();
            if (weight > cost) {
                nodePathMap.put(next, Pair.of(new ArrayList<Node>(){{add(start); add(next); }}, cost));
            }
        }

        for(int i = 1; i < hops.intValue(); i++) {
            nextHop(n, nodePathMap,DEFAULT_DIRECTION);
        }

        ArrayList<Entry<Node, Pair<ArrayList<Node>, Double>>> list = new ArrayList<>(nodePathMap.entrySet());
        list.sort(Comparator.comparing(o -> o.getValue().other()));

        return list.subList(0, Math.min(list.size(), n.intValue())).stream().map(x -> new SimplifiedWeightedPathResult(x.getValue().first(), x.getValue().other()));
    }

    private void nextHop(@Name("n") Number n, HashMap<Node, Pair<ArrayList<Node>, Double>> nodePathMap,Direction DEFAULT_DIRECTION) {
        if(nodePathMap.size() < n.intValue()) {
            ArrayList<Entry<Node, Pair<ArrayList<Node>, Double>>> list = new ArrayList<>(nodePathMap.entrySet());

            for(Entry<Node, Pair<ArrayList<Node>, Double>> entry : list) {
                ArrayList<Node> nodes = entry.getValue().first();
                Double cost = entry.getValue().other();
                Node from = nodes.get(nodes.size() - 1);
                for(Relationship r : from.getRelationships(DEFAULT_DIRECTION,RelationshipTypes.to)) {
                    Node next = r.getOtherNode(from);
                    Double nextCost = ((Number)r.getProperty("count", 0.0)).doubleValue();
                    if (nodePathMap.containsKey(next)) {
                        Pair<ArrayList<Node>, Double> found = nodePathMap.get(next);
                        if(found.other() > cost + nextCost) {
                            nodePathMap.put(next, Pair.of(new ArrayList<Node>(){{addAll(nodes); add(next);}}, cost + nextCost));
                        }
                    } else {
                        nodePathMap.put(next, Pair.of(new ArrayList<Node>(){{addAll(nodes); add(next);}}, cost + nextCost));
                    }
                }
            }
        }
    }
}
