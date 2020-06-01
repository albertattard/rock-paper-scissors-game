package demo.games;

import demo.games.model.RandomHand;
import demo.games.model.PvcGameResult;
import demo.games.model.PvcOutcome;
import demo.games.model.Hand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@DisplayName( "PvC Game application" )
@SpringBootTest( webEnvironment = WebEnvironment.RANDOM_PORT )
public class PvcGameApplicationTests {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  @DisplayName( "should return a random hand" )
  public void shouldReturnARandomHand() {
    final var candidates = List.of(
      new RandomHand( Hand.ROCK ),
      new RandomHand( Hand.PAPER ),
      new RandomHand( Hand.SCISSORS )
    );

    final String url = String.format( "http://localhost:%d/hand", port );
    assertThat( this.restTemplate.getForObject( url, RandomHand.class ) )
      .isIn( candidates );
  }

  @Test
  @DisplayName( "should play against computer" )
  public void shouldPlayAgainstComputer() {
    final Hand player = Hand.ROCK;
    final var outcomes = List.of(
      new PvcGameResult( Hand.ROCK, player, PvcOutcome.DRAW ),
      new PvcGameResult( Hand.PAPER, player, PvcOutcome.COMPUTER_WIN ),
      new PvcGameResult( Hand.SCISSORS, player, PvcOutcome.PLAYER_WIN )
    );

    final String url = String.format( "http://localhost:%d/play/%s", port, player.name() );
    assertThat( this.restTemplate.getForObject( url, PvcGameResult.class ) )
      .isIn( outcomes );
  }

  @Test
  @DisplayName( "should play against another player" )
  public void shouldPlayAgainstAnotherPlayer() {
    final Hand player1 = Hand.ROCK;
    final var outcomes = List.of(
      new PvcGameResult( Hand.ROCK, player1, PvcOutcome.DRAW ),
      new PvcGameResult( Hand.PAPER, player1, PvcOutcome.COMPUTER_WIN ),
      new PvcGameResult( Hand.SCISSORS, player1, PvcOutcome.PLAYER_WIN )
    );

    final String url = String.format( "http://localhost:%d/play/%s", port, player1.name() );
    assertThat( this.restTemplate.getForObject( url, PvcGameResult.class ) )
      .isIn( outcomes );
  }
}
