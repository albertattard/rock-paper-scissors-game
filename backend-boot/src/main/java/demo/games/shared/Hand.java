package demo.games.shared;

public enum Hand {
  ROCK,
  PAPER,
  SCISSORS;

  public Hand beatenBy() {
    final Hand[] hands = Hand.values();
    return hands[( ordinal() + 1 ) % hands.length];
  }
}
