package backend.academy.linktracker.scrapper.infrastructure.api.dtos;

public record PushSubscribeRequest(String endpoint, PushKeys keys) {

    public record PushKeys(String p256dh, String auth) {}
}
