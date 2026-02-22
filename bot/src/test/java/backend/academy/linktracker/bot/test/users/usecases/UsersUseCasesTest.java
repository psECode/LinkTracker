package backend.academy.linktracker.bot.test.users.usecases;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.application.users.usecases.CreateUserUseCase;
import backend.academy.linktracker.application.users.usecases.DeleteUserUseCase;
import backend.academy.linktracker.application.users.usecases.ReadUserByTgIdUseCase;
import backend.academy.linktracker.application.users.usecases.ReadUserByUUIDUseCase;
import backend.academy.linktracker.domain.users.UsersRepository;
import backend.academy.linktracker.domain.users.dtos.CreateUserDto;
import backend.academy.linktracker.domain.users.entities.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsersUseCasesTest {

    @Mock
    private UsersRepository usersRepository;

    private CreateUserUseCase createUserUseCase;
    private DeleteUserUseCase deleteUserUseCase;
    private ReadUserByTgIdUseCase readUserByTgIdUseCase;
    private ReadUserByUUIDUseCase readUserByUUIDUseCase;

    @BeforeEach
    void setUp() {
        createUserUseCase = new CreateUserUseCase(usersRepository);
        deleteUserUseCase = new DeleteUserUseCase(usersRepository);
        readUserByTgIdUseCase = new ReadUserByTgIdUseCase(usersRepository);
        readUserByUUIDUseCase = new ReadUserByUUIDUseCase(usersRepository);
    }

    @Test
    void CreateUserUseCaseTest() {
        CreateUserDto dto = CreateUserDto.builder().chatId(100L).build();
        User expected = User.builder().chatId(100L).build();
        when(usersRepository.save(dto)).thenReturn(Optional.of(expected));

        Optional<User> result = createUserUseCase.execute(dto);

        assertThat(result).isPresent().contains(expected);
        verify(usersRepository).save(dto);
    }

    @Test
    void DeleteUserUseCaseTest() {
        UUID id = UUID.randomUUID();
        User expected = User.builder().id(id).build();
        when(usersRepository.delete(id)).thenReturn(Optional.of(expected));

        Optional<User> result = deleteUserUseCase.execute(id);

        assertThat(result).isPresent().contains(expected);
        verify(usersRepository).delete(id);
    }

    @Test
    void ReadUserByTgIdUseCaseTest() {
        Long chatId = 200L;
        User expected = User.builder().chatId(chatId).build();
        when(usersRepository.readByChatId(chatId)).thenReturn(Optional.of(expected));

        Optional<User> result = readUserByTgIdUseCase.execute(chatId);

        assertThat(result).isPresent().contains(expected);
        verify(usersRepository).readByChatId(chatId);
    }

    @Test
    void ReadUserByUUIDUseCase() {
        UUID id = UUID.randomUUID();
        User expected = User.builder().id(id).build();
        when(usersRepository.readByUUID(id)).thenReturn(Optional.of(expected));

        Optional<User> result = readUserByUUIDUseCase.execute(id);

        assertThat(result).isPresent().contains(expected);
        verify(usersRepository).readByUUID(id);
    }
}
