package demo.games.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static test.utils.Sha256Utils.computeSha256;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName( "Hand" )
public class HandTest {

  @CsvSource( { "ROCK,PAPER", "PAPER,SCISSORS", "SCISSORS,ROCK" } )
  @DisplayName( "beaten by" )
  @ParameterizedTest( name = "{0} should be beaten by {1}" )
  void shouldByBeatenBy( final Hand hand, final Hand beatenBy ) {
    assertSame( beatenBy, hand.beatenBy() );
  }

  @Test
  @DisplayName( "should have known names" )
  void shouldHaveKnownNames() {
    final Map<String, String> knownNames = Map.of(
      "ROCK", "5adfabaf0034944241e990102d633da1570763930acbb84213b8552bd393a17c",
      "PAPER", "c87f290656e4b4d73c43dcbe6e37a6405fbe06ec3910c3ae3c9e10e8e9dbd12a",
      "SCISSORS", "3733015c7e0b75ce5daf9609bb9fd780bff1d580cd4f614ec8f31ca17b8750ba" );

    assertEquals( knownNames.size(), Hand.values().length );

    knownNames.forEach( ( name, sha256 ) -> {
        /* Make sure that the expected name was not modified by mistake during refactoring */
        assertEquals( sha256, computeSha256( name ), String.format( "expected name %s was changed unexpectedly", name ) );

        final Hand hand = Hand.valueOf( name );
        assertEquals( name, hand.name() );
      }
    );
  }
}
