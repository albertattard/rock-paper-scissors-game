package demo.games.resource;

import demo.games.model.ActiveGame;
import demo.games.model.AllGames;
import demo.games.model.CreateGame;
import demo.games.model.GameDetails;
import demo.games.model.Hand;
import demo.games.model.RandomHand;
import demo.games.model.PlayGame;
import demo.games.model.PvcGameResult;
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

  @GetMapping( "/hand" )
  public @ResponseBody RandomHand hand() {
    final Hand hand = service.random();
    return new RandomHand( hand );
  }

  @GetMapping( "/play/{player}" )
  public @ResponseBody PvcGameResult play( final @PathVariable( "player" ) Hand player ) {
    return service.play( player );
  }

  @ResponseBody
  @PostMapping( "/game" )
  public ResponseEntity<ActiveGame> create( final @RequestBody CreateGame game ) throws URISyntaxException {
    final ActiveGame response = service.create( game.getPlayer1() );
    final URI uri = new URI( String.format( "/game/%s", response.getCode() ) );
    return ResponseEntity.created( uri ).body( response );
  }

  @ResponseBody
  @PutMapping( "/game/{code}" )
  public ResponseEntity<GameDetails> play( final @PathVariable( "code" ) String code, final @RequestBody PlayGame game ) {
    return service.play( code, game.getPlayer2() )
      .map( ResponseEntity::ok )
      .orElse( ResponseEntity.notFound().build() );
  }

  @ResponseBody
  @GetMapping( "/game/{code}" )
  public ResponseEntity<GameDetails> list( final @PathVariable( "code" ) String code ) {
    return service.findGame( code )
      .map( ResponseEntity::ok )
      .orElse( ResponseEntity.notFound().build() );
  }

  @ResponseBody
  @GetMapping( "/game/list/all" )
  public AllGames listAllGames() {
    return new AllGames( service.listActiveGames(), service.listClosedGames() );
  }

  @ResponseBody
  @GetMapping( "/game/list/open" )
  public List<ActiveGame> listOpenGames() {
    return service.listActiveGames();
  }
}
