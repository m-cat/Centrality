import java.util.*;
import java.io.*;

public class Graph {
  
  boolean directed;
  boolean weighted;
  boolean defaultS; // whether nodes are part of the S set by default
  boolean defaultT;
  
  ArrayList<Node> V = new ArrayList<Node>(); // All vertices in the graph
  ArrayList<Edge> E = new ArrayList<Edge>(); // All edges
  ArrayList<Node> S = new ArrayList<Node>(); // All source vertices
  ArrayList<Node> T = new ArrayList<Node>(); // All destination vertices
  
  HashMap<String, Integer> weights = new HashMap<String, Integer>();
  
  public Graph(boolean dir, boolean weight, boolean defS, boolean defT) {
    directed = dir;
    weighted = weight;
    defaultS = defS;
    defaultT = defT;
  }
  
  public Node findNode(String name) {
    for (Node v : V) {
      if (v.name.equals(name))
        return v;
    }
    return null;
  }
  
  public void addNode(String name, boolean s, boolean t) {
    Node n = new Node(name, s, t);
    V.add(n);
    if (s)
      S.add(n);
    if (t)
      T.add(n);
  }
  
  /* Add an existing node to the set S */
  public void addNodeS(String name) {
    Node n = findNode(name);
    assert(n != null);
    S.add(n);
    n.inS = true;
  }
  
  /* Add an existing node to the set T */
  public void addNodeT(String name) {
    Node n = findNode(name);
    assert(n != null);
    T.add(n);
    n.inT = true;
  }
  
  public void addEdge(String name1, String name2) {
    addEdge(name1, name2, false, -1);
  }
  public void addEdge(String name1, String name2, boolean ghost, int weight) {
    Node node1 = null, node2 = null;
    
    node1 = findNode(name1);
    node2 = findNode(name2);
    
    if (node1 == null) {
      node1 = new Node(name1, defaultS, defaultT);
      V.add(node1);
    }
    if (node2 == null) {
      node2 = new Node(name2, defaultS, defaultT);
      V.add(node2);
    }
    if (!directed && (node1.neighbors.contains(node2) || node2.neighbors.contains(node1)))
        return; // graph is undirected and edge already exists
    E.add(new Edge(node1, node2, ghost, directed, weight));
    if (weight != -1) {
      weights.put(node1.name + ":" + node2.name, weight);
      weights.put(node2.name + ":" + node1.name, weight);
    }
  }
  
  public int getWeight(String name1, String name2) {
    return weights.get(name1 + ":" + name2);
  }
  
  /* Import a graph from .txt format */
  public void importTxt(String filename) throws FileNotFoundException, IOException {
    String name1, name2, line;
    String[] tokens;
    BufferedReader in = new BufferedReader(new FileReader(filename));
    
    while (in.ready()) {
      line = in.readLine();
      tokens = line.split("\t");
      if (weighted)
        addEdge(tokens[0], tokens[1], false, Integer.parseInt(tokens[2]));
      else
        addEdge(tokens[0], tokens[1]);
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
    if (!directed)
      out.println("\tedge [arrowhead=none];");
    for (Node n : V) {
      out.print("\t" + n.name + " ");
      if (n.inS && n.inT)
        out.print("[style=filled, color=\"palevioletred\"]");
      else if (n.inS)
        out.print("[style=filled, color=\"lightsalmon\"]");
      else if (n.inT)
        out.print("[style=filled, color=\"powderblue\"]");
      if (n.inGroup)
        out.print("[shape=doublecircle]");
      //out.print("[label=\""+n.name+"\"]");
      out.print("[label=\""+Integer.toString(n.pathsInOK)+"\"]");
      out.println(";");
    }
    for (Edge e : E) {
      out.print("\t" + e.node1.name + " " + conn + " " + e.node2.name + " ");
      if (e.isGhost)
        out.print("[style=dotted]");
      if (e.weight >= 0)
        out.print("[label=\"" + Integer.toString(e.weight) + "\"]");
      out.println(";");
    }
    out.println("}");
    out.close();
  }
  
  public void print() {
    for (Node v : V) {
      System.out.print(v.name + ": ");
      for (Node n : v.neighbors)
        System.out.print(n.name + " ");
      System.out.println();
    }
  }
}