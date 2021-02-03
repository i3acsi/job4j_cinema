package ru.job4j.cinema.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.cinema.store.Store;

public class Mapper {
    private static final Logger log = LoggerFactory.getLogger(Store.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Object obj) {
        String result = "{}";
        try {
            result = MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }
}
