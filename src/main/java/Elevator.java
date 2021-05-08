import java.util.ArrayList;
import java.util.LinkedList;

public class Elevator implements Runnable {
    private final int id;
    private final int maxWorkload;
    private final ArrayList<Integer> destinations = new ArrayList<>();
    private int floor = 0;
    private volatile Directions direction = Directions.UP;
    private final ElevatorsManager manager;
    private final int numOfFloors;
    private volatile boolean on = true;
    private boolean isEmpty = true;

    Elevator(int id, int maxWorkload, int numOfFloors, ElevatorsManager manager) {
        this.maxWorkload = maxWorkload;
        this.manager = manager;
        this.id = id;
        this.numOfFloors = numOfFloors;
    }

    public int getWorkload() {
        if (isEmpty) {
            return 0;
        }
        return destinations.size();
    }

    private void move() {
        deletePersons();
        getPersons();
        if (!destinations.isEmpty()) {
            isEmpty = false;
            checkDirection();
            if (direction == Directions.UP) {
                if (floor < numOfFloors - 1) {
                    floor++;
                }
            } else {
                if (floor > 0) {
                    floor--;
                }
            }
        } else {
            isEmpty = true;
            int maxFloor = manager.getLargestFloor();
            if (maxFloor != -1) {
                destinations.add(maxFloor);
            }
        }
    }

    public void on() {
        on = true;
    }

    public void off() {
        on = false;
    }

    private void checkDirection() {
        for (int dest : destinations) {
            if (getPersonDirection(dest) == direction) {
                return;
            }
        }
        changeDirection();
    }

    private void changeDirection() {
        if (direction == Directions.UP && floor > 0) {
            direction = Directions.DOWN;
        } else if (floor < manager.getNumOfFloors() - 1) {
            direction = Directions.UP;
        }
    }

    private void deletePersons() {
        destinations.removeIf(it -> floor == it);
    }

    private void getPersons() {
        LinkedList<Integer> requests = manager.getRequests(floor);

        if (requests == null || requests.isEmpty()) {
            return;
        }
        synchronized (requests) {
            while(manager.isBlockedFloor(floor)) {
                try {
                    requests.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            manager.blockFloor(floor);
            for (int i = 0; i < requests.size(); i++) {
                if (destinations.isEmpty() || (getPersonDirection(requests.get(i)) == direction
                        && getWorkload() + 1 <= maxWorkload)) {
                    destinations.add(requests.get(i));
                    requests.remove(i);
                    i--;
                }
                if (getWorkload() == maxWorkload) {
                    break;
                }
            }
            manager.unblockFloor(floor);
            requests.notifyAll();
        }
    }

    private Directions getPersonDirection(int destination) {
        if (floor > destination) {
            return Directions.DOWN;
        } else {
            return Directions.UP;
        }
    }

    @Override
    public void run() {
        while(on) {
            move();
            synchronized (Visualizer.get()) {
                System.out.println();
                Visualizer.get().updateElevator(id, floor, getWorkload());
                Visualizer.get().updateRequests(manager.getRequests());
                Visualizer.get().show();
                System.out.println();
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
