package nutrieasy.backend.repository;

import nutrieasy.backend.entity.User;
import nutrieasy.backend.entity.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {

    List<UserHistory> findAllByUser(User user);

    List<UserHistory> findAllByUserAndCreatedAtBetween(User user, Timestamp date, Timestamp date2);


    @Query("SELECT uh FROM UserHistory uh WHERE uh.user = ?1 AND DATE(uh.createdAt) = DATE(?2)")
    List<UserHistory> findAllByUserAndDate(User user, Date date);
}
