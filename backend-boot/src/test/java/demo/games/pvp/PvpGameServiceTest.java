package demo.games.pvp;

import demo.games.shared.Hand;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName( "PvP Game service" )
public class PvpGameServiceTest {

  @Nested
  @DisplayName( "create game" )
  class CreateGame {

    @Test
    @DisplayName( "should create game and return the game code" )
    public void shouldCreateGameAndReturnCode() {
      final Game gameToSaved = createRandomGame();

      final GameCodeService codeService = mock( GameCodeService.class );
      final GameRepository repository = mock( GameRepository.class );

      when( codeService.nextCode( eq( 8 ) ) ).thenReturn( gameToSaved.getCode() );
      when( repository.save( eq( gameToSaved ) ) ).thenReturn( gameToSaved );

      final PvpGameService service = new PvpGameService( codeService, repository );

      final ActiveGame created = service.create( gameToSaved.getPlayer1() );
      assertEquals( toGameResponse( gameToSaved ), created );

      verify( codeService, times( 1 ) ).nextCode( 8 );
      verify( repository, times( 1 ) ).save( gameToSaved );
    }
  }

  @Nested
  @DisplayName( "list open games" )
  class ListOpenAllGames {

    @Test
    @DisplayName( "should return a list of open games" )
    public void shouldReturnOpenGames() {
      final List<Game> gamesInDb = createRandomGames( 5 );

      final GameCodeService codeService = mock( GameCodeService.class );
      final GameRepository repository = mock( GameRepository.class );

      when( repository.findByStateEquals( eq( GameState.ACTIVE ) ) ).thenReturn( gamesInDb );

      final PvpGameService service = new PvpGameService( codeService, repository );

      final List<ActiveGame> games = service.listActiveGames();
      assertEquals( toGameResponse( gamesInDb ), games );

      verifyNoInteractions( codeService );
      verify( repository, times( 1 ) ).findByStateEquals( GameState.ACTIVE );
    }
  }

  @Nested
  @DisplayName( "find game" )
  class FindGame {

    @Test
    @DisplayName( "should return Optional empty when game is not found" )
    public void shouldReturnEmptyWhenNotFound() {

      final String code = "12345678";

      final GameCodeService codeService = mock( GameCodeService.class );
      final GameRepository repository = mock( GameRepository.class );

      when( repository.findById( eq( code ) ) ).thenReturn( Optional.empty() );

      final PvpGameService service = new PvpGameService( codeService, repository );
      final Optional<GameDetails> game = service.findGame( code );
      assertNotNull( game );
      assertTrue( game.isEmpty() );

      verifyNoInteractions( codeService );
      verify( repository, times( 1 ) ).findById( code );
    }

    @Test
    @DisplayName( "should return limited game details when game is still open" )
    public void shouldReturnLimitedDetails() {

      final Game gameInDb = createRandomGame();
      assertSame( GameState.ACTIVE, gameInDb.getState() );

      final GameCodeService codeService = mock( GameCodeService.class );
      final GameRepository repository = mock( GameRepository.class );

      when( repository.findById( eq( gameInDb.getCode() ) ) ).thenReturn( Optional.of( gameInDb ) );

      final PvpGameService service = new PvpGameService( codeService, repository );
      final Optional<GameDetails> game = service.findGame( gameInDb.getCode() );
      assertNotNull( game );
      assertFalse( game.isEmpty() );

      final GameDetails details = game.get();
      assertEquals( gameInDb.getCode(), details.getCode() );
      assertSame( gameInDb.getState(), details.getState() );
      assertNull( details.getPlayer1() );
      assertNull( details.getPlayer2() );
      assertNull( details.getOutcome() );

      verifyNoInteractions( codeService );
      verify( repository, times( 1 ) ).findById( gameInDb.getCode() );
    }

