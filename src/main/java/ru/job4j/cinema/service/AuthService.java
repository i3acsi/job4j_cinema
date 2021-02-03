package ru.job4j.cinema.service;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import ru.job4j.cinema.model.Role;
import ru.job4j.cinema.model.User;
import ru.job4j.cinema.store.PsqlStore;
import ru.job4j.cinema.store.Store;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class AuthService {
    private static final String SALT = "DEFAULT_SALT";
    private static Store store = PsqlStore.instOf();
    private static HashFunction hashFunction = Hashing.sha256();
    private static Charset charset = StandardCharsets.UTF_8;

    private static String encodePassword(String password) {
        return hashFunction.hashString(password + SALT, charset).toString();
    }

    public static boolean checkAndSetCredentials(String email, String password, HttpServletRequest req) {
        User user = store.findUserByEmail(email);
        boolean result = false;
        if (user != null) {
            String passwordFromDB = user.getPassword();
            String passwordHash = encodePassword(password);
            if (passwordFromDB.equals(passwordHash)) {
                req.getSession().setAttribute("user", user);
                result = true;
            }
        }
        return result;
    }

    public static boolean regAccount(String name, String email, String password) {
        String encodedPwd = encodePassword(password);
        Set<Role> roles = Set.of(Role.USER);
        User user = store.save(new User(0, name, email, roles, encodedPwd));
        return user.getId() > 0;
    }
}
