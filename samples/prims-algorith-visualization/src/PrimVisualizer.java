import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import sk.upjs.paz.graph.Edge;
import sk.upjs.paz.graph.Graph;
import sk.upjs.paz.graph.Vertex;
import sk.upjs.paz.graph.visualization.GraphVisualizer;

public class PrimVisualizer {

   private static Color SPANNING_TREE_COLOR =  new Color(62, 210, 29);

   private static Color INCIDENT_EDGE_COLOR = new Color(224, 90, 13);

   private static Color LOOP_EDGE_COLOR =  new Color(207, 217, 218);

   private static Set<Edge> spanningTree = new HashSet<>();

   private static Set<Vertex> spanningTreeVertices = new HashSet<>();

   private static Map<Vertex, Edge> shortestEdgeToVertex = new HashMap<>();

   public static void main(String[] args) {
      Graph graph = generateRandomGraph();

      GraphVisualizer visualizer = new GraphVisualizer(graph);
      visualizer.addToMonitor("Spanning Tree", spanningTree);
      visualizer.addToMonitor("Spanning Tree Vertices", spanningTreeVertices);

      Vertex firstVertex = getRandomVertex(graph);
      addToTree(firstVertex, visualizer);

      while (true) {
         visualizer.pause();

         Optional<Edge> shortestEdge = shortestEdge();

         if (shortestEdge.isPresent()) {
            addToTree(shortestEdge.get(), visualizer);
         } else {
            System.out.println("No more edges to add to spanning tree");
            break;
         }
      }
   }

   private static Graph generateRandomGraph() {
      int vertices = 5 + new Random().nextInt(3);
      int edges = vertices * 2 + new Random().nextInt(vertices * 2);

      Graph graph = Graph.createRandomGraph(vertices, edges);
      graph.getEdges().forEach(edge -> {
         double randomWeight = roundToDigits(Math.random() * 10, 1);
         edge.setWeight(randomWeight);
      });

      //      graph.getVertices().forEach(vertex -> {
      //         if (vertex.getOutEdges().size() == 0) {
      //            graph.removeVertex(vertex);
      //         }
      //      });

      return graph;
   }

   private static double roundToDigits(double number, int digits) {
      return new BigDecimal(number)
            .setScale(digits, RoundingMode.HALF_UP)
            .doubleValue();
   }

   private static Vertex getRandomVertex(Graph graph) {
      Vertex[] vertices = graph.getVertices().toArray(new Vertex[0]);
      int randomIndex = new Random().nextInt(vertices.length);
      return vertices[randomIndex];
   }

   private static void addToTree(Vertex vertex, GraphVisualizer visualizer) {
      spanningTreeVertices.add(vertex);

      refreshShortestDistance(vertex);
      highlightNewIncidentEdges(vertex, visualizer);
      lowlightNewLoopEdges(vertex, visualizer);

      visualizer.setColor(vertex, SPANNING_TREE_COLOR);
   }

   private static void addToTree(Edge edge, GraphVisualizer visualizer) {
      spanningTree.add(edge);

      addToTree(edge.getTarget(), visualizer);
      addToTree(edge.getSource(), visualizer);

      visualizer.setColor(edge, SPANNING_TREE_COLOR);
   }

   private static void refreshShortestDistance(Vertex addedVertex) {
      addedVertex.getEdges()
                 .stream()
                 .filter(PrimVisualizer::incidentEdge)
                 .forEach(edge -> {
                    shortestEdgeToVertex.computeIfPresent(edge.getTarget(), (vertex, shortestEdge) -> {
                       return shorterEdge(edge, shortestEdge);
                    });

                    shortestEdgeToVertex.putIfAbsent(edge.getTarget(), edge);
                 });
   }

   private static void highlightNewIncidentEdges(Vertex vertex, GraphVisualizer visualizer) {
      vertex.getEdges()
            .stream()
            .filter(PrimVisualizer::incidentEdge)
            .forEach(edge -> visualizer.setColor(edge, INCIDENT_EDGE_COLOR));
   }

   private static void lowlightNewLoopEdges(Vertex vertex, GraphVisualizer visualizer) {
      vertex.getEdges()
            .stream()
            .filter(PrimVisualizer::loopEdge)
            .forEach(edge -> visualizer.setColor(edge, LOOP_EDGE_COLOR));
   }

   private static Optional<Edge> shortestEdge() {
      return shortestEdgeToVertex.values()
                                 .stream()
                                 .filter(PrimVisualizer::incidentEdge)
                                 .reduce(PrimVisualizer::shorterEdge);
   }

   private static boolean incidentEdge(Edge edge) {
      return spanningTreeVertices.contains(edge.getTarget()) ^ spanningTreeVertices.contains(edge.getSource());
   }

   private static boolean loopEdge(Edge edge) {
      return !spanningTree.contains(edge) &&
            spanningTreeVertices.contains(edge.getTarget()) &&
            spanningTreeVertices.contains(edge.getSource());
   }

   private static Edge shorterEdge(Edge firstEdge, Edge secondEdge) {
      return firstEdge.getWeight() < secondEdge.getWeight() ? firstEdge : secondEdge;
   }

}
