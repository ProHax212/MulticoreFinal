import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A fine grained lock based priority queue
 */
public class FineGrainedPriorityQueue {

    private ArrayList<Node> heap;
    private ReentrantLock heapLock;
    private AtomicInteger size;

    public FineGrainedPriorityQueue(){
        heap = new ArrayList<>();

        // Add sentinel node so the root sits at index 1
        Node sentinel = new Node(42, 1, -2L);    // Sentinel is tagged as empty
        heap.add(sentinel);  // Sentinel node
        size = new AtomicInteger(0);

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

    // Remove the highest priority node from the priority queue
    public Integer remove(){
        // Get the data off the root node then delete it
        heapLock.lock();
        int index = heap.size() - 1;

        // Last node is the root
        if(index == 1){
            Integer value = heap.get(index).value;
            heap.remove(index);
            heapLock.unlock();
            return value;
        }

        int returnValue = heap.get(1).value;    // Get the data off the root
        heap.get(1).lock.lock();    // Lock the root note
        swapNodes(heap.get(index), heap.get(1));    // Swap last node with the root
        heap.remove(index); // Remove the last node
        heapLock.unlock();

        // Start percolating down
        index = 1;
        while(index <= heap.size()/2){
            int left=getLeft(index), right=getRight(index);
            Node swapChild; // Will hold reference to child being swapped
            int swapIndex;  // Index of the swapChild
            boolean rightPresent = false;   // Is there a right child

            // Check children before locking
            if(left < heap.size()) heap.get(left).lock.lock();
            else break; // No left child - no possible swap
            // Check children before locking
            if(right < heap.size()){
                heap.get(right).lock.lock();
                rightPresent = true;
            }

            // See if the swapping child is left or right
            if(rightPresent && heap.get(left).priority > heap.get(right).priority){
                heap.get(left).lock.unlock();   // Don't need left anymore - unlock it
                swapChild = heap.get(right);
                swapIndex = right;
            }
            else{
                if(rightPresent) heap.get(right).lock.unlock(); // Don't need right anymore - unlock it
                swapChild = heap.get(left);
                swapIndex = left;
            }

            // If child has a higher priority than parent, then swap
            if(swapChild.priority < heap.get(index).priority){
                swapNodes(swapChild, heap.get(index));
                heap.get(index).lock.unlock();
                index = swapIndex;
            }
            // Don't swap
            else{
                swapChild.lock.unlock();
                break;
            }

        }

        heap.get(index).lock.unlock();
        return returnValue;
    }

    // Helper methods to get indecies
    private int getParent(int index){return index/2;}
    private int getLeft(int index){return index*2;}
    private int getRight(int index){return (index*2) + 1;}

    // Method to add a node to the heap - The children should be allocated and set to empty
    private void addNode(Node newNode){
        // There is already a node allocated
        if(size.get() < heap.size()){
            // Copy over the data
            int index = size.getAndIncrement();
            int left=getLeft(index), right=getRight(index);
            heap.set(index, newNode);

            // You need to allocate nodes for the children
            if(left >= heap.size()) heap.add(new Node(0, 0, 2L));
            if(right >= heap.size()) heap.add(new Node(0, 0, 2L));
        }
        // There's not a node yet, make one
        else{
            int index = size.getAndIncrement();
            int left=getLeft(index), right=getRight(index);
        }
    }

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
