import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A fine grained lock based priority queue
 */
public class FineGrainedPriorityQueue {

    private ArrayList<Node> heap;
    private ReentrantLock heapLock;

    public FineGrainedPriorityQueue(){
        heap = new ArrayList<>();

        // Add sentinel node so the root sits at index 1
        Node sentinel = new Node(42, 1, -2L);    // Sentinel is tagged as empty
        heap.add(sentinel);  // Sentinel node

        heapLock = new ReentrantLock();
    }

    // Insert a new node into the priority queue
    public void insert(Integer value, int priority){
        // Temporarily lock the heap while adding
        heapLock.lock();
        int index = heap.size();
        heap.add(new Node(value, priority, Thread.currentThread().getId()));
        heapLock.unlock();

        // Percolate up while priority is higher than parent
        while(index > 1){
            int parent=getParent(index); heap.get(parent).lock.lock(); heap.get(index).lock.lock();

            // Parent is available and the current node is tagged by me
            if(heap.get(parent).tag == -1L && heap.get(index).tag == Thread.currentThread().getId()){
                // Parent has lower priority - swap them
                if(heap.get(parent).priority > heap.get(index).priority){
                    swapNodes(heap.get(parent), heap.get(index));
                    heap.get(index).lock.unlock(); heap.get(parent).lock.unlock();  // Unlock the locks
                    index = parent;
                }
                // Done percolating up
                else{
                    heap.get(index).tag = -1L; // Available
                    heap.get(index).lock.unlock(); heap.get(parent).lock.unlock();  // Unlock the locks
                    index = 0;
                }
            }
            // Tag of the parent is EMPTY (the current node is now at the root)
            else if(heap.get(parent).tag == -2L){
                heap.get(index).tag = -1L;  // Available
                heap.get(index).lock.unlock(); heap.get(parent).lock.unlock();  // Unlock the locks
                index = 0;
            }
            // Tag of the current node is NOT my process ID -> have to chase it up the heap
            else if(heap.get(index).tag != Thread.currentThread().getId()){
                heap.get(index).lock.unlock(); heap.get(parent).lock.unlock();  // Unlock the locks
                index = parent;
            }
            // None of the cases held, unlock the locks and try again
            else{
                heap.get(index).lock.unlock(); heap.get(parent).lock.unlock();
            }
        }

        // First insert
        if(index == 1){
            heap.get(index).lock.lock();
            if(heap.get(index).tag == Thread.currentThread().getId()) heap.get(index).tag = -1L;  // Available
            heap.get(index).lock.unlock();
        }
    }

    // Remove a node from the priority queue
    public Integer remove(){
        return null;
    }

    // Helper methods to get indecies
    private int getParent(int index){return index/2;}
    private int getLeft(int index){return index*2;}
    private int getRight(int index){return (index*2) + 1;}

    // Swap the values of the two nodes
    private void swapNodes(Node one, Node two){
        Node temp = new Node(one.value, one.priority, one.tag);

        one.value = two.value;
        one.priority = two.priority;
        one.tag = two.tag;

        two.value = temp.value;
        two.priority = temp.priority;
        two.tag = temp.tag;
    }

    // Private node class
    private static class Node{

        Integer value;
        int priority;
        long tag;   // -2 (EMPTY), -1 (AVAILABLE), threadID (being inserted)
        ReentrantLock lock;

        public Node(Integer value, int priority, long threadId){
            this.value = value;
            this.priority = priority;
            this.tag = threadId;
            this.lock = new ReentrantLock();
        }

    }

    public String toString(){
        String returnString = "";
        for(Node node : heap){
            returnString += node.priority + ", ";
        }

        return returnString;
    }

}
