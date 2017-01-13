package com.octo.zdd_java_sql.api.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Person {

    private long id;

    private String name;

    public Person() {
    }

    public Person(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public long getId() {
        return id;
    }
}
