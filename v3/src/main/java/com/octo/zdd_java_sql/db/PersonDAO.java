package com.octo.zdd_java_sql.db;

import com.octo.zdd_java_sql.core.AddressEntity;
import com.octo.zdd_java_sql.core.PersonEntity;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.transform.BasicTransformerAdapter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PersonDAO extends AbstractDAO<PersonEntity> {

    public PersonDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @NotNull
    public List<PersonEntity> findAll() {
        return list(namedQuery("com.octo.zdd_java_sql.core.PersonEntity.findAll"));
    }

    @NotNull
    public List<PersonEntity> findAllWithJoin() {
        return list(
                namedQuery("com.octo.zdd_java_sql.core.PersonEntity.findAllWithJoin").
                        setResultTransformer(new FoldingAddressesResultTransformer())
        );
    }

    @NotNull
    public List<PersonEntity> findAllWithLock() {
        return list(namedQuery("com.octo.zdd_java_sql.core.PersonEntity.findAllWithLock"));
    }

    @NotNull
    public Optional<PersonEntity> findById(@NotNull Long id) {
        return Optional.ofNullable(get(id));
    }

    @NotNull
    public Optional<PersonEntity> findByIdWithLock(@NotNull Long id) {
        PersonEntity person = currentSession().get(PersonEntity.class, id, LockMode.PESSIMISTIC_WRITE);
        return Optional.ofNullable(person);
    }

    @NotNull
    public Optional<PersonEntity> findByIdWithJoin(@NotNull Long id) {
        return Optional.ofNullable(
                uniqueResult(
                        namedQuery("com.octo.zdd_java_sql.core.PersonEntity.findByIdWithJoin").
                                setParameter("personId", id).
                                setResultTransformer(new FoldingAddressesResultTransformer())));
    }

    @NotNull
    public PersonEntity create(@NotNull String name) {
        PersonEntity person = new PersonEntity();
        person.setName(name);
        return persist(person);
    }

    /**
     * Transform the result of a join query into persons with several addresses
     */
    private class FoldingAddressesResultTransformer extends BasicTransformerAdapter {

        @Override
        public List transformList(List list) {
            Map<Long, PersonEntity> persons = new HashMap<>();
            list.forEach(o -> {
                Object[] resultItem = ((Object[]) o);
                PersonEntity personEntity = (PersonEntity) resultItem[0];
                AddressEntity addressEntity = (AddressEntity) resultItem[1];
                PersonEntity personInMap = persons.get(personEntity.getId());
                if (personInMap == null) {
                    personInMap = personEntity;
                    personEntity.setAddresses(new ArrayList<>());
                    persons.put(personEntity.getId(), personEntity);
                }
                personInMap.getAddresses().add(addressEntity);
                // set the first person to avoid ghosts
                addressEntity.setPerson(personInMap);

            });
            return new ArrayList<>(persons.values());
        }

    }

}
