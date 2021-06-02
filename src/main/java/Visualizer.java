
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class Visualizer {
    private GridPane matrix;

    private int maxNumOfElevators = 10;
    private int elevatorsWidth = 50;
    private int elevatorsHeight = 50;
    private int numOfFloors;

    private final TextField elevatorsNumField = new TextField();
    private final TextField workloadField = new TextField();
    private final TextField floorsNumField = new TextField();
    private final TextField requestsDelayField = new TextField();
    private final TextField elevatorsDelayField = new TextField();

    private final ArrayList<Label> workloadList = new ArrayList<>();
    private final ArrayList<SVGPath> elevatorsList = new ArrayList<>();
    private final ArrayList<Label> requestsList = new ArrayList<>();

    private final Button commitButton = new Button("Commit");
    private final Button startButton = new Button("Resume");
    private final Button stopButton = new Button("Pause");

    private final Label errorLabel = new Label();

    private final int floorsShift = 1;
    private final int elevatorsShift = 1;

    private ElevatorsManager manager;

    private Visualizer() {
    }

    public static class SingletonHolder {
        public static final Visualizer HOLDER_INSTANCE = new Visualizer();
    }

    public static Visualizer get() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    public GridPane getMatrix() {
        matrix = createGrid();
        return matrix;
    }

    private GridPane createGrid() {
        GridPane gridPane = new GridPane();
        createElevatorsGrid(gridPane);
        createControlGrid(gridPane);
        setALLPosition(gridPane, HPos.CENTER, VPos.BOTTOM);
        commitButton.setOnAction(event -> resetMatrix());

        startButton.setOnAction(event -> {
            if (manager != null) {
                manager.start();
            }
        });

        stopButton.setOnAction(event -> {
            if (manager != null) {
                manager.off();
            }
        });
        return gridPane;
    }

    private void createElevatorsGrid(GridPane gridPane) {
        ColumnConstraints column = new ColumnConstraints(elevatorsWidth + 10, elevatorsWidth + 10, Double.MAX_VALUE);
        RowConstraints row = new RowConstraints(elevatorsHeight, elevatorsHeight, Double.MAX_VALUE);
        for (int i = 0; i <= maxNumOfElevators + 3; i++) {
            gridPane.getColumnConstraints().add(0, column);
            gridPane.getRowConstraints().add(row);
        }
    }

    private void createControlGrid(GridPane gridPane) {
        ColumnConstraints column = new ColumnConstraints(500, 500, Double.MAX_VALUE);
        column.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().add(column);
        int columnIndex = gridPane.getColumnConstraints().size() - 1;

        gridPane.add(new Text("Number of elevators (max is " + maxNumOfElevators + ")"), columnIndex, 0);
        elevatorsNumField.setMaxWidth(50);
        gridPane.add(elevatorsNumField, columnIndex, 1);

        gridPane.add(new Text("Max workload"), columnIndex, 2);
        workloadField.setMaxWidth(50);
        gridPane.add(workloadField, columnIndex, 3);

        gridPane.add(new Text("Number of floors"), columnIndex, 4);
        floorsNumField.setMaxWidth(50);
        gridPane.add(floorsNumField, columnIndex, 5);

        gridPane.add(new Text("Requests delay"), columnIndex, 6);
        requestsDelayField.setMaxWidth(50);
        gridPane.add(requestsDelayField, columnIndex, 7);

        gridPane.add(new Text("Elevators delay"), columnIndex, 8);
        elevatorsDelayField.setMaxWidth(50);
        gridPane.add(elevatorsDelayField, columnIndex, 9);

        gridPane.add(commitButton, columnIndex, 10);

        gridPane.add(startButton, columnIndex, 11);

        gridPane.add(stopButton, columnIndex, 12);

        errorLabel.setTextFill(Color.RED);
        gridPane.add(errorLabel, columnIndex, 13);
    }

    private void setALLPosition(GridPane gridPane, HPos horizontalPos, VPos verticalPos) {
        for (Node child : gridPane.getChildren()) {
            GridPane.setHalignment(child, horizontalPos);
            GridPane.setValignment(child, verticalPos);
        }
    }

    private int getData(TextField textField) {
        String data = textField.getCharacters().toString().trim();
        if (data.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < data.length(); i++) {
            if (!Character.isDigit(data.charAt(i))) {
                return -1;
            }
        }
        try {
            return Integer.parseInt(data);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void setDefaultElevatorsPosition(GridPane gridPane, int numOfFloors, int numOfElevators) {
        RowConstraints row = new RowConstraints(elevatorsHeight, elevatorsHeight, Double.MAX_VALUE);
        ColumnConstraints column = new ColumnConstraints();
        workloadList.clear();
        requestsList.clear();
        elevatorsList.clear();
        for (int i = 0; i < numOfFloors; i++) {
            SVGPath floor = new SVGPath();
            floor.setContent("M9.5 8V112L20 120H0V0H20L9.5 8Z");
            resize(floor, elevatorsHeight, -1);
            floor.setFill(Color.rgb(43, 34, 34));
            gridPane.getColumnConstraints().set(elevatorsShift, column);
            gridPane.add(floor, elevatorsShift, i + floorsShift);

            SVGPath floorMirrored = new SVGPath();
            floorMirrored.setContent("M9.5 8V112L20 120H0V0H20L9.5 8Z");
            floorMirrored.setRotate(180);
            resize(floorMirrored, elevatorsHeight, -1);
            floorMirrored.setFill(Color.rgb(43, 34, 34));
            gridPane.getColumnConstraints().set(numOfElevators + 1 + elevatorsShift, column);
            gridPane.add(floorMirrored, numOfElevators + 1 + elevatorsShift, i + floorsShift);

            Label requests = new Label("0");
            requests.setMaxWidth(elevatorsWidth * 3);
            requests.setAlignment(Pos.CENTER);
            gridPane.add(requests, numOfElevators + 2 + elevatorsShift, i + floorsShift);
            requestsList.add(requests);

            if (i + floorsShift >= gridPane.getRowConstraints().size()) {
                gridPane.getRowConstraints().add(row);
            }
        }

        for (int i = 0; i < numOfElevators; i++) {
            SVGPath elevator = new SVGPath();
            elevator.setContent("M1 0.5H46V9H27.5H9.5V91.5H92.5V9H74.25H56V0.5H101V101H1V0.5Z");
            resize(elevator, elevatorsHeight, elevatorsWidth);
            elevator.setFill(Color.rgb(168, 168, 168));
            elevator.setStrokeWidth(0.1);
            elevator.setStroke(Color.BLACK);
            gridPane.add(elevator, i + 1 + elevatorsShift, numOfFloors);
            GridPane.setHalignment(elevator, HPos.CENTER);
            elevatorsList.add(elevator);

            Label workload = new Label("0");
            workload.setMaxWidth(elevatorsWidth);
            workload.setAlignment(Pos.CENTER);
            gridPane.add(workload, i + 1 + elevatorsShift, numOfFloors);
            GridPane.setHalignment(workload, HPos.CENTER);
            workloadList.add(workload);
        }

        Label waitingRequests = new Label("Waiting Requests");
        waitingRequests.setAlignment(Pos.CENTER);
        gridPane.add(waitingRequests, numOfElevators + 2 + elevatorsShift, floorsShift - 1);
        GridPane.setHalignment(waitingRequests, HPos.CENTER);
        gridPane.getColumnConstraints().set(numOfElevators + 2 + elevatorsShift, new ColumnConstraints(elevatorsWidth * 3));
    }

    private void resize(SVGPath svg, int height, int width) {
        double originalWidth = svg.prefWidth(-1);
        double originalHeight = svg.prefHeight(originalWidth);
        double scaleX = width / originalWidth;
        double scaleY = height / originalHeight;

        if (height >= 0) {
            svg.setScaleY(scaleY);
        }
        if (width >= 0) {
            svg.setScaleX(scaleX);
        }
    }

    private void cleanElevatorsZone(GridPane gridPane) {
        ColumnConstraints column = new ColumnConstraints(elevatorsWidth + 10, elevatorsWidth + 10, Double.MAX_VALUE);
        List<Node> deleteNodes = new ArrayList<>();
        for (Node child : gridPane.getChildren()) {
            Integer index = GridPane.getColumnIndex(child);
            if (index != null && index < gridPane.getColumnConstraints().size() - 1) {
                deleteNodes.add(child);
            }
        }

        for (int i = 0; i < gridPane.getColumnConstraints().size() - 1; i++) {
            gridPane.getColumnConstraints().set(i, column);
        }

        gridPane.getChildren().removeAll(deleteNodes);
    }

    public void setElevatorsHeight(int elevatorsHeight) {
        this.elevatorsHeight = elevatorsHeight;
    }

    public void setElevatorsWidth(int elevatorsWidth) {
        this.elevatorsWidth = elevatorsWidth;
    }

    public void setMaxNumOfElevators(int maxNumOfElevators) {
        this.maxNumOfElevators = maxNumOfElevators;
    }

    public synchronized void updateElevator(int elevatorId, int floor, int workload) {
        GridPane.setRowIndex(elevatorsList.get(elevatorId), numOfFloors - floor);
        GridPane.setRowIndex(workloadList.get(elevatorId), numOfFloors - floor);
        Platform.runLater(() -> workloadList.get(elevatorId).setText(Integer.toString(workload)));
    }

    public synchronized void updateRequests(List<List<Integer>> requests) {
        synchronized (requests) {
            for (int floor = 0; floor < numOfFloors; floor++) {
                int finalFloor = numOfFloors - floor - 1;
                int requestFloor = floor;
                try {
                    Platform.runLater(() ->
                        requestsList.get(finalFloor).setText(requests.get(requestFloor).toString()));
                } catch (Exception e) {
                    System.out.println("Can not update requests on floor " + finalFloor);
                }
            }
        }
    }

    public void resetMatrix() {
        int elevatorsNum = getData(elevatorsNumField);
        int workload = getData(workloadField);
        int floorsNum = getData(floorsNumField);
        int requestsDelay = getData(requestsDelayField);
        int elevatorsDelay = getData(elevatorsDelayField);
        if (elevatorsNum < 0 || workload < 0 || floorsNum < 0 || requestsDelay < 0 || elevatorsDelay < 0) {
            errorLabel.setText("Incorrect data");
            return;
        }
        if (elevatorsNum > maxNumOfElevators) {
            errorLabel.setText("Num of elevators is incorrect");
            return;
        }
        numOfFloors = floorsNum;
        errorLabel.setText("");

        if (manager != null) {
            manager.off();
        }

        cleanElevatorsZone(matrix);
        setDefaultElevatorsPosition(matrix, floorsNum, elevatorsNum);

        manager = new ElevatorsManager(elevatorsNum, workload, floorsNum, requestsDelay, elevatorsDelay);
        manager.start();
    }

    public void stop() {
        manager.off();
        manager = null;
    }
}
