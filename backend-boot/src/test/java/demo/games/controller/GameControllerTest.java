package demo.games.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.games.model.ActiveGame;
import demo.games.model.CreateGame;
import demo.games.model.Hand;
import demo.games.model.PvcGameResult;
import demo.games.model.PvcOutcome;
import demo.games.resource.GameController;
import demo.games.service.GameService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/* Just load the following controller and all it needs */
@WebMvcTest( GameController.class )
@DisplayName( "PvP game controller" )
public class GameControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private GameService service;

  @Test
  @DisplayName( "should return the hand provided by the service" )
  public void shouldReturnTheHandProvidedByTheService() throws Exception {
    final Hand hand = Hand.ROCK;
    when( service.random() ).thenReturn( hand );

    mockMvc.perform( get( "/hand" ) )
      .andExpect( status().isOk() )
      .andExpect( jsonPath( "$.hand", is( hand.name() ) ) );

    verify( service, times( 1 ) ).random();
  }

  @Test
  @DisplayName( "should return the play outcome provided by the service" )
  public void shouldReturnTheOutcomeProvidedByTheService() throws Exception {
    final PvcGameResult result = new PvcGameResult( Hand.PAPER, Hand.ROCK, PvcOutcome.COMPUTER_WIN );

    when( service.play( result.getPlayer() ) ).thenReturn( result );

    mockMvc.perform( get( String.format( "/play/%s", result.getPlayer().name() ) ) )
      .andExpect( status().isOk() )
      .andExpect( jsonPath( "$.computer", is( result.getComputer().name() ) ) )
      .andExpect( jsonPath( "$.player", is( result.getPlayer().name() ) ) )
      .andExpect( jsonPath( "$.outcome", is( result.getOutcome().name() ) ) );

    verify( service, times( 1 ) ).play( result.getPlayer() );
  }

  @Test
  @DisplayName( "should create the new game and return the code" )
  public void shouldCreateGameAndReturnCode() throws Exception {

    final Hand player1 = Hand.ROCK;
    final ActiveGame game = createRandomGame();

    when( service.create( eq( player1 ) ) ).thenReturn( game );

    mockMvc.perform(
      post( "/game" )
        .contentType( APPLICATION_JSON )
        .content( createGameAsJson( player1 ) )
    )
      .andExpect( status().isCreated() )
      .andExpect( redirectedUrl( String.format( "/game/%s", game.getCode() ) ) );

    verify( service, times( 1 ) ).create( player1 );
  }

  @Test
  @DisplayName( "should return the list of open games" )
  public void shouldReturnOpenGames() throws Exception {

    final List<ActiveGame> games = createRandomGames( 5 );

    when( service.listActiveGames() ).thenReturn( games );

    mockMvc.perform( get( "/game/list/open" ) )
      .andExpect( status().isOk() )
      .andExpect( jsonPath( "$" ).isArray() )
      .andExpect( jsonPath( "$", hasSize( games.size() ) ) )
      .andExpect( jsonPath( "$[*].code", containsInAnyOrder( toGameCode( games ) ) ) );

    verify( service, times( 1 ) ).listActiveGames();
  }

  @Test
  @DisplayName( "should return 404 when game is not found" )
  public void shouldReturnNotFoundGameDetails() throws Exception {
    final String code = "00000000";

    when( service.findGame( eq( code ) ) ).thenReturn( Optional.empty() );

    mockMvc.perform( get( String.format( "/game/%s", code ) ) )
      .andExpect( status().isNotFound() );

    verify( service, times( 1 ) ).findGame( code );
  }

  private String toJson( CreateGame game ) throws JsonProcessingException {
    return new ObjectMapper()
      .writer()
      .withDefaultPrettyPrinter()
      .writeValueAsString( game );
  }

  private String createGameAsJson( Hand player1 ) throws JsonProcessingException {
    return toJson( new CreateGame( player1 ) );
  }

  private String[] toGameCode( List<ActiveGame> games ) {
    final String[] codes = new String[games.size()];
    for ( int i = 0; i < games.size(); i++ ) {
      codes[i] = games.get( i ).getCode();
    }
    return codes;
  }

  private List<ActiveGame> createRandomGames( int number ) {
    final List<ActiveGame> games = new ArrayList<>( number );
    for ( int i = 0; i < number; i++ ) {
      games.add( createRandomGame() );
    }
    return games;
  }

  private ActiveGame createRandomGame() {
    return new ActiveGame( RandomStringUtils.randomAlphanumeric( 8 ) );
  }
}
