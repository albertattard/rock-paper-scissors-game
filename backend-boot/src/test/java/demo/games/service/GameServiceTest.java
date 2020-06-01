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
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static demo.games.model.PvcOutcome.COMPUTER_WIN;
import static demo.games.model.PvcOutcome.DRAW;
import static demo.games.model.PvcOutcome.PLAYER_WIN;
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

@DisplayName( "Game service" )
public class GameServiceTest {

  @Nested
  @DisplayName( "play against computer" )
  class PlayAgainstComputer {

    private final int NUMBER_OF_HANDS = Hand.values().length;

    @Test
    @DisplayName( "should return a random hand based on the random number generated by the service" )
    public void shouldReturnARandomHand() {
      final Hand expectedHand = Hand.ROCK;

      final GameCodeService codeService = mock( GameCodeService.class );
      final GameRepository repository = mock( GameRepository.class );
      final RandomService randomService = mockRandomService( expectedHand );

      final GameService service = new GameService( codeService, repository, randomService );
      final Hand hand = service.random();
      assertSame( expectedHand, hand );

      verifyMocks( randomService );
      verifyNoInteractions( codeService );
      verifyNoInteractions( repository );
    }

    @EnumSource( Hand.class )
    @ParameterizedTest( name = "should return DRAW when both players play the same hand {0}" )
    public void shouldReturnDraw( final Hand hand ) {
      playAndAssert( hand, hand, DRAW );
    }

    @CsvSource( { "PAPER,ROCK", "SCISSORS,PAPER", "ROCK,SCISSORS" } )
    @ParameterizedTest( name = "should return COMPUTER_WIN when computer plays {0} and player plays {1}" )
    public void shouldReturnComputerWin( final Hand computer, final Hand player ) {
      playAndAssert( computer, player, COMPUTER_WIN );
    }

    @CsvSource( { "ROCK,PAPER", "PAPER,SCISSORS", "SCISSORS,ROCK" } )
    @ParameterizedTest( name = "should return PLAYER_WIN when computer plays {0} and player plays {1}" )
    public void shouldReturnPlayerWin( final Hand computer, final Hand player ) {
      playAndAssert( computer, player, PLAYER_WIN );
    }

    private void playAndAssert( Hand computer, Hand player, PvcOutcome outcome ) {
      final GameCodeService codeService = mock( GameCodeService.class );
      final GameRepository repository = mock( GameRepository.class );
      final RandomService randomService = mockRandomService( computer );

      final GameService service = new GameService( codeService, repository, randomService );

      final PvcGameResult result = new PvcGameResult( computer, player, outcome );
      assertEquals( result, service.play( player ) );

      verifyMocks( randomService );
      verifyNoInteractions( codeService );
      verifyNoInteractions( repository );
    }

    private RandomService mockRandomService( final Hand computer ) {
      final RandomService randomService = mock( RandomService.class );
      when( randomService.nextInt( eq( NUMBER_OF_HANDS ) ) ).thenReturn( computer.ordinal() );
      return randomService;
    }

    private void verifyMocks( final RandomService randomService ) {
      verify( randomService, times( 1 ) ).nextInt( NUMBER_OF_HANDS );
    }
  }

  @Nested
  @DisplayName( "play against another player" )
  class PlayAgainstPlayer {

    @Nested
    @DisplayName( "create game" )
    class CreateGame {
      @Test
      @DisplayName( "should create game and return the game code" )
      public void shouldCreateGameAndReturnCode() {
        final Game gameToSaved = createActiveGame();

        final GameCodeService codeService = mock( GameCodeService.class );
        final GameRepository repository = mock( GameRepository.class );
        final RandomService randomService = mock( RandomService.class );

        when( codeService.nextCode( eq( 8 ) ) ).thenReturn( gameToSaved.getCode() );
        when( repository.save( eq( gameToSaved ) ) ).thenReturn( gameToSaved );

        final GameService service = new GameService( codeService, repository, randomService );

        final ActiveGame created = service.create( gameToSaved.getPlayer1() );
        assertEquals( toActiveGame( gameToSaved ), created );

        verify( codeService, times( 1 ) ).nextCode( 8 );
        verify( repository, times( 1 ) ).save( gameToSaved );
        verifyNoInteractions( randomService );
      }
    }

