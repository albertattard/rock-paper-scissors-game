package demo.games.pvc;

import demo.games.shared.Hand;
import org.springframework.stereotype.Service;

@Service
public class PvcGameService {

  private final RandomService randomService;

  public PvcGameService( final RandomService randomService ) {
    this.randomService = randomService;
  }

  public PlayResult play( final Hand player ) {
    final Hand computer = random();
    final Outcome outcome = determineOutcome( computer, player );
    return new PlayResult( computer, player, outcome );
  }

  private Outcome determineOutcome( final Hand computer, final Hand player ) {
    return computer == player ? Outcome.DRAW :
      computer.beatenBy() == player ? Outcome.PLAYER_WIN :
        Outcome.COMPUTER_WIN;
  }

  public Hand random() {
    final Hand[] candidates = Hand.values();
    return candidates[randomService.nextInt( candidates.length )];
  }
}
