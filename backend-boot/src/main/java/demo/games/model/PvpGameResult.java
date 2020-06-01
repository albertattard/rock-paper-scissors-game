package demo.games.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PvpGameResult {
  private Hand player1;
  private Hand player2;
  private PvcOutcome outcome;
}
