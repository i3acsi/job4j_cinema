package ru.job4j.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RowDto {
    private int placeNo;
    private boolean bought;
    private boolean busy;
    private boolean selected;
}
