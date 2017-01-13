package com.octo.zdd_java_sql.db;

import com.octo.zdd_java_sql.core.AddressEntity;
import com.octo.zdd_java_sql.core.PersonEntity;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolationException;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressDAOTest extends AbstractDAOTest {

    private AddressDAO addressDAO;

    private PersonDAO personDAO;

    public AddressDAOTest() throws FileNotFoundException, ClassNotFoundException {
    }

    @Before
    public void setUp() throws Exception {
        addressDAO = new AddressDAO(daoTestRule.getSessionFactory());
        personDAO = new PersonDAO(daoTestRule.getSessionFactory());
    }

    @Test
    public void createAddress() {
        PersonEntity jeff = personDAO.create("Jeff");
        final AddressEntity jeffAddress = daoTestRule.inTransaction(() -> addressDAO.create("Near the old shore", jeff));
        assertThat(jeffAddress.getId()).isGreaterThan(0);
        assertThat(jeffAddress.getAddress()).isEqualTo("Near the old shore");
        assertThat(jeffAddress.getPerson().getId()).isEqualTo(jeff.getId());

        Optional<AddressEntity> optionalActual = addressDAO.findByIdAndPersonId(jeffAddress.getId(), jeff.getId());
        assertThat(optionalActual.isPresent()).isTrue();
        AddressEntity actual = optionalActual.get();
        assertThat(actual.getId()).isEqualTo(jeffAddress.getId());
        assertThat(actual.getAddress()).isEqualTo(jeffAddress.getAddress());
        assertThat(actual.getPerson().getId()).isEqualTo(jeffAddress.getPerson().getId());
    }

    @Test
    public void findAll() {
        PersonEntity jeff = personDAO.create("Jeff");
        addressDAO.create("Near the old shore", jeff);
        addressDAO.create("Above the store", jeff);
        addressDAO.create("Better not asking", jeff);
        List<AddressEntity> addresses = addressDAO.findByPerson(jeff);
        assertThat(addresses).extracting("person").extracting("id").containsOnly(jeff.getId());
        assertThat(addresses).extracting("address").containsOnly("Near the old shore", "Above the store", "Better not asking");
    }


    @Test
    public void findByIdAndPersonId() {
        PersonEntity jeff = personDAO.create("Jeff");
        AddressEntity jeffAddress = addressDAO.create("Near the old shore", jeff);

        Optional<AddressEntity> optionalFound = addressDAO.findByIdAndPersonId(jeffAddress.getId(), jeff.getId());
        assertThat(optionalFound.isPresent()).isTrue();
        AddressEntity found = optionalFound.get();
        assertThat(jeffAddress.getAddress()).isEqualTo(found.getAddress());
        assertThat(jeffAddress.getPerson().getId()).isEqualTo(found.getPerson().getId());

        Optional<AddressEntity> optionalNotFound = addressDAO.findByIdAndPersonId((long) -1, (long) -1);
        assertThat(optionalNotFound.isPresent()).isFalse();
        optionalNotFound = addressDAO.findByIdAndPersonId(jeffAddress.getId(), (long) -1);
        assertThat(optionalNotFound.isPresent()).isFalse();
        optionalNotFound = addressDAO.findByIdAndPersonId((long) -1, jeff.getId());
        assertThat(optionalNotFound.isPresent()).isFalse();
    }

    @Test(expected = ConstraintViolationException.class)
    public void handlesNullAddressInsert() {
        PersonEntity jeff = personDAO.create("Jeff");
        daoTestRule.inTransaction(() -> addressDAO.create(null, jeff));
    }

    @Test(expected = ConstraintViolationException.class)
    public void handlesNullAddressUpdate() {
        PersonEntity jeff = personDAO.create("Jeff");
        AddressEntity jeffAddress = addressDAO.create("Near the old shore", jeff);
        jeffAddress.setAddress(null);
        daoTestRule.inTransaction(() -> addressDAO.update(jeffAddress));
    }

    @Test
    public void delete() {
        PersonEntity jeff = personDAO.create("Jeff");
        AddressEntity jeffAddress = addressDAO.create("Near the old shore", jeff);
        daoTestRule.inTransaction(() -> addressDAO.delete(jeffAddress));
        final List<AddressEntity> addresses = addressDAO.findByPerson(jeff);
        assertThat(addresses).isEmpty();
    }

    @Test
    public void deleteByPerson() {
        PersonEntity jeff = personDAO.create("Jeff");
        AddressEntity jeffAddress1 = addressDAO.create("Near the old shore 1", jeff);
        AddressEntity jeffAddress2 = addressDAO.create("Near the old shore 2", jeff);
        daoTestRule.inTransaction(() -> addressDAO.deleteByPerson(jeff));
        final List<AddressEntity> addresses = addressDAO.findByPerson(jeff);
        assertThat(addresses).isEmpty();
    }


}
