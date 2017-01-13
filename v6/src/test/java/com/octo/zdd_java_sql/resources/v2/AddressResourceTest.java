package com.octo.zdd_java_sql.resources.v2;

import com.octo.zdd_java_sql.api.v2.Address;
import com.octo.zdd_java_sql.api.v2.Addresses;
import com.octo.zdd_java_sql.api.v2.ErrorResult;
import com.octo.zdd_java_sql.api.v2.Person;
import com.octo.zdd_java_sql.core.AddressEntity;
import com.octo.zdd_java_sql.core.PersonEntity;
import com.octo.zdd_java_sql.db.AddressDAO;
import com.octo.zdd_java_sql.db.PersonDAO;
import com.octo.zdd_java_sql.resources.AbstractResourceTest;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddressResourceTest extends AbstractResourceTest {

    private static final PersonDAO personDAO = mock(PersonDAO.class);

    private static final AddressDAO addressDAO = mock(AddressDAO.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new AddressResource(personDAO, addressDAO))
            .build();

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {
        // we have to reset the mock after each test because of the
        // @ClassRule, or use a @Rule as mentioned below.
        reset(personDAO);
        reset(addressDAO);
    }

    @Test
    public void testFindByAllNotFound() {
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.empty());
        try {
            resources.client().target("/v2/people/1/addresses").request().get(ErrorResult.class);
            fail();
        } catch (NotFoundException exception) {
        }
        verify(personDAO).findByIdWithLock(1L);
    }


    @Test
    public void testFindAllEmpty() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe");
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.of(personEntity));
        when(addressDAO.findByPerson(personEntity)).thenReturn(new ArrayList<>());
        Addresses actual = resources.client().target("/v2/people/1/addresses").request().get(Addresses.class);
        assertThat(actual.getAddresses().size()).isEqualTo(0);
        verify(personDAO).findByIdWithLock(1L);
        verify(addressDAO).findByPerson(personEntity);
    }

    @Test
    public void testFindAllSomethingNewPersistence() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe");
        AddressEntity addressEntity = createAddressEntity(1L, "Near the shore", personEntity);
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.of(personEntity));
        List<AddressEntity> addressesEntities = new ArrayList<>();
        addressesEntities.add(addressEntity);
        when(addressDAO.findByPerson(personEntity)).thenReturn(addressesEntities);
        Addresses actual = resources.client().target("/v2/people/1/addresses").request().get(Addresses.class);
        assertThat(actual.getAddresses().size()).isEqualTo(1);
        Address actualAddress = actual.getAddresses().get(0);
        assertThat(actualAddress.getId())
                .isEqualTo(addressEntity.getId());
        assertThat(actualAddress.getAddress())
                .isEqualTo(addressEntity.getAddress());
        assertThat(actualAddress.getPersonId())
                .isEqualTo(addressEntity.getPerson().getId());
        verify(personDAO).findByIdWithLock(1L);
        verify(addressDAO).findByPerson(personEntity);
    }

    @Test
    public void testFindByIdFound() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe");
        AddressEntity addressEntity = createAddressEntity(1L, "Near the shore", personEntity);
        when(addressDAO.findByIdAndPersonId(1L, 1L)).thenReturn(Optional.of(addressEntity));
        Address actual = resources.client().target("/v2/people/1/addresses/1").request().get(Address.class);
        assertThat(actual.getId())
                .isEqualTo(addressEntity.getId());
        assertThat(actual.getAddress())
                .isEqualTo(addressEntity.getAddress());
        assertThat(actual.getId())
                .isEqualTo(addressEntity.getPerson().getId());
        verify(addressDAO).findByIdAndPersonId(1L, 1L);
    }

    @Test
    public void testCreateOk() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe");
        when(personDAO.findById(1L)).thenReturn(Optional.of(personEntity));
        AddressEntity addressEntity = createAddressEntity(1L, "Near the shore", personEntity);
        when(addressDAO.create("Near the shore", personEntity)).thenReturn(addressEntity);
        Address address = new Address(1L, "Near the shore", personEntity.getId());
        Address actual = resources.
                client().
                target("/v2/people/1/addresses").
                request().
                post(Entity.entity(address, MediaType.APPLICATION_JSON_TYPE), Address.class);
        assertThat(actual.getId())
                .isEqualTo(address.getId());
        assertThat(actual.getAddress())
                .isEqualTo(address.getAddress());
        assertThat(actual.getPersonId())
                .isEqualTo(address.getPersonId());
        verify(personDAO).findById(1L);
        verify(addressDAO).create("Near the shore", personEntity);
    }

    @Test
    public void testCreateKoNoAddress() {
        Address address = new Address(1L, null, 1L);
        try {
            resources.
                    client().
                    target("/v2/people/1/addresses").
                    request().
                    post(Entity.entity(address, MediaType.APPLICATION_JSON_TYPE), Address.class);
            fail();
        } catch (BadRequestException exception) {
        }
    }

    @Test
    public void testCreateKoNoPerson() {
        when(personDAO.findById(1L)).thenReturn(Optional.empty());
        Address address = new Address(1L, "Near the shore", 1L);
        try {
            resources.
                    client().
                    target("/v2/people/1/addresses").
                    request().
                    post(Entity.entity(address, MediaType.APPLICATION_JSON_TYPE), Address.class);
            fail();
        } catch (NotFoundException exception) {
        }
    }

    @Test
    public void testFindByIdNotFound() {
        when(addressDAO.findByIdAndPersonId(1L, 1L)).thenReturn(Optional.empty());
        try {
            resources.client().target("/v2/people/1/addresses/1").request().get(ErrorResult.class);
            fail();
        } catch (NotFoundException exception) {
        }
        verify(addressDAO).findByIdAndPersonId(1L, 1L);
    }

    @Test
    public void testDeleteFound() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe");
        AddressEntity addressEntity = createAddressEntity(1L, "Near the shore", personEntity);

        when(addressDAO.findByIdAndPersonId(1L, 1L)).thenReturn(Optional.of(addressEntity));
        Response response = resources.client().target("/v2/people/1/addresses/1").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(addressDAO).findByIdAndPersonId(1L, 1L);
        verify(addressDAO).delete(addressEntity);
    }

    @Test
    public void testDeleteNotFound() {
        when(addressDAO.findByIdAndPersonId(1L, 1L)).thenReturn(Optional.empty());
        try {
            resources.client().target("/v2/people/1/addresses/1").request().delete(ErrorResult.class);
            fail();
        } catch (NotFoundException exception) {

        }
        verify(addressDAO).findByIdAndPersonId(1L, 1L);
    }

    @Test
    public void testUpdateOk() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe");
        AddressEntity addressEntity1 = createAddressEntity(1L, "Near the shore", personEntity);
        AddressEntity addressEntity2 = createAddressEntity(1L, "Near the shorea", personEntity);
        when(addressDAO.findByIdAndPersonId(1L, 1L)).thenReturn(Optional.of(addressEntity1));
        when(addressDAO.update(addressEntity1)).thenReturn(addressEntity2);

        Address address = new Address(1l, "Near the shore", 1L);
        Address actual = resources.
                client().
                target("/v2/people/1/addresses/1").
                request().
                put(Entity.entity(address, MediaType.APPLICATION_JSON_TYPE), Address.class);
        assertThat(actual.getId())
                .isEqualTo(addressEntity2.getId());
        assertThat(actual.getAddress())
                .isEqualTo(addressEntity2.getAddress());
        assertThat(actual.getPersonId())
                .isEqualTo(addressEntity2.getPerson().getId());
        verify(addressDAO).findByIdAndPersonId(1L, 1L);
        verify(addressDAO).update(addressEntity1);
    }

    @Test
    public void testUpdateKoNotFound() {
        Address address = new Address(1L, "Near the shore", 1L);
        when(addressDAO.findByIdAndPersonId(1L, 1L)).thenReturn(Optional.empty());
        try {
            resources.
                    client().
                    target("/v2/people/1/addresses/1").
                    request().
                    put(Entity.entity(address, MediaType.APPLICATION_JSON_TYPE), Person.class);
            fail();
        } catch (NotFoundException exception) {
        }
        verify(addressDAO).findByIdAndPersonId(1L, 1L);
    }

    @Test
    public void testUpdateKoNoAddress() {
        Address address = new Address();
        try {
            resources.
                    client().
                    target("/v2/people/1/addresses/1").
                    request().
                    put(Entity.entity(address, MediaType.APPLICATION_JSON_TYPE), Address.class);
            fail();
        } catch (BadRequestException exception) {
        }
    }

}
