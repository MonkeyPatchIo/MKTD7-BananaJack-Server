import {Action, Player, Room, RoomEvent, RoomEventListener} from '../models/models';
import {AppState} from '../models/state';


export class BackendApi {

    private _state: AppState;
    get state(): AppState {
        return this._state;
    }

    set state(newState: AppState) {
        this._state = newState;
    }

    constructor(readonly url: string,
                readonly listener: RoomEventListener) {
        console.debug('Use', url);
    }

    // TODO WS
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