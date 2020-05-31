package demo.games.pvp;

import demo.games.shared.Hand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors( chain = true )
public class GameDetails {
  private String code;
  private Hand player1;
  private Hand player2;
  private Outcome outcome;
  private GameState state;
}
