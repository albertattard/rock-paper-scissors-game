import React, {Component} from 'react';
import './App.css';

class App extends Component {

  state = {
    opponentType: "COMPUTER",
    gameState: "GAME_HAVE_NOT_STATED",
    code: "",
    result: {},
    activeGames: [],
    closedGames: []
  }

  constructor(props) {
    super(props);

    this.checkGameStatus = this.checkGameStatus.bind(this);
  }

  playGame(player) {
    const {opponentType} = this.state;
    if (opponentType === 'COMPUTER') {
      this.playerAgainstComputer(player);
    } else if (opponentType === 'PLAYER') {
      this.createNewGame(player)
    }
  }

  createNewGame(player1) {
    fetch('/game', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({player1})
    })
      .then(response => response.json())
      .then(json => {
        const {code} = json;
        this.setState({
          gameState: 'WAITING_FOR_OPPONENT',
          code,
          result: {}
        })
      })
      .then(() => this.updateActiveGames())
      .then(() => setTimeout(this.checkGameStatus, 1000))
      .catch(e => console.log(e));
  }

  checkGameStatus() {
    fetch(`/game/${this.state.code}`)
      .then(response => response.json())
      .then(json => {
        const {state} = json;
        if (state === 'ACTIVE') {
          setTimeout(this.checkGameStatus, 1000)
        } else {
          const {player1, player2, outcome} = json;
          this.setState({
            gameState: 'OPPONENT_PLAYED',
            result: {player: player1, opponent: player2, outcome}
          })
        }
      })
      .catch(e => console.log(e));
  }

  playerAgainstComputer(player) {
    fetch(`/play/${player}`)
      .then(response => response.json())
      .then(json => {
        const {computer, player, outcome} = json;
        this.setState({
            gameState: 'OPPONENT_PLAYED',
            result: {player, opponent: computer, outcome}
          }
        )
      })
      .catch(e => console.log(e));
  }

  playOpponent(code, player2) {
    fetch(`/game/${code}`, {
      method: 'PUT',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({player2})
    })
      .then(response => response.json())
      .then(() => this.updateAllGames())
      .catch(e => console.log(e));
  }

  componentDidMount() {
    this.updateAllGames()
  }

  updateActiveGames() {
    fetch('/game/list/open')
      .then(response => response.json())
      .then(activeGames => this.setState({activeGames}));
  }

  updateAllGames() {
    fetch('/game/list/all')
      .then(response => response.json())
      .then(games => this.setState({
        activeGames: games.activeGames,
        closedGames: games.closedGames
      }));
  }

  render() {
    return (
      <div className="App">
        {this.renderNewGamePane()}
        {this.renderActiveGamesPane()}
        {this.renderClosedGamesPane()}
      </div>
    );
  }

  renderNewGamePane() {
    return (
      <div className="new-game">
        <h1>Rock Paper Scissors</h1>
        <p>
          Select the hand you want to play against
          <select value={this.state.opponentType}
                  onChange={e => this.setState({opponentType: e.target.value})}>
            <option value="COMPUTER">Computer</option>
            <option value="PLAYER">Player</option>
          </select>
        </p>
        <button onClick={() => this.playGame("ROCK")}>Rock</button>
        <button onClick={() => this.playGame("PAPER")}>Paper</button>
        <button onClick={() => this.playGame("SCISSORS")}>Scissors</button>
        {this.renderPlayedGameStatus()}
      </div>
    );
  }

  renderPlayedGameStatus() {
    if (this.state.gameState === 'WAITING_FOR_OPPONENT') {
      return this.renderWaitingForOpponentGameStatus()
    }

    if (this.state.gameState === 'GAME_HAVE_NOT_STATED') {
      return this.renderGameHaveNotStartedGameStatus()
    }

    return this.renderPendingPlayedGameStatus()
  }

  renderWaitingForOpponentGameStatus() {
    return (
      <div className="played-game">
        <h1>Waiting for opponent to play {this.state.code}...</h1>
      </div>
    );
  }

  renderGameHaveNotStartedGameStatus() {
    return (
      <div className="played-game">
        <h1>No game</h1>
      </div>
    );
  }

  renderPendingPlayedGameStatus() {
    const {player, opponent, outcome} = this.state.result;
    return (
      <div className="played-game">
        <h1>{player} vs. {opponent} = {outcome}</h1>
      </div>
    );
  }

  renderActiveGamesPane() {
    return (
      <div className='active-games'>
        <h1>Active Games</h1>
        <table>
          <thead>
          <tr>
            <th>#</th>
            <th>Game</th>
            <th>Challenge</th>
          </tr>
          </thead>
          <tbody>
          {this.renderActiveGamesTableData()}
          </tbody>
        </table>
      </div>
    );
  }

  renderActiveGamesTableData() {
    return this.state.activeGames.map((game, index) => {
      const {code} = game
      return (
        <tr key={`active-${code}`}>
          <td>{index + 1}</td>
          <td><a>{code}</a></td>
          <td>
            <button onClick={() => this.playOpponent(code, "ROCK")}>Rock</button>
            <button onClick={() => this.playOpponent(code, "PAPER")}>Paper</button>
            <button onClick={() => this.playOpponent(code, "SCISSORS")}>Scissors</button>
          </td>
        </tr>
      )
    });
  }

  renderClosedGamesPane() {
    return (
      <div className="closed-games">
        <h1>Closed Games</h1>
        <table>
          <thead>
          <tr>
            <th>#</th>
            <th>Game</th>
            <th>Player 1</th>
            <th>Player 2</th>
            <th>Outcome</th>
          </tr>
          </thead>
          <tbody>
          {this.renderClosedGamesTableData()}
          </tbody>
        </table>
      </div>
    )
  }

  renderClosedGamesTableData() {
    return this.state.closedGames.map((game, index) => {
      const {code, player1, player2, outcome} = game
      return (
        <tr key={`closed-${code}`}>
          <td>{index + 1}</td>
          <td>{code}</td>
          <td>{player1}</td>
          <td>{player2}</td>
          <td>{outcome}</td>
        </tr>
      )
    });
  }
}

export default App;
