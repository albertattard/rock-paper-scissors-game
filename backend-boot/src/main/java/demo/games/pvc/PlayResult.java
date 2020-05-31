package demo.games.pvc;

import demo.games.shared.Hand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayResult {
  private Hand computer;
  private Hand player;
  private Outcome outcome;
}
