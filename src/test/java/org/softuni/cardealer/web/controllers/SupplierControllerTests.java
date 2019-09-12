package org.softuni.cardealer.web.controllers;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.softuni.cardealer.domain.entities.Supplier;
import org.softuni.cardealer.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class SupplierControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRepository supplierRepository;


    private Supplier first;
    private Supplier second;

    private void initSuppliers() {
        first = new Supplier();
        first.setName("first");
        first.setIsImporter(true);

        second = new Supplier();
        second.setName("second");
        second.setIsImporter(false);
    }

    private void saveTheTwoSuppliersWithIds() {
        initSuppliers();
        first = supplierRepository.save(first);
        second = supplierRepository.save(second);
    }


    // Testing Logic

    @Test
    @WithMockUser
    public void allSuppliers_ShouldReturnCorrectView() throws Exception {
        mockMvc
                .perform(get("/suppliers/all"))
                .andExpect(view().name("all-suppliers"));
    }

    @Test
    @WithMockUser
    public void addSupplier_SaveCorrectSupplier_RedirectCorrectView() throws Exception {
        mockMvc
                .perform(post("/suppliers/add")
                        .param("name", "someSupplier")
                        .param("isImporter", "true")
                )
                .andExpect(view().name("redirect:all"));
    }

    @Test
    @WithMockUser
    public void addSupplier_ShouldSaveSupplier() throws Exception {
        supplierRepository.deleteAll();
        mockMvc
                .perform(post("/suppliers/add")
                        .param("name", "someSupplier")
                        .param("isImporter", "on")
                );
        Supplier supplier = supplierRepository.findAll().get(0);

        Assert.assertEquals("someSupplier", supplier.getName());
        Assert.assertTrue(supplier.getIsImporter());
    }

    @Test
    @WithMockUser
    public void editSupplier_ShouldEditSupplier() throws Exception {
        saveTheTwoSuppliersWithIds();
        String supplierName = "someOtherName";
        mockMvc
                .perform(post("/suppliers/edit/" + first.getId())
                        .param("name", supplierName)
                        .param("isImporter", "false")
                );

        Supplier supplier = supplierRepository.findById(first.getId()).orElse(null);

        assert supplier != null;
        Assert.assertEquals(supplierName, supplier.getName());
        Assert.assertFalse(supplier.getIsImporter());
    }

    @Test
    @WithMockUser
    public void editSupplier_EditCorrectSupplier_RedirectCorrectView() throws Exception {
        saveTheTwoSuppliersWithIds();
        mockMvc
                .perform(post("/suppliers/edit/" + first.getId())
                        .param("name", "supplierName")
                        .param("isImporter", "false")
                )
                .andExpect(view().name("redirect:/suppliers/all"));
    }

    @Test
    @WithMockUser
    public void deleteSupplier_ShouldDeleteSupplier() throws Exception {
        saveTheTwoSuppliersWithIds();
        mockMvc
                .perform(post("/suppliers/delete/" + first.getId())
                        .param("name", "supplierName")
                        .param("isImporter", "false")
                );

        Assert.assertNull(supplierRepository.findById(first.getId()).orElse(null));
    }

    @Test
    @WithMockUser
    public void deleteSupplier_EditCorrectSupplier_RedirectCorrectView() throws Exception {
        saveTheTwoSuppliersWithIds();
        mockMvc
                .perform(post("/suppliers/delete/" + first.getId())
                        .param("name", "supplierName")
                        .param("isImporter", "false")
                )
                .andExpect(view().name("redirect:/suppliers/all"));
    }

    @Test
    @WithMockUser
    public void fetchSuppliers_ShouldReturnAllSuppliers() throws Exception {
        supplierRepository.deleteAll();
        saveTheTwoSuppliersWithIds();

        String actual = mockMvc
                .perform(get("/suppliers/fetch/"))
                .andExpect(status().isOk())
                .andReturn()
                    .getResponse()
                    .getContentAsString();

        String expected = "[{\"id\":\"" + first.getId() + "\",\"name\":\"first\",\"isImporter\":true}," +
                            "{\"id\":\"" + second.getId() + "\",\"name\":\"second\",\"isImporter\":false}]";

        Assert.assertEquals(expected, actual);
    }

}
