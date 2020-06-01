package demo.games.model;

public enum Hand {
  ROCK,
  PAPER,
  SCISSORS;

  public Hand beatenBy() {
    final Hand[] hands = Hand.values();
    return hands[( ordinal() + 1 ) % hands.length];
  }
}
