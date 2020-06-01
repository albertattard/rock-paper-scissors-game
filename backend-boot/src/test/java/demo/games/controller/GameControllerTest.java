package demo.games.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.games.model.ActiveGame;
import demo.games.model.CreateGame;
import demo.games.model.GameDetails;
import demo.games.model.GameState;
import demo.games.model.Hand;
import demo.games.model.PlayGame;
import demo.games.model.PvcGameResult;
import demo.games.model.PvcOutcome;
import demo.games.model.PvpOutcome;
import demo.games.resource.GameController;
import demo.games.service.GameService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/* Just load the following controller and all it needs */
@WebMvcTest( GameController.class )
@DisplayName( "Game controller" )
public class GameControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private GameService service;

  @BeforeEach
  public void setUp() {
    reset( service );
  }

  @Test
  @DisplayName( "should return the hand provided by the service" )
  public void shouldReturnTheHandProvidedByTheService() throws Exception {
    final Hand hand = Hand.ROCK;
    when( service.randomHand() ).thenReturn( hand );

    mockMvc.perform( get( "/randomHand" ) )
      .andExpect( status().isOk() )
      .andExpect( jsonPath( "$.hand", is( hand.name() ) ) );

    verify( service, times( 1 ) ).randomHand();
  }

  @Test
  @DisplayName( "should return the play outcome provided by the service" )
  public void shouldReturnTheOutcomeProvidedByTheService() throws Exception {
    final PvcGameResult result = new PvcGameResult( Hand.PAPER, Hand.ROCK, PvcOutcome.COMPUTER_WIN );

    when( service.playAgainstComputer( result.getPlayer() ) ).thenReturn( result );

    mockMvc.perform( get( String.format( "/pvc/%s", result.getPlayer().name() ) ) )
      .andExpect( status().isOk() )
      .andExpect( jsonPath( "$.computer", is( result.getComputer().name() ) ) )
      .andExpect( jsonPath( "$.player", is( result.getPlayer().name() ) ) )
      .andExpect( jsonPath( "$.outcome", is( result.getOutcome().name() ) ) );

    verify( service, times( 1 ) ).playAgainstComputer( result.getPlayer() );
  }

  @Test
  @DisplayName( "should create the new game and return the code" )
  public void shouldCreateGameAndReturnCode() throws Exception {

    final Hand player1 = Hand.ROCK;
    final ActiveGame game = createRandomGame();

    when( service.createPvpGame( eq( player1 ) ) ).thenReturn( game );

    mockMvc.perform(
      post( "/pvp" )
        .contentType( APPLICATION_JSON )
        .content( toJson( new CreateGame( player1 ) ) )
    )
      .andExpect( status().isCreated() )
      .andExpect( redirectedUrl( String.format( "/pvp/%s", game.getCode() ) ) );

    verify( service, times( 1 ) ).createPvpGame( player1 );
  }

  @Test
  @DisplayName( "should return the list of open games" )
  public void shouldReturnOpenGames() throws Exception {

    final List<ActiveGame> games = createRandomGames( 5 );

    when( service.listActivePvpGames() ).thenReturn( games );

    mockMvc.perform( get( "/pvp/list/active" ) )
      .andExpect( status().isOk() )
      .andExpect( jsonPath( "$" ).isArray() )
      .andExpect( jsonPath( "$", hasSize( games.size() ) ) )
      .andExpect( jsonPath( "$[*].code", containsInAnyOrder( toGameCode( games ) ) ) );

    verify( service, times( 1 ) ).listActivePvpGames();
  }

  @Nested
  @DisplayName( "game details" )
  class ReturnGameDetails {
    @Test
    @DisplayName( "should return 404 when game is not found or not active" )
    public void shouldReturnNotFound() throws Exception {
      final String code = "00000000";

      when( service.findPvpGame( eq( code ) ) ).thenReturn( Optional.empty() );

      mockMvc.perform( get( String.format( "/pvp/%s", code ) ) )
        .andExpect( status().isNotFound() );

      verify( service, times( 1 ) ).findPvpGame( code );
    }

    @Test
    @DisplayName( "should return game details when game is found" )
    public void shouldReturnGameDetails() throws Exception {
      final String code = "00000000";
      final Hand player1 = Hand.ROCK;
      final GameState state = GameState.ACTIVE;

      when( service.findPvpGame( eq( code ) ) )
        .thenReturn( Optional.of( new GameDetails().setCode( code ).setPlayer1( player1 ).setState( state ) ) );

      mockMvc.perform( get( String.format( "/pvp/%s", code ) ) )
        .andExpect( status().isOk() )
        .andExpect( jsonPath( "$" ).isMap() )
        .andExpect( jsonPath( "$.code", is( code ) ) )
        .andExpect( jsonPath( "$.player1", is( player1.name() ) ) )
        .andExpect( jsonPath( "$.player2" ).doesNotExist() )
        .andExpect( jsonPath( "$.outcome" ).doesNotExist() )
        .andExpect( jsonPath( "$.state", is( state.name() ) ) )
      ;

      verify( service, times( 1 ) ).findPvpGame( code );
    }
  }

  @Nested
  @DisplayName( "game details" )
  class PlayExistingGame {
    @Test
    @DisplayName( "should return 404 when game is not found or not active" )
    public void shouldReturnNotFound() throws Exception {
      final String code = "00000000";
      final Hand player2 = Hand.ROCK;

      when( service.playAgainstPlayer( eq( code ), eq( player2 ) ) ).thenReturn( Optional.empty() );

      mockMvc.perform( put( String.format( "/pvp/%s", code ) )
        .contentType( APPLICATION_JSON )
        .content( toJson( new PlayGame( player2 ) ) )
      )
        .andExpect( status().isNotFound() );

      verify( service, times( 1 ) ).playAgainstPlayer( code, player2 );
    }

    @Test
    @DisplayName( "should return game result when playing an active game" )
    public void shouldReturnResult() throws Exception {
      final String code = "00000000";
      final Hand player2 = Hand.ROCK;
      final GameDetails details = new GameDetails()
        .setCode( code )
        .setPlayer1( Hand.ROCK )
        .setPlayer2( player2 )
        .setState( GameState.CLOSED )
        .setOutcome( PvpOutcome.DRAW );

      when( service.playAgainstPlayer( eq( code ), eq( player2 ) ) ).thenReturn( Optional.of( details ) );

      mockMvc.perform( put( String.format( "/pvp/%s", code ) )
        .contentType( APPLICATION_JSON )
        .content( toJson( new PlayGame( player2 ) ) )
      )
        .andExpect( status().isOk() )
        .andExpect( jsonPath( "$" ).isMap() )
        .andExpect( jsonPath( "$.code", is( code ) ) )
        .andExpect( jsonPath( "$.player1", is( details.getPlayer1().name() ) ) )
        .andExpect( jsonPath( "$.player2", is( details.getPlayer2().name() ) ) )
        .andExpect( jsonPath( "$.outcome", is( details.getOutcome().name() ) ) )
        .andExpect( jsonPath( "$.state", is( details.getState().name() ) ) )
      ;

      verify( service, times( 1 ) ).playAgainstPlayer( code, player2 );
    }
  }

  @Test
  @DisplayName( "should return all game" )
  public void shouldReturnAllGames() throws Exception {

    when( service.listActivePvpGames() ).thenReturn( Collections.emptyList() );
    when( service.listClosedPvpGames() ).thenReturn( Collections.emptyList() );

    mockMvc.perform( get( String.format( "/pvp/list/all" ) ) )
      .andExpect( status().isOk() )
      .andExpect( jsonPath( "$" ).isMap() )
      .andExpect( jsonPath( "$.activeGames" ).isArray() )
      .andExpect( jsonPath( "$.activeGames" ).isEmpty() )
      .andExpect( jsonPath( "$.closedGames" ).isArray() )
      .andExpect( jsonPath( "$.closedGames" ).isEmpty() )
    ;

    verify( service, times( 1 ) ).listActivePvpGames();
    verify( service, times( 1 ) ).listClosedPvpGames();
  }

  private String[] toGameCode( final List<ActiveGame> games ) {
    final String[] codes = new String[games.size()];
    for ( int i = 0; i < games.size(); i++ ) {
      codes[i] = games.get( i ).getCode();
    }
    return codes;
  }

  private List<ActiveGame> createRandomGames( final int number ) {
    final List<ActiveGame> games = new ArrayList<>( number );
    for ( int i = 0; i < number; i++ ) {
      games.add( createRandomGame() );
    }
    return games;
  }

  private String toJson( final Object object ) throws JsonProcessingException {
    return new ObjectMapper()
      .writer()
      .withDefaultPrettyPrinter()
      .writeValueAsString( object );
  }

  private ActiveGame createRandomGame() {
    return new ActiveGame( RandomStringUtils.randomAlphanumeric( 8 ) );
  }
}
