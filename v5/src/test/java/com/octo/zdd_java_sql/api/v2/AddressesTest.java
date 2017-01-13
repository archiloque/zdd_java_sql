package com.octo.zdd_java_sql.api.v2;

import com.octo.zdd_java_sql.api.AbstractRepresentationTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class AddressesTest extends AbstractRepresentationTest {

    @Test
    public void serializesToJSON() throws Exception {
        final Addresses addresses = createAddresses();
        final String expected = MAPPER.writeValueAsString(readAddressesFixture());
        assertThat(MAPPER.writeValueAsString(addresses)).isEqualTo(expected);
    }

    private Addresses readAddressesFixture() throws java.io.IOException {
        return MAPPER.readValue(fixture("fixtures/v2/addresses.json"), Addresses.class);
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        final Addresses addresses = createAddresses();
        Addresses actual = readAddressesFixture();
        assertThat(actual.getAddresses().size()).isEqualTo(1);
        Address actualAddress = actual.getAddresses().get(0);
        Address addressesAddress = addresses.getAddresses().get(0);
        assertThat(actualAddress.getId()).isEqualTo(addressesAddress.getId());
        assertThat(actualAddress.getAddress()).isEqualTo(addressesAddress.getAddress());
        assertThat(actualAddress.getPersonId()).isEqualTo(addressesAddress.getPersonId());
    }

    private Addresses createAddresses() {
        Addresses addresses = new Addresses();
        List<Address> addressesContent = new ArrayList<>();
        addressesContent.add(new Address(1, "Near the old shore", 2));
        addresses.setAddresses(addressesContent);
        return addresses;
    }


}
