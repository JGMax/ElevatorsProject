
import java.util.ArrayList;
import java.util.LinkedList;

public class Visualizer {
    private final ArrayList<ArrayList<Character>> matrix = new ArrayList<>();
    private int elevatorXSize = 0;

    private Visualizer() {
    }

    public static class SingletonHolder {
        public static final Visualizer HOLDER_INSTANCE = new Visualizer();
    }

    public static Visualizer get() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    public void updateElevator(int elevatorId, int floor, int workload) {
        clearElevator(elevatorId);
        int x = elevatorId * (elevatorXSize + 3) + 2;
        int y = matrix.size() - floor * 3 - 1;
        String load = Integer.toString(workload);

        ArrayList<Character> line = matrix.get(y);
        for (int i = 1; i <= elevatorXSize; i++) {
            line.set(x + i, '=');
        }
        line = matrix.get(y - 1);
        line.set(x, '|');
        line.set(x + elevatorXSize + 1, '|');

        for (int i = 1; i <= load.length(); i++) {
            line.set(x + i, load.charAt(i - 1));
        }

        line = matrix.get(y - 2);
        for (int i = 1; i <= elevatorXSize; i++) {
            line.set(x + i, '=');
        }
    }

    private void clearElevator(int id) {
        int x = id * (elevatorXSize + 3) + 2;
        for (ArrayList<Character> line : matrix) {
            for (int i = 0; i <= elevatorXSize + 1; i++) {
                line.set(x + i, ' ');
            }
        }
    }

    public void updateRequests(Requests requests) {
        deleteRequests();
        for (int floor = 0; floor < requests.size(); floor++) {
            int y = matrix.size() - floor * 3 - 2;
            ArrayList<Character> line = matrix.get(y);
            line.add('<');
            LinkedList<Integer> request = requests.getRequests(floor);
            if (request == null || request.isEmpty()) {
                continue;
            }
            synchronized (request) {
                requests.blockFloor(floor);
                request = requests.getRequests(floor);
                for (int i = 0; i < request.size(); i++) {
                    try {
                        for (char ch : Integer.toString(request.get(i)).toCharArray()) {
                            line.add(ch);
                        }
                    } catch (NullPointerException e) {
                        line.add('0');
                        for (int j = 1; j < elevatorXSize; j++) {
                            line.add(' ');
                        }
                        System.out.println("Request returns null, floor " + floor + " visualization");
                    }
                    line.add(' ');
                }
                requests.unblockFloor(floor);
            }
        }
    }

    private void deleteRequests() {
        int buildingSize = matrix.get(0).size();
        for (int floor = 1; floor * 3 - 1 < matrix.size(); floor += 1) {
            ArrayList<Character> line = matrix.get(floor * 3 - 1);
            while (line.size() > buildingSize) {
                line.remove(buildingSize);
            }
        }
    }

    public void createMatrix(int numOfFloors, int numOfElevators, int maxWorkload) {
        elevatorXSize = Integer.toString(maxWorkload).length();
        for (int k = 0; k < 3 * numOfFloors + 1; k++) {
            ArrayList<Character> line = new ArrayList<>();
            line.add('|');
            for (int i = 0; i < numOfElevators; i++) {
                if (k % 3 == 0) {
                    line.add('+');
                } else {
                    line.add('|');
                }
                line.add(' ');
                for (int j = 0; j < elevatorXSize; j++) {
                    line.add(' ');
                }
                line.add(' ');
            }
            if (k % 3 == 0) {
                line.add('+');
            } else {
                line.add('|');
            }
            line.add('|');
            matrix.add(line);
        }
    }

    public void show() {
        for (ArrayList<Character> line : matrix) {
            for (char ch : line) {
                System.out.print(ch);
            }
            System.out.println();
        }
    }
}
