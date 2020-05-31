package demo.games.pvp;

import demo.games.shared.Hand;
import demo.games.pvc.Outcome;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameResult {
  private Hand player1;
  private Hand player2;
  private Outcome outcome;
}
