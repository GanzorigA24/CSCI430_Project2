
import java.util.function.BiFunction;
import java.io.Console;
import java.util.Scanner;
import java.util.NoSuchElementException;

// Owns the active menu state and controls permitted role transitions.
public class WarehouseContext {

    // STATE CONSTANTS
    public static final int LOGIN = 0;
    public static final int CLIENT = 1;
    public static final int CLERK = 2;
    public static final int MANAGER = 3;
    public static final int WISHLIST = 4;

    // TRANSITION EVENT CONSTANTS
    public static final int TO_LOGIN = 0;
    public static final int TO_CLIENT = 1;
    public static final int TO_CLERK = 2;
    public static final int TO_MANAGER = 3;
    public static final int EXIT = 4;
    public static final int TO_WISHLIST = 5;

    // LOGIN ROLE CONSTANTS
    public static final int ROLE_NONE = -1;
    public static final int ROLE_CLIENT = 0;
    public static final int ROLE_CLERK = 1;
    public static final int ROLE_MANAGER = 2;

    // SPECIAL TRANSITION VALUES
    private static final int INVALID = -1;
    private static final int EXIT_STATE = -2;

    // FIELDS
    private static WarehouseContext instance;

    private static final Scanner inputScanner =
            new Scanner(System.in);

    private static BiFunction<String, Boolean, String> inputDialog;

    private int currentState;
    private WarehouseState[] states;
    private int[][] nextState;
    private String clientID;
    private int loginRole;

    // PRIVATE CONSTRUCTOR
    private WarehouseContext() {

        // Create the state array
        states = new WarehouseState[5];

        states[LOGIN] = LoginState.instance();
        states[CLIENT] = ClientMenuState.instance();
        states[CLERK] = ClerkMenuState.instance();
        states[MANAGER] = ManagerMenuState.instance();
        states[WISHLIST] = WishlistMenuState.instance();

        // Initialize the transition matrix
        // Columns: TO_LOGIN, TO_CLIENT, TO_CLERK,
        // TO_MANAGER, EXIT, TO_WISHLIST
        nextState = new int[][] {

            // LOGIN
            { INVALID, CLIENT, CLERK, MANAGER,
              EXIT_STATE, INVALID },

            // CLIENT
            { LOGIN, INVALID, CLERK, INVALID,
              INVALID, WISHLIST },

            // CLERK
            { LOGIN, CLIENT, INVALID, MANAGER,
              INVALID, INVALID },

            // MANAGER
            { LOGIN, INVALID, CLERK, INVALID,
              INVALID, INVALID },

            // WISHLIST
            { INVALID, CLIENT, INVALID, INVALID,
              INVALID, INVALID }
        };

        currentState = LOGIN;
        clientID = null;
        loginRole = ROLE_NONE;
    }

    // SINGLETON INSTANCE
    public static WarehouseContext instance() {

        if (instance == null) {
            instance = new WarehouseContext();
        }

        return instance;
    }

    public static Scanner inputScanner() {
        return inputScanner;
    }

    static void setInputDialog(
            BiFunction<String, Boolean, String> dialog) {
        inputDialog = dialog;
    }

    static String prompt(String message) {
        if (inputDialog != null) {
            return inputDialog.apply(message, false);
        }

        System.out.print(message);
        return readLine();
    }

    static String promptPassword(String message) {
        if (inputDialog != null) {
            return inputDialog.apply(message, true);
        }

        System.out.print(message);
        return readPassword();
    }

    WarehouseState activeState() {
        return currentState == EXIT_STATE
                ? null
                : states[currentState];
    }

    // INPUT HANDLING
    public static String readLine() {

        System.out.flush();

        Console console = System.console();

        if (console != null) {
            String line = console.readLine();

            if (line == null) {
                throw new InputClosedException();
            }

            return line;
        }

        try {
            return inputScanner.nextLine();
        } catch (NoSuchElementException exception) {
            throw new InputClosedException();
        }
    }

