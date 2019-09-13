package org.softuni.cardealer.web.controllers;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.softuni.cardealer.domain.entities.Part;
import org.softuni.cardealer.domain.entities.Supplier;
import org.softuni.cardealer.repository.PartRepository;
import org.softuni.cardealer.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class PartControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    private Part first;
    private Part second;

    private Supplier firstSupplier;
    private Supplier secondSupplier;


    private void initSuppliers() {
        firstSupplier = new Supplier();
        firstSupplier.setName("firstSupplier");
        firstSupplier.setIsImporter(true);

        secondSupplier = new Supplier();
        secondSupplier.setName("secondSupplier");
        secondSupplier.setIsImporter(false);
    }

    private void saveTheTwoSuppliersWithIds() {
        initSuppliers();

        firstSupplier = supplierRepository.saveAndFlush(firstSupplier);
        secondSupplier = supplierRepository.saveAndFlush(secondSupplier);
    }

    private void initParts() {
        saveTheTwoSuppliersWithIds();

        first = new Part();
        first.setName("firstPart");
        first.setPrice(BigDecimal.ONE);
        first.setSupplier(firstSupplier);

        second = new Part();
        second.setName("secondPart");
        second.setPrice(BigDecimal.TEN);
        second.setSupplier(secondSupplier);
    }

    private void saveTheTwoPartsWithIds() {
        initParts();

        first = partRepository.save(first);
        second = partRepository.saveAndFlush(second);
    }


    // Testing Logic

    @Test
    @WithMockUser
    public void allParts_ShouldReturnCorrectView() throws Exception {
        mockMvc
                .perform(get("/parts/all"))
                .andExpect(view().name("all-parts"));
    }

    @Test
    @WithMockUser
    public void addPart_SaveCorrectPart_RedirectCorrectView() throws Exception {
        partRepository.deleteAll();
        supplierRepository.deleteAll();
        saveTheTwoSuppliersWithIds();

        mockMvc
                .perform(post("/parts/add")
                        .param("name", "somePart")
                        .param("price", "1")
                        .param("supplier", "firstSupplier")
                )
                .andExpect(view().name("redirect:all"));
    }

    @Test
    @WithMockUser
    public void addPart_ShouldSavePart() throws Exception {
        partRepository.deleteAll();
        saveTheTwoSuppliersWithIds();

        mockMvc
                .perform(post("/parts/add")
                        .param("name", "somePart")
                        .param("price", "1")
                        .param("supplier", "firstSupplier")
                );
        Part part = partRepository.findAll().get(0);

        Assert.assertEquals("somePart", part.getName());
        Assert.assertEquals("1.00", String.format("%.2f",part.getPrice()));
        Assert.assertEquals(firstSupplier.getName(), part.getSupplier().getName());
        Assert.assertEquals(firstSupplier.getIsImporter(), part.getSupplier().getIsImporter());

    }

    @Test
    @WithMockUser
    public void editPart_ShouldEditPart() throws Exception {
        saveTheTwoPartsWithIds();
        saveTheTwoSuppliersWithIds();
        String partName = "someOtherName";

        mockMvc
                .perform(post("/parts/edit/" + first.getId())
                        .param("name", partName)
                        .param("price", "1")
                );

        Part part = partRepository.findById(first.getId()).orElse(null);

        assert part != null;
        Assert.assertEquals(partName, part.getName());
        Assert.assertEquals("1.00", String.format("%.2f",part.getPrice()));
    }

    @Test
    @WithMockUser
    public void editPart_EditCorrectPart_RedirectCorrectView() throws Exception {
        saveTheTwoPartsWithIds();

        mockMvc
                .perform(post("/parts/edit/" + first.getId())
                        .param("name", "somePart")
                        .param("price", "1")
                )
                .andExpect(view().name("redirect:/parts/all"));
    }

    @Test
    @WithMockUser
    public void deletePart_ShouldDeletePart() throws Exception {
        saveTheTwoPartsWithIds();
        mockMvc
                .perform(post("/parts/delete/" + first.getId())
                        .param("name", "somePart")
                        .param("price", "1")
                        .param("supplier", "firstSupplier")
                );

        Assert.assertNull(partRepository.findById(first.getId()).orElse(null));
    }

    @Test
    @WithMockUser
    public void deletePart_EditCorrectPart_RedirectCorrectView() throws Exception {
        saveTheTwoPartsWithIds();
        mockMvc
                .perform(post("/parts/delete/" + first.getId())
                        .param("name", "somePart")
                        .param("price", "1")
                        .param("supplier", "firstSupplier")
                )
                .andExpect(view().name("redirect:/parts/all"));
    }

    @Test
    @WithMockUser
    public void fetchParts_ShouldReturnAllParts() throws Exception {
        partRepository.deleteAll();
        saveTheTwoPartsWithIds();

        String actual = mockMvc
                .perform(get("/parts/fetch/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expected = "[{\"id\":\"" + first.getId() + "\",\"name\":\"firstPart\",\"price\":1.00," +
                "\"supplier\":{\"id\":\"" + firstSupplier.getId() + "\",\"name\":\"firstSupplier\",\"isImporter\":true}}," +
                            "{\"id\":\"" + second.getId() + "\",\"name\":\"secondPart\",\"price\":10.00," +
                "\"supplier\":{\"id\":\"" + secondSupplier.getId() + "\",\"name\":\"secondSupplier\",\"isImporter\":false}}]";

        Assert.assertEquals(expected, actual);
    }
}