    @Nested
    @DisplayName( "list games" )
    class ListGames {
      @Test
      @DisplayName( "should return a list of active games" )
      public void shouldReturnActiveGames() {
        final int numberOfGamesInDb = 5;
        final List<Game> gamesInDb = createActiveGames( numberOfGamesInDb );

        final GameCodeService codeService = mock( GameCodeService.class );
        final GameRepository repository = mock( GameRepository.class );
        final RandomService randomService = mock( RandomService.class );

        when( repository.findByStateEquals( eq( GameState.ACTIVE ) ) ).thenReturn( gamesInDb );

        final GameService service = new GameService( codeService, repository, randomService );

        final List<ActiveGame> games = service.listActiveGames();
        assertEquals( numberOfGamesInDb, games.size() );
        assertEquals( toActiveGame( gamesInDb ), games );

        verify( repository, times( 1 ) ).findByStateEquals( GameState.ACTIVE );
        verifyNoInteractions( codeService );
        verifyNoInteractions( randomService );
      }

      @Test
      @DisplayName( "should return a list of closed games" )
      public void shouldReturnClosedGames() {
        final int numberOfGamesInDb = 5;
        final List<Game> gamesInDb = createCloseGames( numberOfGamesInDb );

        final GameCodeService codeService = mock( GameCodeService.class );
        final GameRepository repository = mock( GameRepository.class );
        final RandomService randomService = mock( RandomService.class );

        when( repository.findByStateEquals( eq( GameState.CLOSED ) ) ).thenReturn( gamesInDb );

        final GameService service = new GameService( codeService, repository, randomService );

        final List<GameDetails> games = service.listClosedGames();
        assertEquals( numberOfGamesInDb, games.size() );
        assertEquals( toGameDetails( gamesInDb ), games );

        verify( repository, times( 1 ) ).findByStateEquals( GameState.CLOSED );
        verifyNoInteractions( codeService );
        verifyNoInteractions( randomService );
      }
    }

    @Nested
    @DisplayName( "find game" )
    class FindGame {

      @Test
      @DisplayName( "should return Optional empty when game is not found" )
      public void shouldReturnEmptyWhenNotFound() {
        final String code = "000000";

        final GameCodeService codeService = mock( GameCodeService.class );
        final GameRepository repository = mock( GameRepository.class );
        final RandomService randomService = mock( RandomService.class );

        when( repository.findById( eq( code ) ) ).thenReturn( Optional.empty() );

        final GameService service = new GameService( codeService, repository, randomService );
        final Optional<GameDetails> game = service.findGame( code );
        assertNotNull( game );
        assertTrue( game.isEmpty() );

        verify( repository, times( 1 ) ).findById( code );
        verifyNoInteractions( codeService );
        verifyNoInteractions( randomService );
      }

      @Test
      @DisplayName( "should return limited game details when game is still open" )
      public void shouldReturnLimitedDetails() {
        final Game gameInDb = createActiveGame();

        final GameCodeService codeService = mock( GameCodeService.class );
        final GameRepository repository = mock( GameRepository.class );
        final RandomService randomService = mock( RandomService.class );

        when( repository.findById( eq( gameInDb.getCode() ) ) ).thenReturn( Optional.of( gameInDb ) );

        final GameService service = new GameService( codeService, repository, randomService );
        final Optional<GameDetails> game = service.findGame( gameInDb.getCode() );
        assertNotNull( game );
        assertFalse( game.isEmpty() );

        final GameDetails details = game.get();
        assertEquals( gameInDb.getCode(), details.getCode() );
        assertSame( gameInDb.getState(), details.getState() );
        assertNull( details.getPlayer1() );
        assertNull( details.getPlayer2() );
        assertNull( details.getOutcome() );

        verify( repository, times( 1 ) ).findById( gameInDb.getCode() );
        verifyNoInteractions( codeService );
        verifyNoInteractions( randomService );
      }

