/**
 * Sequential version of a priority queue
 * Implement this first to get an idea of the functionality
 */
public class SequentialPriorityQueue {

    // Constructor
    public SequentialPriorityQueue(){

    }

    // Insert an object with the given priority
    public void insert(int priority){

    }

    // Node for each element of the priority queue
    private static class Node{

        public int priority;
        public Object value;

        // Constructor
        public Node(Object value, int priority){
            this.priority = priority;
            this.value = value;
        }

    }

}
