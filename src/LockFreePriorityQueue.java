import javafx.util.Pair;

import java.util.Random;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * A lock free priority queue
 */
public class LockFreePriorityQueue {

    static final int MAX_LEVEL = 10;    // Maximum height of the skiplist
    final AtomicMarkableReference<Node> head = new AtomicMarkableReference<>(new Node(Integer.MIN_VALUE), false);
    final AtomicMarkableReference<Node> tail = new AtomicMarkableReference<>(new Node(Integer.MAX_VALUE), false);

    Random r = new Random();

    public LockFreePriorityQueue(){
        for(int i = 0; i < head.getReference().next.length; i++){
            head.getReference().next[i] = new AtomicMarkableReference<Node>(tail.getReference(), false);
        }
    }

    private AtomicMarkableReference<Node> readNode(AtomicMarkableReference<Node> node){
        if(node.isMarked()) return null;
        else return node;
    }

    private Wrapper removeNode(AtomicMarkableReference<Node> node, AtomicMarkableReference<Node> prev, int level){
        AtomicMarkableReference<Node> last;
        Wrapper w;
        while (true){
            if(node.getReference().next[level].getReference() == null) break;
            w = scanKey(prev, level, node.getReference().key);
        }
    }

    private AtomicMarkableReference<Node> helpDelete(AtomicMarkableReference<Node> node, int level){
        AtomicMarkableReference<Node> prev, last, node2;
        for(int i = level; i >= 1; i--){
            do{
                node2 = node.getReference().next[i];
            }while(!node2.isMarked() && (!node.compareAndSet(node2.getReference(), node2.getReference(), false, true)));
        }
        prev = node.getReference().prev;
        if(prev.getReference() == null || level >= prev.getReference().validLevel){
            prev = head;
        }
        Wrapper w = removeNode(node, prev, level);
        return w.prev;
    }

    private Wrapper readNext(AtomicMarkableReference<Node> node1, int level){
        AtomicMarkableReference<Node> node2;
        if(node1.getReference().value.isMarked()) node1 = helpDelete(node1, level);
        node2 = readNode(node1.getReference().next[level]);

        while(node2 == null){
            node1 = helpDelete(node1, level);
            node2 = readNode(node1.getReference().next[level]);
        }

        return new Wrapper(node2, node1);
    }

    private Wrapper scanKey(AtomicMarkableReference<Node> node1, int level, int key){
        AtomicMarkableReference<Node> node2;

        Wrapper w = readNext(node1, level);
        node2 = w.node;

        // Loop while key is less
        while(node2.getReference().key < key){
            node1 = node2;
            w = readNext(node1, level);
            node2 = w.node;
        }

        return w;
    }

    // Enqueue a value/priority pair into the queue
    public void insert(Integer value, int key){
        AtomicMarkableReference<Node> node1, node2, newNode;
        AtomicMarkableReference<Node>[] savedNodes = new AtomicMarkableReference[MAX_LEVEL];

        int level = r.nextInt(MAX_LEVEL);
        newNode = new AtomicMarkableReference<>(new Node(level, key, value), false);

        // Loop through the levels
        for(int i = MAX_LEVEL; i >= 1; i++){
            node2 = scanKey(node1, i, key);
        }
    }

    // Pop off the top priority in the queue
    public Integer remove(){

        return 0;
    }

    // Node class for nodes in the skiplist
    private static class Node{
        int key, level, validLevel;
        AtomicMarkableReference<Integer> value; // Mark for the current node
        AtomicMarkableReference<Node> prev;
        AtomicMarkableReference<Node> next[];

        // Constructor for normal Nodes
        public Node(int level, int key, int value){
            prev = null;
            validLevel = 0;
            this.level = level;
            this.key = key;
            this.value = new AtomicMarkableReference<>(value, false);
        }

        // Constructor for a Node
        public Node(){

        }
    }

    // Wrapper class used so ReadNext and ScanKey can return the node as well as previous
    private static class Wrapper{
        AtomicMarkableReference<Node> node, prev;

        public Wrapper(AtomicMarkableReference<Node> node, AtomicMarkableReference<Node> prev){
            this.node = node;
            this.prev = prev;
        }
    }

}
