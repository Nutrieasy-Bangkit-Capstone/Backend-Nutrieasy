package nutrieasy.backend.repository;

import nutrieasy.backend.entity.User;
import nutrieasy.backend.entity.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {

    List<UserHistory> findAllByUser(User user);

    List<UserHistory> findAllByUserAndCreatedAtBetween(User user, Timestamp date, Timestamp date2);
}
