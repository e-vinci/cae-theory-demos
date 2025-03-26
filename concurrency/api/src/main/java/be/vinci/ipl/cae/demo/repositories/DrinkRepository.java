package be.vinci.ipl.cae.demo.repositories;

import be.vinci.ipl.cae.demo.models.entities.Drink;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Drink repository.
 */
@Repository
public interface DrinkRepository extends CrudRepository<Drink, Long> {

  /**
   * Find all drinks with a price less than or equal to the given price.
   *
   * @param price the price
   * @return the drinks
   */
  Iterable<Drink> findAllByPriceLessThanEqual(double price);

  /**
   * Find a drink by its id.
   *
   * @param id the id
   * @return the drink
   */
  @Override
  @NonNull
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Drink> findById(@NonNull Long id);
}
