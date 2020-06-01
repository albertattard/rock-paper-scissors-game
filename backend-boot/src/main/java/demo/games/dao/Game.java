package demo.games.dao;

import demo.games.model.GameState;
import demo.games.model.Hand;
import demo.games.model.PvpOutcome;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table( name = "games" )
@Accessors( chain = true )
public class Game {

  @Id
  private String code;

  @Enumerated( EnumType.STRING )
  @Column( name = "player_1" )
  private Hand player1;

  @Enumerated( EnumType.STRING )
  @Column( name = "player_2" )
  private Hand player2;

  @Enumerated( EnumType.STRING )
  private GameState state;

  @Enumerated( EnumType.STRING )
  private PvpOutcome outcome;
}
