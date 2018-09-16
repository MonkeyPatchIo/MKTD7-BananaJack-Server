import {html} from 'lit-html';

import {AppState, initialState} from '../models/state';
import {api, updateState} from './app';
import {stopProgress} from '../services/progess';

const onLogin = (state: AppState) => (event: Event) => {
    event.preventDefault();
    const name = (event.target['name'] as HTMLInputElement).value;
    Promise.all([api.login(name), api.getRooms()])
        .then(([me, rooms]) => updateState({...state, me, rooms, error: null}))
        .catch(err => updateState({...state, error: err}));
    return false;
};

const onLogout = (state: AppState) => () => {
    stopProgress();
    return api.logout(state.me.id)
        .then(() => updateState(initialState))
        .catch(err => updateState({...state, error: err}));
};

export const loggedTemplate = (state: AppState) =>
    html`
<div class="logged">
  <span class="name">${state.me.name}</span>
  <button type="button" @click=${onLogout(state)}>Logout</button>
</div>`;

export const loginFormTempalte = (state: AppState) =>
    html`
<section>
  <form name="login" @submit='${onLogin(state)}'>
    <label> Name <input name='name' value='' required placeholder="Enter your name"></label>
    <button>Login</button>
  </form>
</section>`;
