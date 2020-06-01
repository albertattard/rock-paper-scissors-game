package demo.games.pvp;

import demo.games.shared.Hand;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static demo.games.pvp.GameState.CLOSED;
import static demo.games.pvp.GameState.ACTIVE;

@Service
public class PvpGameService {

  private final GameCodeService randomService;
  private final GameRepository repository;

  public PvpGameService( final GameCodeService randomService, final GameRepository repository ) {
    this.randomService = randomService;
    this.repository = repository;
  }

  public ActiveGame create( final Hand player1 ) {
    final String code = randomService.nextCode( 8 );

    final Game game = new Game()
      .setCode( code )
      .setPlayer1( player1 )
      .setState( ACTIVE );
    repository.save( game );

    return new ActiveGame( code );
  }

  public List<ActiveGame> listActiveGames() {
    return repository.findByStateEquals( ACTIVE )
      .stream()
      .map( r -> new ActiveGame( r.getCode() ) )
      .collect( Collectors.toList() );
  }

  public List<GameDetails> listClosedGames() {
    return repository.findByStateEquals( CLOSED )
      .stream()
      .map( this::toGameDetails )
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
    return repository.findByCodeAndStateEquals( code, ACTIVE )
      .map( game -> repository.save( game.setPlayer2( player2 ).setState( CLOSED ) ) )
      .map( this::toGameDetails );
  }

  private GameDetails toGameDetails( Game game ) {
    return new GameDetails()
      .setCode( game.getCode() )
      .setState( game.getState() )
      .setPlayer1( game.getPlayer1() )
      .setPlayer2( game.getPlayer2() )
      .setOutcome( determineOutcome( game.getPlayer1(), game.getPlayer2() ) );
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
