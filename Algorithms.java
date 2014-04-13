import java.util.*;
import java.io.*;

public class Algorithms {
  
  /* Calculate a given node's centrality in a DAG */
  /* INCOMPLETE - will probably not implement this for the project */
  public static int centralityNodeDAG(Graph g, String name) {
    HashMap<String, Integer> plist = new HashMap<String, Integer>();
    Node n = g.findNode(name);
    int prefix = 0, suffix = 0;
    
    for (Node t : g.T)
      suffix += plist.get(t.name+":"+n.name);
    return prefix * suffix;
  }
  
  /* Calculate a given node's centrality in an unweighted, undirected graph */
  public static int centralityNodeUUG(Graph g, String name) {
    String[] s = {name};
    return centralityGroupUUG(g, s);
  }
  
  /* Calculate a group's centrality in an unweighted, undirected graph */
  public static int centralityGroupUUG(Graph g, String[] group) {
    int centrality = 0;
    
    /* Initialize group */
    for (Node n : g.V)
      n.inGroup = false;
    for (String str : group) {
      Node n = g.findNode(str);
      n.inGroup = true;
    }
    for (Node s : g.S) {
      centrality += centralityBFS(g, s);
    }
    return centrality;
  }
  
  public static int centralityBFS(Graph g, Node s) {
    LinkedList<Node> q = new LinkedList<Node>();
    int sumPaths = 0;
    
    /* Initialize bookkeeping */
    for (Node v : g.V) {
      v.distance = 0.0;
      v.pathsIn = 0;
      v.pathsInOK = 0;
      v.discovered = false;
      v.visited = false;
    }
    s.discovered = true;
    s.pathsIn = 1;
    s.pathsInOK = s.inGroup ? 1 : 0;
    q.add(s);
    
    /* Run BFS */
    while (!q.isEmpty()) {
      Node u = q.poll();
      u.visited = true;
      
      for (Node v : u.neighbors) {
        if (v.visited)
          continue;
        
        if (!v.discovered) {
          v.distance = u.distance+1;
          v.pathsIn = u.pathsIn;
          if (v.inGroup)
            v.pathsInOK = u.pathsIn;
          else
            v.pathsInOK = u.pathsInOK;
          v.discovered = true;
          q.add(v);
        }
        else { // another equal-length path
          v.pathsIn += u.pathsIn;
          if (v.inGroup)
            v.pathsInOK += u.pathsIn;
          else
            v.pathsInOK += u.pathsInOK;
        }
      }
    }
    
    for (Node t : g.T) {
      sumPaths += t.pathsInOK;
    }
    return sumPaths;
  }
  
  public static int centralityNodeWUG(Graph g, String name) {
    String[] s = {name};
    return centralityGroupWUG(g, s);
  }
  
  public static int centralityGroupWUG(Graph g, String[] group) {
    int centrality = 0;
    
    /* Initialize group */
    for (Node n : g.V)
      n.inGroup = false;
    for (String str : group) {
      Node n = g.findNode(str);
      n.inGroup = true;
    }
    for (Node s : g.S) {
      centrality += centralityDijkstra(g, s);
    }
    return centrality;
  }
  
  /* Calculate a given node's centrality in a weighted, undirected graph */
  public static int centralityDijkstra(Graph g, Node s) {
    PriorityQueue<Node> q = new PriorityQueue<Node>();
    int sumPaths = 0;
    
    /* Initialize bookkeeping and Q */
    for (Node v : g.V) {
      v.distance = Double.POSITIVE_INFINITY;
      v.pathsIn = 0;
      v.pathsInOK = 0;
      v.discovered = false;
      v.visited = false;
      q.add(v);
    }
    q.remove(s);
    s.distance = 0.0;
    s.discovered = true;
    s.pathsIn = 1;
    s.pathsInOK = s.inGroup ? 1 : 0;
    q.add(s);
    
    /* Run Dijkstra's */
    while (!q.isEmpty()) {
      Node u = q.poll();
      u.visited = true;
      
      for (Node v : u.neighbors) {
        if (v.visited)
          continue;
        
        Double alt = u.distance + g.getWeight(u.name, v.name);
        if (!v.discovered || alt < v.distance-.00001) {
          q.remove(v);
          v.distance = alt;
          v.pathsIn = u.pathsIn;
          if (v.inGroup)
            v.pathsInOK = u.pathsIn;
          else
            v.pathsInOK = u.pathsInOK;
          v.discovered = true;
          q.add(v);
        }
        else if (Math.abs(alt - v.distance) < .00001) { // another equal-length path
          q.remove(v);
          v.pathsIn += u.pathsIn;
          if (v.inGroup)
            v.pathsInOK += u.pathsIn;
          else
            v.pathsInOK += u.pathsInOK;
          q.add(v);
        }
      }
    }
    
    for (Node t : g.T) {
      sumPaths += t.pathsInOK;
    }
    return sumPaths;
  }
  
  public static Graph maximizeCentralityNode(Graph g, String name, int k) {
    return new Graph(true, true, true, true);
  }
  
  public static void main(String args[]) throws IOException {
    Graph g = new Graph(false, true, false, false);
    //g.importTxt("TransMatrix.txt");
    g.importTxt("testNodeCent.txt");
    g.addNodeS("S");
    g.addNodeT("T2");
    String[] group = {"A", "B", "E2"};
    System.out.println(centralityGroupWUG(g, group));
    g.exportDot();
  }
  
}