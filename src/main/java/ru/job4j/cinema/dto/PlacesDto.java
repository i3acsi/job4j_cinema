package ru.job4j.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class PlacesDto {
    private int row;
    private List<RowDto> places;
}

