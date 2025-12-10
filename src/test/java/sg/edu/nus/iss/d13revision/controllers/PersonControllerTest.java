package sg.edu.nus.iss.d13revision.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import sg.edu.nus.iss.d13revision.models.Person;
import sg.edu.nus.iss.d13revision.models.PersonForm;
import sg.edu.nus.iss.d13revision.services.PersonService;

@WebMvcTest(PersonController.class)
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    // Hardcode messages from application.properties for assertions
    private final String WELCOME_MESSAGE = "Spring Boot & Thymeleaf Revision";
    private final String ERROR_MESSAGE = "First Name & Last Name are required!";

    @Test
    public void testIndex() throws Exception {
        mockMvc.perform(get("/person"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("message", WELCOME_MESSAGE));

        mockMvc.perform(get("/person/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("message", WELCOME_MESSAGE));

        mockMvc.perform(get("/person/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("message", WELCOME_MESSAGE));
    }

    @Test
    public void testGetAllPersons() throws Exception {
        List<Person> mockPersons = Arrays.asList(
                new Person("1", "John", "Doe"),
                new Person("2", "Jane", "Smith"));
        when(personService.getPersons()).thenReturn(mockPersons);

        mockMvc.perform(get("/person/testRetrieve"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
                // .andExpect(jsonPath("$.length()").value(2))
                // .andExpect(jsonPath("$[0].firstName").value("John"))
                // .andExpect(jsonPath("$[1].lastName").value("Smith"));

        verify(personService, times(1)).getPersons();
    }

    @Test
    public void testPersonList() throws Exception {
        List<Person> mockPersons = Arrays.asList(
                new Person("1", "John", "Doe"),
                new Person("2", "Jane", "Smith"));
        when(personService.getPersons()).thenReturn(mockPersons);

        mockMvc.perform(get("/person/personList"))
                .andExpect(status().isOk())
                .andExpect(view().name("personList"))
                .andExpect(model().attributeExists("persons"))
                .andExpect(model().attribute("persons", mockPersons));

        verify(personService, times(1)).getPersons();
    }

    @Test
    public void testShowAddPersonPage() throws Exception {
        mockMvc.perform(get("/person/addPerson"))
                .andExpect(status().isOk())
                .andExpect(view().name("addPerson"))
                .andExpect(model().attributeExists("personForm"))
                .andExpect(model().attribute("personForm", any(PersonForm.class)));
    }

    @Test
    public void testSavePerson_Success() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "New")
                .param("lastName", "Person")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound()) // 302 for redirect
                .andExpect(redirectedUrl("/person/personList"));

        verify(personService, times(1)).addPerson(any(Person.class));
    }

    @Test
    public void testSavePerson_Error() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                .param("firstName", "") // Empty first name
                .param("lastName", "Person")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("addPerson"))
                .andExpect(model().attribute("errorMessage", ERROR_MESSAGE));

        verify(personService, times(0)).addPerson(any(Person.class)); // Should not call addPerson
    }

    @Test
    public void testPersonToEdit() throws Exception {
        Person personToEdit = new Person("3", "Edit", "Me");
        mockMvc.perform(post("/person/personToEdit")
                .param("id", personToEdit.getId())
                .param("firstName", personToEdit.getFirstName())
                .param("lastName", personToEdit.getLastName())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("editPerson"))
                .andExpect(model().attributeExists("per"))
                .andExpect(model().attribute("per", personToEdit));
    }

    @Test
    public void testPersonEdit() throws Exception {
        Person personToUpdate = new Person("3", "Updated", "Person");
        mockMvc.perform(post("/person/personEdit")
                .param("id", personToUpdate.getId())
                .param("firstName", personToUpdate.getFirstName())
                .param("lastName", personToUpdate.getLastName())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound()) // 302 for redirect
                .andExpect(redirectedUrl("/person/personList"));

        verify(personService, times(1)).updatePerson(any(Person.class));
    }

    @Test
    public void testPersonDelete() throws Exception {
        Person personToDelete = new Person("4", "Delete", "Me");
        mockMvc.perform(post("/person/personDelete")
                .param("id", personToDelete.getId())
                .param("firstName", personToDelete.getFirstName())
                .param("lastName", personToDelete.getLastName())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound()) // 302 for redirect
                .andExpect(redirectedUrl("/person/personList"));

        verify(personService, times(1)).removePerson(any(Person.class));
    }
}
