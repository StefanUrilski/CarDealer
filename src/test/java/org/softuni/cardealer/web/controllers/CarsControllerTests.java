package org.softuni.cardealer.web.controllers;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.softuni.cardealer.domain.entities.Car;
import org.softuni.cardealer.domain.entities.Part;
import org.softuni.cardealer.domain.entities.Supplier;
import org.softuni.cardealer.repository.CarRepository;
import org.softuni.cardealer.repository.PartRepository;
import org.softuni.cardealer.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.parameters.P;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CarsControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartRepository mockPartRepository;

    @Autowired
    private CarRepository carRepository;

    @Before
    public void mockPart() {
        when(mockPartRepository.findById("1"))
                .thenReturn(Optional.of(new Part()));
    }

    @Test
    @WithMockUser
    public void allCars_ShouldReturnCorrectView() throws Exception {
        mockMvc
                .perform(get("/cars/all"))
                .andExpect(view().name("all-cars"));
    }

    @Test
    @WithMockUser
    public void addCar_SaveCorrectCar_RedirectCorrectView() throws Exception {
        carRepository.deleteAll();

        mockMvc
                .perform(post("/cars/add")
                        .param("make", "someMake")
                        .param("model", "someModel")
                        .param("travelledDistance", "123")
                        .param("parts", String.valueOf(Collections.singletonList("1")))
                )
                .andExpect(view().name("redirect:all"));
    }

    @Test
    @WithMockUser
    public void addCar_ShouldSaveCar() throws Exception {
        carRepository.deleteAll();
        Long expected = 123L;
        mockMvc
                .perform(post("/cars/add")
                        .param("make", "someMake")
                        .param("model", "someModel")
                        .param("travelledDistance", "123")
                        .param("parts", String.valueOf(Collections.singletonList("1")))
                );
        Car car = carRepository.findAll().get(0);

        Assert.assertEquals("someMake", car.getMake());
        Assert.assertEquals("someModel", car.getModel());
        Assert.assertEquals(expected, car.getTravelledDistance());

    }
}
