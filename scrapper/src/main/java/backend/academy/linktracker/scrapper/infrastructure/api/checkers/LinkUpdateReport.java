package backend.academy.linktracker.scrapper.infrastructure.api.checkers;

import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import java.util.List;

public record LinkUpdateReport(
    Link link,
    List<UpdateDescription> updates,
    String errorMessage
) {}
