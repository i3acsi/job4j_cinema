package ru.job4j.cinema.store;

import ru.job4j.cinema.dto.PlacesDto;
import ru.job4j.cinema.model.Place;
import ru.job4j.cinema.model.User;

import java.util.Collection;
import java.util.List;

public interface Store {

    User save(User user);

    Collection<User> findAllUsers();

    User findUserById(long id);

    User findUserByEmail(String email);

    void delete(User user);

    Collection<Place> findAllPlacesInHall(int hall);

    Collection<PlacesDto> findAllPlacesDtoInHall(int hall, long userId);

    boolean processPlace(int hall, int row, int col, boolean doSelect, long userId);

    boolean doBuy(int hall, long userId);

    List<int[]> makeOrder(int hall, long userId);
}
