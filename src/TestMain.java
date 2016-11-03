import java.util.Random;

/**
 * Main program for testing
 */
public class TestMain {

    public static void main(String[] args){
        // LOCK FREE PRIORITY QUEUE TESTING
        LockFreePriorityQueue queue = new LockFreePriorityQueue();

        SequentialSkipList skipList = new SequentialSkipList();
        for(int i = 0; i < 10; i++) skipList.insert(i, 0);
        skipList.print();






        /*FineGrainedPriorityQueue queue = new FineGrainedPriorityQueue();
        Random r = new Random();

        for(int i = 0; i < 5; i++){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < 5; i++){
                        int num = r.nextInt(50);
                        queue.insert(num, num);
                    }
//                    System.out.println(queue);
                    for(int i = 0; i < 5; i++){
                        System.out.println(queue.remove());
                    }
                }
            });
            thread.start();
        }

//        try{Thread.sleep(500);}
//        catch (InterruptedException e){}
//
//        System.out.println(queue);*/
    }

}
