import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main program for testing
 */
public class TestMain {

    public static void main(String[] args) {
        // LOCK FREE PRIORITY QUEUE TESTING
        /*int numInserters = 50; int numDeleters = 20;
        int numInsert = 50; int numDelete = 20;
        LockFreePriorityQueue lockFreePriorityQueue = new LockFreePriorityQueue();
        Random r = new Random();

//        for(int i = 0; i < numDeleters*numDelete; i++){
//            int num = r.nextInt();
//            lockFreePriorityQueue.insert(num, num);
//        }

        for (int i = 0; i < numInserters; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int num = Math.abs(r.nextInt());
                    for (int i = 0; i < numInsert; i++) lockFreePriorityQueue.insert(num, num);
                }
            });
            thread.start();
        }

        for (int i = 0; i < numDeleters; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < numDelete; i++) lockFreePriorityQueue.deleteMin();
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
        System.out.println(lockFreePriorityQueue.verify());*/


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
        FineGrainedPriorityQueue fineGrainedPriorityQueue = new FineGrainedPriorityQueue(1000);
        int numInserters = 1000; int numDeleters = 1000;
        int numInsert = 1000; int numDelete = 1000;
        Random r = new Random();
        ExecutorService inserters = Executors.newFixedThreadPool(numInserters);
        ExecutorService deleters = Executors.newFixedThreadPool(numDeleters);

//        for(int i = 0; i < numInsert; i++){
//            int num = r.nextInt(1000);
//            fineGrainedPriorityQueue.insert(num, num);
//        }
//
//        for(int i = 0; i < numDelete; i++){
//            System.out.println(fineGrainedPriorityQueue.deleteMin());
//        }

        for(int i = 0; i < numInserters; i++){
            inserters.execute(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < numInsert; i++){
                        int num = r.nextInt(10000);
                        fineGrainedPriorityQueue.insert(num, num);
                    }
                }
            });
        }

        for(int i = 0; i < numDeleters; i++){
            deleters.execute(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < numDelete; i++){
                        System.out.println(fineGrainedPriorityQueue.deleteMin());
                    }
                }
            });
        }

        try {
            inserters.shutdown();
            deleters.shutdown();
            inserters.awaitTermination(10, TimeUnit.SECONDS);
            deleters.awaitTermination(10, TimeUnit.SECONDS);
        }catch (InterruptedException e){

        }

        System.out.println(fineGrainedPriorityQueue.verify());
        System.out.println(fineGrainedPriorityQueue);

    }
}
