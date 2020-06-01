package demo.games.model;

public enum PvpOutcome {
  PLAYER_1_WIN,
  PLAYER_2_WIN,
  DRAW;

  public static PvpOutcome of( final Hand player1, final Hand player2 ) {
    return player1 == player2 ? PvpOutcome.DRAW :
      player1.beatenBy() == player2 ? PvpOutcome.PLAYER_2_WIN :
        PvpOutcome.PLAYER_1_WIN;
  }
}
