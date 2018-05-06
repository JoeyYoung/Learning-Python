import java.io.IOException;

public class Main {
    /* run function */
    public static void main(String[] args){
        Graph graph = new Graph("C:/Users/xiaoy/Desktop/road/down/NW");

        try{
            graph.initGraph();
        }catch (IOException e){
            System.out.println("Init Graph failed");
        }

        Dirkstra dj = new Dirkstra();
        long startT;
        long endT;

//        startT = System.currentTimeMillis();
//        dj.shortestMinHeap(graph, 1);
//        endT = System.currentTimeMillis();
//        System.out.println("success with MinHeap in "+ (endT-startT) + " ms");

        startT = System.currentTimeMillis();
        dj.shortestFibHeap(graph, 1);
        endT = System.currentTimeMillis();
        System.out.println("success with FibonacciHeap in "+ (endT-startT) + " ms");

//        startT = System.currentTimeMillis();
//        dj.shortestNaive(graph, 1);
//        endT = System.currentTimeMillis();
//        System.out.println("success with Naive Way in "+ (endT-startT) + " ms");


//        startT = System.currentTimeMillis();
//        dj.shortestSkewHeap(graph, 1);
//        endT = System.currentTimeMillis();
//        System.out.println("success with SkewHeap in "+ (endT-startT) + " ms");
    }
}
