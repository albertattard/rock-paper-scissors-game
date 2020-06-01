package demo.games.resource;

import demo.games.model.ActiveGame;
import demo.games.model.AllGames;
import demo.games.model.CreateGame;
import demo.games.model.GameDetails;
import demo.games.model.Hand;
import demo.games.model.PlayGame;
import demo.games.model.PvcGameResult;
import demo.games.model.RandomHand;
import demo.games.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Controller
public class GameController {

  private final GameService service;

  public GameController( final GameService service ) {
    this.service = service;
  }

  @GetMapping( "/randomHand" )
  public @ResponseBody RandomHand randomHand() {
    final Hand hand = service.randomHand();
    return new RandomHand( hand );
  }

  @GetMapping( "/pvc/{player}" )
  public @ResponseBody PvcGameResult playAgainstComputer( final @PathVariable( "player" ) Hand player ) {
    return service.playAgainstComputer( player );
  }

  @ResponseBody
  @PostMapping( "/pvp" )
  public ResponseEntity<ActiveGame> createPvpGame( final @RequestBody CreateGame game ) throws URISyntaxException {
    final ActiveGame response = service.createPvpGame( game.getPlayer1() );
    final URI uri = new URI( String.format( "/pvp/%s", response.getCode() ) );
    return ResponseEntity.created( uri ).body( response );
  }

  @ResponseBody
  @PutMapping( "/pvp/{code}" )
  public ResponseEntity<GameDetails> playAgainstPlayer( final @PathVariable( "code" ) String code,
    final @RequestBody PlayGame game ) {
    return service.playAgainstPlayer( code, game.getPlayer2() )
      .map( ResponseEntity::ok )
      .orElse( ResponseEntity.notFound().build() );
  }

  @ResponseBody
  @GetMapping( "/pvp/{code}" )
  public ResponseEntity<GameDetails> findPvpGame( final @PathVariable( "code" ) String code ) {
    return service.findPvpGame( code )
      .map( ResponseEntity::ok )
      .orElse( ResponseEntity.notFound().build() );
  }

  @ResponseBody
  @GetMapping( "/pvp/list/all" )
  public AllGames listAllPvpGames() {
    return new AllGames( service.listActivePvpGames(), service.listClosedPvpGames() );
  }

  @ResponseBody
  @GetMapping( "/pvp/list/active" )
  public List<ActiveGame> listActivePvpGames() {
    return service.listActivePvpGames();
  }
}
