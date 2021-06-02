import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;


public class Main extends Application {

    private final int MAX_NUM_OF_ELEVATORS = 10;
    private final int ELEVATORS_WIDTH = 50;
    private final int ELEVATORS_HEIGHT = 50;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Visualizer.get().setMaxNumOfElevators(MAX_NUM_OF_ELEVATORS);
        Visualizer.get().setElevatorsHeight(ELEVATORS_HEIGHT);
        Visualizer.get().setElevatorsWidth(ELEVATORS_WIDTH);

        GridPane gridPane = Visualizer.get().getMatrix();
        //gridPane.setGridLinesVisible(true);

        Scene scene = new Scene(new ScrollPane(gridPane));
        stage.setScene(scene);
        stage.setTitle("Elevators project");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Visualizer.get().stop();
    }
}
