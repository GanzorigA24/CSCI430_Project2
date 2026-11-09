
import java.util.List;

// Provides account actions and access to wishlist operations for the active client.
public class ClientMenuState extends WarehouseState {

    private static ClientMenuState instance;

    private WarehouseBusiness warehouse;

    private ClientMenuState() {
        warehouse = WarehouseBusiness.instance();
    }

    // SINGLETON INSTANCE
    public static ClientMenuState instance() {
        if (instance == null) {
            instance = new ClientMenuState();
        }
        return instance;
    }

    // GET CURRENT CLIENT
    private Client getCurrentClient() {

        String clientID =
            WarehouseContext.instance().getClientID();

        if (clientID == null) {
            System.out.println("No client selected.");
            return null;
        }

        Client client = warehouse.getClient(clientID);

        if (client == null) {
            System.out.println("Client not found.");
        }

        return client;
    }

    // SHOW CLIENT DETAILS
    private void showClientDetails() {

        Client client = getCurrentClient();

        if (client == null) {
            return;
        }

        System.out.println("\n===== CLIENT DETAILS =====");
        System.out.println("ID: " + client.getID());
        System.out.println("Name: " + client.getName());
        System.out.println("Address: " + client.getAddress());
        System.out.println("Balance: $" + client.getBalance());
    }

    // SHOW PRODUCTS
    private void showProducts() {
        warehouse.displayProducts();
    }

    // SHOW TRANSACTIONS
    private void showTransactions() {

        Client client = getCurrentClient();

        if (client == null) {
            return;
        }

        System.out.println("\n===== CLIENT TRANSACTIONS =====");
        warehouse.viewClientInvoices(client);
    }

    // OPEN WISHLIST OPERATIONS
    private void openWishlistOperations() {

        Client client = getCurrentClient();

        if (client == null) {
            return;
        }

        WarehouseContext.instance().changeState(
            WarehouseContext.TO_WISHLIST
        );
    }

    // LOGOUT
    private void logout() {

        WarehouseContext.instance().logout();

        System.out.println("Logged out of Client Menu.");
    }

    // CLIENT MENU
    @Override
    String title() {
        return "Client Menu";
    }

    @Override
    List<Action> actions() {
        return List.of(
            new Action("Show Client Details", this::showClientDetails),
            new Action("Show Products", this::showProducts),
            new Action("Show Transactions", this::showTransactions),
            new Action("Wishlist Operations", this::openWishlistOperations),
            new Action("Logout", this::logout)
        );
    }
}
