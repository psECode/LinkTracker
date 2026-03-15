package backend.academy.linktracker.bot.application.bot.commands;

import backend.academy.linktracker.bot.domain.api.ScrapperClient;
import backend.academy.linktracker.bot.domain.api.dtos.LinkResponse;
import backend.academy.linktracker.bot.domain.api.dtos.ListLinksResponse;
import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import backend.academy.linktracker.bot.domain.bot.CommandType;
import backend.academy.linktracker.bot.domain.bot.LinkListFormattingUtil;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListCommand implements CommandInterface {
    private final ScrapperClient scrapperClient;
    private final MessageSource messageSource;

    @Override
    public String execute(Long chatId, String text) {
        String[] parts = text.trim().split("\\s+", 2);
        String filterTag = (parts.length > 1) ? parts[1].trim() : null;

        try {
            ListLinksResponse response = scrapperClient.getAllLinks(chatId);

            if (response.links().isEmpty()) {
                return messageSource.getMessage("bot.command.list.empty", null, Locale.of("ru"));
            }

            List<LinkResponse> filteredLinks = response.links().stream()
                    .filter(link -> filterTag == null || link.tags().contains(filterTag))
                    .toList();

            if (filteredLinks.isEmpty()) {
                return messageSource.getMessage("bot.command.list.no_matches", null, Locale.of("ru"));
            }

            return LinkListFormattingUtil.execute(filteredLinks);
        } catch (Exception e) {
            return messageSource.getMessage("bot.error", null, Locale.of("ru"));
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.LIST;
    }

    @Override
    public String getMenuName() {
        return "list";
    }

    @Override
    public String getMenuDescription() {
        return "Показать список ссылок";
    }
}
