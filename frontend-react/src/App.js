import React, {Component} from 'react';
import './App.css';

class App extends Component {

  state = {
    message: "Select the hand you like to play",
    selectedHand: "ROCK",
    activeGames: []
  }

  constructor(prop) {
    super(prop);

    // this.createNewGame = this.createNewGame.bind(this);
    // this.playOpponent = this.playOpponent.bind(this);
  }

  createNewGame() {
    const selected = this.state.selectedHand;
    this.setState({message: `Playing ${selected}...`});

    fetch('/game', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({player1: selected})
    })
      .then(response => this.setState({message: response.headers.get('Location')}))
      .catch(e => this.setState({message: 'Failed to create game!!'}));
  }

  playOpponent(code, hand) {
    fetch(`/game/${code}`, {
      method: 'PUT',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({player2: hand})
    })
      .then(response => response.json())
      .then(json => this.setState({message: JSON.stringify(json)}))
      .then(() => this.updateGames())
      .catch(e => this.setState({message: 'Failed to create game!!'}));
  }

  componentDidMount() {
    this.updateGames()
  }

  updateGames() {
    fetch('/game/list')
      .then(response => response.json())
      .then(activeGames => this.setState({activeGames}));
  }

  render() {
    return (
      <div className="App">
        <h1>Rock Paper Scissors</h1>
        <p>{this.state.message}</p>
        <select
          value={this.state.selectedHand}
          onChange={e => this.setState({selectedHand: e.target.value})}
        >
          <option value="ROCK">Rock</option>
          <option value="PAPER">Paper</option>
          <option value="SCISSORS">Scissors</option>
        </select>

        <button onClick={this.createNewGame}>New Game</button>
        <h1>Active Games</h1>
        <table id='active-games'>
          <thead>
          <tr>
            <th>#</th>
            <th>Game</th>
            <th></th>
          </tr>
          </thead>
          <tbody>
          {this.renderTableData()}
          </tbody>
        </table>
      </div>
    );
  }

  renderTableData() {
    return this.state.activeGames.map((game, index) => {
      const {code} = game
      return (
        <tr key={code}>
          <td>{index + 1}</td>
          <td><a>{code}</a></td>
          <td>Play:
            <button onClick={() => this.playOpponent(code, "ROCK")}>Rock</button>
            <button onClick={() => this.playOpponent(code, "PAPER")}>Paper</button>
            <button onClick={() => this.playOpponent(code, "SCISSORS")}>Scissors</button>
          </td>
        </tr>
      )
    });
  }
}

export default App;
