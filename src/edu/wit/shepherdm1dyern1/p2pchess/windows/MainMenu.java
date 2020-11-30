package edu.wit.shepherdm1dyern1.p2pchess.windows;

import edu.wit.shepherdm1dyern1.p2pchess.Main;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainMenu extends HBox {

    public MainMenu(Parent parent) {
        HBox startJoin = new HBox();

        Button create = new Button("Create Game");
        create.setPrefSize(100, 20);

        Label portLab = new Label("Local Port:");
        TextField portField = new TextField ();

        Button join = new Button("Join Game:");
        join.setPrefSize(100, 20);

        Label ipLab = new Label("IP Address and port:");
        TextField ipField = new TextField ();

        startJoin.getChildren().addAll(create, portLab, portField, join, ipLab, ipField);
        startJoin.setPadding(new Insets(5,5,5,5));
        startJoin.setSpacing(10);

        create.setOnAction(e -> {
            try {
                //Main.createGame(portField, chessBoard);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }); //CHANGE ME
        join.setOnAction(e -> {
            try {
                //joinGame(ipField, chessBoard);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }); //CHANGE ME

        create.setOnAction(e -> {
            try {
                //createGame(portField, chessBoard);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }); //CHANGE ME
        join.setOnAction(e -> {
            try {
                //joinGame(ipField, chessBoard);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }); //CHANGE ME
    }
}
