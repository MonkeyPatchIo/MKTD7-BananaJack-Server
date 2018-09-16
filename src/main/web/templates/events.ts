import {PlayerStatus, RoomEvent, RoomEventListener} from '../models/models';
import {AppState} from '../models/state';
import {startProgress, stopProgress} from '../services/progess';
import {updateState} from './app';

export const onEvent: RoomEventListener = (event: RoomEvent, state: AppState) => {

    switch (event.type) {
        case 'turn-started':
            if (state.current && event.room.id == state.current.id) {
                updateState({...state, current: event.room, lastEvent: event});
                startProgress();
            }
            break;

        case 'turn-ended':
            if (state.current && event.room.id == state.current.id) {
                const current = event.room.players.find(it => it.player.id === state.me.id) ? event.room : null;
                updateState({...state, current, lastEvent: event});
                startProgress();
            }
            break;

        case 'round-ended':
            if (state.current && event.room.id == state.current.id) {
                stopProgress();
                updateState({...state, current: event.room, lastEvent: event});
            }
            break;

        case 'player-joining':
        case 'player-leaving':
        case 'player-action':
            if (state.current && event.roomId == state.current.id) {
                const players = state.current.players;
                const player = event.player;
                switch (event.type) {
                    case 'player-joining':
                        const status: PlayerStatus = {hand: {score: 0, cards: []}, move: 'wait', canDo: []};
                        players.push({player, status});
                        break;
                    case 'player-leaving':
                        const idx = players.findIndex(it => it.player.id == player.id);
                        players.splice(idx, 1);
                        break;
                    case 'player-action':
                        const p = players.find(it => it.player.id == player.id);
                        if (p) {
                            p.status.move = event.action;
                        }
                        break;
                }
                updateState({...state, current: {...state.current, players}});
            }
            break;

        default:
            console.warn(`type: ${event.type} not supported`, event);
    }
};