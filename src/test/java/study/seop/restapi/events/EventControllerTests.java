package study.seop.restapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import study.seop.restapi.common.RestDocsConfiguration;

import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test")
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void createEvent() throws Exception {

        EventDto event = EventDto.builder()
                    .name("spring")
                    .description("rest api study")
                    .beginEnrollmentDateTime(LocalDateTime.of(2020,04,07,00,00,00))
                    .closeEnrollmentDateTime(LocalDateTime.of(2020,10,07,00,00,00))
                    .beginEventDateTime(LocalDateTime.of(2020,04,07,00,00,00))
                    .endEventDateTime(LocalDateTime.of(2020,10,07,00,00,00))
                    .basePrice(100)
                    .maxPrice(200)
                    .limitOfEnrollment(100)
                    .location("시흥대로 53")
                    .build();

        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-event").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event",
                    links(
                            linkWithRel("self").description("link to self"),
                            linkWithRel("query-event").description("link to query events"),
                            linkWithRel("update-event").description("link to update an existing"),
                            linkWithRel("profile").description("link to update an existing")
                    ),
                    requestHeaders(
                            headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                            headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                    ),
                    requestFields(
                            fieldWithPath("name").description("Name of new event"),
                            fieldWithPath("description").description("description of new event"),
                            fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of new event"),
                            fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of new event"),
                            fieldWithPath("beginEventDateTime").description("beginEventDateTime of new event"),
                            fieldWithPath("endEventDateTime").description("endEventDateTime of new event"),
                            fieldWithPath("location").description("location of new event"),
                            fieldWithPath("basePrice").description("basePrice of new event"),
                            fieldWithPath("maxPrice").description("maxPrice of new event"),
                            fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new event")
                    ),
                    responseHeaders(
                            headerWithName(HttpHeaders.LOCATION).description("Location Header"),
                            headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type")
                    ),
                    responseFields(
                            fieldWithPath("id").description("id"),
                            fieldWithPath("name").description("Name of new event"),
                            fieldWithPath("description").description("description of new event"),
                            fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of new event"),
                            fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of new event"),
                            fieldWithPath("beginEventDateTime").description("beginEventDateTime of new event"),
                            fieldWithPath("endEventDateTime").description("endEventDateTime of new event"),
                            fieldWithPath("location").description("location of new event"),
                            fieldWithPath("basePrice").description("basePrice of new event"),
                            fieldWithPath("maxPrice").description("maxPrice of new event"),
                            fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new event"),
                            fieldWithPath("free").description("free"),
                            fieldWithPath("offline").description("offline"),
                            fieldWithPath("eventStatus").description("eventStatus"),
                            fieldWithPath("_links.self.href").description("link to self"),
                            fieldWithPath("_links.query-event.href").description("link to query event list"),
                            fieldWithPath("_links.update-event.href").description("link to update existing event"),
                            fieldWithPath("_links.profile.href").description("link to update existing event")
                    )
                ));
    }

    @Test
    public void createEvent_Bad_Request() throws Exception {

        Event event = Event.builder()
                .id(100l)
                .name("spring")
                .description("rest api study")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,04,07,00,00,00))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,10,07,00,00,00))
                .beginEventDateTime(LocalDateTime.of(2020,04,07,00,00,00))
                .endEventDateTime(LocalDateTime.of(2020,10,07,00,00,00))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("시흥대로 53")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.FUBLISHED)
                .build();

        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        mockMvc.perform(post("/api/events")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {

        EventDto event = EventDto.builder()
                .name("spring")
                .description("rest api study")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,04,26,00,00,00))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,04,07,00,00,00))
                .beginEventDateTime(LocalDateTime.of(2020,04,24,00,00,00))
                .endEventDateTime(LocalDateTime.of(2020,10,23,00,00,00))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("시흥대로 53")
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists());
    }
}
