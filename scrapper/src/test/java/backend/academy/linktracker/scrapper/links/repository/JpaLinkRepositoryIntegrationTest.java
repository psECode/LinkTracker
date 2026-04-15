package backend.academy.linktracker.scrapper.links.repository;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"app.access-type=jpa", "spring.jpa.hibernate.ddl-auto=validate"})
public class JpaLinkRepositoryIntegrationTest extends LinkRepositoryIntegrationTest {}
