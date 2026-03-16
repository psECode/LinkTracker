package backend.academy.linktracker.scrapper.updaters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.domain.api.stackoverflow.StackoverflowClient;
import backend.academy.linktracker.scrapper.domain.api.stackoverflow.StackoverflowItem;
import backend.academy.linktracker.scrapper.domain.api.stackoverflow.StackoverflowResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.StackoverflowLinkChecker;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StackoverflowCheckerTest {

    @Mock
    private StackoverflowClient stackoverflowClient;

    @InjectMocks
    private StackoverflowLinkChecker checker;

    @Test
    void happyTest() {
        String url = "https://stackoverflow.com/questions/12345/some-slug";

        when(stackoverflowClient.getQuestion("12345"))
                .thenReturn(new StackoverflowResponse(List.of(new StackoverflowItem(1710321600L, 12345L))));

        OffsetDateTime result = checker.getLastUpdatedDate(url);

        assertThat(result).isNotNull();
    }

    @Test
    void emptyItemsTest() {
        String url = "https://stackoverflow.com/questions/12345";
        StackoverflowResponse emptyResponse = new StackoverflowResponse(List.of());

        when(stackoverflowClient.getQuestion(anyString())).thenReturn(emptyResponse);

        OffsetDateTime result = checker.getLastUpdatedDate(url);
        assertThat(result).isNull();
    }
}
