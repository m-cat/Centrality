public class Edge {
  Node node1, node2;
  boolean isGhost = false; // whether the edge is a ghost edge
  boolean directed;
  int weight;
  
  public Edge(Node n1, Node n2, boolean ghost, boolean dir, int w) {
    node1 = n1;
    node2 = n2;
    node1.neighbors.add(node2);
    if (!dir)
      node2.neighbors.add(node1);
    else
      node2.parents.add(node1);
    isGhost = ghost;
    directed = dir;
    weight = w;
  }
}