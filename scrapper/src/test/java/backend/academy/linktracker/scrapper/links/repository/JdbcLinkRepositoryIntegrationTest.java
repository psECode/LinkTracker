package backend.academy.linktracker.scrapper.links.repository;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(
        properties = {"app.access-type=jdbc", "spring.jpa.hibernate.ddl-auto=none", "spring.liquibase.enabled=false"})
@EnableAutoConfiguration(
        excludeName = {
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
            "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@ActiveProfiles({"test", "jdbc"})
public class JdbcLinkRepositoryIntegrationTest extends LinkRepositoryIntegrationTest {}
