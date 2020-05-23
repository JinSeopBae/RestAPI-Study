package study.seop.restapi.events;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;
import study.seop.restapi.accounts.Account;
import study.seop.restapi.accounts.AccountRepository;
import study.seop.restapi.accounts.AccountRole;
import study.seop.restapi.accounts.AccountService;
import study.seop.restapi.common.AppProperties;
import study.seop.restapi.common.BaseControllerTest;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class EventControllerTests extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;

    @Before
    public void setUp() {
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

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
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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
                    relaxedResponseFields(
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

    private String getBearerToken() throws Exception {
        return "Bearer " + getAccessToken();
    }

    private String getAccessToken() throws Exception {
        // Given
        Account account = Account.builder()
                .email(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();

        this.accountService.saveAccount(account);

        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password"));

        var responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
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
                .basePrice(1000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("시흥대로 53")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.FUBLISHED)
                .build();

        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("_links.index").exists());
    }

    @Test
    public void getEvents() throws Exception{
        // Given
        IntStream.range(0,30).forEach(this::generateEvent);

        // When // Then
        this.mockMvc.perform(get("/api/events")
                    .param("page", "1")
                    .param("size", "10")
                    .param("sort", "name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events"));
    }

    @Test
    public void getEvent() throws Exception {
        // Given
        Event event = this.generateEvent(100);
        this.mockMvc.perform(get("/api/events/{id}",event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"));

    }

    @Test
    public void getEvent404() throws Exception {
        // Given
        Event event = this.generateEvent(100);
        this.mockMvc.perform(get("/api/events/3220",event.getId()))
                .andExpect(status().isNotFound());


    }

    @Test
    public void updateEvent() throws Exception {
        // Given

        Event event = this.generateEvent(200);
        String eventName = "Updated Event";
        EventDto eventDto = this.modelMapper.map(event,EventDto.class);
        eventDto.setName(eventName);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("name").value(eventName))
            .andExpect(jsonPath("_links.self").exists());
    }

    @Test
    public void updateEvent400emtpy() throws Exception {
        // Given

        Event event = this.generateEvent(200);
        EventDto eventDto = new EventDto();

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateEvent400wrong() throws Exception {
        // Given

        Event event = this.generateEvent(200);
        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateEvent404() throws Exception {
        // Given
        Event event = this.generateEvent(200);
        EventDto eventDto = modelMapper.map(event, EventDto.class);

        // When & Then
        this.mockMvc.perform(put("/api/events/123123123")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private Event generateEvent(int i) {
        Event event = Event.builder()
                .name("event" + i)
                .description("rest api study")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,04,07,00,00,00))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,10,07,00,00,00))
                .beginEventDateTime(LocalDateTime.of(2020,04,07,00,00,00))
                .endEventDateTime(LocalDateTime.of(2020,10,07,00,00,00))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("....")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();

        return this.eventRepository.save(event);
    }
}
