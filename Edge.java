public class Edge implements Comparable<Edge> {
  Node node1, node2;
  boolean isGhost = false; // whether the edge is a ghost edge
  boolean directed;
  int weight;
  int impact;
  String id;
  
  public Edge(Node n1, Node n2, boolean ghost, boolean dir, int w) {
    node1 = n1;
    node2 = n2;
    directed = dir;
    connect();
    
    isGhost = ghost;
    weight = w;
  }
  /* Connects the two nodes */
  public void connect() {
    node1.neighbors.add(node2);
    if (!directed)
      node2.neighbors.add(node1);
    else
      node2.parents.add(node1);
  }
  
  /* Disconnects the two nodes */
  public void disconnect() {
    node1.neighbors.remove(node2);
    if (!directed)
      node2.neighbors.remove(node1);
    else
      node2.parents.remove(node1);
  }
  
  public int compareTo(Edge e) {
    if (impact == e.impact)
      return 0;
    else if (impact < e.impact)
      return 1;
    else
      return -1;
  }
}