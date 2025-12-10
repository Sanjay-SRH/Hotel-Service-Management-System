package com.intellisix.service_request_management.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.intellisix.service_request_management.model.UserAccount;
import com.intellisix.service_request_management.model.Role;
import com.intellisix.service_request_management.model.Client;
import com.intellisix.service_request_management.model.Staff;
import com.intellisix.service_request_management.model.Admin;

/**
 * Authentication utility (file-backed). Kept mostly as your original implementation,
 * but moved into package: com.intellisix.service_request_management.stroage
 *
 * Place at:
 * src/main/java/com/intellisix/service_request_management/stroage/Authentication.java
 */
@Component
public class Authentication {
    private static final String ADMIN_FILE = "admin_credentials.json";
    private static final String STAFF_FILE = "staff_credentials.json";
    private static final String CUSTOMER_FILE = "customer_credentials.json";

    private static final List<UserAccount> accounts = new ArrayList<>();

    // Static initializer block: executed when the class is loaded
    static {
        initializeFiles();
        loadAllAccounts();
    }

    private static void initializeFiles() {
        try {
            Path adminPath = Path.of(ADMIN_FILE);
            if (!Files.exists(adminPath)) {
                String defaultAdmin = "[\n" +
                        "  {\n" +
                        "    \"id\": \"A001\",\n" +
                        "    \"name\": \"System Admin\",\n" + // ADDED: Name field for default admin
                        "    \"username\": \"admin\",\n" +
                        "    \"password\": \"admin123\",\n" +
                        "    \"role\": \"Admin\"\n" +
                        "  }\n" +
                        "]";
                Files.writeString(adminPath, defaultAdmin, StandardCharsets.UTF_8);
                System.out.println("Created " + ADMIN_FILE + " with default admin (username=admin, password=admin123, id=A001, name=System Admin)");
            }

            Path staffPath = Path.of(STAFF_FILE);
            if (!Files.exists(staffPath)) {
                Files.writeString(staffPath, "[]", StandardCharsets.UTF_8);
                System.out.println("Created empty " + STAFF_FILE);
            }

            Path customerPath = Path.of(CUSTOMER_FILE);
            if (!Files.exists(customerPath)) {
                Files.writeString(customerPath, "[]", StandardCharsets.UTF_8);
                System.out.println("Created empty " + CUSTOMER_FILE);
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize credential files: " + e.getMessage());
        }
    }

    private static void loadAllAccounts() {
        // Clear existing accounts before loading
        accounts.clear();
        loadAccountsFromFile(ADMIN_FILE, Role.Admin);
        loadAccountsFromFile(STAFF_FILE, Role.Staff);
        loadAccountsFromFile(CUSTOMER_FILE, Role.Customer);
    }

    private static void loadAccountsFromFile(String fileName, Role role) {
        try {
            Path p = Path.of(fileName);
            if (!Files.exists(p)) return;
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);

            String content = String.join("", lines);
            // Simple JSON array object splitting
            int firstBracket = content.indexOf('[');
            int lastBracket = content.lastIndexOf(']');
            if (firstBracket == -1 || lastBracket == -1 || lastBracket <= firstBracket) return;
            String arrayContent = content.substring(firstBracket + 1, lastBracket).trim();
            if (arrayContent.isEmpty()) return;

            // Basic splitting that might be fragile, but consistent with pattern
            String[] userBlocks = arrayContent.split("}\\s*,\\s*\\{");

            for (String block : userBlocks) {
                if (block.trim().isEmpty()) continue;
                String json = block.trim();
                if (!json.startsWith("{")) json = "{" + json;
                if (!json.endsWith("}")) json = json + "}";

                String id = extractFieldValue(json, "\"id\"");
                String username = extractFieldValue(json, "\"username\"");
                String password = extractFieldValue(json, "\"password\"");
                String name = extractFieldValue(json, "\"name\""); // NEW: Extract name
                // Ensure name is not null
                String finalName = (name != null && !"null".equalsIgnoreCase(name)) ? name : "";

                if (id != null && username != null && password != null) {
                    UserAccount newAccount = null;
                    if (role == Role.Customer) {
                        String roomNumber = extractFieldValue(json, "\"roomNumber\"");
                        // UPDATED: Pass name to Client constructor
                        newAccount = new Client(id, username, password, roomNumber, finalName);
                    } else if (role == Role.Staff) {
                        // UPDATED: Pass name to Staff constructor
                        newAccount = new Staff(id, username, password, finalName);
                    } else if (role == Role.Admin) {
                        // Admin constructor is 3-args, set name separately (via UserAccount)
                        newAccount = new Admin(id, username, password);
                        newAccount.setName(finalName);
                    }
                    if (newAccount != null) {
                        accounts.add(newAccount);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load accounts from " + fileName + ": " + e.getMessage());
        }
    }

    private static String extractFieldValue(String json, String fieldNameQuoted) {
        // Pattern 1: Finds quoted string values: "fieldName": "value"
        Pattern p = Pattern.compile(Pattern.quote(fieldNameQuoted) + ":\\s*\"(.*?)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        // Pattern 2: Finds unquoted values (e.g., numbers, boolean, or 'null'): "fieldName": value
        // Also handles fields like "roomNumber":null or "roomNumber":101 if they were stored unquoted
        p = Pattern.compile(Pattern.quote(fieldNameQuoted) + ":\\s*([^,}\\n]+)");
        m = p.matcher(json);
        if (m.find()) {
            String value = m.group(1).trim();
            // Convert "null" literal to Java null, and remove any quotes if they slipped through
            return "null".equalsIgnoreCase(value) ? null : value.replaceAll("\"", "");
        }

        return null;
    }

    private static String formatAccountToJson(UserAccount account) {
        String nameValue = account.getName() != null ? account.getName() : "";

        String json = "  {\n" +
                "    \"id\": \"" + account.getId() + "\",\n" +
                "    \"name\": \"" + escapeJson(nameValue) + "\",\n" + // ADDED: Name field
                "    \"username\": \"" + escapeJson(account.getUsername()) + "\",\n" +
                "    \"password\": \"" + escapeJson(account.getPassword()) + "\",\n" +
                "    \"role\": \"" + account.getRole().name() + "\"";

        // Include roomNumber field for Client accounts
        if (account.getRole() == Role.Customer) {
            Client client = (Client) account;
            String roomNumber = client.getRoomNumber() != null ? client.getRoomNumber() : "";
            json += ",\n    \"roomNumber\": \"" + escapeJson(roomNumber) + "\"";
        }

        json += "\n  }";
        return json;
    }

    /**
     * Persists a new account to the in-memory list and the corresponding JSON file.
     * @param account The user account to persist.
     * @return true if successful, false otherwise.
     */
    public static boolean persistNewAccount(UserAccount account) {
        try {
            String fileName = getFileNameForRole(account.getRole());
            accounts.add(account);

            // Filter the master list to only get accounts of the current role for saving
            List<UserAccount> accountsToSave = accounts.stream()
                    .filter(a -> a.getRole() == account.getRole())
                    .collect(Collectors.toList());

            String jsonArray = accountsToSave.stream()
                    .map(Authentication::formatAccountToJson)
                    .collect(Collectors.joining(",\n", "[\n", "\n]"));

            Files.writeString(Path.of(fileName), jsonArray, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to persist new account: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing account in the in-memory list and the corresponding JSON file.
     * This is required by UserService.updateAccount().
     * @param account The user account with updated details.
     */
    public static void updateAccount(UserAccount account) {
        // Since the accounts list holds references, we just need to find the file and save all.
        try {
            String fileName = getFileNameForRole(account.getRole());

            // Reload all accounts just to ensure the list is clean before filtering
            // (Alternatively, remove the old instance before adding the new one, but reloading is simpler/safer for file persistence)
            loadAllAccounts();

            // Filter the master list to only get accounts of the current role for saving
            List<UserAccount> accountsToSave = accounts.stream()
                    .filter(a -> a.getRole() == account.getRole())
                    .collect(Collectors.toList());

            String jsonArray = accountsToSave.stream()
                    .map(Authentication::formatAccountToJson)
                    .collect(Collectors.joining(",\n", "[\n", "\n]"));

            Files.writeString(Path.of(fileName), jsonArray, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to update account: " + e.getMessage());
        }
    }

    public static Optional<Client> findClientByRoomNumber(String roomNumber) {
        if(roomNumber == null) return Optional.empty();
        return accounts.stream()
                .filter(a -> a instanceof Client)
                .map(a -> (Client) a)
                .filter(client -> client.getRoomNumber().equalsIgnoreCase(roomNumber))
                .findFirst();
    }


    private static String getFileNameForRole(Role role) {
        if (role == Role.Admin) {
            return ADMIN_FILE;
        } else if (role == Role.Staff) {
            return STAFF_FILE;
        } else if (role == Role.Customer) {
            return CUSTOMER_FILE;
        } else {
            return null;
        }
    }

    private static String escapeJson(String s) {
        return s == null ? "" : s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    public static Optional<UserAccount> findByUsername(String username) {
        return accounts.stream().filter(a -> a.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    // NEW METHOD: Find by ID (Essential for ServiceRequestService lookup)
    public static Optional<UserAccount> findById(String id) {
        return accounts.stream().filter(a -> a.getId().equalsIgnoreCase(id)).findFirst();
    }

    public static synchronized String generateIdForRolePrefix(String prefix) {
        int max = 0;
        Pattern p = Pattern.compile("^" + Pattern.quote(prefix) + "(\\d{3})$");
        for (UserAccount a : accounts) {
            String id = a.getId();
            if (id == null) continue;
            Matcher m = p.matcher(id);
            if (m.matches()) {
                int val = Integer.parseInt(m.group(1));
                if (val > max) max = val;
            }
        }
        int next = max + 1;
        if (next > 999) next = 999;
        return String.format("%s%03d", prefix, next);
    }

    public static boolean staffExists(String staffId) {
        return accounts.stream()
                .filter(a -> a.getRole() == Role.Staff)
                .anyMatch(a -> a.getId().equals(staffId));
    }
}