package demo.games.model;

public enum PvcOutcome {
  PLAYER_WIN,
  COMPUTER_WIN,
  DRAW;

  public static PvcOutcome of( final Hand computer, final Hand player ) {
    return computer == player ? PvcOutcome.DRAW :
      computer.beatenBy() == player ? PvcOutcome.PLAYER_WIN :
        PvcOutcome.COMPUTER_WIN;
  }
}
