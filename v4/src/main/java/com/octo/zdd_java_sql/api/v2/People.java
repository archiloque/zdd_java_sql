package com.octo.zdd_java_sql.api.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

public class People {

    private List<Person> people;

    public People() {
    }

    @JsonProperty
    @NotNull
    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(@NotNull List<Person> people) {
        this.people = people;
    }
}
