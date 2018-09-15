import {html} from 'lit-html';
import {when} from 'lit-html/directives/when';
import {repeat} from 'lit-html/directives/repeat';

import {AppState} from '../models/state';
import {Room} from '../models/models';
import {api, updateState} from './app';

const onJoin = (room: Room, state: AppState) => () =>
    api.join(room.id, state.me.id)
        .then(current => updateState({...state, current}))
        .catch(err => updateState({...state, error: err}));

const roomTpl = (room: Room, state: AppState) => html`
<div class="room">
  <div class="name">
    <h2>${room.name}</h2>
    <div class="players">${room.players.map(it => it.player.name).join(', ')}</div>
  </div>
  <div class="status ${room.full ? 'full' : ''}">${room.players.length} / 4</div>
  <div class="action">
    ${when(room.full,
    () => html`Full`,
    () => html`<button type="button" class="join" @click=${onJoin(room, state)}>Join</button>`
)}
  </div>
</div>`;

export const roomsTemplate = (state: AppState) => html`<ul class="rooms">
  ${repeat(state.rooms, room => room.name, room => html`<li>${roomTpl(room, state)}</li>`)}
</ul>`;
