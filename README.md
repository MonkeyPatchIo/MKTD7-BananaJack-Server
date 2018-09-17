BananaJack
===


Rules
---

The purpose is to earn lots of 🍌.
The rules are a very simplified version of BlackJack.

When you enter the room

- you had to wait the end of the active round
- at the beginning of the round, two card are given to every active players, including the *bank*
- the score is the sum of the value of your cards (King, Queen and Jack give 10, Ace can give 1 or 11).
  - if score is 21, you have a BananaJack 🎉
  - if score is greater than 21, you loose 😢
  - otherwise you can `draw` a new card or `stay`, notice than if already have `stay` once, you cannot `draw` a card
- at the end of the round, players with greater score earn a 🍌, if you have a BananaJack you earn one more 🍌.


You can play [here](http://ilaborie.org:9898)


REST API
---


### Login


`POST http://ilaborie.org:9898/api/auth/login`

With body:

```json
{
    "name": "toto"
}
```

Return a `Player`:

```json
{
    "id": "12f128c8a81d9fcf58615b6ba871e74e9c961975",
    "name": "toto",
    "score": 0
}
```

### Logout


`POST http://ilaborie.org:9898/api/auth/logout`

With body:

```json
{
    "playerId": "12f128c8a81d9fcf58615b6ba871e74e9c961975"
}
```

Return a `Player`:

```json
{
    "id": "12f128c8a81d9fcf58615b6ba871e74e9c961975",
    "name": "toto",
    "score": 42
}
```


### Get Rooms


`GET http://ilaborie.org:9898/api/room`


Return an array of `Room`:

```json
[
    {
        "bank": {
            "canDo": [],
            "hand": {
                "cards": [ "4H", "5H" ],
                "score": 9
            },
            "move": "burst"
        },
        "full": false,
        "id": 1,
        "name": "Room #01",
        "players": [
            {
                "player": {
                    "id": "d14202bd59734f221afacdee1ae97d5461088c28",
                    "name": "toto",
                    "score": 2
                },
                "status": {
                    "canDo": [ "draw", "stay" ],
                    "hand": {
                        "cards": [ "0S", "8D" ],
                        "score": 18
                    },
                    "move": "in-game"
                }
            }
        ]
    }, /* ... */
]
```

### Join Room

`POST http://ilaborie.org:9898/api/room/join`

With body:

```json
{
  "roomId":1,
  "playerId":"d14202bd59734f221afacdee1ae97d5461088c28",
}
```

Return the updated `Room`:

```json
{
  "id":1,
  "name":"Room #01",
  "players":[
    {
      "player": {"id":"d14202bd59734f221afacdee1ae97d5461088c28","name":"toto","score":0},
      "status": {
        "hand":{"cards":[],"score":0},
        "move":"wait",
        "canDo":[]
      }
    }
  ],
  "bank": {
    "hand":{"cards":[],"score":0},
    "move":"in-game",
    "canDo":["draw","stay"]
  },
  "full":false
}
```

### Leave Room

`POST http://ilaborie.org:9898/api/room/leave`

With body:

```json
{
  "roomId":1,
  "playerId":"d14202bd59734f221afacdee1ae97d5461088c28",
}
```

Return the updated `Room`


### Do an action `draw`, `stay`

`POST http://ilaborie.org:9898/api/room/move`

With body:

```json
{
  "roomId": 1,
  "playerId": "d14202bd59734f221afacdee1ae97d5461088c28",
  "action": "stay"
}
```

Return the updated `Room`


WebSocket API
---

When you join a room, you need to open a [WebSocket](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API)
at `ws://ilaborie.org:9898/ws/<ROOM_ID>`.

When connected, you also need to send a register message like that

```json
{
  "playerId": "d14202bd59734f221afacdee1ae97d5461088c28",
}
```

Then the server notify when event appends, they look like:

```typescript
interface RoomEvent {
    type: string;
    round?: number;
    step?: number;
    room?: Room;
    roomId?: number;
    player?: Player;
    winners?: string;
    action?: Move;
}
```

(See below for other model definitions).

### type `turn-started`

A new round have started, the `room` attribute contains the updated `Room`.


### type `turn-ended`

A turn is ended, thus another one is starting, the `room` attribute contains the updated `Room`.


### type `round-ended`

A round is ended, the `room` attribute contains the updated `Room`.

### type `player-joining`

A player is joining the room, see the `player` attribute.

### type `player-leaving`

A player is joining the room, see the `player` attribute.

### type `player-action`

A player have made a move, see the `action` attribute.


Suggestions
---

### Typescript Model

```typescript
interface Hand {
    cards: Card[];
    score: number;
}

interface Card {
    code: string;
    image: string;
}

interface Room {
    id: number;
    name: string;
    players: PlayerWithStatus[];
    bank: PlayerStatus;
    full: boolean;
}

interface Player {
    id: string;
    name: string;
    score: number;
}

interface PlayerWithStatus {
    player: Player;
    status: PlayerStatus;
}

interface PlayerStatus {
    hand: Hand;
    move: Move;
    canDo: Action[];
}

type Action = 'draw' | 'stay';

type Move = 'wait' | 'in-game' | 'burst' | 'draw' | 'stay' | 'timeout' ;
```

### Backend API

You can use or be inspired by this backend api. 

```typescript
class BackendApi {

    constructor(readonly url: string,
                readonly listener: (RoomEvent) -> void) {}
    
    private handle<T>(res: Response): Promise<T> {
        if (res.ok) {
            return res.json();
        } else {
            return res.text()
                .then(msg => Promise.reject(msg));
        }
    }

    private getJson<T>(uri: string): Promise<T> {
        return fetch(this.url + uri)
            .then(res => this.handle<T>(res));
    };

    private postJson<T>(uri: string, json: any): Promise<T> {
        const request: RequestInit = {
            method: 'POST',
            body: JSON.stringify(json)
        };
        return fetch(this.url + uri, request)
            .then(res => this.handle<T>(res));
    };

    private registerWS(roomId: number, playerId: string) {
        const wsUrl = this.url.replace('http://', 'ws://');
        const ws = new WebSocket(wsUrl + `/ws/${roomId}`);

        // Register events
        ws.onopen = () => {
            console.debug('WS open', roomId);
            // Register to room
            ws.send(JSON.stringify({playerId}));
        };
        ws.onmessage = (event: MessageEvent) => {
            console.debug('WS message', event.data);
            const roomEvent = JSON.parse(event.data) as RoomEvent;
            this.listener(roomEvent, this.state);
        };
        ws.onclose = () => console.info('WS close');
        ws.onerror = (event: Event) => console.error('WS error', event);
    }

    // Auth
    login(name: string): Promise<Player> {
        return this.postJson('/api/auth/login', {name});
    }

    logout(playerId: string): Promise<Player> {
        return this.postJson('/api/auth/logout', {playerId});
    }

    // Room
    getRooms(): Promise<Room[]> {
        return this.getJson('/api/room');
    }

    join(roomId: number, playerId: string): Promise<Room> {
        return this.postJson<Room>('/api/room/join', {roomId, playerId})
            .then(room => {
                this.registerWS(roomId, playerId);
                return room;
            });
    }

    leave(roomId: number, playerId: string): Promise<Room> {
        return this.postJson('/api/room/leave', {roomId, playerId})
    }

    action(roomId: number, playerId: string, action: Action): Promise<Room> {
        return this.postJson('/api/room/move', {roomId, playerId, action})
    }
}
```

### HTML & CSS code


#### Error Page

```html
<body>
    <header>
        <h1>Banana Jack</h1>
        <progress value="0" max="58"></progress>
    </header>
    <main>
        <div class="error">
            TypeError: Failed to fetch
            <button class="button">Close</button>
        </div>
    </main>
    <footer>Made with 🍌 in Toulouse</footer>
</body>
```

#### Login Page

```html
<main>
    <section>
      <form name="login">
        <label> Name <input name="name" value="" required="" placeholder="Enter your name"></label>
        <button>Login</button>
      </form>
    </section>
</main>
```

#### Rooms Page

```html
<main>
    <div class="logged">
        <span class="name">toto</span>
        <button type="button">Logout</button>
    </div>
    <section>
        <ul class="rooms">
            <li>
                <div class="room">
                    <div class="name">
                        <h2>Room #01</h2>
                        <div class="players">Tata, Titi</div>
                    </div>
                    <div class="status ">2 / 4</div>
                    <div class="action">
                        <button type="button" class="join">Join</button>
                    </div>
                </div>
            </li>
            <!-- ... -->
        </ul>
    </section>
</main>
```

### Current Room Page 

```html
<section>
  <div class="room-current">
    <header>
      <div class="name">Room #01</div>
      <button type="button">Leave</button>
    </header>
    
    <div class="players">
      <!--bank-->
      <div class="player bank">
        <div class="name">Bank</div>
        <div class="score">16</div>
        <div class="move"></div>
        <div class="cards">
          <div class="card">
            <img src="assets/0D.png" alt="0D">
          </div>
          <div class="card">
            <img src="assets/6C.png" alt="6C">
          </div>
        </div>
        <div class="actions"></div>
      </div>
      
      <!-- me -->
      <div class="player me in-game">
        <div class="name">toto</div>
        <div class="score">10</div>
        <div class="move"></div>
        <div class="cards">
            <div class="card">
              <img src="assets/4S.png" alt="4S">
            </div>
            <div class="card">
              <img src="assets/2C.png" alt="2C">
            </div>
            <div class="card">
              <img src="assets/4C.png" alt="4C">
            </div>
        </div>
        <div class="actions">
          <button type="button">Draw</button>
          <button type="button">Stay</button>
        </div>
      </div>
        
      <!--other players-->              
      <div class="player in-game">
        <div class="name">tata</div>
        <div class="score">13</div>
        <div class="move"></div>
        <div class="cards">
          <div class="card">
            <img src="assets/4H.png" alt="4H">
          </div>
          <div class="card">
            <img src="assets/AH.png" alt="AH">
          </div>
          <div class="card">
            <img src="assets/8C.png" alt="8C">
          </div>
        </div>
        <div class="actions">
          <span>draw</span><span>stay</span>
        </div>
      </div>
      <!-- ... -->   
    </div>
  </div>
</section>
```

#### CSS stylesheet

```css
:root {
	--green: forestgreen;
	--greenText: ghostwhite;
	--red: #7a0017;
	--yellow: #ffc600;
}

/*body*/
body {
	background-color: var(--green, green);
	color: var(--greenText, white);
	padding: 0;
	margin: 0;
	display: flex;
	flex-direction: column;
	height: 100vh;
	font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

body > header, body > footer {
	margin: 0;
	padding: .25rem;
	text-align: center;
	font-family: 'Fascinate', cursive;
	background: rgba(0, 0, 0, .25);
}

body > header {
	font-size: 1.25em;
	flex-shrink: 0;
	box-shadow: 0 .125em .125em rgba(0, 0, 0, .5);
	padding-bottom: 0;
	position: relative;
}

h1 {
	margin: .125em;
	display: inline-block;
}

h1::before, h1::after {
	margin: 0 .25em;
	content: '🍌';
}

/*progress*/

progress {
	width: 100%;
	-moz-appearance: none;
	-webkit-appearance: none;
	appearance: none;
	border: none;
	height: .25rem;
	animation: all 1s;
	position: absolute;
	bottom: 0;
	left: 0;
	right: 0;
}

progress::-webkit-progress-bar {
	background: transparent;
}

progress::-webkit-progress-value {
	background: var(--yellow, yellow);
}

progress::-moz-progress-bar {
	background: var(--yellow, yellow);
}

/*main*/
main {
	flex: 1 1 auto;
	display: flex;
	flex-direction: column;
	justify-content: space-around;
	align-items: center;
	overflow: auto;
}

main section {
	min-width: 75vw;
	margin: .5em;
	padding: .25rem;
	border: thin solid hsla(0, 100%, 100%, .5);
}

main section:empty {
	display: none;
	border-color: transparent;
}

/*error*/

main .error {
	background: var(--red, red);
	font-size: 2em;
	padding: .5em;
	border-radius: .25em;
	border: .25em solid rgba(0, 0, 0, .5);
}

main .error::before {
	content: '💣';
	margin-right: .5em;
}

/*button*/

button {
	font-size: 1.2rem;
	border: .125em solid rgba(0, 0, 0, .25);
	border-radius: .125rem;
	color: var(--green, green);
	text-shadow: .0625em .0625em .25em rgba(0, 0, 0, .25);
	animation: all .4s;
	min-height: 1.8rem;
	margin: .25em;
	background: hsla(0, 100%, 100%, .85);
}

button:hover {
	background: var(--yellow, yellow);
	box-shadow: 1px 1px .0625rem black;
	transform: translateY(-1px);
	outline: thin solid transparent;
}

/*login*/

form[name=login] {
	display: flex;
	flex-direction: column;
	align-items: center;
	font-size: 1.2em;
	margin: 1em;
}

form label, form button {
	width: 50%;
}

form label {
	display: flex;
	align-items: center;
}

form label input {
	flex-grow: 1;
	margin: .5em;
	background: hsla(0, 100%, 100%, .5);
	border: .125em solid rgba(0, 0, 0, .25);
	border-radius: .125em;
	padding: .125em;
	font-size: 1em;
	color: var(--green, green);
	transition: all .4s;
	outline: thin currentColor;
}

form label input:focus {
	background: hsla(0, 100%, 100%, .75);
}

/*me*/
.logged {
	position: absolute;
	top: 0;
	right: 0;
}

/*rooms*/
.rooms {
	padding: 1rem .5rem;
	list-style: none;
	display: grid;
	margin: 0 auto;
	grid-template-columns: repeat(auto-fill, minmax(16em, 1fr));
	grid-auto-rows: minmax(3em, auto);
	grid-gap: .25em .5em;
}

.rooms li, .rooms .room {
	height: 100%;
	box-sizing: border-box;
}

.rooms .room {
	padding: .5rem;
	border: thin solid rgba(0, 0, 0, .25);
	min-width: 16ch;
	display: flex;
	align-items: center;
	background: rgba(0, 0, 0, .25);
}

.rooms .room .name {
	font-family: 'Fascinate', sans-serif;
}

.rooms .room .status {
	flex-grow: 1;
	text-align: center;
}

/*current*/

.room-current header {
	display: flex;
	justify-content: space-between;
	font-size: 1.2em;
	border-bottom: .125em solid rgba(0, 0, 0, .25);
	text-shadow: 1px 1px 2px black;
	background: rgba(0, 0, 0, .25);
	box-shadow: 0 0 0 .25em rgba(0, 0, 0, .25);
	align-items: center;
}

.room-current header .name {
	font-family: 'Fascinate', cursive;
	font-size: 1.5em;
}

/*room winner*/
.room-current .winner {
	color: var(--yellow, yellow);
}

.room-current .winner::before, .room-current .winner::after {
	content: '🎊';
	margin: 0 .5em;
}

.room-current .winner::before {
	content: 'Winner: ';
}

/*room players*/
.room-current .players {
	display: grid;
	margin: .5em 0;
	grid-template-columns: repeat(auto-fill, minmax(24em, 1fr));
	grid-gap: 1em;
}

.room-current .player {
	padding: .25rem;
	border: thin solid currentColor;
	display: inline-block;
}

.room-current .player.bank {
	background: hsla(0, 100%, 100%, .25);
	border-color: hsla(0, 100%, 100%, .25);
}

.room-current .player.me {
	background: rgba(0, 0, 0, .25);
	border-color: rgba(0, 0, 0, .25);
}

.room-current .player.me {
	grid-column: 1;
}

.room-current .player .move {
	text-align: right;
}

.room-current .player.timeout {
	opacity: .25;
}
.room-current .player.timeout .move::before {
	content: '💤';
}
.room-current .player.draw .move::before {
	content: '▶️';
}
.room-current .player.stay .move::before {
	content: '⏹';
}
.room-current .player.burst .move::before {
	content: '💣';
}
.room-current .player.burst {
	color: var(--red, red);
}
.room-current .player.wait .move::before {
	content: '😴';
}
.room-current .player.in-game .move::before {
	content: '⏰';
}

.room-current .player {
	border: thin solid transparent;
	overflow: hidden;
	display: grid;
	grid-template-columns: 2fr 1fr 1fr;
	grid-template-rows: 2em 1fr 2.5em;
	grid-auto-flow: dense;
}

.room-current .player .cards {
	grid-column: 1 / 4;
	display: flex;
	align-items: center;
}

.room-current .player .cards .card {
	flex: 1 2 3em;
	max-width: 3em;
}

.room-current .player .cards:first-child {
	box-shadow: 0 0 0 1px red;
}

.room-current .player .cards:nth-child(1+n) {
	margin-left: -6em;
}

.room-current .player .cards .card img {
	max-height: 12em;
}

.room-current .player .actions {
	grid-column: 1 / 4;
	display: flex;
	justify-content: space-around;
	border-top: .125em solid rgba(0, 0, 0, .25);
}
```