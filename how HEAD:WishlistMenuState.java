
import java.util.List;

// Handles all wishlist operations for the active client.
public class WishlistMenuState extends WarehouseState {

    private static WishlistMenuState instance;

    private WarehouseBusiness warehouse;

    // PRIVATE CONSTRUCTOR
    private WishlistMenuState() {
        warehouse = WarehouseBusiness.instance();
    }

    // SINGLETON INSTANCE
    public static WishlistMenuState instance() {

        if (instance == null) {
            instance = new WishlistMenuState();
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

    // DISPLAY WISHLIST
    private void showWishlist() {

        Client client = getCurrentClient();

        if (client == null) {
            return;
        }

        System.out.println("\n===== DISPLAY WISHLIST =====");

        warehouse.displayWishlist(client);
    }

    // ADD PRODUCT TO WISHLIST
    private void addToWishlist() {

        Client client = getCurrentClient();

        if (client == null) {
            return;
        }

        System.out.println("\n===== ADD PRODUCT TO WISHLIST =====");

        String productID =
            WarehouseContext.prompt("Enter Product ID: ").trim();

        Product product =
            warehouse.getInventory().findProductByID(productID);

        if (product == null) {
            System.out.println("Product not found.");
            return;
        }

        int quantity;

        try {
            quantity = Integer.parseInt(
                WarehouseContext.prompt("Enter quantity: ").trim()
            );

        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return;
        }

        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }

        boolean added = warehouse.addProductWishlist(
            client,
            product,
            quantity
        );

        if (added) {
            System.out.println("Product added to wishlist.");
        } else {
            System.out.println("Unable to add product to wishlist.");
        }
    }

    // REMOVE PRODUCT FROM WISHLIST
    private void removeFromWishlist() {

        Client client = getCurrentClient();

        if (client == null) {
            return;
        }

        System.out.println("\n===== REMOVE PRODUCT FROM WISHLIST =====");

        String productID =
            WarehouseContext.prompt("Enter Product ID to remove: ").trim();

        boolean removed = warehouse.removeProductWishlist(
            client,
            productID
        );

        if (removed) {
            System.out.println("Product removed from wishlist.");
        } else {
            System.out.println("Product was not found in the wishlist.");
        }
    }

    // MODIFY PRODUCT QUANTITY
    private void modifyWishlistQuantity() {

        Client client = getCurrentClient();

        if (client == null) {
            return;
        }

        System.out.println("\n===== MODIFY WISHLIST QUANTITY =====");

        String productID =
            WarehouseContext.prompt("Enter Product ID: ").trim();

        int quantity;

        try {
            quantity = Integer.parseInt(
                WarehouseContext.prompt("Enter new quantity: ").trim()
            );

        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return;
        }

        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }

        boolean updated = warehouse.updateWishlistQuantity(
            client,
            productID,
            quantity
        );

        if (updated) {
            System.out.println("Wishlist quantity updated successfully.");
        } else {
            System.out.println("Product was not found in the wishlist.");
        }
    }

    // PLACE ORDER
    private void placeOrder() {

        Client client = getCurrentClient();

        if (client == null) {
            return;
        }

        System.out.println("\n===== PLACE ORDER =====");

        warehouse.processClientOrder(client);
    }

    // RETURN TO CLIENT MENU
    private void returnToClientMenu() {

        WarehouseContext.instance().changeState(
            WarehouseContext.TO_CLIENT
        );
    }

    // WISHLIST MENU
    @Override
    String title() {
        return "Wishlist Operations";
    }

    @Override
    List<Action> actions() {

        return List.of(
            new Action("Display Wishlist", this::showWishlist),
            new Action("Add Product", this::addToWishlist),
            new Action("Remove Product", this::removeFromWishlist),
            new Action("Modify Product Quantity", this::modifyWishlistQuantity),
            new Action("Place Order", this::placeOrder),
            new Action("Return to Client Menu", this::returnToClientMenu)
        );
    }
}