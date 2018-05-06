package Fibonacci;

/* represents every heap in Fibonacci.FibonacciHeap */
public class HeapEntry{
    /* init elements and co-relationship */
    public boolean marked;
    public int degree;

    /* the part link in the list
     * double-link heap to heap
     */
    public HeapEntry next;
    public HeapEntry pre;

    /* the part build the heap
     * heap's intern
     */
    public HeapEntry parent;
    public HeapEntry child;

    public int id;
    public int dst;

    HeapEntry(int id, int dst){
        marked = false;
        degree = 0;

        /* add the value (id, dst), dst as the priority */
        this.id = id;
        this.dst = dst;

        pre = next = this;
    }

}
