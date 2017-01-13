package com.octo.zdd_java_sql.resources.v2;

import com.octo.zdd_java_sql.api.v2.ErrorResult;
import com.octo.zdd_java_sql.api.v2.People;
import com.octo.zdd_java_sql.api.v2.Person;
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
        when(personDAO.findAll()).thenReturn(new ArrayList<PersonEntity>());
        People actual = resources.client().target("/v2/people").request().get(People.class);
        assertThat(actual.getPeople().size()).isEqualTo(0);
        verify(personDAO).findAll();
    }


    @Test
    public void testFindAllSomething() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", null);
        ArrayList<PersonEntity> people = new ArrayList<>();
        people.add(personEntity);
        when(personDAO.findAll()).thenReturn(people);
        People actual = resources.client().target("/v2/people").request().get(People.class);
        assertThat(actual.getPeople().size()).isEqualTo(1);
        Person actualPerson = actual.getPeople().get(0);
        assertThat(actualPerson.getId())
                .isEqualTo(personEntity.getId());
        assertThat(actualPerson.getName())
                .isEqualTo(personEntity.getName());
        verify(personDAO).findAll();
    }

    @Test
    public void testFindByIdFound() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", null);
        when(personDAO.findById(1L)).thenReturn(Optional.of(personEntity));
        Person actual = resources.client().target("/v2/people/1").request().get(Person.class);
        assertThat(actual.getId())
                .isEqualTo(personEntity.getId());
        assertThat(actual.getName())
                .isEqualTo(personEntity.getName());
        verify(personDAO).findById(1L);
    }


    @Test
    public void testCreateOk() {
        Person person = new Person(1, "John Doe");

        PersonEntity personEntity = createPersonEntity(1L, "John Doe", null);

        when(personDAO.create("John Doe")).thenReturn(personEntity);
        Person actual = resources.
                client().
                target("/v2/people").
                request().
                post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
        assertThat(actual.getId())
                .isEqualTo(person.getId());
        assertThat(actual.getName())
                .isEqualTo(person.getName());
        verify(personDAO).create("John Doe");
    }

    @Test
    public void testCreateKoNoName() {
        Person person = new Person(1, null);
        try {
            resources.
                    client().
                    target("/v2/people").
                    request().
                    post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
            fail();
        } catch (BadRequestException exception) {
        }
    }

    @Test
    public void testFindByIdNotFound() {
        when(personDAO.findById(1L)).thenReturn(Optional.empty());
        try {
            resources.client().target("/v2/people/1").request().get(ErrorResult.class);
            fail();
        } catch (NotFoundException exception) {
        }
        verify(personDAO).findById(1L);
    }

    @Test
    public void testDeleteFoundWithOldAddress() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", "Near the shore");
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.of(personEntity));
        Response response = resources.client().target("/v2/people/1").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(personDAO).findByIdWithLock(1L);
        verify(personDAO).delete(personEntity);
    }

    @Test
    public void testDeleteFoundWithNewAddress() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", null);
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.of(personEntity));
        Response response = resources.client().target("/v2/people/1").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(personDAO).findByIdWithLock(1L);
        verify(personDAO).delete(personEntity);
        verify(addressDAO).deleteByPerson(personEntity);
    }

    @Test
    public void testDeleteNotFound() {
        when(personDAO.findByIdWithLock(1L)).thenReturn(Optional.empty());
        try {
            resources.client().target("/v2/people/1").request().delete(ErrorResult.class);
            fail();
        } catch (NotFoundException exception) {

        }
        verify(personDAO).findByIdWithLock(1L);
    }

    @Test
    public void testUpdateOk() {
        PersonEntity person1 = createPersonEntity(1L, "John Doe", null);
        when(personDAO.findById(1L)).thenReturn(Optional.of(person1));
        PersonEntity person2 = createPersonEntity(1L, "John Doea", null);
        Person person = new Person(1L, "John Doea");
        when(personDAO.update(person1)).thenReturn(person2);
        Person actual = resources.
                client().
                target("/v2/people/1").
                request().
                put(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
        assertThat(actual.getId())
                .isEqualTo(person2.getId());
        assertThat(actual.getName())
                .isEqualTo(person2.getName());
        verify(personDAO).findById(1L);
        verify(personDAO).update(person1);
    }

    @Test
    public void testUpdateKoNotFound() {
        Person person = new Person(1L, "John Doe");
        when(personDAO.findById(1L)).thenReturn(Optional.empty());
        try {
            resources.
                    client().
                    target("/v2/people/1").
                    request().
                    put(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
            fail();
        } catch (NotFoundException exception) {
        }
        verify(personDAO).findById(1L);
    }

    @Test
    public void testUpdateKoNoName() {
        Person person = new Person();
        try {
            resources.
                    client().
                    target("/v2/people/1").
                    request().
                    put(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
            fail();
        } catch (BadRequestException exception) {
        }
    }

}
