package backend.academy.linktracker.bot.domain.bot;

public interface CommandInterface {
    CommandType getCommandType();

    String execute(Long chatId, String text);

    default String getMenuName() {
        return null;
    }

    default String getMenuDescription() {
        return null;
    }
}
