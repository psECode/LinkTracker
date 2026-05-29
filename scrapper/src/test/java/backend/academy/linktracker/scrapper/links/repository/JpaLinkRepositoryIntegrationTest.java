package backend.academy.linktracker.scrapper.links.repository;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {"app.access-type=jpa"})
public class JpaLinkRepositoryIntegrationTest extends LinkRepositoryIntegrationTest {}
