package edu.wit.shepherdm1dyern1.p2pchess;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;



public class ChatWindow extends Application {
    public Stage stage;
    public Scene scene;
    private BorderPane base;
    public ScrollPane scroll;
    public VBox scrollBox;
    TextField msgField;

    public ChatWindow(){
        this.stage = new Stage();
        startChat();
    }

    @Override
    public void start(Stage stage) throws Exception {
    }

    private void startChat(){
        base = new BorderPane();
        scroll = new ScrollPane();
        HBox entry = new HBox();
        scrollBox = new VBox();
        scrollBox.setPrefWidth(550);

        Button sendMsg = new Button("Send");
        sendMsg.setPrefSize(70, 20);

        msgField = new TextField();
        msgField.setPrefWidth(480);
        sendMsg.setOnAction(e -> {
            try {
                send(msgField.getText());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        entry.getChildren().addAll(msgField, sendMsg);
        scroll.setContent(scrollBox);
        base.setCenter(scroll);
        base.setBottom(entry);

        this.stage.setTitle("Chat");
        this.scene = new Scene(base, 550, 550);
        scene.setOnKeyPressed(ke -> {
            KeyCode keyCode = ke.getCode();
            if (keyCode.equals(KeyCode.ENTER)) {
                send(msgField.getText());
            }
        });
        this.stage.setScene(scene);
        this.stage.setResizable(false);
        this.stage.show();
    }

    public void send(String msg){
        //add stuff here to send msg string to other client (sending it to their "recieve" method !!!!!!!!!!!!!!!!!!!!!!!
        recieve(msg, true);
        msgField.clear();
    }
    //msg will need to be input from some socket/thread elsewhere (msg will come from other client's "send" method
    public void recieve(String msg, boolean isMe){
        if(isMe){
            Text message = new Text("You:\n"+msg);
            scrollBox.getChildren().add(message);
        }
        else{
            Label message = new Label("Them:\n"+msg);
            message.setPrefWidth(550);
            message.setWrapText(true);
            scrollBox.getChildren().add(message);
        }
    }
}