package backend.academy.linktracker.scrapper.links.repository;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "app.access-type=jdbc",
    "spring.autoconfigure.exclude=" +
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
public class JdbcLinkRepositoryIntegrationTest extends LinkRepositoryIntegrationTest {}
