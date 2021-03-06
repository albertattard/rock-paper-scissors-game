package demo.games.service;

import demo.games.dao.Game;
import demo.games.dao.GameRepository;
import demo.games.model.ActiveGame;
import demo.games.model.GameDetails;
import demo.games.model.GameState;
import demo.games.model.Hand;
import demo.games.model.PvcGameResult;
import demo.games.model.PvcOutcome;
import demo.games.model.PvpOutcome;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static demo.games.model.GameState.ACTIVE;
import static demo.games.model.GameState.CLOSED;

@Service
public class GameService {

  private final GameCodeService codeService;
  private final GameRepository repository;
  private final RandomService randomService;

  public GameService( final GameCodeService codeService, final GameRepository repository, final RandomService randomService ) {
    this.codeService = codeService;
    this.repository = repository;
    this.randomService = randomService;
  }

  public Hand randomHand() {
    final Hand[] candidates = Hand.values();
    return candidates[randomService.nextInt( candidates.length )];
  }

  public PvcGameResult playAgainstComputer( final Hand player ) {
    final Hand computer = randomHand();
    final PvcOutcome outcome = PvcOutcome.of( computer, player );
    return new PvcGameResult( computer, player, outcome );
  }

  public ActiveGame createPvpGame( final Hand player1 ) {
    final String code = codeService.nextCode( 8 );

    final Game game = new Game()
      .setCode( code )
      .setPlayer1( player1 )
      .setState( ACTIVE );
    repository.save( game );

    return new ActiveGame( code );
  }

  @Transactional
  public Optional<GameDetails> playAgainstPlayer( final String code, final Hand player2 ) {
    return repository.findByCodeAndStateEquals( code, ACTIVE )
      .map( game -> repository.save(
        game.setPlayer2( player2 )
          .setOutcome( PvpOutcome.of( game.getPlayer1(), player2 ) )
          .setState( CLOSED ) )
      )
      .map( GameService::toGameDetails );
  }

  public List<ActiveGame> listActivePvpGames() {
    return repository.findByStateEquals( ACTIVE )
      .stream()
      .map( r -> new ActiveGame( r.getCode() ) )
      .collect( Collectors.toList() );
  }

  public List<GameDetails> listClosedPvpGames() {
    return repository.findByStateEquals( CLOSED )
      .stream()
      .map( GameService::toGameDetails )
      .collect( Collectors.toList() );
  }

  public Optional<GameDetails> findPvpGame( final String code ) {
    return repository.findById( code )
      .map( game -> {
          final GameDetails details = new GameDetails()
            .setCode( game.getCode() )
            .setState( game.getState() );

          if ( shouldIncludeDetails( game.getState() ) ) {
            details.setPlayer1( game.getPlayer1() )
              .setPlayer2( game.getPlayer2() )
              .setOutcome( game.getOutcome() );
          }

          return details;
        }
      );
  }

  private static GameDetails toGameDetails( Game game ) {
    return new GameDetails()
      .setCode( game.getCode() )
      .setState( game.getState() )
      .setPlayer1( game.getPlayer1() )
      .setPlayer2( game.getPlayer2() )
      .setOutcome( game.getOutcome() );
  }

  private static boolean shouldIncludeDetails( final GameState state ) {
    return state == CLOSED;
  }
}
