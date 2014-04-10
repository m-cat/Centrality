import java.util.*;
import java.io.*;

public class Graph {
  
  boolean directed;
  
  ArrayList<Node> V = new ArrayList<Node>(); // All vertices in the graph
  ArrayList<Edge> E = new ArrayList<Edge>(); // All edges
  ArrayList<Node> S = new ArrayList<Node>(); // All source vertices
  ArrayList<Node> T = new ArrayList<Node>(); // All destination vertices
  
  public class Node {
    boolean inS = true, inT = true;
    String name;
    ArrayList<Node> neighbors = new ArrayList<Node>();
    
    public Node(String n) {
      name = n;
    }
    
    public Node(String n, boolean s, boolean t) {
      name = n;
      inS = s;
      inT = t;
    }
  }
  
  public class Edge {
    Node node1, node2;
    boolean isGhost = false; // whether the edge is a ghost edge
    boolean directed = Graph.this.directed;
    
    public Edge(Node n1, Node n2, boolean ghost) {
      node1 = n1;
      node2 = n2;
      node1.neighbors.add(node2);
      isGhost = ghost;
    }
  }
  
  public Graph(boolean dir) {
    directed = dir;
  }
  
  public void addNode(String name) {
    V.add(new Node(name));
  }
  
  public void addEdge(String name1, String name2, boolean ghost) {
    Node node1 = null, node2 = null;
    
    for (Node v : V) {
      if (v.name.equals(name1)) {
        node1 = v;
        break;
      }
    }
    for (Node v : V) {
      if (v.name.equals(name2)) {
        node2 = v;
        break;
      }
    }
            
    if (node1 == null) {
      node1 = new Node(name1);
      V.add(node1);
    }
    if (node2 == null) {
      node2 = new Node(name2);
      V.add(node2);
    }
    E.add(new Edge(node1, node2, ghost));
  }
  
  /* Import a graph from .txt format */
  public void importTxt(String filename) throws FileNotFoundException, IOException {
    String name1, name2, line;
    String[] tokens;
    BufferedReader in = new BufferedReader(new FileReader(filename));
    
    while (in.ready()) {
      line = in.readLine();
      tokens = line.split("\t");
      addEdge(tokens[0], tokens[1], false);
    }
    
    in.close();
  }
  
  /* Export the graph in .dot format */
  public void exportDot() throws IOException {
    String conn;
    PrintWriter out = new PrintWriter(new FileWriter("graph.dot"));
    if (directed) {
      out.println("digraph G{");
      conn = "->";
    }
    else {
      out.println("graph G {");
      conn = "--";
    }
    out.println("\tnode [shape=circle, label=\"\"];");
    for (Node n : V) {
      if (n.inS && n.inT)
        out.println("\t" + n.name + " [style=filled, color=\"palevioletred\"];");
      else if (n.inS)
        out.println("\t" + n.name + " [style=filled, color=\"lightsalmon\"];");
      else if (n.inT)
        out.println("\t" + n.name + " [style=filled, color=\"powderblue\"];");
    }
    for (Edge e : E) {
      out.print("\t" + e.node1.name + " " + conn + " " + e.node2.name + " [");
      if (e.isGhost)
        out.print("style=dotted, ");
      if (!e.directed)
        out.print("arrowhead=none, ");
      out.println("label=\"\"];");
    }
    out.println("}");
    out.close();
  }
  
  public static void main(String args[]) throws IOException {
    Graph g = new Graph(true);
    //g.importTxt("TransMatrix.txt");
    g.addEdge("A", "B", false);
    g.addEdge("B", "C", true);
    g.exportDot();
  }
  
}