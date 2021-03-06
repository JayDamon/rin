package com.factotum.rin.service;

import com.factotum.rin.model.FrequencyType;
import com.factotum.rin.repository.FrequencyTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FrequencyServiceImplUT {

    private FrequencyService frequencyService;
    @Mock
    private FrequencyTypeRepository frequencyTypeRepository;

    @BeforeEach
    void setUp() {
        this.frequencyService = new FrequencyServiceImpl(frequencyTypeRepository);
    }


    @Test
    void getAllFrequencyTypes_GivenFrequencyTypesExist_ThenReturnAllTypes() {

        // Arrange
        FrequencyType frequencyType = createFrequencyType(1, "Name");

        FrequencyType frequencyTypeTwo = createFrequencyType(2, "NameTwo");

        List<FrequencyType> frequencyTypes = new ArrayList<>();
        frequencyTypes.add(frequencyType);
        frequencyTypes.add(frequencyTypeTwo);

        when(frequencyTypeRepository.findAll()).thenReturn(frequencyTypes);

        // Act
        List<FrequencyType> types = frequencyService.getAllFrequencyTypes();

        // Assert
        assertThat(types, hasSize(2));

        for (FrequencyType type : types) {
            assertThat(type.getId(), is(oneOf(1, 2)));
            assertThat(type.getFrequencyTypeName(), is(oneOf("Name", "NameTwo")));
        }

    }

    @Test
    void getAllFrequencyTypes_GivenFrequencyTypesDoNotExist_ThenReturnEmptyList() {

        // Arrange
        when(frequencyTypeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<FrequencyType> types = frequencyService.getAllFrequencyTypes();

        // Assert
        assertThat(types, is(emptyIterable()));
    }

    //    getFrequencyTypeById
    @Test
    void getFrequencyTypeById_GivenFrequencyTypeExists_ThenReturnFrequencyType() {

        // Arrange
        FrequencyType frequencyType = createFrequencyType(1, "testName");
        when(frequencyTypeRepository.findById(eq(1))).thenReturn(Optional.of(frequencyType));

        // Act
        FrequencyType retrievedFrequencyType = frequencyService.getFrequencyTypeById(1);

        // Assert
        assertThat(retrievedFrequencyType, is(not(nullValue())));
        assertThat(retrievedFrequencyType.getFrequencyTypeName(), is(equalTo("testName")));
        assertThat(retrievedFrequencyType.getId(), is(equalTo(1)));
    }

    @Test
    void getFrequencyTypeById_GivenIdDoesNotExist_ThenThrowNoResultFoundException() {

        when(frequencyTypeRepository.findById(eq(2))).thenReturn(Optional.empty());

        assertThrows(NoResultException.class, () -> frequencyService.getFrequencyTypeById(2));

    }

    private FrequencyType createFrequencyType(int id, String name) {
        FrequencyType frequencyType = new FrequencyType();
        frequencyType.setId(id);
        frequencyType.setFrequencyTypeName(name);
        return frequencyType;
    }

}
