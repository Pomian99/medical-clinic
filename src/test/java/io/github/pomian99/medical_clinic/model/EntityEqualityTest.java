package io.github.pomian99.medical_clinic.model;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityEqualityTest {

    static Stream<Arguments> entities() {
        return Stream.of(
            entity(Patient.class, Patient::new, Patient::setId),
            entity(User.class, User::new, User::setId),
            entity(Doctor.class, Doctor::new, Doctor::setId),
            entity(Facility.class, Facility::new, Facility::setId),
            entity(Visit.class, Visit::new, Visit::setId)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("entities")
    void hashCode_idAssignedAfterAddingToSet_setStillContainsEntity(EntityType type) {
        // given
        Object entity = type.create().get();
        Set<Object> entities = new HashSet<>();
        entities.add(entity);

        // when
        type.setId().accept(entity, 42L);

        // then
        assertTrue(entities.contains(entity));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("entities")
    void equals_sameId_entitiesAreEqual(EntityType type) {
        // given
        Object first = type.withId(42L);
        Object second = type.withId(42L);

        // when
        boolean equal = first.equals(second);

        // then
        assertTrue(equal);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("entities")
    void equals_noId_entitiesAreNotEqual(EntityType type) {
        // given
        Object first = type.create().get();
        Object second = type.create().get();

        // when
        boolean equal = first.equals(second);

        // then
        assertFalse(equal);
    }

    private static <T> Arguments entity(Class<T> type, Supplier<T> create, BiConsumer<T, Long> setId) {
        EntityType entityType = new EntityType(
            create::get,
            (entity, id) -> setId.accept(type.cast(entity), id)
        );
        return Arguments.of(Named.of(type.getSimpleName(), entityType));
    }

    record EntityType(Supplier<Object> create, BiConsumer<Object, Long> setId) {

        Object withId(Long id) {
            Object entity = create.get();
            setId.accept(entity, id);
            return entity;
        }
    }
}
