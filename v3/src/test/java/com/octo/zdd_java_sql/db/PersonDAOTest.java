package com.octo.zdd_java_sql.db;

import com.octo.zdd_java_sql.core.AddressEntity;
import com.octo.zdd_java_sql.core.PersonEntity;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

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
        Optional<PersonEntity> optionalActual = personDAO.findById(jeff.getId());
        assertThat(optionalActual.isPresent()).isTrue();
        PersonEntity actual = optionalActual.get();
        assertThat(actual.getId()).isEqualTo(jeff.getId());
        assertThat(actual.getName()).isEqualTo(jeff.getName());
        assertThat(actual.getAddress()).isEqualTo(jeff.getAddress());
    }

    @Test
    public void findAll() {
        createPersonWithAddress("Jeff", "Near the old shore");
        createPersonWithAddress("Jim", "Above the store");
        createPersonWithAddress("Randy", "Better not asking");
        List<PersonEntity> persons = personDAO.findAll();
        assertThat(persons).extracting("name").containsOnly("Jeff", "Jim", "Randy");
        assertThat(persons).extracting("address").containsOnly("Near the old shore", "Above the store", "Better not asking");
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
    public void findAllWithLock() {
        daoTestRule.inTransaction(() -> {
            createPersonWithAddress("Jeff", "Near the old shore");
            createPersonWithAddress("Jim", "Above the store");
            createPersonWithAddress("Randy", "Better not asking");
        });

        final List<PersonEntity> persons = personDAO.findAllWithLock();
        assertThat(persons).extracting("name").containsOnly("Jeff", "Jim", "Randy");
        assertThat(persons).extracting("address").containsOnly("Near the old shore", "Above the store", "Better not asking");
    }

    @Test
    public void findByIdWithJoin() {
        PersonEntity jeff = personDAO.create("Jeff");
        AddressEntity addressEntity1 = addressDAO.create("Near the old shore", jeff);
        AddressEntity addressEntity2 = addressDAO.create("Above the store", jeff);
        Optional<PersonEntity> optionalFound = personDAO.findByIdWithJoin(jeff.getId());
        assertThat(optionalFound.isPresent()).isTrue();
        PersonEntity found = optionalFound.get();
        assertThat(jeff.getName()).isEqualTo(found.getName());
        assertThat(jeff.getAddresses().size()).isEqualTo(2);
        assertThat(jeff.getAddresses()).
                extracting("address").
                containsOnly(addressEntity1.getAddress(), addressEntity2.getAddress());

        Optional<PersonEntity> optionalNotFound = personDAO.findById((long) -1);
        assertThat(optionalNotFound.isPresent()).isFalse();
    }

    @Test
    public void findById() {
        PersonEntity jeff = createPersonWithAddress("Jeff", "Near the old shore");
        Optional<PersonEntity> optionalFound = personDAO.findById(jeff.getId());
        assertThat(optionalFound.isPresent()).isTrue();
        PersonEntity found = optionalFound.get();
        assertThat(jeff.getName()).isEqualTo(found.getName());
        assertThat(jeff.getAddress()).isEqualTo(found.getAddress());

        Optional<PersonEntity> optionalNotFound = personDAO.findById((long) -1);
        assertThat(optionalNotFound.isPresent()).isFalse();
    }

    @Test
    public void findByIdWithLock() {
        PersonEntity jeff = createPersonWithAddress("Jeff", "Near the old shore");
        Optional<PersonEntity> optionalFound = personDAO.findByIdWithLock(jeff.getId());
        assertThat(optionalFound.isPresent()).isTrue();
        PersonEntity found = optionalFound.get();
        assertThat(jeff.getName()).isEqualTo(found.getName());
        assertThat(jeff.getAddress()).isEqualTo(found.getAddress());

        Optional<PersonEntity> optionalNotFound = personDAO.findByIdWithLock((long) -1);
        assertThat(optionalNotFound.isPresent()).isFalse();
    }

    @Test(expected = ConstraintViolationException.class)
    public void handlesNullNameInsert() {
        daoTestRule.inTransaction(() -> createPersonWithAddress(null, "Somewhere"));
    }

    @Test(expected = ConstraintViolationException.class)
    public void handlesNullNameUpdate() {
        PersonEntity jeff = createPersonWithAddress("Jeff", "Near the old shore");
        jeff.setName(null);
        daoTestRule.inTransaction(() -> personDAO.update(jeff));
    }

    @Test
    public void delete() {
        PersonEntity jeff = createPersonWithAddress("Jeff", "Near the old shore");
        PersonEntity jim = createPersonWithAddress("Jim", "Above the store");
        daoTestRule.inTransaction(() -> personDAO.delete(jeff));
        final List<PersonEntity> persons = personDAO.findAll();
        assertThat(persons.size()).isEqualTo(1);
        assertThat(persons.get(0).getName()).isEqualTo(jim.getName());
    }

    /**
     * Create a person with an address, since it's no more available in the DAO.
     */
    private PersonEntity createPersonWithAddress(@NotNull String name, @NotNull String address) {
        PersonEntity personEntity = personDAO.create(name);
        personEntity.setAddress(address);
        return personDAO.update(personEntity);
    }

}
