import java.util.*;
import java.io.*;

public class Algorithms {
  
  /* Calculate a given node's centrality */
  public static int centralityNode(Graph g, String name) {
    String[] s = {name};
    return centralityGroup(g, s);
  }
  
  /* Calculate a group's centrality */
  public static int centralityGroup(Graph g, String[] group) {
    int centrality = 0;
    
    /* Initialize group */
    for (Node n : g.V)
      n.inGroup = false;
    for (String str : group) {
      Node n = g.findNode(str);
      n.inGroup = true;
    }
    for (Node s : g.S) {
      if (g.weighted)
        centrality += centralityDijkstra(g, s);
      else
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
    for (Edge e : g.E) {
      g.edgeScores.put(e.node1.name+":"+e.node2.name, 0.0);
      if (!g.directed)
        g.edgeScores.put(e.node2.name+":"+e.node1.name, 0.0);
    }
    
    /* Run BFS */
    while (!q.isEmpty()) {
      Node u = q.poll();
      u.visited = true;
      
      for (Node v : u.neighbors) {
        if (v.visited)
          continue;
        
        Double alt = u.distance+1;
        if (!v.discovered) {
          v.distance = alt;
          v.pathsIn = u.pathsIn;
          if (v.inGroup)
            v.pathsInOK = u.pathsIn;
          else
            v.pathsInOK = u.pathsInOK;
          v.discovered = true;
          v.parentsOK.clear();
          v.parentsOK.add(u);
          q.add(v);
        }
        else if (Math.abs(v.distance - alt) < .00001) { // another equal-length path
          v.pathsIn += u.pathsIn;
          if (v.inGroup)
            v.pathsInOK += u.pathsIn;
          else
            v.pathsInOK += u.pathsInOK;
          v.parentsOK.add(u);
        }
      }
    }
    
    /* Post-processing for edge removal heuristic */
    for (Node v : g.V) {
      int scoreUpdate = 0;
      for (Node n : g.directed ? v.parents : v.neighbors) {
        if (n.pathsInOK > v.pathsInOK)
          scoreUpdate += n.pathsInOK - v.pathsInOK;
      }
      if (scoreUpdate > 0) {
        for (Node n : v.parentsOK) {
          String edgeStr = n.name+":"+v.name;
          g.edgeScores.put(edgeStr, g.edgeScores.get(edgeStr) + scoreUpdate/v.parentsOK.size());
        }
      }
    }
    
    /* Sum good paths in all destinations */
    for (Node t : g.T) {
      sumPaths += t.pathsInOK;
    }
    return sumPaths;
  }
  
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
  
  public static Graph maximizeCentralityGroup(Graph g, String name, int k) {
    return new Graph(true, true, true, true);
  }
  
  public static void main(String args[]) throws IOException {
    Graph g = new Graph(true, false, false, false);
    //g.importTxt("TransMatrix.txt");
    g.importTxt("testNodeCent2.txt");
    g.addNodeS("S");
    g.addNodeT("T2");
    String[] group = {"A", "B", "E2"};
    System.out.println(centralityGroup(g, group));
    g.exportDot();
  }
  
}