import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class Graph {
    /* data input filename */
    private String Filename;

    /* represents a graph using adjacency list */
    private HashMap<Integer, HashMap<Integer, Integer>> adjList = new HashMap<>();

    /* init process
     * added form like: node1 node2 length
     */
    private void addEdge(Integer node1, Integer node2, Integer length){
        /* the edge already exist, will be updated */
        if(adjList.containsKey(node1)){

            /* add two symmetrical edges */
            adjList.get(node1).put(node2, length);

            /* the other */
            if(adjList.containsKey(node2))
                adjList.get(node2).put(node1, length);
            else{
                HashMap<Integer, Integer> newSet = new HashMap<>();
                newSet.put(node1, length);
                adjList.put(node2, newSet);
            }
        }
        else if(adjList.containsKey(node2)){

            /* add two symmetrical edges */
            adjList.get(node2).put(node1, length);

            /* the other */
            if(adjList.containsKey(node1))
                adjList.get(node1).put(node2, length);
            else{
                HashMap<Integer, Integer> newSet = new HashMap<>();
                newSet.put(node2, length);
                adjList.put(node1, newSet);
            }
        }
        else{
            /* init all */
            HashMap<Integer, Integer> m = new HashMap<>();
            HashMap<Integer, Integer> n = new HashMap<>();
            m.put(node2, length);
            n.put(node1, length);
            adjList.put(node1, m);
            adjList.put(node2, n);
        }
    }


    /* build the Graph from file */
    public void initGraph() throws IOException{
        File fin = new File(Filename);
        BufferedReader bufred = new BufferedReader(new FileReader(fin));
        String line;
        while((line = bufred.readLine()) != null){
            String[] tmp = line.split(" ");
            addEdge(Integer.valueOf(tmp[0]), Integer.valueOf(tmp[1]), Integer.valueOf(tmp[2]));
        }
        bufred.close();
    }

    /* search process
     * get the neighbors of a certain nodes
     * return NULL when node is isolated or not exist
     */
    public HashMap<Integer, Integer> getNeighbors(int node){
        /* isolate or not exist */
        if(!adjList.containsKey(node))
            return null;
        else
            return adjList.get(node);
    }

    /* get the nodes set*/
    public Set<Integer> getAllNodes(){
        return this.adjList.keySet();
    }

    /* get the length between start & end */
    public int getEdge(int start, int end){
        return adjList.get(start).get(end);
    }

    /* can be modify as read from file */
    Graph(String f){
        Filename = f;
    }
}
