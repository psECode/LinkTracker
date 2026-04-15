package backend.academy.linktracker.scrapper;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.access-type=jdbc")
class JdbcSubscriptionTest extends SubscriptionIntegrationTest {}
