import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    private int elevatorDelay;

    Elevator(int id, int maxWorkload, int numOfFloors, ElevatorsManager manager, int elevatorDelay) {
        this.maxWorkload = maxWorkload;
        this.manager = manager;
        this.id = id;
        this.numOfFloors = numOfFloors;
        this.elevatorDelay = elevatorDelay;
    }

    public void setElevatorDelay(int elevatorDelay) {
        this.elevatorDelay = elevatorDelay;
    }

    public int getWorkload() {
        if (isEmpty) {
            return 0;
        }
        return destinations.size();
    }

    private void move() {
        deletePersons();
        checkBoundaryFloor();
        getPersons();
        if (!destinations.isEmpty()) {
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

    private void checkBoundaryFloor() {
        if (floor == 0) {
            direction = Directions.UP;
        } else if (floor == numOfFloors - 1) {
            direction = Directions.DOWN;
        }
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

    private synchronized void getPersons() {
        synchronized (manager.getRequests()) {
            boolean wasWaited = false;
            while (manager.isBlockedFloor(floor)) {
                try {
                    manager.getRequests().wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Elevator " + id + " is waiting");
                wasWaited = true;
            }

            if (wasWaited) {
                System.out.println("Elevator " + id + " is working");
            }

            manager.blockFloor(floor);
            LinkedList<Integer> requests = manager.getRequests(floor);
            requests.remove(null);
            for (int i = 0; i < requests.size(); i++) {
                try {
                    if (isEmpty || (getPersonDirection(requests.get(i)) == direction
                            && getWorkload() + 1 <= maxWorkload)) {
                        isEmpty = false;
                        destinations.add(requests.get(i));
                        requests.remove(i);
                        i--;
                    }
                    if (getWorkload() == maxWorkload) {
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Request returns null, floor " + floor + " elevator " + id);
                }
            }
            manager.unblockFloor(floor);
            manager.getRequests().notifyAll();
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
                Visualizer.get().updateElevator(id, floor, getWorkload());
                synchronized (manager.getRequests()) {
                    Visualizer.get().updateRequests(copy(manager.getRequests().getRequests()));
                }
            }
            try {
                Thread.sleep(elevatorDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Elevator " + id + " stopped");
    }

    private synchronized List<List<Integer>> copy(ArrayList<LinkedList<Integer>> list) {
        ArrayList<List<Integer>> clone = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            synchronized (list.get(i)) {
                LinkedList<Integer> innerClone = new LinkedList<>();
                LinkedList<Integer> inner = list.get(i);
                if (inner != null) {
                    for (int j = 0; j < inner.size(); j++) {
                        try {
                            innerClone.add(inner.get(j));
                        } catch (NullPointerException e) { }
                    }
                }
                clone.add(innerClone);
            }
        }
        return clone;
    }
}
