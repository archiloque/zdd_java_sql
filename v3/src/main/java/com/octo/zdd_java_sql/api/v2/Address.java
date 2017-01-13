package com.octo.zdd_java_sql.api.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Address {

    private long id;

    private String address;

    private long personId;

    public Address() {
    }

    public Address(long id, String address, long personId) {
        this.id = id;
        this.address = address;
        this.personId = personId;
    }

    @JsonProperty
    public String getAddress() {
        return address;
    }

    @JsonProperty
    public long getId() {
        return id;
    }

    @JsonProperty
    public long getPersonId() {
        return personId;
    }
}
