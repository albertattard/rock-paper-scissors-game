package db.migration;

import demo.games.model.Hand;
import demo.games.model.PvpOutcome;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class V3__add_outcome_to_game extends BaseJavaMigration {

  private static final String ADD_COLUMN = "ALTER TABLE games ADD COLUMN outcome CHAR(12) DEFAULT NULL";

  private static final String SELECT_CLOSED = "SELECT * FROM games WHERE state = 'CLOSED'";

  private static final String SET_OUTCOME = "UPDATE games SET outcome = ? WHERE code = ?";

  @Override
  public void migrate( final Context context ) throws Exception {
    try ( final Statement statement = context.getConnection().createStatement();
      final PreparedStatement update = context.getConnection().prepareStatement( SET_OUTCOME ) ) {
      statement.execute( ADD_COLUMN );

      try ( final ResultSet rows = statement.executeQuery( SELECT_CLOSED ) ) {
        while ( rows.next() ) {

          final String code = rows.getString( "code" );
          /* Char is being used and this the db value will have trailing spaces */
          final Hand player1 = Hand.valueOf( rows.getString( "player_1" ).stripTrailing() );
          final Hand player2 = Hand.valueOf( rows.getString( "player_2" ).stripTrailing() );
          final PvpOutcome outcome = PvpOutcome.of( player1, player2 );

          update.clearParameters();
          update.setString( 1, outcome.name() );
          update.setString( 2, code );
          update.execute();
        }
      }
    }
  }
}
