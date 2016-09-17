package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.*;
import java.util.*;

public class Main extends Application {

    public enum Direction{
        UP, DOWN, LEFT, RIGHT
    }

    public static final int BLOCK_SIZE = 40;
    public static final int APP_W = 20 * BLOCK_SIZE;
    public static final int APP_H = 15 * BLOCK_SIZE;

    private Direction direction = Direction.RIGHT;
    private boolean moved = false;
    private boolean running = false;
    private boolean isPaused = false;

    Stage window;
    Scene scene;
    Label label;
    int score = 0;

    private Timeline timeline = new Timeline();

    private ObservableList<Node> snake;

    private Rectangle food;

    private Parent createContent(){
        Pane root = new Pane();
        root.setPrefSize(APP_W,APP_H);

        Group snakeBody = new Group();
        snake = snakeBody.getChildren();

        food = new Rectangle(BLOCK_SIZE,BLOCK_SIZE);
        food.setFill(Color.BLUE);
        placeFood();

        KeyFrame frame = new KeyFrame(Duration.seconds(0.16), event ->{
            if(!running)
                return;

            boolean toRemove = snake.size() > 1;

            Node tail = toRemove ? snake.remove(snake.size()-1) : snake.get(0);

            double tailX = tail.getTranslateX();
            double tailY = tail.getTranslateY();

            switch(direction){
                case UP:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() - BLOCK_SIZE);
                    break;
                case DOWN:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() + BLOCK_SIZE);
                    break;
                case RIGHT:
                    tail.setTranslateX(snake.get(0).getTranslateX() + BLOCK_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;
                case LEFT:
                    tail.setTranslateX(snake.get(0).getTranslateX() - BLOCK_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;
            }

            moved = true;

            if(toRemove)
                snake.add(0,tail);

            // collision detection
            for(Node rect: snake){
                if(rect != tail && tail.getTranslateX() == rect.getTranslateX()
                        && tail.getTranslateY() == rect.getTranslateY()){
                    // if the snake hits its own body
                    restartGame();
                    break;
                }
            }

            if(tail.getTranslateX() < 0 || tail.getTranslateX() > APP_W
                    || tail.getTranslateY() < 0 || tail.getTranslateY() > APP_H){
                // if the snake hits the edge of the scene
                restartGame();
            }

            if(tail.getTranslateX() == food.getTranslateX() && tail.getTranslateY() == food.getTranslateY()){
                // if the snake hits the food

                placeFood();

                score ++;
                label.setText("Score: "+String.valueOf(score)+" ");

                Rectangle rect = new Rectangle(BLOCK_SIZE,BLOCK_SIZE);
                rect.setTranslateX(tailX);
                rect.setTranslateY(tailY);

                snake.add(rect);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.setCycleCount(Timeline.INDEFINITE);

        root.getChildren().addAll(food,snakeBody);
        return root;
    }

    // Place the new food somewhere outside the snake's body
    private void placeFood(){
        food.setTranslateX((int)(Math.random() * (APP_W-BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
        food.setTranslateY((int)(Math.random() * (APP_H-BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
        boolean flag = true;
        while(true){
            for(int i=0;i<snake.size();i++){
                if(snake.get(i).getTranslateX() == food.getTranslateX() &&
                        snake.get(i).getTranslateY() == food.getTranslateY()){
                    food.setTranslateX((int)(Math.random() * (APP_W-BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
                    food.setTranslateY((int)(Math.random() * (APP_H-BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
                    flag = false;
                    break;
                }
            }
            if(flag)
                break;
            flag = true;
        }
    }

    private void restartGame(){
        checkHighscore();
        stopGame();
        startGame();
    }

    private void stopGame(){
        score = 0;
        label.setText("Score: "+String.valueOf(score)+" ");
        running = false;
        timeline.stop();
        snake.clear();
    }

    private void startGame(){
        direction = Direction.RIGHT;
        Rectangle head = new Rectangle(BLOCK_SIZE,BLOCK_SIZE);
        snake.add(head);
        timeline.play();
        running = true;
    }

    private void checkHighscore(){
        try {
            Scanner fin = new Scanner(new File("highscore"));
            int hs = fin.nextInt();
            if(score > hs){

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("GAME OVER !");
                alert.setContentText("New highscore: "+score);
                alert.show();
                Thread.sleep(2000);
                PrintStream output = new PrintStream(new File("highscore"));
                output.print(score);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        window = primaryStage;

        BorderPane root = new BorderPane();
        root.setCenter(createContent());
        scene = new Scene(root,840,712,Color.TRANSPARENT);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        MenuItem newGameItem = new MenuItem("New Game");
        fileMenu.getItems().addAll(newGameItem,exitItem);
        menuBar.getMenus().add(fileMenu);

        exitItem.setOnAction(e -> window.close());
        newGameItem.setOnAction(e -> restartGame());

        label = new Label("Score: "+String.valueOf(score)+" ");
        Region reg = new Region();
        reg.setPrefWidth(720);
        HBox hBox = new HBox(10,reg,label);
        hBox.setStyle("-fx-background-color: white;");

        VBox vBox = new VBox(menuBar,hBox);
        root.setTop(vBox);

        scene.setOnKeyPressed(e ->{
            if(!moved)
                return;

            switch(e.getCode()){
                case UP:
                    if(direction != Direction.DOWN)
                        direction = Direction.UP;
                    break;
                case DOWN:
                    if(direction != Direction.UP)
                        direction = Direction.DOWN;
                    break;
                case LEFT:
                    if(direction != Direction.RIGHT)
                        direction = Direction.LEFT;
                    break;
                case RIGHT:
                    if(direction != Direction.LEFT)
                        direction = Direction.RIGHT;
                    break;
                case P:
                    if(isPaused){
                        timeline.play();
                        direction = Direction.RIGHT;
                        isPaused = false;
                    }else{
                        timeline.pause();
                        isPaused = true;
                    }
                    break;
            }

            moved = false;
        });

        window.setTitle("Snake Game");
        window.setScene(scene);
        window.initStyle(StageStyle.TRANSPARENT);
        scene.getStylesheets().add
                (Main.class.getResource("Spaghetti.css").toExternalForm());
        window.show();
        startGame();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
