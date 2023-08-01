package main;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Product {
    private int id;
    private String name;
    private double price;

    public Product(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name + " - $" + price;
    }
}

class User {
    private int id;
    private String username;
    private String password;

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

public class OnlineShoppingApp {
    private static Connection connection;
    private static List<Product> products = new ArrayList<>();
    private static List<Product> cart = new ArrayList<>();
    private static List<User> users = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static boolean isLoggedIn = false;
    private static User currentUser = null;

    public static void main(String[] args) {
        connectToDatabase();
        initializeProducts();

        while (!isLoggedIn) {
            showLoginMenu();
        }

        showMainMenu();
        closeDatabaseConnection();
    }

    private static void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/online_shopping";
            String username = "root";
            String password = "";
            connection = DriverManager.getConnection(url, username, password);
            createProductTableIfNotExists();
            createUserTableIfNotExists();
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
    }

    private static void createProductTableIfNotExists() {
        try (Statement statement = connection.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS products (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "price DOUBLE NOT NULL)";
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error creating products table: " + e.getMessage());
        }
    }

    private static void createUserTableIfNotExists() {
        try (Statement statement = connection.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(255) NOT NULL UNIQUE," +
                    "password VARCHAR(255) NOT NULL)";
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error creating users table: " + e.getMessage());
        }
    }

    private static void initializeProducts() {
        try (Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM products";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("price");
                products.add(new Product(id, name, price));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching products from the database: " + e.getMessage());
        }
    }

    private static void signUp() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.executeUpdate();
            System.out.println("User registered successfully!");
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
        }
    }

    private static boolean login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                currentUser = new User(resultSet.getInt("id"), resultSet.getString("username"), resultSet.getString("password"));
                return true;
            } else {
                System.out.println("Invalid username or password. Please try again.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
            return false;
        }
    }

    private static void showLoginMenu() {
        System.out.println("\n=== Login Menu ===");
        System.out.println("1. Sign Up");
        System.out.println("2. Login");
        System.out.println("3. Exit");

        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Clear the newline character

        switch (choice) {
            case 1:
                signUp();
                break;
            case 2:
                if (login()) {
                    isLoggedIn = true;
                    System.out.println("Login successful!");
                }
                break;
            case 3:
                System.out.println("Thank you for using our Online Shopping App!");
                closeDatabaseConnection();
                System.exit(0);
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. View Products");
            System.out.println("2. Add Product to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Checkout");
            System.out.println("5. Exit");

            if (isLoggedIn) {
                System.out.println("6. Logout");
            }

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear the newline character

            switch (choice) {
                case 1:
                    viewProducts();
                    break;
                case 2:
                    if (isLoggedIn) {
                        addProductToCart();
                    } else {
                        System.out.println("Please log in or sign up to access this feature.");
                    }
                    break;
                case 3:
                    if (isLoggedIn) {
                        viewCart();
                    } else {
                        System.out.println("Please log in or sign up to access this feature.");
                    }
                    break;
                case 4:
                    if (isLoggedIn) {
                        checkout();
                    } else {
                        System.out.println("Please log in or sign up to access this feature.");
                    }
                    break;
                case 5:
                    System.out.println("Thank you for using our Online Shopping App!");
                    closeDatabaseConnection();
                    System.exit(0);
                case 6:
                    if (isLoggedIn) {
                        logout();
                        System.out.println("Logged out successfully!");
                        return; // Return to the login menu after logging out
                    } else {
                        System.out.println("Invalid choice. Please try again.");
                    }
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewProducts() {
        System.out.println("\n=== Available Products ===");
        for (int i = 0; i < products.size(); i++) {
            System.out.println((i + 1) + ". " + products.get(i));
        }
    }

    private static void addProductToCart() {
        System.out.print("Enter the product number to add to cart: ");
        int productNumber = scanner.nextInt();
        scanner.nextLine(); // Clear the newline character

        if (productNumber >= 1 && productNumber <= products.size()) {
            Product selectedProduct = products.get(productNumber - 1);
            cart.add(selectedProduct);
            System.out.println(selectedProduct.getName() + " added to the cart.");
        } else {
            System.out.println("Invalid product number. Please try again.");
        }
    }

    private static void viewCart() {
        System.out.println("\n=== Shopping Cart ===");
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
        } else {
            for (Product item : cart) {
                System.out.println("- " + item);
            }
        }
    }

    private static void checkout() {
        System.out.println("\n=== Checkout ===");
        double totalPrice = 0;
        for (Product item : cart) {
            totalPrice += item.getPrice();
        }
        System.out.println("Total Price: $" + totalPrice);
        cart.clear();
    }

    private static void logout() {
        isLoggedIn = false;
        currentUser = null;
        cart.clear();
    }

    private static void closeDatabaseConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
