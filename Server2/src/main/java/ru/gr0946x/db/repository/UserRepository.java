package ru.gr0946x.db.repository;

import ru.gr0946x.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // Поиск пользователя по имени (без учёта регистра)
    Optional<User> findByNicknameIgnoreCase(String nickname);

    // Существует ли пользователь с таким именем
    boolean existsByNicknameIgnoreCase(String nickname);
}
