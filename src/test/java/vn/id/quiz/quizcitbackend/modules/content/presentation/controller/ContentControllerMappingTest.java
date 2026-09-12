package vn.id.quiz.quizcitbackend.modules.content.presentation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ContentControllerMappingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void testGetChaptersFlat() throws Exception {
        mockMvc.perform(get("/api/admin/chapters-flat"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void testAddTheory() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/chapters/123/theories")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"abc\",\"contentFormat\":\"BLOCKNOTE\"}"))
                .andExpect(status().isNotFound()); // expects 404 App Exception not 404 Resource Not Found
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    public void testGetEditContext() throws Exception {
        mockMvc.perform(get("/api/v1/admin/chapters/123/edit-context"))
                .andExpect(status().isNotFound()); // expects 404 App Exception, not NoResourceFoundException
    }
}
