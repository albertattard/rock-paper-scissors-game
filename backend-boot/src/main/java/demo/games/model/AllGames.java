package demo.games.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllGames {
  private List<ActiveGame> activeGames;
  private List<GameDetails> closedGames;
}
