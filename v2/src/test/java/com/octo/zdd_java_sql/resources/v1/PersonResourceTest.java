package com.octo.zdd_java_sql.resources.v1;

import com.octo.zdd_java_sql.api.v1.ErrorResult;
import com.octo.zdd_java_sql.api.v1.People;
import com.octo.zdd_java_sql.api.v1.Person;
import com.octo.zdd_java_sql.core.PersonEntity;
import com.octo.zdd_java_sql.db.PersonDAO;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
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

public class PersonResourceTest {

    private static final PersonDAO dao = mock(PersonDAO.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new PersonResource(dao))
            .build();

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {
        // we have to reset the mock after each test because of the
        // @ClassRule, or use a @Rule as mentioned below.
        reset(dao);
    }

    @Test
    public void testFindAllEmpty() {
        when(dao.findAll()).thenReturn(new ArrayList<PersonEntity>());
        People actual = resources.client().target("/v1/people").request().get(People.class);
        assertThat(actual.getPeople().size()).isEqualTo(0);
        verify(dao).findAll();
    }


    @Test
    public void testFindAllSomething() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", "Near the shore");
        ArrayList<PersonEntity> people = new ArrayList<>();
        people.add(personEntity);
        when(dao.findAll()).thenReturn(people);
        People actual = resources.client().target("/v1/people").request().get(People.class);
        assertThat(actual.getPeople().size()).isEqualTo(1);
        Person actualPerson = actual.getPeople().get(0);
        assertThat(actualPerson.getId())
                .isEqualTo(personEntity.getId());
        assertThat(actualPerson.getName())
                .isEqualTo(personEntity.getName());
        assertThat(actualPerson.getAddress())
                .isEqualTo(personEntity.getAddress());
        verify(dao).findAll();
    }

    @Test
    public void testFindByIdFound() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", "Near the shore");
        when(dao.findById(1L)).thenReturn(Optional.of(personEntity));
        Person actual = resources.client().target("/v1/people/1").request().get(Person.class);
        assertThat(actual.getId())
                .isEqualTo(personEntity.getId());
        assertThat(actual.getName())
                .isEqualTo(personEntity.getName());
        assertThat(actual.getAddress())
                .isEqualTo(personEntity.getAddress());
        verify(dao).findById(1L);
    }

    @Test
    public void testCreateOk() {
        Person person = new Person(1, "John Doe", "Near the shore");


        PersonEntity personEntity = createPersonEntity(1L, "John Doe", "Near the shore");
        when(dao.create("John Doe", "Near the shore")).thenReturn(personEntity);
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
        verify(dao).create("John Doe", "Near the shore");
    }


    @Test
    public void testCreateKoNoName() {
        Person person = new Person(1L, null, "Near the shore");
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
        when(dao.findById(1L)).thenReturn(Optional.empty());
        try {
            resources.client().target("/v1/people/1").request().get(ErrorResult.class);
            fail();
        } catch (NotFoundException exception) {

        }
        verify(dao).findById(1L);
    }

    @Test
    public void testDeleteFound() {
        PersonEntity personEntity = createPersonEntity(1L, "John Doe", "Near the shore");
        when(dao.findById(1L)).thenReturn(Optional.of(personEntity));
        Response response = resources.client().target("/v1/people/1").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(dao).findById(1L);
        verify(dao).delete(personEntity);
    }

    @Test
    public void testDeleteNotFound() {
        when(dao.findById(1L)).thenReturn(Optional.empty());
        try {
            resources.client().target("/v1/people/1").request().delete(ErrorResult.class);
            fail();
        } catch (NotFoundException exception) {

        }
        verify(dao).findById(1L);
    }

    @Test
    public void testUpdateOk() {
        PersonEntity personEntity1 = createPersonEntity(1L, "John Doe", "Near the shore");
        when(dao.findById(1L)).thenReturn(Optional.of(personEntity1));

        PersonEntity personEntity2 = createPersonEntity(1L, "John Doea", "Near the shorea");
        when(dao.update(personEntity2)).thenReturn(personEntity2);

        Person person = new Person(1, "John Doea", "Near the shorea");

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
                .isEqualTo(personEntity2.getAddress());
        verify(dao).findById(1L);
        verify(dao).update(personEntity1);
    }


    @Test
    public void testUpdateKoNotFound() {
        Person person = new Person(1, "John Doe", "Near the shore");

        when(dao.findById(1L)).thenReturn(Optional.empty());
        try {
            resources.
                    client().
                    target("/v1/people/1").
                    request().
                    put(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE), Person.class);
            fail();
        } catch (NotFoundException exception) {
        }
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

    @NotNull
    private PersonEntity createPersonEntity(@Nullable Long id, @Nullable String name, @Nullable String address) {
        PersonEntity personEntity = new PersonEntity();
        if (id != null) {
            personEntity.setId(id);
        }
        if (name != null) {
            personEntity.setName(name);
        }
        if (address != null) {
            personEntity.setAddress(address);
        }
        return personEntity;
    }

}
