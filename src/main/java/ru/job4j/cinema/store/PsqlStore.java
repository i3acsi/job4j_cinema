package ru.job4j.cinema.store;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.cinema.dto.PlacesDto;
import ru.job4j.cinema.dto.RowDto;
import ru.job4j.cinema.model.Place;
import ru.job4j.cinema.model.Role;
import ru.job4j.cinema.model.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PsqlStore implements Store {
    private static final Logger log = LoggerFactory.getLogger(Store.class);
    private final BasicDataSource pool = new BasicDataSource();
    private final Map<Integer, //hall
            Map<Integer, //row
                    Map<Integer, //col
                            Object[]>>> selectionMap = new ConcurrentHashMap<>(); // selected , bought, account_id

    private PsqlStore() {
        Properties cfg = new Properties();
        try (BufferedReader io = new BufferedReader(
                new FileReader("dbCinema.properties")
        )) {
            cfg.load(io);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        pool.setDriverClassName(cfg.getProperty("jdbc.driver"));
        pool.setUrl(cfg.getProperty("jdbc.url"));
        pool.setUsername(cfg.getProperty("jdbc.username"));
        pool.setPassword(cfg.getProperty("jdbc.password"));
        pool.setMinIdle(5);
        pool.setMaxIdle(10);
        pool.setMaxOpenPreparedStatements(100);
        updateMap(1);
    }

    private static final class Lazy {
        private static final Store INST = new PsqlStore();
    }

    public static Store instOf() {
        return Lazy.INST;
    }


    @Override
    public User save(User user) {
        if (user.getId() == 0) {
            return create(user);
        } else {
            return update(user);
        }
    }

    private User create(User user) {
        List<Long> roleIds;
        long userId;
        String name = user.getName();
        String email = user.getEmail();
        String password = user.getPassword();
        try (Connection cn = pool.getConnection();
        ) {
            PreparedStatement createAccount = cn.prepareStatement("INSERT INTO accounts (name, email, password) VALUES (?,?,?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            createAccount.setString(1, name);
            createAccount.setString(2, email);
            createAccount.setString(3, password);
            createAccount.execute();
            ResultSet resultSet = createAccount.getGeneratedKeys();
            if (resultSet.next())
                user.setId(resultSet.getLong(1));
            userId = user.getId();

            createRolesInDB(user, cn);
            roleIds = getRoleIds(user, cn);
            createAccountRolesInDB(userId, roleIds, cn);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return user;
    }

    private User update(User user) {
        List<Long> roleIds;
        long userId = user.getId();
        String name = user.getName();
        String email = user.getEmail();
        String password = user.getPassword();
        try (Connection cn = pool.getConnection()) {
            PreparedStatement updateUser = cn.prepareStatement("UPDATE accounts SET name = ?, email = ?, password = ? WHERE id = ?");
            updateUser.setString(1, name);
            updateUser.setString(2, email);
            updateUser.setString(3, password);
            updateUser.setLong(4, userId);
            updateUser.executeUpdate();

            deleteAccountRolesInDB(userId, cn);
            createRolesInDB(user, cn);
            roleIds = getRoleIds(user, cn);
            createAccountRolesInDB(userId, roleIds, cn);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return user;
    }

    @Override
    public Collection<User> findAllUsers() {
        List<User> users = new ArrayList<>();
        Map<Long, Set<Role>> rolesMap = new HashMap<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement selectAccounts = cn.prepareStatement("SELECT * FROM accounts");
             PreparedStatement selectRoles = cn.prepareStatement("SELECT a_r.account_id, roles.name from roles INNER JOIN account_roles AS a_r ON roles.id = a_r.role_id")
        ) {
            try (ResultSet rolesSet = selectRoles.executeQuery();
                 ResultSet accountsSet = selectAccounts.executeQuery()) {
                while (rolesSet.next()) {
                    long userId = rolesSet.getLong(1);
                    Role role = Role.valueOf(rolesSet.getString(2));
                    rolesMap.putIfAbsent(userId, new HashSet<>());
                    rolesMap.compute(userId, (k, v) -> {
                        v.add(role);
                        return v;
                    });
                }
                while (accountsSet.next()) {
                    long id = accountsSet.getLong("id");
                    String name = accountsSet.getString("name");
                    String email = accountsSet.getString("email");
                    String password = accountsSet.getString("password");
                    Set<Role> roles = rolesMap.get(id);
                    users.add(new User(id, name, email, roles, password));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return users;
    }

    @Override
    public User findUserById(long id) {
        User result = null;
        Set<Role> roles = new HashSet<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement selectAccount = cn.prepareStatement("SELECT name, email, password FROM accounts WHERE id = ?");
             PreparedStatement selectRoles = cn.prepareStatement("SELECT roles.name from roles INNER JOIN account_roles AS a_r ON roles.id = a_r.role_id WHERE a_r.account_id = ?")
        ) {
            selectAccount.setLong(1, id);
            selectRoles.setLong(1, id);
            selectAccount.executeQuery();
            selectRoles.executeQuery();
            try (ResultSet rolesSet = selectRoles.executeQuery();
                 ResultSet accountsSet = selectAccount.executeQuery()) {
                while (rolesSet.next()) {
                    Role role = Role.valueOf(rolesSet.getString(1));
                    roles.add(role);
                }
                if (accountsSet.next()) {
                    String name = accountsSet.getString("name");
                    String email = accountsSet.getString("email");
                    String password = accountsSet.getString("password");
                    result = new User(id, name, email, roles, password);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public User findUserByEmail(String email) {
        User result = null;
        long id = 0;
        String name = null, password = null;
        Set<Role> roles = new HashSet<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement selectAccount = cn.prepareStatement("SELECT id, name, password FROM accounts WHERE email LIKE ?");
             PreparedStatement selectRoles = cn.prepareStatement("SELECT roles.name from roles INNER JOIN account_roles AS a_r ON roles.id = a_r.role_id WHERE a_r.account_id = ?")
        ) {
            selectAccount.setString(1, email);
            log.info(selectAccount.toString());
            selectAccount.executeQuery();
            try (ResultSet accountsSet = selectAccount.executeQuery()) {
                if (accountsSet.next()) {
                    id = accountsSet.getLong("id");
                    name = accountsSet.getString("name");
                    password = accountsSet.getString("password");
                    log.info("ID: " + id + ", NAME: " + name + ", PASSWORD:" + password);
                }
            }
            if (id >= 0) {
                selectRoles.setLong(1, id);
                try (ResultSet rolesSet = selectRoles.executeQuery()) {
                    log.info(selectRoles.toString());
                    while (rolesSet.next()) {
                        Role role = Role.valueOf(rolesSet.getString(1));
                        log.info(role.name());
                        roles.add(role);
                    }
                }
                result = new User(id, name, email, roles, password);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void delete(User user) {
        long id = user.getId();
        try (Connection cn = pool.getConnection();
             PreparedStatement deleteAccount = cn.prepareStatement("DELETE FROM accounts WHERE id = ?");
             PreparedStatement deleteAccountRoles = cn.prepareStatement("DELETE FROM account_roles WHERE account_id = ?")) {
            deleteAccount.setLong(1, id);
            deleteAccountRoles.setLong(1, id);
            deleteAccount.executeQuery();
            deleteAccountRoles.executeQuery();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Collection<Place> findAllPlacesInHall(int hall) {
        List<Place> places = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement selectPlacesInHall = cn.prepareStatement("SELECT row, col, account_id FROM places WHERE hall = ?")) {
            selectPlacesInHall.setInt(1, hall);
            ResultSet selectedPlaces = selectPlacesInHall.executeQuery();
            while (selectedPlaces.next()) {
                int row = selectedPlaces.getInt("row");
                int col = selectedPlaces.getInt("col");
                Long accountId;
                try {
                    accountId = Long.parseLong(selectedPlaces.getString("account_id"));
                } catch (Exception e) {
                    accountId = null;
                }
                System.out.println(accountId);
                places.add(new Place(hall, row, col, accountId));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return places;
    }

    @Override
    public Collection<PlacesDto> findAllPlacesDtoInHall(int hall, long userId) {
        Map<Integer, List<RowDto>> places = new HashMap<>();
        selectionMap.get(hall).forEach((row, colMap) -> {
            colMap.forEach((col, place) -> {
                places.compute(row, (k, v) -> {
                    if (v == null)
                        v = new ArrayList<>();
                    boolean selected = (boolean) place[0] && (long) place[2] == userId;
                    v.add(new RowDto(col, (boolean) place[1], selected));
                    return v;
                });
            });
        });
        List<PlacesDto> result = new ArrayList<>();
        places.forEach((k, v) -> {
            result.add(new PlacesDto(k, v));
        });
        return result;
    }

    @Override
    public boolean processPlace(int hall, int row, int col, boolean doSelect, long userId) {
        final boolean[] result = new boolean[1];
        selectionMap.computeIfPresent(hall, (hallNo, hallMap) -> {
            hallMap.computeIfPresent(row, (rowNo, rowMap) -> {
                rowMap.computeIfPresent(col, (colNo, place) -> {
                    if (doSelect) {
                        if (!(boolean) place[0] && !(boolean) place[1]) {
                            place[0] = true;
                            place[1] = false;
                            place[2] = userId;
                            result[0] = true;
                        }
                    } else {
                        if ((long) place[2] == userId && !(boolean) place[1]) {
                            place[0] = false;
                            place[1] = false;
                            place[2] = 0L;
                            result[0] = true;
                        }
                    }
                    return place;
                });
                return rowMap;
            });
            return hallMap;
        });

        return result[0];
    }

    private List<Long> getRoleIds(User user, Connection cn) throws SQLException {
        List<Long> result = new ArrayList<>();
        for (Role role : user.getRoles()) {
            PreparedStatement selectRole = cn.prepareStatement("SELECT id FROM roles WHERE name LIKE ?");
            selectRole.setString(1, role.name());
            selectRole.execute();
            ResultSet id = selectRole.getResultSet();
            if (id.next())
                result.add(id.getLong(1));
        }
        return result;
    }

    private void createRolesInDB(User user, Connection cn) throws SQLException {
        Set<Role> existedRoles = new HashSet<>();
        PreparedStatement selectRoles = cn.prepareStatement("SELECT name FROM roles");
        ResultSet selectedRoles = selectRoles.executeQuery();
        while (selectedRoles.next()) {
            Role role = Role.valueOf(selectedRoles.getString("name"));
            existedRoles.add(role);
        }
        if (!existedRoles.containsAll(user.getRoles())) {
            for (Role role : user.getRoles()) {
                PreparedStatement createRole = cn.prepareStatement("INSERT INTO roles (name) VALUES (?)");
                createRole.setString(1, role.name());
                createRole.execute();
            }
        }

    }

    private void createAccountRolesInDB(long userId, List<Long> roleIds, Connection cn) throws SQLException {
        for (Long roleId : roleIds) {
            PreparedStatement createAccountRoles = cn.prepareStatement("INSERT INTO account_roles (account_id, role_id) VALUES (?,?)");
            createAccountRoles.setLong(1, userId);
            createAccountRoles.setLong(2, roleId);
            createAccountRoles.execute();
        }
    }

    private void deleteAccountRolesInDB(long userId, Connection cn) throws SQLException {
        PreparedStatement createAccountRoles = cn.prepareStatement("DELETE FROM account_roles WHERE account_id = ?");
        createAccountRoles.setLong(1, userId);
        createAccountRoles.execute();
    }

    private void updateMap(int hall) {
        try (Connection cn = pool.getConnection();
             PreparedStatement selectPlacesInHall = cn.prepareStatement("SELECT row, col, account_id FROM places WHERE hall = ?")) {
            selectPlacesInHall.setInt(1, hall);
            ResultSet selectedPlaces = selectPlacesInHall.executeQuery();
            while (selectedPlaces.next()) {
                int row = selectedPlaces.getInt("row");
                int col = selectedPlaces.getInt("col");
                Long accountId = selectedPlaces.getLong("account_id");
                selectionMap.compute(hall, (hallNo, hallMap) -> {
                    if (hallMap == null)
                        hallMap = new ConcurrentHashMap<>();
                    hallMap.compute(row, (rowNo, rowMap) -> {
                        if (rowMap == null)
                            rowMap = new ConcurrentHashMap<>();
                        rowMap.compute(col, (colNo, place) -> {
                            if (place == null)
                                place = new Object[3];
                            if (accountId > 0) {
                                // если accountId > 0 в БД значит место куплено если меньше, значит оставляем данне в мапе как есть
                                place[0] = true;
                                place[1] = true;
                                place[2] = accountId;
                            } else {
                                if (place[0] == null) { // если не инициализированно
                                    place[0] = false;
                                    place[1] = false;
                                    place[2] = 0L;
                                }
                            }
                            return place;
                        });
                        return rowMap;
                    });
                    return hallMap;
                });

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}