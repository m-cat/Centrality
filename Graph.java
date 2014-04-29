import java.util.*;
import java.io.*;

public class Graph {
  
  String name = null;  
  boolean directed;
  boolean weighted;
  boolean defaultS; // whether nodes are part of the S set by default
  boolean defaultT;
  
  ArrayList<Node> V = new ArrayList<Node>(); // All vertices in the graph
  ArrayList<Edge> E = new ArrayList<Edge>(); // All edges
  ArrayList<Node> S = new ArrayList<Node>(); // All source vertices
  ArrayList<Node> T = new ArrayList<Node>(); // All destination vertices
  
  HashMap<String, Double> weights = new HashMap<String, Double>();
  HashMap<String, Double> edgeScores = new HashMap<String, Double>();
  
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
  
  public Node addNode(String name, boolean s, boolean t) {
    Node n = new Node(name, s, t);
    V.add(n);
    if (s)
      S.add(n);
    if (t)
      T.add(n);
    return n;
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
  
  public Edge findEdge(String name1, String name2) {
    for (Edge e : E) {
      if (e.node1.name.equals(name1) && e.node2.name.equals(name2))
        return e;
      else if (!directed && e.node2.name.equals(name1) && e.node1.name.equals(name2))
        return e;
    }
    return null;
  }
  
  public Edge addEdge(Edge e) {
    e.connect();
    E.add(e);
    weights.put(e.node1.name+":"+e.node2.name, e.weight);
    edgeScores.put(e.node1.name+":"+e.node2.name, 0.0);
    if (!directed) {
      weights.put(e.node2.name+":"+e.node1.name, e.weight);
      edgeScores.put(e.node2.name+":"+e.node1.name, 0.0);
    }
    return e;
  }
  public Edge addEdge(String name1, String name2) {
    return addEdge(name1, name2, false, -1);
  }
  public Edge addEdge(String name1, String name2, boolean ghost, double weight) {
    Node node1 = null, node2 = null;
    Edge e;
    
    node1 = findNode(name1);
    node2 = findNode(name2);
    
    if (node1 == null) {
      node1 = addNode(name1, defaultS, defaultT);
    }
    if (node2 == null) {
      node2 = addNode(name2, defaultS, defaultT);
    }
    if (!directed && (node1.neighbors.contains(node2) || node2.neighbors.contains(node1)))
        return null; // graph is undirected and edge already exists
    E.add(e = new Edge(node1, node2, ghost, directed, weight));
    weights.put(node1.name + ":" + node2.name, weight);
    if (!directed)
      weights.put(node2.name + ":" + node1.name, weight);
    edgeScores.put(node1.name+":"+node2.name, 0.0);
    if (!directed)
      edgeScores.put(node2.name+":"+node1.name, 0.0);
    return e;
  }
  
  public Edge removeEdge(String name1, String name2) {
    Edge e = findEdge(name1, name2);
    removeEdge(e);
    return e;
  }
  
  public void removeEdge(Edge e) {
    e.disconnect();
    E.remove(e);
    weights.put(e.node1.name+":"+e.node2.name, null);
    edgeScores.put(e.node1.name+":"+e.node2.name, null);
    if (!directed) {
      weights.put(e.node2.name+":"+e.node1.name, null);
      edgeScores.put(e.node2.name+":"+e.node1.name, null);
    }
  }
  
  public boolean edgeExists(String name1, String name2) {
    return (edgeScores.get(name1+":"+name2) != null || (!directed && edgeScores.get(name2+":"+name1) != null));
  }
  
  public double getWeight(String name1, String name2) {
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
  
  /* Imports very basic GML files */
  public void importGML(String filename) throws FileNotFoundException, IOException {
    String name1, name2, line;
    String[] tokens;
    BufferedReader in = new BufferedReader(new FileReader(filename));
    
    while (in.ready()) {
      line = in.readLine();
      tokens = line.trim().split(" ");
      if (!tokens[0].equals("edge"))
        continue;
      line = in.readLine();
      line = in.readLine();
      tokens = line.trim().split(" ");
      name1 = tokens[1];
      line = in.readLine();
      tokens = line.trim().split(" ");
      name2 = tokens[1];
      line = in.readLine();
      addEdge(name1, name2);
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
    if (name != null)
      out.println("\tgraph [label=\""+name+"\"];");
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
      //out.print("[label=\""+Integer.toString(n.pathsIn)+"\"]");
      out.print("[label=\""+Integer.toString(n.pathsInOK)+"\"]");
      //out.print("[label=\""+Double.toString(n.distance)+"\"]");
      //out.print("[label=\""+Integer.toString(n.destAmount)+"\"]");
      out.println(";");
    }
    for (Edge e : E) {
      out.print("\t" + e.node1.name + " " + conn + " " + e.node2.name + " ");
      if (e.isGhost)
        out.print("[style=dotted]");
      if (e.weight >= 0)
        out.print("[label=\"" + Double.toString(e.weight) + "\"]");
      //if (edgeScores.get(e.node1.name+":"+e.node2.name) != 0.0)
      //  out.print("[label=\"" + Double.toString(edgeScores.get(e.node1.name+":"+e.node2.name)) + "\"]");
      out.println(";");
    }
    out.println("}");
    out.close();
  }
  
  public void print() {
    for (Node v : V) {
      System.out.print(v.name + ": ");
      for (Node n : v.neighbors) {
        System.out.print(n.name);
        if (weighted)
          System.out.print("("+getWeight(v.name, n.name)+")");
        System.out.print(" ");
      }
      System.out.println();
    }
  }
}