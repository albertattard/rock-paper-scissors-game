package demo.games.pvp;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends CrudRepository<Game, String> {

  List<Game> findByStateEquals( final GameState state );

  Optional<Game> findByCodeAndStateEquals( final String code, final GameState state );
}
