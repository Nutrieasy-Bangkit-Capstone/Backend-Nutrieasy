package nutrieasy.backend.repository;

import nutrieasy.backend.entity.UserMealHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMealHistoryRepository extends JpaRepository<UserMealHistory, Long> {
    @Query("SELECT u FROM UserMealHistory u WHERE u.user.uid = ?1")
    UserMealHistory findByAllByUserId(String userId);
}
