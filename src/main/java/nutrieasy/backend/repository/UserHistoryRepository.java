package nutrieasy.backend.repository;

import nutrieasy.backend.entity.User;
import nutrieasy.backend.entity.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {

    UserHistory findAllByUser(User user);
}
