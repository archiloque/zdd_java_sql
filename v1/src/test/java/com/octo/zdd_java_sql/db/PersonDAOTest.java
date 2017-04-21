package com.octo.zdd_java_sql.db;

import com.octo.zdd_java_sql.core.PersonEntity;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolationException;
import java.io.FileNotFoundException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonDAOTest extends AbstractDAOTest {

    private PersonDAO personDAO;

    public PersonDAOTest() throws FileNotFoundException, ClassNotFoundException {
    }

    @Before
    public void setUp() throws Exception {
        personDAO = new PersonDAO(daoTestRule.getSessionFactory());
    }

    @Test
    public void createPerson() {
        final PersonEntity jeff = daoTestRule.inTransaction(() -> personDAO.create("Jeff", "Near the old shore"));
        assertThat(jeff.getId()).isGreaterThan(0);
        assertThat(jeff.getName()).isEqualTo("Jeff");
        PersonEntity actual = personDAO.findById(jeff.getId());
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(jeff.getId());
        assertThat(actual.getName()).isEqualTo(jeff.getName());
        assertThat(actual.getAddress()).isEqualTo(jeff.getAddress());
    }

    @Test
    public void findAll() {
        personDAO.create("Jeff", "Near the old shore");
        personDAO.create("Jim", "Above the store");
        personDAO.create("Randy", "Better not asking");
        List<PersonEntity> persons = personDAO.findAll();
        assertThat(persons).extracting("name").containsOnly("Jeff", "Jim", "Randy");
        assertThat(persons).extracting("address").containsOnly("Near the old shore", "Above the store", "Better not asking");
    }

    @Test
    public void findById() {
        PersonEntity jeff = personDAO.create("Jeff", "Near the old shore");
        PersonEntity found = personDAO.findById(jeff.getId());
        assertThat(found).isNotNull();
        assertThat(jeff.getName()).isEqualTo(found.getName());
        assertThat(jeff.getAddress()).isEqualTo(found.getAddress());

        PersonEntity notFound = personDAO.findById((long) -1);
        assertThat(notFound).isNull();
    }

    @Test(expected = ConstraintViolationException.class)
    public void handlesNullNameInsert() {
        daoTestRule.inTransaction(() -> personDAO.create(null, "Somewhere"));
    }

    @Test(expected = ConstraintViolationException.class)
    public void handlesNullNameUpdate() {
        PersonEntity jeff = personDAO.create("Jeff", "Near the old shore");
        jeff.setName(null);
        daoTestRule.inTransaction(() -> personDAO.update(jeff));
    }

    @Test
    public void delete() {
        PersonEntity jeff = personDAO.create("Jeff", "Near the old shore");
        PersonEntity jim = personDAO.create("Jim", "Above the store");
        daoTestRule.inTransaction(() -> personDAO.delete(jeff));
        final List<PersonEntity> persons = personDAO.findAll();
        assertThat(persons.size()).isEqualTo(1);
        assertThat(persons.get(0).getName()).isEqualTo(jim.getName());
    }

}
