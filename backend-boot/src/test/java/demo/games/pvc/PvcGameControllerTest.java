package demo.games.pvc;

import demo.games.shared.Hand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/* Just load the following controller and all it needs */
@WebMvcTest( PvcGameController.class )
@DisplayName( "PvC Game controller" )
public class PvcGameControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private PvcGameService service;

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
    final PlayResult result = new PlayResult( Hand.PAPER, Hand.ROCK, Outcome.COMPUTER_WIN );

    when( service.play( result.getPlayer() ) ).thenReturn( result );

    mockMvc.perform( get( String.format( "/play/%s", result.getPlayer().name() ) ) )
      .andExpect( status().isOk() )
      .andExpect( jsonPath( "$.computer", is( result.getComputer().name() ) ) )
      .andExpect( jsonPath( "$.player", is( result.getPlayer().name() ) ) )
      .andExpect( jsonPath( "$.outcome", is( result.getOutcome().name() ) ) );

    verify( service, times( 1 ) ).play( result.getPlayer() );
  }
}
