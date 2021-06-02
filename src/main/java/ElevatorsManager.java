import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ElevatorsManager {
    private final Requests requests = Requests.get();
    private final ArrayList<Elevator> elevators = new ArrayList<>();

    ElevatorsManager(int numOfElevators, int maxWorkload, int numOfFloors, int requestsDelay, int elevatorDelay) {
        requests.setRequestsDelay(requestsDelay);
        for (int i = 0; i < numOfElevators; i++) {
            elevators.add(new Elevator(i, maxWorkload, numOfFloors, this, elevatorDelay));
            Visualizer.get().updateElevator(i, 0, 0);
        }
        requests.setNumOfFloors(numOfFloors);
    }

    public void setRequestsDelay(int requestsDelay) {
        requests.setRequestsDelay(requestsDelay);
    }

    public void setElevatorDelay(int elevatorDelay) {
        for (Elevator elevator : elevators) {
            elevator.setElevatorDelay(elevatorDelay);
        }
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

    public void start() {
        Executor ex = Executors.newFixedThreadPool(elevators.size() + 1);
        for (Elevator e : elevators) {
            ex.execute(e);
        }
        ex.execute(requests);
        on();
    }

    private void on() {
        requests.on();

        for (Elevator e : elevators) {
            e.on();
        }
    }

    void off() {
        requests.off();
        for (Elevator e : elevators) {
            e.off();
        }
    }
}
