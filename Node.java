import java.util.*;

public class Node implements Comparable<Node> {
  boolean inS = true, inT = true;
  boolean inGroup = false;
  String name;
  ArrayList<Node> neighbors = new ArrayList<Node>(); // in directed graphs, the children of a node
  ArrayList<Node> parents = new ArrayList<Node>(); // directed graphs only
  
  /* Information used in graph algorithms */
  Double distance;
  int pathsIn, pathsInOK;
  boolean visited;
  boolean discovered;
  
  public Node(String n) {
    name = n;
  }
  
  public Node(String n, boolean s, boolean t) {
    name = n;
    inS = s;
    inT = t;
  }
  
  public int compareTo(Node n) {
    if (Math.abs(distance - n.distance) < .00001)
      return 0;
    else if (distance > n.distance)
      return 1;
    else
      return -1;
  }
}
