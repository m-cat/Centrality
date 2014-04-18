import java.util.*;
import java.io.*;

public class Algorithms {
  
  public static void setGroup(Graph g, String[] group) {
    for (Node n : g.V)
      n.inGroup = false;
    for (String str : group) {
      Node n = g.findNode(str);
      n.inGroup = true;
    }
  }
  
  /* Calculate a given node's centrality */
  public static int centralityNode(Graph g, String name, boolean post) {
    String[] s = {name};
    return centralityGroup(g, s, post);
  }
  
  /* Calculate a group's centrality */
  public static int centralityGroup(Graph g, String[] group, boolean post) {
    int centrality = 0;
    
    /* Initialize group */
    setGroup(g, group);
    
    /* Initialize edge scores for edge removal heuristic */
    if (post) {
      for (Edge e : g.E) {
        g.edgeScores.put(e.node1.name+":"+e.node2.name, 0.0);
        if (!g.directed)
          g.edgeScores.put(e.node2.name+":"+e.node1.name, 0.0);
      }
    }
    
    for (Node s : g.S) {
      if (g.weighted)
        centrality += centralityDijkstra(g, s);
      else
        centrality += centralityBFS(g, s, post);
    }
    return centrality;
  }
  
  public static int centralityBFS(Graph g, Node s, boolean post) {
    LinkedList<Node> q = new LinkedList<Node>();
    int sumPaths = 0;
    
    /* Initialize bookkeeping */
    for (Node v : g.V) {
      v.distance = 0.0;
      v.pathsIn = 0;
      v.pathsInOK = 0;
      v.discovered = false;
      v.visited = false;
      v.destAmount = 0;
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
    if (post) {
      /* Trace back to a source, update destination amounts along the way */
      for (Node t : g.T) {
        LinkedHashSet<Node> temp = new LinkedHashSet<Node>();
        temp.add(t);
        traceBack(temp, 0);
      }
      
      /* Compute scores */
      for (Node v : g.V) {
        int scoreUpdate = 0;
        for (Node n : g.directed ? v.parents : v.neighbors) {
          if (n.pathsInOK > v.pathsInOK)
            scoreUpdate += n.pathsInOK - v.pathsInOK;
        }
        if (scoreUpdate > 0) {
          for (Node p : v.parentsOK) {
            String edgeStr = p.name+":"+v.name;
            g.edgeScores.put(edgeStr, g.edgeScores.get(edgeStr) + scoreUpdate/v.parentsOK.size()*p.destAmount);
          }
        }
      }
    }
    
    /* Sum good paths in all destinations */
    for (Node t : g.T) {
      sumPaths += t.pathsInOK;
    }
    return sumPaths;
  }

  /* Helper function for post-processing step in edge removal heuristic */
  /* THIS DOESN'T WORK RIGHT NOW */
  public static void traceBack(LinkedHashSet<Node> nodes, int dests) {
    if (nodes.isEmpty())
      return;
    
    LinkedHashSet<Node> next = new LinkedHashSet<Node>();
    int nextDests = dests;
    for (Node n : nodes) {
      n.destAmount += dests;
      nextDests += n.inT ? 1 : 0;
      for (Node p : n.parentsOK)
        next.add(p);
    }
    traceBack(next, nextDests);
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
        else if (Math.abs(alt - v.distance) < .00001) { // another equal-length incoming path
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
  
  public static void maximizeCentralityNode(Graph g, String name, int k, String heuristic) {
    String[] s = {name};
    maximizeCentralityGroup(g, s, k, heuristic);
  }
  
  public static void maximizeCentralityGroup(Graph g, String[] group, int k, String heuristic) {
    setGroup(g, group);
    
    if (!g.weighted) {
      if (heuristic.equals("greedy")) {
        maximizeCentralityGroupGreedy(g, group, k);
        g.name = "greedy k="+Integer.toString(k);
      }
      else if (heuristic.equals("batch")) {
        maximizeCentralityGroupBatch(g, group, k);
        g.name = "batch k="+Integer.toString(k);
      }
      else if (heuristic.equals("adjacent")) {
        maximizeCentralityGroupAdjacent(g, group, k);
        g.name = "greedy-1 k="+Integer.toString(k);
      }
    }
    else { // run weighted versions of the algorithms
      if (heuristic.equals("greedy")) {
        maximizeCentralityGroupGreedyWeighted(g, group, k);
        g.name = "greedy k="+Integer.toString(k);
      }
    }
  }
  
  public static void maximizeCentralityGroupGreedy(Graph g, String[] group, int k) {
    for (int i = 0; i < k; i ++) {
      Edge curBestEdge = null;
      
      for (Node n1 : g.V) {
        for (Node n2 : g.V) {
          if (n1.name.equals(n2.name) || g.edgeExists(n1.name, n2.name))
            continue;
          Edge e = g.addEdge(n1.name, n2.name, true, -1);
          e.impact = centralityGroup(g, group, false);
          if (curBestEdge == null || e.impact > curBestEdge.impact)
            curBestEdge = e;
          g.removeEdge(e);
        }
      }
      
      g.addEdge(curBestEdge);
    }
  }
  
  public static void maximizeCentralityGroupBatch(Graph g, String[] group, int k) {
    PriorityQueue<Edge> ghostEdges = new PriorityQueue<Edge>();
    
    for (Node n1 : g.V) {
      for (Node n2 : g.V) {
        if (n1.name.equals(n2.name) || g.edgeExists(n1.name, n2.name))
          continue;
        Edge e = g.addEdge(n1.name, n2.name, true, -1);
        e.impact = centralityGroup(g, group, false);
        ghostEdges.add(e);
        g.removeEdge(e);
      }
    }
    
    for (int i = 0; i < k; i ++) {
      g.addEdge(ghostEdges.poll());
    }
  }
  
  public static void maximizeCentralityGroupAdjacent(Graph g, String[] group, int k) {
    for (int i = 0; i < k; i ++) {
      Edge curBestEdge = null;
      
      for (Node n1 : g.V) {
        if (!n1.inGroup)
          continue;
        for (Node n2 : g.V) {
          if (n1.name.equals(n2.name) || g.edgeExists(n1.name, n2.name))
            continue;
          Edge e = g.addEdge(n1.name, n2.name, true, -1);
          e.impact = centralityGroup(g, group, false);
          if (curBestEdge == null || e.impact > curBestEdge.impact)
            curBestEdge = e;
          g.removeEdge(e);
        }
      }
      
      g.addEdge(curBestEdge);
    }
  }
  
  /* TODO: figure out how to assign weights of new edges */
  public static void maximizeCentralityGroupGreedyWeighted(Graph g, String[] group, int k) {
    Edge curBestEdge = null;
    double cost, curCost = 0;
    
    do {
      curBestEdge = null;
      for (Node n1 : g.V) {
        for (Node n2 : g.V) {
          if (n1.name.equals(n2.name) || g.edgeExists(n1.name, n2.name))
            continue;
          cost = Math.abs(n2.distance - n1.distance);
          if (cost > k)
            continue;
          Edge e = g.addEdge(n1.name, n2.name, true, cost);
          e.impact = centralityGroup(g, group, false);
          if ((curBestEdge == null || e.impact > curBestEdge.impact) && cost > 0) {
            curBestEdge = e;
            curCost = cost;
          }
          g.removeEdge(e);
        }
      }
      
      if (curBestEdge != null) {
        g.addEdge(curBestEdge);
        k -= curCost;
      }
    } while (curBestEdge != null);
  }
  
  public static void main(String args[]) throws IOException {
    double cent1, cent2;
    Graph g = new Graph(true, false, false, false);
    //g.importTxt("TransMatrix.txt");
    g.importTxt("testNodeCent.txt");
    g.addNodeS("S");
    g.addNodeT("T2");
    //g.addNodeT("F2");
    //String[] group = {"A"};
    String[] group = {"A", "B", "E2"};
    //g.print();
    cent1 = centralityGroup(g, group, false);
    //maximizeCentralityGroup(g, group, 9, "greedy");
    cent2 = centralityGroup(g, group, true);
    //System.out.println(100*(cent2-cent1)/cent1); // percent centrality increase
    System.out.println(cent2);
    g.exportDot();
  }
  
}