package demo.games.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName( "PvC outcome" )
public class PvcOutcomeTest {

  @DisplayName( "of" )
  @CsvFileSource( resources = { "/sample/pvc_outcome.csv" }, numLinesToSkip = 1 )
  @ParameterizedTest( name = "should return {2} when computer plays {0} and player plays {1}" )
  public void shouldReturnCorrectOutcome( final Hand computer, final Hand player, final PvcOutcome expected ) {
    assertSame( expected, PvcOutcome.of( computer, player ) );
  }
}
