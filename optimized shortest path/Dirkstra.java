import Fibonacci.FibonacciHeap;
import Fibonacci.HeapEntry;
import MinHeap.MinHeap;
import SkewHeap.SkewHeap;
import SkewHeap.SkewNode;

import java.util.HashMap;
import java.util.Set;

public class Dirkstra {
    /* INF init value */
    private int INF = 9999999;

    /* calculate with naive dijkstra */
    public void shortestNaive(Graph graph, int src){
        /* store the results of every node's shortest path from source */
        HashMap<Integer, Integer> shortestMap = new HashMap<>();

        /* store the visit information of nodes, init as false */
        HashMap<Integer, Boolean> visit = new HashMap<>();
        for(Integer n : graph.getAllNodes()){
            if(n == src)
                shortestMap.put(n, 0);
            else
                shortestMap.put(n, INF);
            visit.put(n, false);
        }

        /* loop until all the nodes has been visited */
        while(true){
            int min = INF;
            int minID = -1;

            /* find the shortest not visited node */
            for(Integer cur : graph.getAllNodes()){
                if((!visit.get(cur)) && (shortestMap.get(cur)<min)) {
                    min = shortestMap.get(cur);
                    minID = cur;
                }
            }

            /* entrance of the loop, all visited */
            if(minID == -1)
                break;

            /* visit the current node */
            visit.put(minID, true);

            /* release the neighbors' nodes if needed */
            Set<Integer> nrb = graph.getNeighbors(minID).keySet();
            for(int tmpN : nrb){
                if(shortestMap.get(minID) + graph.getEdge(tmpN, minID) < shortestMap.get(tmpN))
                    shortestMap.put(tmpN, shortestMap.get(minID) + graph.getEdge(tmpN, minID));
            }
        }

        /* print out */
//        System.out.println("Results from naive Dijkstra's:");
//        System.out.println(shortestMap);
    }

    /* calculate the shortest path using MinHeap
     * pars: graph based and the source node
     */
    public void shortestMinHeap(Graph graph, int src){
        /* minheap to get the min not visit node */
        MinHeap minHeap = new MinHeap();
         /* store the results of every node's shortest path from source */
        HashMap<Integer, Integer> shortestMap = new HashMap<>();

        /* insert into heap, init with src 0, others INF */
        for(Integer n : graph.getAllNodes()){
            if(n == src){
                minHeap.insertNode(n, 0);
                continue;
            }
            minHeap.insertNode(n, INF);
        }

        /* until all the nodes visit, heap size 0 */
        while(minHeap.getSize() > 0){
            /* get the min, add to the shortestPath results set */
            int curID = minHeap.getMin();
            int curDst = minHeap.getDst(curID);

            shortestMap.put(curID, curDst);

            /* get the cur's neighbors */
            Set<Integer> nrb = graph.getNeighbors(curID).keySet();
            /* for every node nearby */
            for(int tmpN : nrb){
                /* release when un-visit and can be updated */
                if(shortestMap.containsKey(tmpN) == false
                        && (curDst+graph.getEdge(curID, tmpN)) < minHeap.getDst(tmpN)){
                    /* update, heap will re-build internally */
                    minHeap.setDst(tmpN, curDst+graph.getEdge(curID, tmpN));
                }
            }
        }
        /* simple test for the accuracy of the program */
        //System.out.println(shortestMap.get(1000));

        /* print out */
//        System.out.println("Results from MinHeap Dijkstra's:");
//        System.out.println(shortestMap);
    }

    /* calculate the shortest path FibonacciHeap
     * pars: graph based and the source node
     */
    public void shortestFibHeap(Graph graph, int src){
        /* build the Fibonacci heap and entry for every nodes */
        FibonacciHeap fibonacciHeap = new FibonacciHeap();
        HashMap<Integer, HeapEntry> entries = new HashMap<>();

        /* results map */
        HashMap<Integer, Integer> shortestMap = new HashMap<>();
        /* build the map from node id to its entry in Fibonacci Heap */
        for(Integer id: graph.getAllNodes())
            entries.put(id, fibonacciHeap.insertNode(id, INF));

        /* set the source to 0 */
        fibonacciHeap.decreaseDst(entries.get(src), 0);

        while(fibonacciHeap.min != null){
            HeapEntry m = fibonacciHeap.popMin();
            /* store it to the results as it is visited */
            shortestMap.put(m.id, m.dst);
            /* neighbor sets of current min node */
            Set<Integer> nbr = graph.getNeighbors(m.id).keySet();
            for(int tmpN: nbr){
                /* already visit */
                if(shortestMap.containsKey(tmpN))
                    continue;
                /* the condition that distance can be updated */
                int ncost = m.dst + graph.getEdge(m.id, tmpN);
                /* decrease in O(1) */
                if(ncost < entries.get(tmpN).dst)
                    fibonacciHeap.decreaseDst(entries.get(tmpN), ncost);
            }
        }
        /* simple test for the accuracy of the program */
        //System.out.println(shortestMap.get(1000));

        /* print out */
//        System.out.println("Results from FibHeap Dijkstra's:");
//        System.out.println(shortestMap);
    }

    /* calculate the shortest path SkewHeap
     * pars: graph based and the source node
     */
    public void shortestSkewHeap(Graph graph, int src){
        SkewHeap skewHeap = new SkewHeap();
        HashMap<Integer, SkewNode> entries = new HashMap<>();

        /* results map */
        HashMap<Integer, Integer> shortestMap = new HashMap<>();
        /* build the map from node id to its entry in Fibonacci Heap */
        for(Integer id: graph.getAllNodes()){
            entries.put(id, skewHeap.insertNode(id, INF));
        }
        skewHeap.deceaseDst(entries.get(src), 0);
        /* insert the new node and rebuild the map */
        entries.put(src, skewHeap.insertNode(src, 0));

        /* until heap has been null, nodes visited all */
        while(skewHeap.root != null){
            SkewNode m = skewHeap.getMin();
            shortestMap.put(m.id, m.dst);
            Set<Integer> nbr = graph.getNeighbors(m.id).keySet();
            for(int tmpN: nbr){
                /* already visit */
                if(shortestMap.containsKey(tmpN))
                    continue;
                /* the condition that distance can be updated */
                int ncost = m.dst + graph.getEdge(m.id, tmpN);
                /* decrease in O(1) */
                if(ncost < entries.get(tmpN).dst){
                    skewHeap.deceaseDst(entries.get(tmpN), ncost);
                    /* rebuild the map */
                    entries.put(tmpN, skewHeap.insertNode(tmpN, ncost));
                }

            }
        }
        //System.out.println(shortestMap.get(1000));

        /* print out */
//        System.out.println("Results from SkewHeap Dijkstra's:");
//        System.out.println(shortestMap);
    }

    Dirkstra(){}
}
