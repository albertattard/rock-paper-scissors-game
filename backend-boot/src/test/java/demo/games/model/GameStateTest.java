package demo.games.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.utils.Sha256Utils.computeSha256;

@DisplayName( "Game state" )
public class GameStateTest {

  @Test
  @DisplayName( "should have known names" )
  void shouldHaveKnownNames() {
    final Map<String, String> knownNames = Map.of(
      "ACTIVE", "630c2f1c0ee1b8d7da57cf8936ae7e78274aba0bfd765fe10a20dfe580f9eecc",
      "CLOSED", "f6deab2b121b0bb37c2e6b43bc9cd58c5422e9e638321f421515378e2856595b" );

    assertEquals( knownNames.size(), GameState.values().length );

    knownNames.forEach( ( name, sha256 ) -> {
        /* Make sure that the expected name was not modified by mistake during refactoring */
        assertEquals( sha256, computeSha256( name ), String.format( "expected name %s was changed unexpectedly", name ) );

        final GameState state = GameState.valueOf( name );
        assertEquals( name, state.name() );
      }
    );
  }
}
