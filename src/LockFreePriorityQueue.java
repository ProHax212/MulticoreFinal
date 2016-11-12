import javafx.util.Pair;

import java.util.Random;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * A lock free priority queue
 */
public class LockFreePriorityQueue {

    static final int MAX_LEVEL = 25;    // Maximum height of the skiplist
    final AtomicMarkableReference<Node> head = new AtomicMarkableReference<>(new Node(MAX_LEVEL+1, Integer.MIN_VALUE, Integer.MIN_VALUE), false);
    final AtomicMarkableReference<Node> tail = new AtomicMarkableReference<>(new Node(MAX_LEVEL+1, Integer.MAX_VALUE, Integer.MAX_VALUE), false);

    Random r = new Random();

    public LockFreePriorityQueue(){
        for(int i = 0; i < head.getReference().next.length; i++){
            head.getReference().next[i] = new AtomicMarkableReference<Node>(tail.getReference(), false);
        }
    }

    private int randomLevel(){
        int startingLevel = 0;
        while (startingLevel < MAX_LEVEL){
            if(r.nextBoolean()) startingLevel += 1;
            else break;
        }

        return startingLevel;
    }

    private AtomicMarkableReference<Node> readNode(AtomicMarkableReference<Node> node){
        if(node.isMarked()) return null;
        else return node;
    }

    private void removeNode(AtomicMarkableReference<Node> node, AtomicMarkableReference<Node> prev, int level){
        AtomicMarkableReference<Node> last;
        Node nodeRef = node.getReference();
        while (true){
            if(node.getReference().next[level] == null && node.getReference().next[level].isMarked()) break;
            Wrapper w = scanKey(prev, level, node.getReference().key);
            last = w.node;
            prev = w.prev;

            if((last.getReference() != node.getReference()) || (node.getReference().next[level] == null && node.getReference().next[level].isMarked())) break;
            if(prev.getReference().next[level].compareAndSet(node.getReference(), node.getReference().next[level].getReference(), false, false)){
                nodeRef.next[level] = new AtomicMarkableReference<>(null, true);
                break;
            }
            if(node.getReference().next[level].getReference() == null && node.getReference().next[level].isMarked()) break;

            Thread.yield(); // Back off
        }
    }

    private AtomicMarkableReference<Node> helpDelete(AtomicMarkableReference<Node> node, int level){
        AtomicMarkableReference<Node> prev, last, node2;
        for(int i = level; i >= node.getReference().level - 1; i--){
            do{
                node2 = node.getReference().next[i];
            }while(!node2.isMarked() && (!node.compareAndSet(node2.getReference(), node2.getReference(), false, true)));
        }
        prev = node.getReference().prev;
        if(prev.getReference() == null || level >= prev.getReference().validLevel){
            prev = head;
        }
        removeNode(node, prev, level);
        return prev;
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
        node1 = w.prev;
        node2 = w.node;

        // Loop while key is less
        while(node2.getReference().key < key){
            node1 = node2;
            w = readNext(node1, level);
            node1 = w.prev;
            node2 = w.node;
        }

        return w;
    }

    // Enqueue a value/priority pair into the queue
    public boolean insert(Integer value, int key){
        AtomicMarkableReference<Node> node1, node2, newNode;
        AtomicMarkableReference<Node>[] savedNodes = new AtomicMarkableReference[MAX_LEVEL];
        node1 = head;
        int level = randomLevel();
        if(level == 0) level = 1;
        newNode = new AtomicMarkableReference<>(new Node(level, key, value), false);

        // Loop through the levels
        for(int i = MAX_LEVEL; i >= 1; i--){
            Wrapper w = scanKey(node1, i, key);
            node2 = w.node;
            node1 = w.prev;
            if(i < level) savedNodes[i] = node1;
        }

        while(true){
            Wrapper w = scanKey(node1, 0, key);
            node1 = w.prev;
            node2 = w.node;
            AtomicMarkableReference<Integer> value2 = node2.getReference().value;
            // Found the same key, update the value
            if(!value2.isMarked() && node2.getReference().key == key){
                if(node2.getReference().value.compareAndSet(value2.getReference(), value, false, false)){
                    return true;
                }
            }

            newNode.getReference().next[0] = new AtomicMarkableReference<Node>(node2.getReference(), false);
            if(node1.getReference().next[0].compareAndSet(node2.getReference(), newNode.getReference(), false, false)) break;
            Thread.yield(); // Back off
        }

        // Insert at higher levels
        for(int i = 1; i <= level-1; i++){
            newNode.getReference().validLevel = i;
            node1 = savedNodes[i];
            while(true){
                Wrapper w = scanKey(node1, i, key);
                node1 = w.prev;
                node2 = w.node;
                newNode.getReference().next[i] = new AtomicMarkableReference<>(node2.getReference(), false);
                if(newNode.isMarked() || node1.getReference().next[i].compareAndSet(node2.getReference(), newNode.getReference(), node2.isMarked(), node2.isMarked())) break;
                Thread.yield(); // Back off
            }
        }
        newNode.getReference().validLevel = level;
        if(newNode.getReference().value.isMarked()){
            helpDelete(newNode, 0);
        }

        return true;
    }

