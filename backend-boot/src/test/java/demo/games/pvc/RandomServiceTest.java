package demo.games.pvc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName( "Random service" )
public class RandomServiceTest {

  @Test
  @DisplayName( "should return a random int with a fair probability" )
  public void shouldReturnARandomNumber() {

    final int numberOfCandidates = 10;
    final int sampleSize = numberOfCandidates * 1_000;

    /* Retrieve a random number and count the occurrence */
    final RandomService service = new RandomService();
    final int[] candidateCounts = new int[numberOfCandidates];
    for ( int i = 0; i < sampleSize; i++ ) {
      final int randomNumber = service.nextInt( numberOfCandidates );
      candidateCounts[randomNumber]++;
    }

    /* The expected counts for each hand */
    final int expectedCount = sampleSize / numberOfCandidates;
    final int buffer = Math.round( expectedCount * 0.1F );

    /* Verify that each number has the same probability like any other */
    for ( int i = 0; i < numberOfCandidates; i++ ) {
      assertThat( candidateCounts[i] )
        .isBetween( expectedCount - buffer, expectedCount + buffer );
    }
  }
}
