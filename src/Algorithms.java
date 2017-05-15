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
  
    /* Tries to find a group of size k with maximal centrality.
     * Uses greedy selection method. */
    public static String[] maxGroup(Graph g, int k) {
        ArrayList<String> group = new ArrayList<String>();

        for (int i = 0; i < k; i ++) {
            Node cur = null;
            long curCent = 0;
      
            for (Node v : g.V) {
                if (group.contains(v.name))
                    continue;
        
                group.add(v.name);
                long cent = centralityGroup(g, group.toArray(new String[group.size()]), false);
                if (cur == null || cent > curCent) {
                    cur = v;
                    curCent = cent;
                }
                group.remove(v.name);
            }
      
            if (cur == null)
                break;
            group.add(cur.name);
        }
        return group.toArray(new String[group.size()]);
    }
  
    public static String[] minGroup(Graph g, int k) {
        ArrayList<String> group = new ArrayList<String>();
        for (int i = 0; i < k; i ++) {
            Node cur = null;
            long curCent = 0;
      
            for (Node v : g.V) {
                if (group.contains(v.name))
                    continue;
        
                group.add(v.name);
                long cent = centralityGroup(g, group.toArray(new String[group.size()]), false);
                if (cur == null || cent < curCent) {
                    cur = v;
                    curCent = cent;
                }
                group.remove(v.name);
            }
      
            if (cur == null)
                break;
            group.add(cur.name);
        }
        return group.toArray(new String[group.size()]);
    }
  
    /* Calculate a given node's centrality */
    public static long centralityNode(Graph g, String name, boolean post) {
        String[] s = {name};
        return centralityGroup(g, s, post);
    }
  
    /* Calculate a group's centrality */
    public static long centralityGroup(Graph g, String[] group, boolean post) {
        long centrality = 0;
    
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
  
    public static double centralityGroupSample(Graph g, String[] group, boolean post, double percent) {
        ArrayList<Node> seenNodes = new ArrayList<Node>();
        long centrality = 0;
    
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
            seenNodes.add(s);
            if (g.weighted)
                centrality += centralityDijkstra(g, s);
            else
                centrality += centralityBFS(g, s, post);
            if ((double)seenNodes.size() / (double)g.S.size() > percent)
                break;
        }
        return centrality * (1/percent);
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
            v.parentsOK.clear();
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
            for (Node v : g.V)
                v.destAmount = 0;
            /* Trace back to a source, update destination amounts along the way */
            for (Node t : g.T) {
                LinkedHashSet<Node> temp = new LinkedHashSet<Node>();
                temp.add(t);
                traceBack(temp);
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
    public static void traceBack(LinkedHashSet<Node> nodes) {
        if (nodes.isEmpty())
            return;
    
        LinkedHashSet<Node> next = new LinkedHashSet<Node>();
        for (Node n : nodes) {
            n.destAmount += 1;
            for (Node p : n.parentsOK)
                next.add(p);
        }
        traceBack(next);
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
  
    public static void maximizeCentralityNode(Graph g, String name, int k, String heuristic, int removals) {
        String[] s = {name};
        maximizeCentralityGroup(g, s, k, heuristic, removals, 0.0);
    }
  
    public static void maximizeCentralityGroup(Graph g, String[] group, int k, String heuristic, int removals, double percent) {
        setGroup(g, group);
    
        if (!g.weighted) {
            if (heuristic.equals("greedy")) {
                maximizeCentralityGroupGreedy(g, group, k, removals);
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
            else if (heuristic.equals("sample")) {
                maximizeCentralityGroupSample(g, group, k, percent);
                g.name = "sample k="+Integer.toString(k);
            }
            else if (heuristic.equals("all")) {
                maximizeCentralityGroupAll(g, group, k, percent);
                g.name = "all heuristics k="+Integer.toString(k);
            }
        }
        else { // run weighted versions of the algorithms
            if (heuristic.equals("greedy")) {
                maximizeCentralityGroupGreedyWeighted(g, group, k);
                g.name = "greedy k="+Integer.toString(k);
            }
        }
    }
  
    public static void maximizeCentralityGroupGreedy(Graph g, String[] group, int k, int removals) {
        for (int i = 0; i < k; i ++) {
            Edge curBestEdge = null;
            Edge curBestEdge2 = null;
      
            if (removals != 0) {
                for (Node n1 : g.V) {
                    for (Node n2 : g.V) {
                        if (n1.name.equals(n2.name) || g.edgeExists(n1.name, n2.name)) {
                            continue;
                        }
                        Edge e = g.addEdge(n1.name, n2.name, true, -1);
                        e.impact = centralityGroup(g, group, false);
                        if (curBestEdge == null || e.impact > curBestEdge.impact)
                            curBestEdge = e;
                        g.removeEdge(e);
                    }
                }
            }
      
            if (removals != -1) {
                double curBestScore = 0.0;
                for (Edge e : g.E) {
                    g.edgeScores.put(e.node1.name+":"+e.node2.name, 0.0);
                    g.edgeScores.put(e.node2.name+":"+e.node1.name, 0.0);
                }
                centralityGroup(g, group, true);
        
                for (Edge e : g.E) {
                    double score = g.edgeScores.get(e.node1.name+":"+e.node2.name);
          
                    if (curBestEdge2 == null || score > curBestScore) {
                        curBestEdge2 = e;
                    }
                }
                /*
                  for (Node n1 : g.V) {
                  for (Node n2 : g.V) {
                  if (n1.name.equals(n2.name) || !g.edgeExists(n1.name, n2.name)) {
                  continue;
                  }
                  Edge e = g.removeEdge(n1.name, n2.name);
                  e.impact = centralityGroup(g, group, false);
                  if (curBestEdge2 == null || e.impact > curBestEdge2.impact)
                  curBestEdge2 = e;
                  g.addEdge(e);
                  }
                  }*/
            }
      
            if (removals == -1)
                g.addEdge(curBestEdge);
            else if (removals == 0)
                g.removeEdge(curBestEdge2);
            else {
                g.removeEdge(curBestEdge2);
                long impact = centralityGroup(g, group, false);
                if (impact < curBestEdge.impact) {
                    g.addEdge(curBestEdge2);
                    g.addEdge(curBestEdge);
                }/*
                   if (curBestEdge.impact > curBestEdge2.impact)
                   g.addEdge(curBestEdge);
                   else
                   g.removeEdge(curBestEdge2);*/
            }
        }
    }
  
    public static void maximizeCentralityGroupBatch(Graph g, String[] group, int k) {
        PriorityQueue<Edge> ghostEdges = new PriorityQueue<Edge>();
        ArrayList<String> ghostEdgeIDs = new ArrayList<String>();
        ArrayList<Edge> addEdges = new ArrayList<Edge>();
        double cent1 = centralityGroup(g, group, false), cent2;
    
        for (Node n1 : g.V) {
            for (Node n2 : g.V) {
                if (n1.name.equals(n2.name) || g.edgeExists(n1.name, n2.name))
                    continue;
                if (ghostEdgeIDs.contains(n1.name+":"+n2.name))
                    continue;
                Edge e = g.addEdge(n1.name, n2.name, true, -1);
                e.impact = centralityGroup(g, group, false);
                ghostEdges.add(e);
                ghostEdgeIDs.add(n1.name+":"+n2.name);
                ghostEdgeIDs.add(n2.name+":"+n1.name);
                g.removeEdge(e);
            }
        }
    
        for (int i = 0; i < k; i ++) {
            Edge e = ghostEdges.poll();
            g.addEdge(e);
            addEdges.add(e);
        }
    
        cent2 = centralityGroup(g, group, false);
        System.out.println(100*(cent2-cent1)/cent1); // percent centrality increase
    
        for (Edge e : addEdges)
            g.removeEdge(e);
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
  
    public static void maximizeCentralityGroupSample(Graph g, String[] group, int k, double percent) {
        for (int i = 0; i < k; i ++) {
            Edge curBestEdge = null;
      
            for (Node n1 : g.V) {
                for (Node n2 : g.V) {
                    if (n1.name.equals(n2.name) || g.edgeExists(n1.name, n2.name)) {
                        continue;
                    }
                    Edge e = g.addEdge(n1.name, n2.name, true, -1);
                    e.impact = (long)centralityGroupSample(g, group, false, percent);
                    if (curBestEdge == null || e.impact > curBestEdge.impact)
                        curBestEdge = e;
                    g.removeEdge(e);
                }
            }
      
            g.addEdge(curBestEdge);
        }
    }
  
    public static void maximizeCentralityGroupAll(Graph g, String[] group, int k, double percent) {
        PriorityQueue<Edge> ghostEdges = new PriorityQueue<Edge>();
        ArrayList<String> ghostEdgeIDs = new ArrayList<String>();
        ArrayList<Edge> addEdges = new ArrayList<Edge>();
        double cent1 = centralityGroup(g, group, false), cent2;
    
        for (Node n1 : g.V) {
            for (Node n2 : g.V) {
                if (!n1.inGroup)
                    continue;
                if (n1.name.equals(n2.name) || g.edgeExists(n1.name, n2.name))
                    continue;
                if (ghostEdgeIDs.contains(n1.name+":"+n2.name))
                    continue;
                Edge e = g.addEdge(n1.name, n2.name, true, -1);
                e.impact = (long)centralityGroupSample(g, group, false, percent);
                ghostEdges.add(e);
                ghostEdgeIDs.add(n1.name+":"+n2.name);
                ghostEdgeIDs.add(n2.name+":"+n1.name);
                g.removeEdge(e);
            }
        }
    
        for (int i = 0; i < k; i ++) {
            Edge e = ghostEdges.poll();
            g.addEdge(e);
            addEdges.add(e);
        }
    
        cent2 = centralityGroup(g, group, false);
        System.out.println(100*(cent2-cent1)/cent1); // percent centrality increase
    
        for (Edge e : addEdges)
            g.removeEdge(e);
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
}
