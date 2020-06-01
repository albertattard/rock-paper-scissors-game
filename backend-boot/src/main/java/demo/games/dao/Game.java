package demo.games.dao;

import demo.games.model.GameState;
import demo.games.model.Hand;
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

  @Column( name = "player_1" )
  @Enumerated( EnumType.STRING )
  private Hand player1;

  @Column( name = "player_2" )
  @Enumerated( EnumType.STRING )
  private Hand player2;

  @Enumerated( EnumType.STRING )
  private GameState state;
}
