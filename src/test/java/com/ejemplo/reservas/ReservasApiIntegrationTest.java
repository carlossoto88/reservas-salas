package com.ejemplo.reservas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de integración: arranca la aplicación completa (con H2 y las migraciones de Flyway)
 * y hace peticiones HTTP reales con MockMvc.
 *
 * @Transactional deshace los cambios al terminar cada test, así no se pisan entre ellos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservasApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void flujoCompleto_crearReservar409CancelarYVolverAReservar() throws Exception {
        long salaId = crearSala("Sala Roja");

        // Reserva de 10:00 a 11:00 -> 201
        long reservaId = crearReserva(salaId, "2030-01-15T10:00:00", "2030-01-15T11:00:00", 201);

        // Otra que se solapa (10:30 a 11:30) -> 409
        mockMvc.perform(post("/api/reservas").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonReserva(salaId, "2030-01-15T10:30:00", "2030-01-15T11:30:00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("ya está ocupada")));

        // Una que empieza justo cuando acaba la primera (11:00 a 12:00) -> 201
        crearReserva(salaId, "2030-01-15T11:00:00", "2030-01-15T12:00:00", 201);

        // Cancelamos la primera -> 204
        mockMvc.perform(delete("/api/reservas/" + reservaId).with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());

        // Cancelarla otra vez -> 409
        mockMvc.perform(delete("/api/reservas/" + reservaId).with(httpBasic("admin", "admin123")))
                .andExpect(status().isConflict());

        // Ese hueco ya está libre -> 201
        crearReserva(salaId, "2030-01-15T10:00:00", "2030-01-15T11:00:00", 201);
    }

    @Test
    void listadoFiltraPorSalaYFechaYPagina() throws Exception {
        long salaA = crearSala("Sala A");
        long salaB = crearSala("Sala B");
        crearReserva(salaA, "2030-02-01T09:00:00", "2030-02-01T10:00:00", 201);
        crearReserva(salaA, "2030-02-01T11:00:00", "2030-02-01T12:00:00", 201);
        crearReserva(salaA, "2030-02-02T09:00:00", "2030-02-02T10:00:00", 201);
        crearReserva(salaB, "2030-02-01T09:00:00", "2030-02-01T10:00:00", 201);

        // Sala A el 1 de febrero, de 1 en 1: hay 2 reservas -> 2 páginas
        mockMvc.perform(get("/api/reservas")
                        .param("salaId", String.valueOf(salaA))
                        .param("fecha", "2030-02-01")
                        .param("pagina", "0")
                        .param("tamano", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.contenido[0].inicio").value("2030-02-01T09:00:00"));
    }

    @Test
    void crearSinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(post("/api/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "Sala sin permiso", "capacidad": 4 }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void datosInvalidosDevuelven400ConElDetalle() throws Exception {
        mockMvc.perform(post("/api/salas").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "", "capacidad": 0 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.errores.capacidad").value("La capacidad debe ser al menos 1"));
    }

    // ---------- Métodos de ayuda ----------

    private long crearSala(String nombre) throws Exception {
        String respuesta = mockMvc.perform(post("/api/salas").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nombre\": \"" + nombre + "\", \"capacidad\": 6 }"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return leerId(respuesta);
    }

    private long crearReserva(long salaId, String inicio, String fin, int estadoEsperado) throws Exception {
        String respuesta = mockMvc.perform(post("/api/reservas").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonReserva(salaId, inicio, fin)))
                .andExpect(status().is(estadoEsperado))
                .andReturn().getResponse().getContentAsString();
        return leerId(respuesta);
    }

    private String jsonReserva(long salaId, String inicio, String fin) {
        return """
                { "salaId": %d, "responsable": "Ana", "inicio": "%s", "fin": "%s" }
                """.formatted(salaId, inicio, fin);
    }

    private long leerId(String json) throws Exception {
        JsonNode nodo = objectMapper.readTree(json);
        return nodo.get("id").asLong();
    }
}
