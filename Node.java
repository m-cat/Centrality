import java.util.*;

public class Node implements Comparable<Node> {
  boolean inS = true, inT = true;
  String name;
  ArrayList<Node> neighbors = new ArrayList<Node>(); // in directed graphs, the children of a node
  ArrayList<Node> parents = new ArrayList<Node>(); // directed graphs only
  
  /* Information used in graph algorithms */
  double distance;
  int prefix;
  boolean visited;
  
  public Node(String n) {
    name = n;
  }
  
  public Node(String n, boolean s, boolean t) {
    name = n;
    inS = s;
    inT = t;
  }
  
  public int compareTo(Node n) {
    if (distance == n.distance)
      return 0;
    else if (distance > n.distance)
      return 1;
    else
      return -1;
  }
}
