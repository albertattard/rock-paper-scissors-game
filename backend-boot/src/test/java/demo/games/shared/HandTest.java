package demo.games.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName( "Hand" )
public class HandTest {

  @CsvSource( { "ROCK,PAPER", "PAPER,SCISSORS", "SCISSORS,ROCK" } )
  @ParameterizedTest( name = "{0} should be beaten by {1}" )
  void shouldByBeatenBy( final Hand hand, final Hand beatenBy ) {
    assertSame( beatenBy, hand.beatenBy() );
  }
}
