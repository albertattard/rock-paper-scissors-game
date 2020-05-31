package demo.games.pvp;

import demo.games.shared.Hand;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static demo.games.pvp.GameState.CLOSED;
import static demo.games.pvp.GameState.OPEN;

@Service
public class PvpGameService {

  private final GameCodeService randomService;
  private final GameRepository repository;

  public PvpGameService( final GameCodeService randomService, final GameRepository repository ) {
    this.randomService = randomService;
    this.repository = repository;
  }

  public GameResponse create( final Hand player1 ) {
    final String code = randomService.nextCode( 8 );

    final Game game = new Game()
      .setCode( code )
      .setPlayer1( player1 )
      .setState( OPEN );
    repository.save( game );

    return new GameResponse( code );
  }

  public List<GameResponse> listOpenGames() {
    return repository.findByStateEquals( OPEN )
      .stream()
      .map( r -> new GameResponse( r.getCode() ) )
      .collect( Collectors.toList() );
  }

  public Optional<GameDetails> findGame( final String code ) {
    return repository.findById( code )
      .map( r -> {
          final GameDetails details = new GameDetails()
            .setCode( r.getCode() )
            .setState( r.getState() );

          if ( shouldIncludeDetails( r.getState() ) ) {
            details.setPlayer1( r.getPlayer1() )
              .setPlayer2( r.getPlayer2() )
              .setOutcome( determineOutcome( r.getPlayer1(), r.getPlayer2() ) );
          }

          return details;
        }
      );
  }

  @Transactional
  public Optional<GameDetails> play( final String code, final Hand player2 ) {
    return repository.findByCodeAndStateEquals( code, OPEN )
      .map( game -> repository.save( game.setPlayer2( player2 ).setState( CLOSED ) ) )
      .map( game -> new GameDetails()
        .setCode( game.getCode() )
        .setState( game.getState() )
        .setPlayer1( game.getPlayer1() )
        .setPlayer2( game.getPlayer2() )
        .setOutcome( determineOutcome( game.getPlayer1(), game.getPlayer2() ) )
      );
  }

  private boolean shouldIncludeDetails( final GameState state ) {
    return state == CLOSED;
  }

  private Outcome determineOutcome( final Hand player1, final Hand player2 ) {
    return player1 == player2 ? Outcome.DRAW :
      player1.beatenBy() == player2 ? Outcome.PLAYER_2_WIN :
        Outcome.PLAYER_1_WIN;
  }
}
