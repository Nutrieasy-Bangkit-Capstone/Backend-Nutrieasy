package nutrieasy.backend.repository;

import nutrieasy.backend.entity.Nutrients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutrientsRepository extends JpaRepository<Nutrients, Integer>{
    @Override
    List<Nutrients> findAllById(Iterable<Integer> integers);
}