      @Test
      @DisplayName( "should return all game details when game is still open" )
      public void shouldReturnAllDetails() {
        final Game gameInDb = createCloseGame();

        final GameCodeService codeService = mock( GameCodeService.class );
        final GameRepository repository = mock( GameRepository.class );
        final RandomService randomService = mock( RandomService.class );

        when( repository.findById( eq( gameInDb.getCode() ) ) ).thenReturn( Optional.of( gameInDb ) );

        final GameService service = new GameService( codeService, repository, randomService );
        final Optional<GameDetails> game = service.findGame( gameInDb.getCode() );
        assertNotNull( game );
        assertFalse( game.isEmpty() );

        final GameDetails details = game.get();
        assertEquals( gameInDb.getCode(), details.getCode() );
        assertSame( gameInDb.getState(), details.getState() );
        assertSame( gameInDb.getPlayer1(), details.getPlayer1() );
        assertSame( gameInDb.getPlayer2(), details.getPlayer2() );
        assertSame( gameInDb.getOutcome(), details.getOutcome() );

        verify( repository, times( 1 ) ).findById( gameInDb.getCode() );
        verifyNoInteractions( codeService );
        verifyNoInteractions( randomService );
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
        final RandomService randomService = mock( RandomService.class );

        when( repository.findByCodeAndStateEquals( eq( code ), eq( GameState.ACTIVE ) ) ).thenReturn( Optional.empty() );

        final GameService service = new GameService( codeService, repository, randomService );
        final Optional<GameDetails> game = service.play( code, player2 );
        assertNotNull( game );
        assertTrue( game.isEmpty() );

        verify( repository, times( 1 ) ).findByCodeAndStateEquals( code, GameState.ACTIVE );
        verifyNoInteractions( codeService );
        verifyNoInteractions( randomService );
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
          .setOutcome( PvpOutcome.DRAW )
          .setState( GameState.CLOSED );

        final GameCodeService codeService = mock( GameCodeService.class );
        final GameRepository repository = mock( GameRepository.class );
        final RandomService randomService = mock( RandomService.class );

        when( repository.findByCodeAndStateEquals( eq( code ), eq( GameState.ACTIVE ) ) ).thenReturn( Optional.of( gameInDb ) );
        when( repository.save( eq( gameToBeSaved ) ) ).thenReturn( gameToBeSaved );

        final GameService service = new GameService( codeService, repository, randomService );
        final Optional<GameDetails> game = service.play( code, player2 );
        assertNotNull( game );
        assertFalse( game.isEmpty() );

        final GameDetails details = game.get();
        assertEquals( gameToBeSaved.getCode(), details.getCode() );
        assertSame( gameToBeSaved.getState(), details.getState() );
        assertSame( gameToBeSaved.getPlayer1(), details.getPlayer1() );
        assertSame( gameToBeSaved.getPlayer2(), details.getPlayer2() );
        assertSame( gameToBeSaved.getOutcome(), details.getOutcome() );

        verify( repository, times( 1 ) ).findByCodeAndStateEquals( code, GameState.ACTIVE );
        verifyNoInteractions( codeService );
        verifyNoInteractions( randomService );
      }
    }

    private List<Game> createActiveGames( final int number ) {
      return IntStream.range( 0, number )
        .mapToObj( i -> createActiveGame() )
        .collect( Collectors.toList() );
    }

    private Game createActiveGame() {
      return new Game()
        .setCode( RandomStringUtils.randomAlphanumeric( 8 ) )
        .setPlayer1( Hand.ROCK )
        .setState( GameState.ACTIVE );
    }

    private List<Game> createCloseGames( final int number ) {
      return IntStream.range( 0, number )
        .mapToObj( i -> createCloseGame() )
        .collect( Collectors.toList() );
    }

    private Game createCloseGame() {
      return new Game()
        .setCode( RandomStringUtils.randomAlphanumeric( 8 ) )
        .setPlayer1( Hand.ROCK )
        .setPlayer2( Hand.ROCK )
        .setState( GameState.CLOSED )
        .setOutcome( PvpOutcome.DRAW );
    }

    private List<ActiveGame> toActiveGame( final List<Game> games ) {
      return games.stream()
        .map( this::toActiveGame )
        .collect( Collectors.toList() );
    }

    private ActiveGame toActiveGame( final Game game ) {
      return new ActiveGame( game.getCode() );
    }

    private List<GameDetails> toGameDetails( final List<Game> games ) {
      return games.stream()
        .map( this::toGameDetails )
        .collect( Collectors.toList() );
    }

    private GameDetails toGameDetails( final Game game ) {
      final GameDetails details = new GameDetails()
        .setCode( game.getCode() )
        .setPlayer1( game.getPlayer1() )
        .setState( game.getState() );

      if ( game.getState() == GameState.CLOSED ) {
        details.setPlayer2( game.getPlayer2() )
          .setOutcome( PvpOutcome.of( game.getPlayer1(), game.getPlayer2() ) );
      }

      return details;
    }
  }
}
