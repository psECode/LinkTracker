package backend.academy.linktracker.scrapper.links;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.domain.links.LinkType;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class LinkTypeTest {
    @Test
    void linkType() {
        String url1 = "https://github.com/user/repo";
        String url2 = "https://www.github.com/user/repo/";
        String url3 = "https://stackoverflow.com/questions/123456/love-test";
        String url4 = "https://google.com";
        String url5 = "https://github.com/user";
        String url6 = null;

        Optional<LinkType> linkType1 = LinkType.of(url1);
        Optional<LinkType> linkType2 = LinkType.of(url2);
        Optional<LinkType> linkType3 = LinkType.of(url3);
        Optional<LinkType> linkType4 = LinkType.of(url4);
        Optional<LinkType> linkType5 = LinkType.of(url5);
        Optional<LinkType> linkType6 = LinkType.of(url6);

        assertThat(linkType1).isPresent();
        assertThat(linkType2).isPresent();
        assertThat(linkType3).isPresent();

        assertThat(linkType1.get()).isEqualTo(LinkType.GITHUB);
        assertThat(linkType2.get()).isEqualTo(LinkType.GITHUB);
        assertThat(linkType3.get()).isEqualTo(LinkType.STACKOVERFLOW);

        assertThat(linkType4).isEmpty();
        assertThat(linkType5).isEmpty();
        assertThat(linkType6).isEmpty();
    }
}
