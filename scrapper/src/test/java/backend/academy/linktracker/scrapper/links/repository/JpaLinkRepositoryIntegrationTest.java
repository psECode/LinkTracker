package backend.academy.linktracker.scrapper.links.repository;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Disabled
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(
        properties = {"app.access-type=jpa", "spring.jpa.hibernate.ddl-auto=update", "spring.liquibase.enabled=false"})
public class JpaLinkRepositoryIntegrationTest extends LinkRepositoryIntegrationTest {}
