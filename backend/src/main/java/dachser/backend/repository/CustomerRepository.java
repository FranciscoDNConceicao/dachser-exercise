package dachser.backend.repository;

import dachser.backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access for {@link Customer}.
 * Basic CRUD operations are provided by {@link JpaRepository}.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
