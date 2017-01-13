package com.octo.zdd_java_sql.api.v1;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Person {

    private long id;

    private String name;

    private String address;

    public Person() {
    }

    public Person(long id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public String getAddress() {
        return address;
    }

    @JsonProperty
    public long getId() {
        return id;
    }
}
