import java.util.*;

public class Requests implements Runnable {
    private boolean makeRequests = true;
    private int numOfFloors = 0;
    private final ArrayList<LinkedList<Integer>> requests = new ArrayList<>();
    private final ArrayList<Boolean> blocks = new ArrayList<>();
    private int requestsDelay = 1;

    private Requests() {}
    public static class SingletonHolder {
        public static final Requests HOLDER_INSTANCE = new Requests();
    }

    public static Requests get() {
        return Requests.SingletonHolder.HOLDER_INSTANCE;
    }

    public synchronized void blockFloor(int floor) {
        blocks.set(floor, true);
    }

    public boolean isBlocked(int floor) {
        return blocks.get(floor);
    }

    public synchronized void unblockFloor(int floor) {
        blocks.set(floor, false);
    }

    public ArrayList<LinkedList<Integer>> getRequests() {
        return requests;
    }

    public void setRequestsDelay(int delay) {
        requestsDelay = delay;
    }

    public void block() {
        for (int i = 0; i < blocks.size(); i++) {
            blocks.set(i, true);
        }
    }

    public void unblock() {
        for (int i = 0; i < blocks.size(); i++) {
            blocks.set(i, false);
        }
    }

    public void setNumOfFloors(int numOfFloors) {
        this.numOfFloors = numOfFloors;
        requests.clear();
        blocks.clear();
        for (int i = 0; i < numOfFloors; i++) {
            requests.add(new LinkedList<>());
            blocks.add(false);
        }
    }

    public synchronized LinkedList<Integer> getRequests(int floor) {
        if (floor >= 0 && floor < numOfFloors) {
            return requests.get(floor);
        }
        return null;
    }

    public int size() {
        return requests.size();
    }

    public int getNumOfFloors() {
        return numOfFloors;
    }

    public void off() {
        makeRequests = false;
    }

    public void on() {
        makeRequests = true;
    }

    private void generate(Random rnd) {
        cleanNulls();
        ArrayList<Integer> unblockedFloors = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            if (!blocks.get(i)) {
                unblockedFloors.add(i);
            }
        }

        if (unblockedFloors.size() < 1) {
            return;
        }

        int idx = rnd.nextInt(unblockedFloors.size());
        int floorIndex = unblockedFloors.get(idx);
        int destinationIndex = rnd.nextInt(numOfFloors);
        while (destinationIndex == floorIndex) {
            destinationIndex = rnd.nextInt(numOfFloors);
        }
        synchronized (this) {
            requests.get(floorIndex).add(destinationIndex);
        }
    }

    private void cleanNulls() {
        for (LinkedList<Integer> request : requests) {
            request.remove(null);
        }
    }

    @Override
    public void run() {
        Random rnd = new Random();
        while(makeRequests) {
            generate(rnd);
            try {
                Thread.sleep(requestsDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Requests stopped");
    }
}
