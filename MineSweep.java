package minesweep;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class MineSweep extends Application implements MineSweepUpdatableUI
{
    private static abstract class CellHandler<E extends Event> implements EventHandler<E>
    {
        public final int ROW, COL;

        CellHandler(int cellRow, int cellCol)
        {
            ROW = cellRow;
            COL = cellCol;
        }
    }

    // path to folder containing the images
    private static final String IMG_PATH = "images/";

    // the images
    private static final Image[] numberImgs = {
            new Image(IMG_PATH + "0.png"),
            new Image(IMG_PATH + "1.png"),
            new Image(IMG_PATH + "2.png"),
            new Image(IMG_PATH + "3.png"),
            new Image(IMG_PATH + "4.png"),
            new Image(IMG_PATH + "5.png"),
            new Image(IMG_PATH + "6.png"),
            new Image(IMG_PATH + "7.png"),
            new Image(IMG_PATH + "8.png")
    };
    private static final Image logoImg =
            new Image(IMG_PATH + "logo.png");
    private static final Image cellImg =
            new Image(IMG_PATH + "cell.png");
    private static final Image cellPressedImg =
            new Image(IMG_PATH + "cell_pressed.png");
    private static final Image flagImg =
            new Image(IMG_PATH + "flag.png");
    private static final Image mineImg =
            new Image(IMG_PATH + "mine.png");
    private static final Image mineRedImg =
            new Image(IMG_PATH + "mine_red.png");
    private static final Image mineCrossImg =
            new Image(IMG_PATH + "mine_cross.png");

    private MineSweepGame game;
    private Label messageLbl;
    private Label mineCountLbl;
    private ImageView[][] cells;

    @Override
    public void start(Stage stage)
    {
        game = new MineSweepGame(this, 9, 9, 10);

        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(15, 15, 15, 15));

        Button newGameBtn = new Button("New Game");
        newGameBtn.setOnAction(this::newGame);
        topBar.setLeft(newGameBtn);

        messageLbl = new Label();
        topBar.setCenter(messageLbl);

        HBox mineCountBox = new HBox();
        mineCountBox.setAlignment(Pos.CENTER);
        mineCountLbl = new Label();
        mineCountBox.getChildren().addAll(
                new Label("Mines: "),
                mineCountLbl,
                new Label("/" + game.NUM_MINES));
        topBar.setRight(mineCountBox);

        cells = new ImageView[game.NUM_ROWS][game.NUM_COLS];
        GridPane cellPane = new GridPane();
        cellPane.setPadding(new Insets(1));
        cellPane.setHgap(1);
        cellPane.setVgap(1);
        cellPane.setStyle("-fx-background-color: black;");

        for (int i = 0; i < game.NUM_ROWS; ++i)
        {
            for (int j = 0; j < game.NUM_COLS; ++j)
            {
                ImageView iv = new ImageView();

                iv.setOnMousePressed(new CellHandler<MouseEvent>(i, j){
                    @Override
                    public void handle(MouseEvent e)
                    {
                        if (game.isOver()) return;

                        switch (e.getButton())
                        {
                            case PRIMARY:
                            case SECONDARY:
                                if (!(game.isCellShowing(ROW, COL) ||
                                      game.isCellFlagged(ROW, COL)))
                                {
                                    iv.setImage(cellPressedImg);
                                }
                                break;
                            default:
                        }
                    }
                });

                iv.setOnMouseReleased(new CellHandler<MouseEvent>(i, j){
                    @Override
                    public void handle(MouseEvent e)
                    {
                        if (game.isOver()) return;

                        switch (e.getButton())
                        {
                            case PRIMARY:
                            case SECONDARY:
                                if (!(game.isCellShowing(ROW, COL) ||
                                      game.isCellFlagged(ROW, COL)))
                                {
                                    iv.setImage(cellImg);
                                }
                                break;
                            default:
                        }
                    }
                });

                iv.setOnMouseClicked(new CellHandler<MouseEvent>(i, j){
                    @Override
                    public void handle(MouseEvent e)
                    {
                        if (game.isOver()) return;

                        switch (e.getButton())
                        {
                            case PRIMARY:
                                if (!(game.isCellShowing(ROW, COL) ||
                                      game.isCellFlagged(ROW, COL)))
                                {
                                    game.showCell(ROW, COL);

                                    if (game.isOver()) revealMines(ROW, COL);
                                }
                                break;
                            case SECONDARY:
                                game.toggleFlag(ROW, COL);
                                updateMineCountLbl();
                                break;
                            default:
                        }

                        if (game.isOver())
                        {
                            if (game.hasWon())
                            {
                                messageLbl.setText("You Win!");

                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("You Win!");
                                alert.setHeaderText(null);
                                alert.setContentText("Congratulations, you win!");
                                alert.showAndWait();
                            }
                            else
                            {
                                messageLbl.setText("Game Over!");
                            }
                        }
                    }
                });

                cells[i][j] = iv;
                cellPane.add(iv, j, i);
            }
        }

        initNewGame();

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(cellPane);

        Scene scene = new Scene(root);
        stage.setTitle("MineSweep");
        stage.getIcons().add(logoImg);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void initNewGame()
    {
        for (int i = 0; i < game.NUM_ROWS; ++i)
        {
            for (int j = 0; j < game.NUM_COLS; ++j)
            {
                cells[i][j].setImage(cellImg);
            }
        }

        game.newGame();
        messageLbl.setText("");
        updateMineCountLbl();
    }

    private void newGame(ActionEvent e)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("New game?");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to start a new game?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) initNewGame();
    }

    @Override
    public void updateCell(int row, int col)
    {
        Image im = cellImg;

        if (game.isCellShowing(row, col))
        {
            im = getImgFromVal(game.cellValue(row, col));
        }
        else if (game.isCellFlagged(row, col))
        {
            im = flagImg;
        }

        cells[row][col].setImage(im);
    }

    private void updateMineCountLbl()
    {
        int mineCount = game.NUM_MINES - game.numMinesRemaining();
        mineCountLbl.setText(String.valueOf(mineCount));
    }

    // Returns the image associated with the given value.
    // For example: the value 2 is associated with numberImgs[2].
    private static Image getImgFromVal(int val)
    {
        if (val == MineSweepGame.MINE) return mineImg;

        return numberImgs[val];
    }

    // clickedCellRow and clickCellCol are the row and column of
    // the last clicked cell
    private void revealMines(int clickedCellRow, int clickedCellCol)
    {
        for (int i = 0; i < game.NUM_ROWS; ++i)
        {
            for (int j = 0; j < game.NUM_COLS; ++j)
            {
                if (game.cellValue(i, j) == MineSweepGame.MINE)
                {
                    if (i == clickedCellRow && j == clickedCellCol)
                    {
                        cells[i][j].setImage(mineRedImg);
                    }
                    else if (!game.isCellFlagged(i, j))
                    {
                        cells[i][j].setImage(mineImg);
                    }
                }
                else if (game.isCellFlagged(i, j))
                {
                    cells[i][j].setImage(mineCrossImg);
                }
            }
        }
    }

    public static void main(String[] args)
    {
        Application.launch(args);
    }
}