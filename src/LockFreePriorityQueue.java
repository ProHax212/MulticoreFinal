import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A lock free priority queue
 */
public class LockFreePriorityQueue {

    static final int MAX_LEVEL = 10;    // Maximum height of the skiplist

    public LockFreePriorityQueue(){

    }

    // Enqueue a value/priority pair into the queue
    public void insert(Integer value, int priority){

    }

    // Pop off the top priority in the queue
    public Integer remove(){

        return 0;
    }

    // Node class for nodes in the skiplist
    private static class Node{
        int key, level, validLevel;
        Integer value;
        AtomicMarkableReference<Node> prev;
        AtomicMarkableReference<Node> next[];

        // Constructor for a Node
        public Node(){

        }
    }

}
