package demo.games.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName( "PvP outcome" )
public class PvpOutcomeTest {

  @DisplayName( "of" )
  @CsvFileSource( resources = { "/sample/pvp_outcome.csv" }, numLinesToSkip = 1 )
  @ParameterizedTest( name = "should return {2} when player 1 plays {0} and player 2 plays {1}" )
  public void shouldReturnCorrectOutcome( final Hand player1, final Hand player2, final PvpOutcome expected ) {
    assertSame( expected, PvpOutcome.of( player1, player2 ) );
  }
}
