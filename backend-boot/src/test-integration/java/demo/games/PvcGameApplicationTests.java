package demo.games;

import demo.games.model.Hand;
import demo.games.model.PvcGameResult;
import demo.games.model.PvcOutcome;
import demo.games.model.RandomHand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@DisplayName( "PvC Game application" )
@SpringBootTest( webEnvironment = WebEnvironment.RANDOM_PORT )
public class PvcGameApplicationTests {

  @Test
  @DisplayName( "should return a random hand" )
  public void shouldReturnARandomHand() {
    final var candidates = List.of(
      new RandomHand( Hand.ROCK ),
      new RandomHand( Hand.PAPER ),
      new RandomHand( Hand.SCISSORS )
    );

    assertThat( this.restTemplate.getForObject( randomHandPath(), RandomHand.class ) )
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

    assertThat( this.restTemplate.getForObject( pvcPath( player ), PvcGameResult.class ) )
      .isIn( outcomes );
  }

  private String pvcPath( final Hand player ) {
    return String.format( "/pvc/%s", player.name() );
  }

  private String randomHandPath() {
    return "/randomHand";
  }

  @Autowired
  private TestRestTemplate restTemplate;

}
