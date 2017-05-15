import java.io.*;

public class Main {

    public static void main(String args[]) throws IOException {
        double cent1 = 0.0, cent2 = 0.0;
        //Graph g = new Graph(false, false, false, false);
        Graph g = new Graph(false, false, true, true);

        //g.importGML("data/celegansneural.gml");
        //g.importGML("data/karate.gml");
        g.importTxt("data/TransMatrix.txt");
        //g.importTxt("data/testNodeCent3.txt");
        //g.addNodeS("S");
        //g.addNodeT("T2");
        //g.addNodeT("F2");
        String[] group = Algorithms.maxGroup(g, 5);
        //String[] group = {"A", "B", "E2"};
        //String[] group = {"C"};
        //g.print();
        cent1 = Algorithms.centralityGroup(g, group, false);
        //g.exportDot();

        for (int k = 0; k < 20; k ++) {
            Algorithms.maximizeCentralityGroup(g, group, k, "all", -1, .05);
            cent2 = Algorithms.centralityGroup(g, group, false);
            //System.out.print(Integer.toString(k+1) + "\t");
            //System.out.println(100*(cent2-cent1)/cent1); // percent centrality increase
        }
        //System.out.println(cent1);
        g.exportDot();
    }
}
