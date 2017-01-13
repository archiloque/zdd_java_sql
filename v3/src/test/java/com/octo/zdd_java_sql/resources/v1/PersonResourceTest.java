package com.octo.zdd_java_sql.resources.v1;

import com.octo.zdd_java_sql.api.v1.ErrorResult;
import com.octo.zdd_java_sql.api.v1.People;
import com.octo.zdd_java_sql.api.v1.Person;
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

public class PersonResourceTest extends AbstractResourceTest {

    private static final PersonDAO personDAO = mock(PersonDAO.class);

    private static final AddressDAO addressDAO = mock(AddressDAO.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new PersonResource(personDAO, addressDAO))
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
    public void testFindAllEmpty() {
        when(personDAO.findAllWithJoin()).thenReturn(new ArrayList<PersonEntity>());
        People actual = resources.client().target("/v1/people").request().get(People.class);
        assertThat(actual.getPeople().size()).isEqualTo(0);
        verify(personDAO).findAllWithJoin();
    }

    @Test
    public void testFindAllSomethingOldPersistence() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", "Near the shore");
        personEntity.setAddresses(new ArrayList<>());
        ArrayList<PersonEntity> people = new ArrayList<>();
        people.add(personEntity);
        when(personDAO.findAllWithJoin()).thenReturn(people);
        People actual = resources.client().target("/v1/people").request().get(People.class);
        assertThat(actual.getPeople().size()).isEqualTo(1);
        Person actualPerson = actual.getPeople().get(0);
        assertThat(actualPerson.getId())
                .isEqualTo(personEntity.getId());
        assertThat(actualPerson.getName())
                .isEqualTo(personEntity.getName());
        assertThat(actualPerson.getAddress())
                .isEqualTo(personEntity.getAddress());
        verify(personDAO).findAllWithJoin();
    }

    @Test
    public void testFindAllSomethingNewPersistence() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", null);
        List<PersonEntity> people = new ArrayList<>();
        people.add(personEntity);
        AddressEntity addressEntity = createAddressEntity(2L, "Near the shore", personEntity);
        List<AddressEntity> addresses = new ArrayList<>();
        addresses.add(addressEntity);
        personEntity.setAddresses(addresses);
        when(personDAO.findAllWithJoin()).thenReturn(people);

        People actual = resources.client().target("/v1/people").request().get(People.class);
        assertThat(actual.getPeople().size()).isEqualTo(1);
        Person actualPerson = actual.getPeople().get(0);
        assertThat(actualPerson.getId())
                .isEqualTo(personEntity.getId());
        assertThat(actualPerson.getName())
                .isEqualTo(personEntity.getName());
        assertThat(actualPerson.getAddress())
                .isEqualTo(addressEntity.getAddress());
        verify(personDAO).findAllWithJoin();
    }

    @Test
    public void testFindByIdFoundOldPersistence() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", "Near the shore");
        personEntity.setAddresses(new ArrayList<>());
        when(personDAO.findByIdWithJoin(1L)).thenReturn(Optional.of(personEntity));
        Person actual = resources.client().target("/v1/people/1").request().get(Person.class);
        assertThat(actual.getId())
                .isEqualTo(personEntity.getId());
        assertThat(actual.getName())
                .isEqualTo(personEntity.getName());
        assertThat(actual.getAddress())
                .isEqualTo(personEntity.getAddress());
        verify(personDAO).findByIdWithJoin(1L);
    }

    @Test
    public void testFindByIdFoundNewPersistence() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", null);
        AddressEntity addressEntity = createAddressEntity(2L, "Near the shore", personEntity);
        List<AddressEntity> addresses = new ArrayList<>();
        addresses.add(addressEntity);
        personEntity.setAddresses(addresses);
        when(personDAO.findByIdWithJoin(1L)).thenReturn(Optional.of(personEntity));
        Person actual = resources.client().target("/v1/people/1").request().get(Person.class);
        assertThat(actual.getId())
                .isEqualTo(personEntity.getId());
        assertThat(actual.getName())
                .isEqualTo(personEntity.getName());
        assertThat(actual.getAddress())
                .isEqualTo(addressEntity.getAddress());
        verify(personDAO).findByIdWithJoin(1L);
    }

    @Test
    public void testCreateOkWithAddress() {
        Person person = new Person(1, "John Doe", "Near the shore");

        PersonEntity personEntity = createPersonEntity(1L, "John Doe", null);
        AddressEntity addressEntity = createAddressEntity(2L, "Near the shore", personEntity);

        when(personDAO.create("John Doe")).thenReturn(personEntity);
        when(addressDAO.create("Near the shore", personEntity)).thenReturn(addressEntity);
        Person actual = resources.
                client().
                target("/v1/people").
                request().
                post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
        assertThat(actual.getId())
                .isEqualTo(person.getId());
        assertThat(actual.getName())
                .isEqualTo(person.getName());
        assertThat(actual.getAddress())
                .isEqualTo(person.getAddress());
        verify(personDAO).create("John Doe");
        verify(addressDAO).create("Near the shore", personEntity);
    }


    @Test
    public void testCreateOkWithoutAddress() {
        Person person = new Person(1, "John Doe", null);
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", null);
        personEntity.setAddresses(new ArrayList<>());
        when(personDAO.create("John Doe")).thenReturn(personEntity);
        Person actual = resources.
                client().
                target("/v1/people").
                request().
                post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
        assertThat(actual.getId())
                .isEqualTo(personEntity.getId());
        assertThat(actual.getName())
                .isEqualTo(personEntity.getName());
        assertThat(actual.getAddress())
                .isEqualTo(personEntity.getAddress());
        verify(personDAO).create("John Doe");
    }

    @Test
    public void testCreateKoNoName() {
        Person person = new Person(1, null, "Near the shore");
        try {
            resources.
                    client().
                    target("/v1/people").
                    request().
                    post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
            fail();
        } catch (BadRequestException exception) {
        }
    }

    @Test
    public void testFindByIdNotFound() {
        when(personDAO.findByIdWithJoin(1L)).thenReturn(Optional.empty());
        try {
            resources.client().target("/v1/people/1").request().get(ErrorResult.class);
            fail();
        } catch (NotFoundException exception) {
        }
        verify(personDAO).findByIdWithJoin(1L);
    }

    @Test
    public void testDeleteFoundWithOldAddress() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", "Near the shore");
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.of(personEntity));
        Response response = resources.client().target("/v1/people/1").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(personDAO).findByIdWithLock(1L);
        verify(personDAO).delete(personEntity);
    }

    @Test
    public void testDeleteFoundWithNewAddress() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", null);
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.of(personEntity));
        Response response = resources.client().target("/v1/people/1").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(personDAO).findByIdWithLock(1L);
        verify(personDAO).delete(personEntity);
        verify(addressDAO).deleteByPerson(personEntity);
    }

    @Test
    public void testDeleteNotFound() {
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.empty());
        try {
            resources.client().target("/v1/people/1").request().delete(ErrorResult.class);
            fail();
        } catch (NotFoundException exception) {

        }
        verify(personDAO).findByIdWithLock(1L);
    }

    @Test
    public void testUpdateWithAddressOkOldPersistence() {
        PersonEntity personEntity1 = createPersonEntity(1L, "John Doe", "Near the shore");
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.of(personEntity1));
        Person person = new Person(1, "John Doea", "Near the shorea");
        PersonEntity personEntity2 = createPersonEntity(1L, "John Doea", null);
        AddressEntity addressEntity = createAddressEntity(2L, "Near the shore", personEntity2);
        when(personDAO.update(personEntity1)).thenReturn(personEntity2);
        when(addressDAO.create("Near the shorea", personEntity2)).thenReturn(addressEntity);
        Person actual = resources.
                client().
                target("/v1/people/1").
                request().
                put(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
        assertThat(actual.getId())
                .isEqualTo(personEntity2.getId());
        assertThat(actual.getName())
                .isEqualTo(personEntity2.getName());
        assertThat(actual.getAddress())
                .isEqualTo(addressEntity.getAddress());
        verify(personDAO).findByIdWithLock(1L);
        verify(personDAO).update(personEntity1);
        verify(addressDAO).deleteByPerson(personEntity2);
        verify(addressDAO).create("Near the shorea", personEntity2);
    }

    @Test
    public void testUpdateWithAddressOkNewPersistence() {
        PersonEntity personEntity1 = createPersonEntity(1L, "John Doe", null);
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.of(personEntity1));

        Person person = new Person(1L, "John Doea", "Near the shorea");

        PersonEntity personEntity2 = createPersonEntity(1L, "John Doea", null);
        AddressEntity addressEntity = createAddressEntity(2L, "Near the shorea", personEntity1);
        when(personDAO.update(personEntity1)).thenReturn(personEntity2);
        when(addressDAO.create("Near the shorea", personEntity2)).thenReturn(addressEntity);
        Person actual = resources.
                client().
                target("/v1/people/1").
                request().
                put(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
        assertThat(actual.getId())
                .isEqualTo(personEntity2.getId());
        assertThat(actual.getName())
                .isEqualTo(personEntity2.getName());
        assertThat(actual.getAddress())
                .isEqualTo(addressEntity.getAddress());
        verify(personDAO).findByIdWithLock(1L);
        verify(addressDAO).deleteByPerson(personEntity2);
        verify(addressDAO).create("Near the shorea", personEntity2);
    }


    @Test
    public void testUpdateWithoutAddressOkOldPersistence() {
        PersonEntity person1 = createPersonEntity(1L, "John Doe", "Near the shore");
        person1.setAddresses(new ArrayList<>());
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.of(person1));
        PersonEntity person2 = createPersonEntity(1L, "John Doea", null);
        person2.setAddresses(new ArrayList<>());
        Person person = new Person(1L, "John Doea", null);
        when(personDAO.update(person1)).thenReturn(person2);
        Person actual = resources.
                client().
                target("/v1/people/1").
                request().
                put(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
        assertThat(actual.getId())
                .isEqualTo(person2.getId());
        assertThat(actual.getName())
                .isEqualTo(person2.getName());
        assertThat(actual.getAddress())
                .isEqualTo(person2.getAddress());
        verify(personDAO).findByIdWithLock(1L);
        verify(addressDAO).deleteByPerson(person2);
        verify(personDAO).update(person1);
    }

    @Test
    public void testUpdateWithoutAddressOkNewPersistence() {
        PersonEntity person1 = createPersonEntity(1L, "John Doe", null);
        person1.setAddresses(new ArrayList<>());
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.of(person1));

        PersonEntity person2 = createPersonEntity(1L, "John Doea", null);
        person2.setAddresses(new ArrayList<>());
        Person person = new Person(1, "John Doea", null);
        when(personDAO.update(person1)).thenReturn(person2);
        Person actual = resources.
                client().
                target("/v1/people/1").
                request().
                put(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
        assertThat(actual.getId())
                .isEqualTo(person2.getId());
        assertThat(actual.getName())
                .isEqualTo(person2.getName());
        assertThat(actual.getAddress())
                .isEqualTo(person2.getAddress());
        verify(personDAO).findByIdWithLock(1L);
        verify(personDAO).update(person1);
    }

    @Test
    public void testUpdateKoNotFound() {
        Person person = new Person(1L, "John Doe", "Near the shore");
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.empty());
        try {
            resources.
                    client().
                    target("/v1/people/1").
                    request().
                    put(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
            fail();
        } catch (NotFoundException exception) {
        }
        verify(personDAO).findByIdWithLock(1L);
    }

    @Test
    public void testUpdateKoNoName() {
        Person person = new Person(1, null, "Near the shore");
        try {
            resources.
                    client().
                    target("/v1/people/1").
                    request().
                    put(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
            fail();
        } catch (BadRequestException exception) {
        }
    }


}
