package org.softuni.cardealer.web.controllers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.softuni.cardealer.domain.entities.Customer;
import org.softuni.cardealer.domain.entities.Part;
import org.softuni.cardealer.repository.CustomerRepository;
import org.softuni.cardealer.repository.PartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CustomersControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @WithMockUser
    public void allCustomers_ShouldReturnCorrectView() throws Exception {
        mockMvc
                .perform(get("/customers/all"))
                .andExpect(view().name("all-customers"));
    }

    @Test
    @WithMockUser
    public void addCustomer_SaveCorrectCustomer_RedirectCorrectView() throws Exception {
        customerRepository.deleteAll();

        mockMvc
                .perform(post("/customers/add")
                        .param("name", "someName")
                        .param("birthDate", "1881-01-12")
                )
                .andExpect(view().name("redirect:all"));
    }

    @Test
    @WithMockUser
    public void addCustomer_ShouldSaveCustomer() throws Exception {
        customerRepository.deleteAll();

        mockMvc
                .perform(post("/customers/add")
                        .param("name", "someName")
                        .param("birthDate", "1881-01-12")
                );
        Customer customer = customerRepository.findAll().get(0);

        Assert.assertEquals("someName", customer.getName());
        Assert.assertEquals("1881-01-12", customer.getBirthDate().toString());
    }

}
