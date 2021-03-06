package cn.jerryshell.polls;

import cn.jerryshell.polls.payload.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PollsApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testA_AuthControllerAndUserController() throws Exception {
        // find
        mockMvc.perform(get("/users/username/admin"))
                .andExpect(jsonPath("$.username").value("admin"));
        mockMvc.perform(get("/users/usernameOrEmail/admin@email.com"))
                .andExpect(jsonPath("$.username").value("admin"));
        mockMvc.perform(get("/users/usernameOrEmail/none"))
                .andExpect(status().isNotFound());

        // login
        ObjectMapper objectMapper = new ObjectMapper();
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername("admin");
        loginForm.setPassword("123");
        String token = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(loginForm)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(get("/auth/verify").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));

        // register
        RegisterForm registerForm = new RegisterForm();
        registerForm.setUsername("new_user");
        registerForm.setPassword("111");
        registerForm.setPassword2("111");
        registerForm.setEmail("new_user@email.com");
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(registerForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("new_user"));
    }

    @Test
    public void testB_PollController() throws Exception {
        // find
        mockMvc.perform(get("/polls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/polls/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("One or Two"));

        // status
        mockMvc.perform(get("/polls/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].choiceId").value(1))
                .andExpect(jsonPath("$[0].choiceCount").value(2));

        // get token
        ObjectMapper objectMapper = new ObjectMapper();
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername("admin");
        loginForm.setPassword("123");
        String token = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(loginForm)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // create new poll
        CreateNewPollForm createNewPollForm = new CreateNewPollForm();
        createNewPollForm.setQuestion("new question");
        mockMvc.perform(post("/polls")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(createNewPollForm))
                .header("Authorization", token))
                .andExpect(status().isOk());

        // update poll
        UpdatePollForm updatePollForm = new UpdatePollForm();
        updatePollForm.setQuestion("update question");
        mockMvc.perform(put("/polls/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(updatePollForm))
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("update question"))
                .andExpect(jsonPath("$.id").value(1));

        // delete poll
        mockMvc.perform(delete("/polls/2").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/polls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void testC_ChoiceController() throws Exception {
        // find
        mockMvc.perform(get("/polls/1/choices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("One"));

        // get token
        ObjectMapper objectMapper = new ObjectMapper();
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername("admin");
        loginForm.setPassword("123");
        String token = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(loginForm)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // create new choice
        CreateNewChoiceForm createNewChoiceForm = new CreateNewChoiceForm();
        createNewChoiceForm.setText("new choice");
        mockMvc.perform(post("/polls/1/choices")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(createNewChoiceForm))
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("new choice"));

        // update choice
        UpdateChoiceForm updateChoiceForm = new UpdateChoiceForm();
        updateChoiceForm.setText("update choice");
        mockMvc.perform(put("/polls/1/choices/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(updateChoiceForm))
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("update choice"));

        // delete choice
        mockMvc.perform(delete("/polls/1/choices/1").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/polls/1/choices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

}
