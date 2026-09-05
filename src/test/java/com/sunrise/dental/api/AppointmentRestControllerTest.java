package com.sunrise.dental.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AppointmentRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void health_returnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void createAppointment_thenFetchByNumber_returnsCreatedRecord() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("patientName", "Ruwan Bandara");
        payload.put("address", "10 Main Street, Negombo");
        payload.put("contactNo", "0709876543");
        payload.put("dentistName", "Dr. Rajapaksha");
        payload.put("treatmentType", "Consultation");
        payload.put("appointmentDate", "2026-12-01");
        payload.put("appointmentTime", "09:00:00");

        String response = mockMvc.perform(post("/api/appointments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentNo").exists())
                .andReturn().getResponse().getContentAsString();

        String appointmentNo = objectMapper.readTree(response).get("appointmentNo").asText();

        mockMvc.perform(get("/api/appointments/" + appointmentNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientName").value("Ruwan Bandara"));
    }
}
