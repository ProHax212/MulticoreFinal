import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main program for testing
 */
public class TestMain {

    public static void main(String[] args) {
        // LOCK FREE PRIORITY QUEUE TESTING
        int numInserters = 300; int numDeleters = 200;
        int numInsert = 200; int numDelete = 200;
        LockFreePriorityQueue lockFreePriorityQueue = new LockFreePriorityQueue();
        Random r = new Random();

        for(int i = 0; i < 10000; i++){
            int num = r.nextInt(10000);
            lockFreePriorityQueue.insert(num, num);
        }

        for (int i = 0; i < numInserters; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int num = r.nextInt(1000);
                    for (int i = 0; i < numInsert; i++) lockFreePriorityQueue.insert(num, num);
                }
            });
            //thread.start();
        }

        for (int i = 0; i < numDeleters; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < numDelete; i++) System.out.println(lockFreePriorityQueue.deleteMin());
                }
            });
            thread.start();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(lockFreePriorityQueue);
        System.out.println(lockFreePriorityQueue.verify());


        // LOCK FREE SKIP LIST TESTING
        /*LockFreeSkipList skipList = new LockFreeSkipList();
        int numAdd=5, numRemove=4;

        for(int i = 0; i < numAdd; i++){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < 5; i++){
                        skipList.add(i + (Integer.parseInt(Thread.currentThread().getName()) * 5));
                    }
                }
            });
            thread.setName(Integer.toString(i));
            thread.start();
        }

        for(int i = 0; i < numRemove; i++){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < 5; i++){
                        skipList.remove(i + (Integer.parseInt(Thread.currentThread().getName()) * 5));
                    }
                }
            });
            thread.setName(Integer.toString(i));
            thread.start();
        }

        try{Thread.sleep(1000);}
        catch (InterruptedException e){}

        skipList.print();*/


        // FINE GRAINED PRIORITY QUEUE TESTING
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
