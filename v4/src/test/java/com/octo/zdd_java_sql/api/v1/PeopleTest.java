package com.octo.zdd_java_sql.api.v1;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class PeopleTest extends AbstractRepresentationTest {

    @Test
    public void serializesToJSON() throws Exception {
        final People people = createPeople();
        final String expected = MAPPER.writeValueAsString(readPeopleFixture());
        assertThat(MAPPER.writeValueAsString(people)).isEqualTo(expected);
    }

    private People readPeopleFixture() throws java.io.IOException {
        return MAPPER.readValue(fixture("fixtures/v1/people.json"), People.class);
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        final People people = createPeople();
        People actual = readPeopleFixture();
        assertThat(actual.getPeople().size()).isEqualTo(1);
        Person actualPerson = actual.getPeople().get(0);
        Person peoplePerson = people.getPeople().get(0);
        assertThat(actualPerson.getId()).isEqualTo(peoplePerson.getId());
        assertThat(actualPerson.getName()).isEqualTo(peoplePerson.getName());
        assertThat(actualPerson.getAddress()).isEqualTo(peoplePerson.getAddress());
    }

    private People createPeople() {
        People people = new People();
        List<Person> peopleContent = new ArrayList<>();
        peopleContent.add(new Person(1, "John Doe", "Near the old shore"));
        people.setPeople(peopleContent);
        return people;
    }


}
