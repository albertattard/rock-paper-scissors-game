package demo.games.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PvcGameResult {
  private Hand computer;
  private Hand player;
  private PvcOutcome outcome;
}
