import {html} from 'lit-html';
import {when} from 'lit-html/directives/when';
import {repeat} from 'lit-html/directives/repeat';

import {Action, actionLabel, Hand, PlayerWithStatus} from '../models/models';
import {AppState} from '../models/state';
import {api, updateState} from './app';
import {stopProgress} from '../services/progess';

const onLeave = (state: AppState) => () => {
    stopProgress();
    api.leave(state.current.id, state.me.id)
        .then(() => api.getRooms())
        .then(rooms => updateState({...state, rooms, current: null}))
        .catch(err => updateState({...state, error: err}));
};
const onAction = (state: AppState, action: Action) => () =>
    api.action(state.current.id, state.me.id, action)
        .then(current => updateState({...state, current}))
        .catch(err => updateState({...state, error: err}));

const handTpl = (hand: Hand) =>
    html`${hand.cards.map(card => html`
<div class="card">
  <img src=${`assets/${card}.png`} alt=${card}>
</div>`)}`;

const playerTpl = ({player, status}: PlayerWithStatus) =>
    html`
<div class="player ${status.move}">
  <div class="name">${player.name}</div>
  <div class="score">${status.hand.score}</div>
  <div class="move"></div>
  <div class="cards">${handTpl(status.hand)}</div>
  <div class="actions">
      ${repeat(status.canDo, x => x, action => html`<span>${action}</span>`)}
  </div>
</div>`;

const currentTpl = (state: AppState) => () => {
    let current = state.current;
    const ps = state.me && current.players.find(it => it.player.id === state.me.id);

    const winner = (state.lastEvent && state.lastEvent.winners) ? state.lastEvent.winners : null;
    return html`
<div class="room-current">
  <header>
    <div class="name">${current.name}</div>
    <button type="button" @click=${onLeave(state)}>Leave</button>
  </header>
  
  ${winner ? html`<h3 class="winner">${winner}</h3>` : ''}
  
  <div class="players">
      <!--bank-->
      <div class="player bank">
        <div class="name">Bank</div>
        <div class="score">${current.bank.hand.score}</div>
        <div class="move"></div>
        <div class="cards">${handTpl(current.bank.hand)}</div>
        <div class="actions"></div>
      </div>
      
      <!-- me -->
      ${ps ? html`
      <div class="player me ${ps.status.move}">
        <div class="name">${ps.player.name}</div>
        <div class="score">${ps.status.hand.score}</div>
        <div class="move"></div>
        <div class="cards">${handTpl(ps.status.hand)}</div>
        <div class="actions">
          ${repeat(ps.status.canDo, action => action, (action) =>
        html`<button type="button" @click=${onAction(state, action)}>
               ${actionLabel(action)}
             </button>`)}
        </div>
      </div>
        
      <!--other players-->
      ${repeat(current.players.filter(it => !state.me || it.player.id !== state.me.id),
        it => it.player.id,
        (it) => playerTpl(it))}` : ''}
      </div>
</div>`;
};

export const currentTemplate = (state: AppState) =>
    html`${when(state.current, currentTpl(state), () => html``)}`;
