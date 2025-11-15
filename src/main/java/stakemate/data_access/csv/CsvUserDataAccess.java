package stakemate.data_access.csv;

import stakemate.entity.User;
import stakemate.use_case.login.LoginUserDataAccessInterface;
import stakemate.use_case.signup.SignupUserDataAccessInterface;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CsvUserDataAccess implements SignupUserDataAccessInterface, LoginUserDataAccessInterface {
    private final File csvFile;
    private final Map<String, User> users = new HashMap<>();

    public CsvUserDataAccess(String path) {
        this.csvFile = new File(path);
        load();
    }

    private void load() {
        if (!csvFile.exists()) {
            return; // no users yet
        }
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                String username = parts[0];
                String password = parts[1];
                int balance = Integer.parseInt(parts[2]);
                users.put(username, new User(username, password, balance));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {
            for (User user : users.values()) {
                bw.write(user.getUsername() + "," + user.getPassword() + "," + user.getBalance());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Signup interface
    @Override
    public boolean existsByUsername(String username) {
        return users.containsKey(username);
    }

    @Override
    public void save(User user) {
        users.put(user.getUsername(), user);
        saveToFile();
    }

    // Login interface
    @Override
    public User getByUsername(String username) {
        return users.get(username);
    }
}
