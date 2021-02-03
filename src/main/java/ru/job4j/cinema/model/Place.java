package ru.job4j.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Place {
    private int hall;
    private int row;
    private int col;
    private Long accountId;
}
