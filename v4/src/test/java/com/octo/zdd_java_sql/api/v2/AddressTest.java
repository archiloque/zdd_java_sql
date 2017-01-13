package com.octo.zdd_java_sql.api.v2;

import com.octo.zdd_java_sql.api.AbstractRepresentationTest;
import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class AddressTest extends AbstractRepresentationTest {

    @Test
    public void serializesToJSON() throws Exception {
        final Address address = createAddress();
        final String expected = MAPPER.writeValueAsString(readAddressFixture());
        assertThat(MAPPER.writeValueAsString(address)).isEqualTo(expected);
    }

    private Address readAddressFixture() throws java.io.IOException {
        return MAPPER.readValue(fixture("fixtures/v2/address.json"), Address.class);
    }


    @Test
    public void deserializesFromJSON() throws Exception {
        final Address address = createAddress();
        Address actual = readAddressFixture();
        assertThat(actual.getId()).isEqualTo(address.getId());
        assertThat(actual.getAddress()).isEqualTo(address.getAddress());
        assertThat(actual.getPersonId()).isEqualTo(address.getPersonId());
    }

    private Address createAddress() {
        return new Address(1, "Near the old shore", 2);
    }

}
