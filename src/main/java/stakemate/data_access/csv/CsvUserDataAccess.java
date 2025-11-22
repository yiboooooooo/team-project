package stakemate.data_access.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import stakemate.entity.User;
import stakemate.use_case.login.LoginUserDataAccessInterface;
import stakemate.use_case.signup.SignupUserDataAccessInterface;

public class CsvUserDataAccess implements SignupUserDataAccessInterface, LoginUserDataAccessInterface {
    private final File csvFile;
    private final Map<String, User> users = new HashMap<>();

    public CsvUserDataAccess(final String path) {
        this.csvFile = new File(path);
        load();
    }

    private void load() {
        if (!csvFile.exists()) {
            return;
        }
        try (final BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                final String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue;
                }
                final String username = parts[0];
                final String password = parts[1];
                final int balance = Integer.parseInt(parts[2]);
                users.put(username, new User(username, password, balance));
            }
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveToFile() {
        try (final BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {
            for (final User user : users.values()) {
                bw.write(user.getUsername() + "," + user.getPassword() + "," + user.getBalance());
                bw.newLine();
            }
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Signup interface
    @Override
    public boolean existsByUsername(final String username) {
        return users.containsKey(username);
    }

    @Override
    public void save(final User user) {
        users.put(user.getUsername(), user);
        saveToFile();
    }

    // Login interface
    @Override
    public User getByUsername(final String username) {
        return users.get(username);
    }
}
