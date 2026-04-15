package backend.academy.linktracker.scrapper;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.access-type=jpa")
class JpaSubscriptionTest extends SubscriptionIntegrationTest {}