    @Test
    @DisplayName( "should return all game details when game is still open" )
    public void shouldReturnAllDetails() {

      final Game gameInDb = new Game()
        .setCode( RandomStringUtils.randomAlphanumeric( 8 ) )
        .setPlayer1( Hand.ROCK )
        .setPlayer2( Hand.ROCK )
        .setState( GameState.CLOSED );

      final GameCodeService codeService = mock( GameCodeService.class );
      final GameRepository repository = mock( GameRepository.class );

      when( repository.findById( eq( gameInDb.getCode() ) ) ).thenReturn( Optional.of( gameInDb ) );

      final PvpGameService service = new PvpGameService( codeService, repository );
      final Optional<GameDetails> game = service.findGame( gameInDb.getCode() );
      assertNotNull( game );
      assertFalse( game.isEmpty() );

      final GameDetails details = game.get();
      assertEquals( gameInDb.getCode(), details.getCode() );
      assertSame( gameInDb.getState(), details.getState() );
      assertSame( gameInDb.getPlayer1(), details.getPlayer1() );
      assertSame( gameInDb.getPlayer2(), details.getPlayer2() );
      assertSame( Outcome.DRAW, details.getOutcome() );

      verifyNoInteractions( codeService );
      verify( repository, times( 1 ) ).findById( gameInDb.getCode() );
    }
  }

  @Nested
  @DisplayName( "play game" )
  class PlayGame {

    @Test
    @DisplayName( "should return Optional empty when game is not found or game not open" )
    public void shouldReturnEmptyWhenNotFoundOrNotOpen() {

      final String code = "00000000";
      final Hand player2 = Hand.ROCK;

      final GameCodeService codeService = mock( GameCodeService.class );
      final GameRepository repository = mock( GameRepository.class );

      when( repository.findByCodeAndStateEquals( eq( code ), eq( GameState.ACTIVE ) ) ).thenReturn( Optional.empty() );

      final PvpGameService service = new PvpGameService( codeService, repository );
      final Optional<GameDetails> game = service.play( code, player2 );
      assertNotNull( game );
      assertTrue( game.isEmpty() );

      verifyNoInteractions( codeService );
      verify( repository, times( 1 ) ).findByCodeAndStateEquals( code, GameState.ACTIVE );
    }

    @Test
    @DisplayName( "should return game full detail when game open is found" )
    public void shouldReturnDetailsWhenFound() {

      final String code = "00000000";
      final Hand player2 = Hand.ROCK;
      final Game gameInDb = new Game()
        .setCode( code )
        .setPlayer1( Hand.ROCK )
        .setState( GameState.ACTIVE );
      final Game gameToBeSaved = new Game()
        .setCode( code )
        .setPlayer1( Hand.ROCK )
        .setPlayer2( player2 )
        .setState( GameState.CLOSED );

      final GameCodeService codeService = mock( GameCodeService.class );
      final GameRepository repository = mock( GameRepository.class );

      when( repository.findByCodeAndStateEquals( eq( code ), eq( GameState.ACTIVE ) ) ).thenReturn( Optional.of( gameInDb ) );
      when( repository.save( eq( gameToBeSaved ) ) ).thenReturn( gameToBeSaved );

      final PvpGameService service = new PvpGameService( codeService, repository );
      final Optional<GameDetails> game = service.play( code, player2 );
      assertNotNull( game );
      assertFalse( game.isEmpty() );

      final GameDetails details = game.get();
      assertEquals( gameToBeSaved.getCode(), details.getCode() );
      assertSame( gameToBeSaved.getState(), details.getState() );
      assertSame( gameToBeSaved.getPlayer1(), details.getPlayer1() );
      assertSame( gameToBeSaved.getPlayer2(), details.getPlayer2() );
      assertSame( Outcome.DRAW, details.getOutcome() );

      verifyNoInteractions( codeService );
      verify( repository, times( 1 ) ).findByCodeAndStateEquals( code, GameState.ACTIVE );
    }
  }

  private List<Game> createRandomGames( final int number ) {
    final List<Game> games = new ArrayList<>( number );
    for ( int i = 0; i < number; i++ ) {
      games.add( createRandomGame() );
    }
    return games;
  }

  private Game createRandomGame() {
    return new Game()
      .setCode( RandomStringUtils.randomAlphanumeric( 8 ) )
      .setPlayer1( Hand.ROCK )
      .setState( GameState.ACTIVE );
  }

  private List<ActiveGame> toGameResponse( final List<Game> games ) {
    return games.stream()
      .map( this::toGameResponse )
      .collect( Collectors.toList() );
  }

  private ActiveGame toGameResponse( final Game game ) {
    return new ActiveGame( game.getCode() );
  }
}
