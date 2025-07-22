package com.example.jdbc;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class Person {
    private Long id;
    private String firstName;
    private String lastName;
}
