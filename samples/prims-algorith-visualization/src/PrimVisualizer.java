import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import sk.upjs.paz.graph.Edge;
import sk.upjs.paz.graph.Graph;
import sk.upjs.paz.graph.Vertex;
import sk.upjs.paz.graph.visualization.GraphVisualizer;

/**
 * Visualizer for Prim's algorithm for fining minimum spanning tree of graph
 */
public class PrimVisualizer {

   private static Color SPANNING_TREE_COLOR = new Color(62, 210, 29);

   private static Color INCIDENT_EDGE_COLOR = new Color(224, 90, 13);

   private static Color LOOP_EDGE_COLOR = new Color(207, 217, 218);

   /**
    * Edges of minimum spanning tree
    */
   private static Set<Edge> spanningTree = new HashSet<>();

   /**
    * Vertices inside spanning tree
    */
   private static Set<Vertex> spanningTreeVertices = new HashSet<>();

   /**
    * Edge with lowest weight leading to the vertex
    */
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

         Edge shortestEdge = shortestEdge();
         if (shortestEdge == null) {
            System.out.println("No more edges to add to spanning tree");
            break;
         }

         addToTree(shortestEdge, visualizer);
      }
   }

   /**
    * @return Randomly generated graph
    */
   private static Graph generateRandomGraph() {
      Random random = new Random();

      int vertices = 5 + random.nextInt(3);
      int edges = vertices * 2 + random.nextInt(vertices * 2);

      Graph graph = Graph.createRandomGraph(vertices, edges);
      graph.getEdges().forEach(edge -> {
         double randomWeight = roundToDigits(Math.random() * 10, 1);
         edge.setWeight(randomWeight);
      });

      return graph;
   }

   /**
    * @return Number rounded using half up method to the specified amount of digits
    */
   private static double roundToDigits(double number, int digits) {
      return new BigDecimal(number)
            .setScale(digits, RoundingMode.HALF_UP)
            .doubleValue();
   }

   /**
    * @return Random vertex from graph
    */
   private static Vertex getRandomVertex(Graph graph) {
      Vertex[] vertices = graph.getVertices().toArray(new Vertex[0]);
      int randomIndex = new Random().nextInt(vertices.length);
      return vertices[randomIndex];
   }

   /**
    * Add vertex to the tree and highlight it
    */
   private static void addToTree(Vertex vertex, GraphVisualizer visualizer) {
      spanningTreeVertices.add(vertex);

      refreshShortestDistance(vertex);
      highlightNewIncidentEdges(vertex, visualizer);
      lowlightNewLoopEdges(vertex, visualizer);

      visualizer.setColor(vertex, SPANNING_TREE_COLOR);
   }

   /**
    * Add edge and vertices it connects to the tree and highlight them
    */
   private static void addToTree(Edge edge, GraphVisualizer visualizer) {
      spanningTree.add(edge);

      addToTree(edge.getTarget(), visualizer);
      addToTree(edge.getSource(), visualizer);

      visualizer.setColor(edge, SPANNING_TREE_COLOR);
   }

   /**
    * Update shortest distances to the vertices connected to added vertex
    */
   private static void refreshShortestDistance(Vertex addedVertex) {
      addedVertex.getEdges()
                 .stream()
                 .filter(PrimVisualizer::incidentEdge)
                 .forEach(PrimVisualizer::refreshEdgeDistance);
   }

   /**
    * Update shortest distances to vertices connected by the edge
    */
   private static void refreshEdgeDistance(Edge edge) {
      shortestEdgeToVertex.compute(edge.getTarget(), (vertex, shortestEdge) -> {
         return shortestEdge == null ? edge : shorterEdge(edge, shortestEdge);
      });

      shortestEdgeToVertex.compute(edge.getSource(), (vertex, shortestEdge) -> {
         return shortestEdge == null ? edge : shorterEdge(edge, shortestEdge);
      });
   }

   /**
    * Highlights edges that can be added to the spanning tree
    */
   private static void highlightNewIncidentEdges(Vertex vertex, GraphVisualizer visualizer) {
      vertex.getEdges()
            .stream()
            .filter(PrimVisualizer::incidentEdge)
            .forEach(edge -> visualizer.setColor(edge, INCIDENT_EDGE_COLOR));
   }

   /**
    * Greys out loop edges
    */
   private static void lowlightNewLoopEdges(Vertex vertex, GraphVisualizer visualizer) {
      vertex.getEdges()
            .stream()
            .filter(PrimVisualizer::loopEdge)
            .forEach(edge -> visualizer.setColor(edge, LOOP_EDGE_COLOR));
   }

   /**
    * @return Shortest edge that can be added to the spanning tree
    */
   private static Edge shortestEdge() {
      return shortestEdgeToVertex.values()
                                 .stream()
                                 .filter(PrimVisualizer::incidentEdge)
                                 .reduce(PrimVisualizer::shorterEdge)
                                 .orElse(null);
   }

   /**
    * @return Edge can be added to the spanning tree
    */
   private static boolean incidentEdge(Edge edge) {
      return spanningTreeVertices.contains(edge.getTarget()) ^ spanningTreeVertices.contains(edge.getSource());
   }

   /**
    * @return Adding edge would create a loop inside the graph
    */
   private static boolean loopEdge(Edge edge) {
      return !spanningTree.contains(edge) &&
            spanningTreeVertices.contains(edge.getTarget()) &&
            spanningTreeVertices.contains(edge.getSource());
   }

   /**
    * @return Edge with lower weight
    */
   private static Edge shorterEdge(Edge firstEdge, Edge secondEdge) {
      return firstEdge.getWeight() < secondEdge.getWeight() ? firstEdge : secondEdge;
   }

}
