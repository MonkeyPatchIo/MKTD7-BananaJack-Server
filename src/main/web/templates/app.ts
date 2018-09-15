import {html, render} from 'lit-html';
import {when} from 'lit-html/directives/when';

import {BackendApi} from '../services/BackendApi';
import {AppState} from '../models/state';
import {loginTemplate} from './login';
import {RoomEvent, RoomEventListener} from '../models/models';
import {roomsTemplate} from './rooms';
import {currentTemplate} from './current';

const progress = document.querySelector<HTMLProgressElement>('body > header progress');
let timerId;
const startProgress = () => {
    clearInterval(timerId);
    progress.value = 0;
    timerId = setInterval(() => progress.value += 1, 500);
};
const stopProgress = () => {
    clearInterval(timerId);
    progress.value = 0;
};

const onEvent: RoomEventListener = (event: RoomEvent, state: AppState) => {
    console.debug('event', event.type);
    switch (event.type) {
        case 'turn-started':
            updateState({...state, current: event.room, lastEvent: event});
            startProgress();
            break;

        case 'turn-ended':
            const current = event.room.players.find(it => it.player.id === state.me.id) ? event.room : null;
            updateState({...state, current, lastEvent: event});
            startProgress();
            break;

        case 'player-joining':
        case 'player-leaving':
        case 'player-action':
            // omit last event
            updateState({...state, current: event.room});
            break;

        case 'round-ended':
            clearInterval(timerId);
            updateState({...state, current: event.room, lastEvent: event});
            stopProgress();
            break;

        default:
            console.warn(`type: ${event.type} not supported`, event);
    }
};

export const api = new BackendApi('http://ilaborie.org:9898', onEvent);
const main = document.querySelector('main');

const errorTemplate = (state: AppState) => () =>
    html`
<div class="error">
  ${state.error}
  <button class="button" @click=${() => updateState({...state, error: null})}>Close</button>
</div>`;

const bodyTemplate = (state: AppState) => {
    if (state.me === null) {
        return loginTemplate(state);
    } else if (state.current) {
        return currentTemplate(state);
    } else {
        return roomsTemplate(state);
    }
};

const mainTemplate = (state: AppState) =>
    html`
${when(state.error,
        errorTemplate(state),
        () => html`<section>${bodyTemplate(state)}</section>`)}`;

export const updateState = (state: AppState) => {
    console.debug('updateState', state);
    api.state = state;
    render(mainTemplate(state), main);
};
