import java.util.*;
import java.io.*;

public class Algorithms {
  
  /* Calculate a given node's centrality in a DAG */
  /* INCOMPLETE - will probably not implement this for the project */
  public static int centralityNodeDAG(Graph g, String name) {
    HashMap<String, Integer> PLIST = new HashMap<String, Integer>();
    Node n = g.findNode(name);
    int prefix = 0, suffix = 0;
    
    for (Node t : g.T)
      suffix += PLIST.get(t.name+":"+n.name);
    return prefix * suffix;
  }
  
  /* Calculate a given node's centrality in a weighted, undirected graph */
  public static int centralityNodeWUG(Graph g, String name) {
    PriorityQueue<Node> q = new PriorityQueue<Node>();
    
    /* Initialize Q */
    for (Node v : g.V) {
      v.distance = v.inS ? 0 : Double.POSITIVE_INFINITY;
      v.prefix = 0;
      v.visited = false;
      q.add(v);
    }
    
    /* Run Dijkstra's */
    while (!q.isEmpty()) {
      Node u = q.poll();
      u.visited = true;
      
      for (Node v : u.neighbors) {
        if (!v.visited) {
          Double alt = u.distance + g.getWeight(u.name, v.name);
          if (alt < v.distance) {
            q.remove(v);
            v.distance = alt;
            v.prefix = 1;
            q.add(v);
          }
          else if (alt == v.distance) {
            q.remove(v);
            v.prefix ++;
            q.add(v);
          }
        }
      }
    }
    
    return g.findNode(name).prefix;
  }
  
  public static Graph maximizeCentralityNode(Graph g, String name, int k) {
    return new Graph(true, true, true, true);
  }
  
  public static void main(String args[]) throws IOException {
    Graph g = new Graph(false, true, false, false);
    //g.importTxt("TransMatrix.txt");
    g.importTxt("testNodeCent.txt");
    g.addNodeS("D");
    g.addNodeT("A");
    g.exportDot();
    System.out.println(centralityNodeWUG(g, "E"));
  }
  
}