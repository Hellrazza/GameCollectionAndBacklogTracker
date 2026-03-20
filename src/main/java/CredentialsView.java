import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CredentialsView extends VBox {
    private final Label UUIDLabel = new Label("UUID: Signed out");
    private final Label usernameLabel = new Label("Username: ");
    private final TextField usernameTextField = new TextField();
    private final HBox usernameBox = new HBox(usernameLabel, usernameTextField);
    private final Label passwordLabel = new Label("Password: ");
    private final TextField passwordTextField = new TextField();
    private final HBox passwordBox = new HBox(passwordLabel, passwordTextField);

    private Runnable onLoginIn;
    private Runnable onCreate;

    private final Button loginButton = new Button();
    private final Button createButton = new Button();
    public CredentialsView() {

        loginButton.setText("Log In");
        loginButton.setOnAction(e -> { if (onLoginIn != null) {
            onLoginIn.run();
        }});

        createButton.setText("Create account");
        createButton.setOnAction(e -> {
            if (onCreate != null) {
                onCreate.run();
            }
        });

        getChildren().addAll(UUIDLabel, usernameBox, passwordBox, createButton, loginButton);
    }

    public void setOnLoginIn(Runnable onLoginIn) {this.onLoginIn = onLoginIn;}
    public void setOnCreate(Runnable onCreate) {this.onCreate = onCreate;}

    public void setSessionUUIDLabel(String sessionUUID) {UUIDLabel.setText("UUID: " + sessionUUID);}
    public String getUsername() {return usernameTextField.getText();}
    public String getPlainPassword() {return passwordTextField.getText();}
}
