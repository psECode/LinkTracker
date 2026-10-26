package backend.academy.linktracker.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SensitiveDataMaskerTest {

    private final SensitiveDataMasker masker = new SensitiveDataMasker();

    @Test
    void shouldPartiallyMaskEmail() {
        assertEquals("j*******@example.com", masker.mask("john.doe@example.com"));
        assertEquals("User *@yandex.ru has subscribed", masker.mask("User a@yandex.ru has subscribed"));
    }

    @Test
    void shouldRedactTelegramBotToken() {
        assertEquals(
                "Using token [REDACTED]", masker.mask("Using token 6482739128:AAF3fjLmN0pQrStUvWxYz1234567890ABCDE"));
    }

    @Test
    void shouldRedactJwt() {
        assertEquals(
                "Got jwt [REDACTED]",
                masker.mask(
                        "Got jwt eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dBjftJeZ4CVPmB92K27uhbUJU1p1r_wW1gFWFOEjXk"));
    }

    @Test
    void shouldRedactBearerToken() {
        assertEquals("Authorization: Bearer [REDACTED]", masker.mask("Authorization: Bearer abcdef0123456789"));
    }

    @Test
    void shouldRedactGithubToken() {
        assertEquals("github token [REDACTED]", masker.mask("github token ghp_0123456789abcdefghijklmnopqrstuvwxyz"));
    }

    @Test
    void shouldRedactStackexchangeKeyButKeepParameterName() {
        String masked = masker.mask(
                "Requesting StackExchange API: https://api.stackexchange.com/2.3/search?site=stackoverflow&key=SECRET_KEY_123");
        assertFalse(masked.contains("SECRET_KEY_123"));
        assertTrue(masked.contains("site=stackoverflow"));
        assertTrue(masked.contains("key=[REDACTED]"));
    }

    @Test
    void shouldRedactAccessTokenParam() {
        String masked = masker.mask("url https://example.com/callback?access_token=top-secret&x=1");
        assertFalse(masked.contains("top-secret"));
        assertTrue(masked.contains("access_token=[REDACTED]"));
        assertTrue(masked.contains("x=1"));
    }

    @Test
    void shouldRedactPasswordParam() {
        String masked = masker.mask("url https://example.com/callback?password=hunter2&x=1");
        assertFalse(masked.contains("hunter2"));
        assertTrue(masked.contains("password=[REDACTED]"));
        assertTrue(masked.contains("x=1"));
    }

    @Test
    void shouldRedactWebPushEndpointToken() {
        assertEquals(
                "Web Push доставлен на https://fcm.googleapis.com/fcm/send/[REDACTED]: OK",
                masker.mask("Web Push доставлен на https://fcm.googleapis.com/fcm/send/eJwT0AiTokEn12345: OK"));
    }

    @Test
    void shouldKeepRegularNumbersAndTimestamps() {
        String line = "chatId 123456789 got 2026-10-24T23:00:00.123Z with 100 updates";
        assertEquals(line, masker.mask(line));
    }

    @Test
    void shouldKeepPlainTextUntouched() {
        String line = "Ссылка https://github.com/user/repo: обновлений нет";
        assertEquals(line, masker.mask(line));
    }

    @Test
    void shouldHandleNullAndEmpty() {
        assertNull(masker.mask(null));
        assertEquals("", masker.mask(""));
    }
}
