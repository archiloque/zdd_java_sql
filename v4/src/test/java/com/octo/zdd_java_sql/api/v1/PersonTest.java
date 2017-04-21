package com.octo.zdd_java_sql.api.v1;

import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class PersonTest extends AbstractRepresentationTest {

    @Test
    public void serializesToJSON() throws Exception {
        final Person person = createPerson();
        final String expected = MAPPER.writeValueAsString(readPersonFixture());
        assertThat(MAPPER.writeValueAsString(person)).isEqualTo(expected);
    }

    private Person readPersonFixture() throws java.io.IOException {
        return MAPPER.readValue(fixture("fixtures/V1/person.json"), Person.class);
    }


    @Test
    public void deserializesFromJSON() throws Exception {
        final Person person = createPerson();
        Person actual = readPersonFixture();
        assertThat(actual.getId()).isEqualTo(person.getId());
        assertThat(actual.getName()).isEqualTo(person.getName());
        assertThat(actual.getAddress()).isEqualTo(person.getAddress());
    }

    private Person createPerson() {
        return new Person(1, "John Doe", "Near the old shore");
    }


}
