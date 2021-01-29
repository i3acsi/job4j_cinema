package ru.job4j.cinema.store;

import ru.job4j.cinema.model.User;

import java.util.Collection;

public interface Store {

    User save(User user);

    Collection<User> findAllUsers();

    User findUserById(long id);

    User findUserByEmail(String email);

    void delete(User user);
}
