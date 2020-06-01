package demo.games.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.Map;

import static test.utils.Sha256Utils.computeSha256;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName( "PvP outcome" )
public class PvpOutcomeTest {

  @DisplayName( "of" )
  @CsvFileSource( resources = { "/sample/pvp_outcome.csv" }, numLinesToSkip = 1 )
  @ParameterizedTest( name = "should return {2} when player 1 plays {0} and player 2 plays {1}" )
  public void shouldReturnCorrectOutcome( final Hand player1, final Hand player2, final PvpOutcome expected ) {
    assertSame( expected, PvpOutcome.of( player1, player2 ) );
  }

  @Test
  @DisplayName( "should have known names" )
  void shouldHaveKnownNames() {
    final Map<String, String> knownNames = Map.of(
      "PLAYER_1_WIN", "4f1bc26bdf858dcc9eb3809ab12a80e0e5770ec2dbc903556581995872fbee7c",
      "PLAYER_2_WIN", "e6856a317aabf858cd81b8555f88d91f2018053344b685aeffcd22bf86478020",
      "DRAW", "3017385a0abf92e7b4a50dd10011d4a20372d401390e3004313f2b02c5d7f910" );

    assertEquals( knownNames.size(), PvpOutcome.values().length );

    knownNames.forEach( ( name, sha256 ) -> {
        /* Make sure that the expected name was not modified by mistake during refactoring */
        assertEquals( sha256, computeSha256( name ), String.format( "expected name %s was changed unexpectedly", name ) );

        final PvpOutcome hand = PvpOutcome.valueOf( name );
        assertEquals( name, hand.name() );
      }
    );
  }
}
