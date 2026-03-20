import java.sql.SQLException;
import java.util.SortedMap;

public class CredentialsController {

    private CredentialsView view;
    private CredentialsService service;

    public CredentialsController(CredentialsService service, CredentialsView view) {
        this.view = view;
        this.service = service;

        view.setOnLoginIn(this::handleLogin);
        view.setOnCreate(this::handleCreate);
    }

    public void handleLogin() {
        try {
            String username = view.getUsername();
            String password = view.getPlainPassword();
            if (!username.isEmpty() && !password.isEmpty()) {
                System.out.println("LOG IN");
                login(view.getUsername(), view.getPlainPassword());
                view.setSessionUUIDLabel(Integer.toString(Session.getActiveUUID()));
            }
        }
        catch (SQLException e) {
            System.out.println("Error logging in.");
        }
    }

    public void handleCreate() {
        try {
            String username = view.getUsername();
            String password = view.getPlainPassword();
            if (!username.isEmpty() && !password.isEmpty()) {
                System.out.println("CREATE USER");
                createUser(view.getUsername(), view.getPlainPassword());
                view.setSessionUUIDLabel(Integer.toString(Session.getActiveUUID()));
            }
        }
        catch (SQLException e) {
            System.out.println("ERROR CREATING ACCOUNT");
        }
    }

    public void createUser(String username, String password) throws  SQLException {
        service.createUser(username, password);
        login(username, password);
    }

    public void login(String username, String plainPassword) throws SQLException{
        Session.setActiveUUID(service.validLogin(username, plainPassword));
    }



}