    public static String readPassword() {

        System.out.flush();

        Console console = System.console();

        if (console == null) {
            return readLine();
        }

        char[] password = console.readPassword();

        if (password == null) {
            throw new InputClosedException();
        }

        try {
            return new String(password);
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    public static final class InputClosedException
            extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }

    // CLIENT ID GETTER AND SETTER
    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {

        if (currentState != LOGIN && currentState != CLERK) {
            System.out.println(
                "Client selection is not allowed from the current state."
            );
            return;
        }

        this.clientID = clientID == null
                ? null
                : clientID.trim();
    }

    // LOGIN ROLE GETTER AND SETTER
    public int getLoginRole() {
        return loginRole;
    }

    public void setLoginRole(int role) {

        if (currentState != LOGIN
                || (role != ROLE_CLIENT
                    && role != ROLE_CLERK
                    && role != ROLE_MANAGER)) {

            System.out.println(
                "Login role can only be set after a valid login."
            );
            return;
        }

        loginRole = role;
    }

    // CHANGE STATE USING TRANSITION MATRIX
    public void changeState(int event) {

        // Check whether event is valid
        if (event < TO_LOGIN || event > TO_WISHLIST) {
            System.out.println("Invalid transition event.");
            return;
        }

        // Do not transition after exit
        if (currentState == EXIT_STATE) {
            return;
        }

        // Find destination using matrix
        int destination = nextState[currentState][event];

        if (destination == INVALID) {
            System.out.println("Transition not allowed.");
            return;
        }

        // Handle exit
        if (destination == EXIT_STATE) {
            currentState = EXIT_STATE;
            System.out.println("Exiting Warehouse System.");
            return;
        }

        // Validate client ID when entering Client state
        if (destination == CLIENT) {

            if (clientID == null || clientID.trim().isEmpty()) {
                System.out.println(
                    "A valid Client ID is required."
                );
                return;
            }

            if (WarehouseBusiness.instance().getClient(clientID) == null) {
                System.out.println("Invalid Client ID.");
                return;
            }
        }

        // Validate client ID when entering Wishlist state
        if (destination == WISHLIST) {

            if (clientID == null || clientID.trim().isEmpty()) {
                System.out.println(
                    "A valid Client ID is required for wishlist operations."
                );
                return;
            }

            if (WarehouseBusiness.instance().getClient(clientID) == null) {
                System.out.println("Invalid Client ID.");
                return;
            }
        }

        // Preserve client ID between Client and Wishlist states.
        // Clear it only when leaving the client session entirely.
        if ((currentState == CLIENT || currentState == WISHLIST)
                && destination != CLIENT
                && destination != WISHLIST) {

            clientID = null;
        }

        // Update current state
        currentState = destination;

        // Clear session when returning to Login
        if (currentState == LOGIN) {
            clearSession();
        }
    }

    public void exitForEndOfInput() {

        if (currentState != EXIT_STATE) {
            currentState = EXIT_STATE;

            System.out.println(
                "\nInput closed. Exiting Warehouse System."
            );
        }
    }

    // LOGOUT
    public void logout() {

        // Wishlist operations return to Client Menu first.
        if (currentState == WISHLIST) {
            changeState(TO_CLIENT);
            return;
        }

        if (currentState == CLIENT) {

            if (loginRole == ROLE_CLIENT) {
                changeState(TO_LOGIN);

            } else if (loginRole == ROLE_CLERK
                    || loginRole == ROLE_MANAGER) {

                changeState(TO_CLERK);
            }

        } else if (currentState == CLERK) {

            if (loginRole == ROLE_MANAGER) {
                changeState(TO_MANAGER);

            } else if (loginRole == ROLE_CLERK) {
                changeState(TO_LOGIN);
            }

        } else if (currentState == MANAGER) {

            changeState(TO_LOGIN);
        }
    }

    // CLEAR SESSION
    public void clearSession() {
        clientID = null;
        loginRole = ROLE_NONE;
    }

    // PROCESS FSM
    public void process() {

        while (currentState != EXIT_STATE) {

            try {
                states[currentState].run();

            } catch (InputClosedException e) {
                exitForEndOfInput();
            }
        }
    }

    // MAIN METHOD
    public static void main(String[] args) {
        WarehouseContext.instance().process();
    }
}
