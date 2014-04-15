public class Edge {
  Node node1, node2;
  boolean isGhost = false; // whether the edge is a ghost edge
  boolean directed;
  int weight;
  
  public Edge(Node n1, Node n2, boolean ghost, boolean dir, int w) {
    n1.neighbors.add(n2);
    if (!dir)
      n2.neighbors.add(n1);
    else
      n2.parents.add(n1);
    node1 = n1;
    node2 = n2;
    isGhost = ghost;
    directed = dir;
    weight = w;
  }
}