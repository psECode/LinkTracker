package backend.academy.linktracker.bot.test.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.bot.commands.ListCommand;
import backend.academy.linktracker.bot.domain.api.ScrapperClient;
import backend.academy.linktracker.bot.domain.api.dtos.LinkResponse;
import backend.academy.linktracker.bot.domain.api.dtos.ListLinksResponse;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

@ExtendWith(MockitoExtension.class)
class ListCommandTest {

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ListCommand listCommand;

    @Test
    void happyListTest() {
        var link1 = new LinkResponse(1L, URI.create("https://1"), List.of("java"));
        var link2 = new LinkResponse(2L, URI.create("https://2"), List.of("петухон"));

        when(scrapperClient.getAllLinks(anyLong())).thenReturn(new ListLinksResponse(List.of(link1, link2), 2));

        String result = listCommand.execute(123L, "/list");

        assertThat(result).contains("https://1");
        assertThat(result).contains("https://2");
    }

    @Test
    void emptyListTest() {
        when(scrapperClient.getAllLinks(anyLong())).thenReturn(new ListLinksResponse(List.of(), 0));
        when(messageSource.getMessage(eq("bot.command.list.empty"), any(), any()))
                .thenReturn("Список пуст");

        String result = listCommand.execute(123L, "/list");

        assertThat(result).isEqualTo("Список пуст");
    }

    @Test
    void tagListTest() {
        var link1 = new LinkResponse(1L, URI.create("https://1"), List.of("java"));
        var link2 = new LinkResponse(2L, URI.create("https://2"), List.of("петухон"));

        when(scrapperClient.getAllLinks(anyLong())).thenReturn(new ListLinksResponse(List.of(link1, link2), 2));

        String result = listCommand.execute(123L, "/list java");

        assertThat(result).contains("https://1");
        assertThat(result).doesNotContain("https://2");
    }
}
