package demo.games.dao;

import demo.games.model.GameState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, String> {

  List<Game> findByStateEquals( final GameState state );

  Optional<Game> findByCodeAndStateEquals( final String code, final GameState state );
}
