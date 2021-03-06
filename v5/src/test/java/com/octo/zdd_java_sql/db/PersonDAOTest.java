package com.octo.zdd_java_sql.db;

import com.octo.zdd_java_sql.core.AddressEntity;
import com.octo.zdd_java_sql.core.PersonEntity;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolationException;
import java.io.FileNotFoundException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonDAOTest extends AbstractDAOTest {

    private PersonDAO personDAO;

    private AddressDAO addressDAO;

    public PersonDAOTest() throws FileNotFoundException, ClassNotFoundException {
    }

    @Before
    public void setUp() throws Exception {
        personDAO = new PersonDAO(daoTestRule.getSessionFactory());
        addressDAO = new AddressDAO(daoTestRule.getSessionFactory());
    }

    @Test
    public void createPerson() {
        final PersonEntity jeff = daoTestRule.inTransaction(() -> personDAO.create("Jeff"));
        assertThat(jeff.getId()).isGreaterThan(0);
        assertThat(jeff.getName()).isEqualTo("Jeff");
        PersonEntity actual = personDAO.findById(jeff.getId());
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(jeff.getId());
        assertThat(actual.getName()).isEqualTo(jeff.getName());
    }

    @Test
    public void findAll() {
        personDAO.create("Jeff");
        personDAO.create("Jim");
        personDAO.create("Randy");
        List<PersonEntity> persons = personDAO.findAll();
        assertThat(persons).extracting("name").containsOnly("Jeff", "Jim", "Randy");
    }

    @Test
    public void findAllWithJoin() {
        personDAO.create("Jeff");
        personDAO.create("Jim");
        personDAO.create("Randy");
        List<PersonEntity> persons = personDAO.findAll();
        assertThat(persons).extracting("name").containsOnly("Jeff", "Jim", "Randy");
    }

    @Test
    public void findAllWithJoinAddress() {
        PersonEntity jeff = personDAO.create("Jeff");
        AddressEntity addressEntity1 = addressDAO.create("Near the old shore", jeff);
        AddressEntity addressEntity2 = addressDAO.create("Above the store", jeff);
        List<PersonEntity> persons = personDAO.findAllWithJoin();
        assertThat(persons.size()).isEqualTo(1);
        assertThat(persons.get(0).getName()).isEqualTo(jeff.getName());
        assertThat(persons.get(0).getAddresses().size()).isEqualTo(2);
        assertThat(persons.get(0).getAddresses()).
                extracting("address").
                containsOnly(addressEntity1.getAddress(), addressEntity2.getAddress());
    }

    @Test
    public void findById() {
        PersonEntity jeff = personDAO.create("Jeff");
        PersonEntity found = personDAO.findById(jeff.getId());
        assertThat(found).isNotNull();
        assertThat(jeff.getName()).isEqualTo(found.getName());

        PersonEntity notFound = personDAO.findById((long) -1);
        assertThat(notFound).isNull();
    }

    @Test
    public void findByIdWithLock() {
        PersonEntity jeff = personDAO.create("Jeff");
        PersonEntity found = personDAO.findByIdWithLock(jeff.getId());
        assertThat(found).isNotNull();
        assertThat(jeff.getName()).isEqualTo(found.getName());
        PersonEntity notFound = personDAO.findByIdWithLock((long) -1);
        assertThat(notFound).isNull();
    }

    @Test
    public void findByIdWithJoin() {
        PersonEntity jeff = personDAO.create("Jeff");
        AddressEntity addressEntity1 = addressDAO.create("Near the old shore", jeff);
        AddressEntity addressEntity2 = addressDAO.create("Above the store", jeff);
        PersonEntity found = personDAO.findByIdWithJoin(jeff.getId());
        assertThat(found).isNotNull();
        assertThat(jeff.getName()).isEqualTo(found.getName());
        assertThat(jeff.getAddresses().size()).isEqualTo(2);
        assertThat(jeff.getAddresses()).
                extracting("address").
                containsOnly(addressEntity1.getAddress(), addressEntity2.getAddress());

        PersonEntity notFound = personDAO.findById((long) -1);
        assertThat(notFound).isNull();
    }

    @Test(expected = ConstraintViolationException.class)
    public void handlesNullNameInsert() {
        daoTestRule.inTransaction(() -> personDAO.create(null));
    }

    @Test(expected = ConstraintViolationException.class)
    public void handlesNullNameUpdate() {
        PersonEntity jeff = personDAO.create("Jeff");
        jeff.setName(null);
        daoTestRule.inTransaction(() -> personDAO.update(jeff));
    }

    @Test
    public void delete() {
        PersonEntity jeff = personDAO.create("Jeff");
        PersonEntity jim = personDAO.create("Jim");
        daoTestRule.inTransaction(() -> personDAO.delete(jeff));
        final List<PersonEntity> persons = personDAO.findAll();
        assertThat(persons.size()).isEqualTo(1);
        assertThat(persons.get(0).getName()).isEqualTo(jim.getName());
    }


}
