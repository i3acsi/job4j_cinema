package ru.job4j.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Set;

@Data
@AllArgsConstructor
@ToString
public class User {
    private long id;
    private String name;
    private String email;
    private Set<Role> roles;
    private String password;
}
