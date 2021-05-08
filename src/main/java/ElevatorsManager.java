import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ElevatorsManager {
    private final Requests requests = Requests.get();
    private final ArrayList<Elevator> elevators = new ArrayList<>();
    private final int workTime;

    ElevatorsManager(int numOfElevators, int maxWorkload, int numOfFloors, int workTime) {
        Visualizer.get().createMatrix(numOfFloors, numOfElevators, maxWorkload);
        for (int i = 0; i < numOfElevators; i++) {
            elevators.add(new Elevator(i, maxWorkload, numOfFloors, this));
            Visualizer.get().updateElevator(i, 0, 0);
        }
        Visualizer.get().show();
        requests.setNumOfFloors(numOfFloors);
        this.workTime = workTime;
    }

    public synchronized int getLargestFloor() {
        requests.block();
        int maxSize = 0;
        int maxFloor = -1;

        for (int i = 0; i < requests.size(); i++) {
            if (requests.getRequests(i).size() > maxSize) {
                maxSize = requests.getRequests(i).size();
                maxFloor = i;
            }
        }
        requests.unblock();
        return maxFloor;
    }

    public boolean isBlockedFloor(int floor) {
        return requests.isBlocked(floor);
    }

    public Requests getRequests() {
        return requests;
    }

    public void blockFloor(int floor) {
        if (floor >= 0 && floor < requests.getNumOfFloors()) {
            requests.blockFloor(floor);
        }
    }

    public void unblockFloor(int floor) {
        if (floor >= 0 && floor < requests.getNumOfFloors()) {
            requests.unblockFloor(floor);
        }
    }

    public LinkedList<Integer> getRequests(int floor) {
        if (floor >= 0 && floor < requests.getNumOfFloors()) {
            return requests.getRequests(floor);
        }
        return null;
    }

    public int getNumOfFloors() {
        return requests.getNumOfFloors();
    }

    public void on() {
        Executor ex = Executors.newFixedThreadPool(elevators.size() + 1);

        requests.on();
        ex.execute(requests);

        for (Elevator e : elevators) {
            e.on();
            ex.execute(e);
        }

        if (workTime >= 0) {
            try {
                Thread.sleep(workTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (Elevator e : elevators) {
                e.off();
            }

            requests.off();
        }
    }
}