    // Pop off the top priority in the queue
    public Integer deleteMin(){
        AtomicMarkableReference<Node> prev = head;
        AtomicMarkableReference<Node> node1 = new AtomicMarkableReference<Node>(null, false);
        AtomicMarkableReference<Node> node2;
        AtomicMarkableReference<Integer> value;

        // Loop until you find the min
        boolean retry = false;  // Used to simulate the goto operation in the psuedocode
        while(true){

            if(!retry) {
                Wrapper w = readNext(prev, 0);
                prev = w.prev;
                node1 = w.node;

                // Node is the tail
                if (node1.getReference().value.getReference() == Integer.MAX_VALUE) return null;
            }

            retry = true;
            value = node1.getReference().value;
            if(node1.getReference() != prev.getReference().next[0].getReference()) continue;
            if(!value.isMarked()){
                if(node1.getReference().value.compareAndSet(value.getReference(), value.getReference(), false, true)){
                    node1.getReference().prev = prev;
                    break;
                }else continue;
            }else if(value.isMarked()) node1 = helpDelete(node1, 0);

            prev = node1;
            retry = false;
        }

        for(int i = 0; i < node1.getReference().level-1; i++){
            do{
                node2 = node1.getReference().next[i];
            }while(!node2.isMarked() && !node1.getReference().next[i].compareAndSet(node2.getReference(), node2.getReference(), false, true));
        }
        prev = head;
        for(int i = node1.getReference().level-1; i >= 0; i--){
            removeNode(node1, prev, i);
        }

        return value.getReference();
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
            this.next = new AtomicMarkableReference[level];
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

    public void print(){
        System.out.println();
        int level = MAX_LEVEL-1;
        AtomicMarkableReference<Node> node1 = head;

        while(level >= 0){
            AtomicMarkableReference<Node> node2 = node1.getReference().next[level];
            System.out.print(head.getReference().key + ", ");
            while(node2 != null){
                System.out.print(node2.getReference().key + ", ");
                node2 = node2.getReference().next[level];
            }
            level -= 1;
            System.out.println();
        }

        System.out.println();
    }

    // To string method for debugging
    public String toString(){
        String returnString = "\n";
        int level = MAX_LEVEL-1;
        AtomicMarkableReference<Node> node1 = head;

        while(level >= 0){
            AtomicMarkableReference<Node> node2 = node1.getReference().next[level];
            returnString += head.getReference().key + ", ";
            while(node2 != null){
                returnString += node2.getReference().key + ", ";
                node2 = node2.getReference().next[level];
            }
            level -= 1;
            returnString += "\n";
        }

        returnString += "\n";
        return returnString;
    }

    // Maker sure the ordering at each level is decreasing with with priority (higher keys)
    public boolean verify(){
        int level = MAX_LEVEL-1;

        while(level >= 0){
            AtomicMarkableReference<Node> node1 = head;
            AtomicMarkableReference<Node> node2 = node1.getReference().next[level];
            while(node2 != null){
                if (node2.getReference().key < node1.getReference().key) return false;
                node2 = node2.getReference().next[level];
            }
            level -= 1;
        }

        return true;
    }

}
