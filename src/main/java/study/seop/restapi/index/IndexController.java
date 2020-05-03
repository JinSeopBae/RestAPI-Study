package study.seop.restapi.index;


import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.seop.restapi.events.Event;
import study.seop.restapi.events.EventController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class IndexController {

    @GetMapping("/api")
    public RepresentationModel index() {
        var index = new RepresentationModel();
        index.add(linkTo(EventController.class).withRel("events"));

        return index;
    }
}
