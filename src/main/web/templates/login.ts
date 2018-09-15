import {html} from 'lit-html';
import {when} from 'lit-html/directives/when';

import {AppState, initialState} from '../models/state';
import {api, updateState} from './app';

const onLogin = (state: AppState) => (event: Event) => {
    event.preventDefault();
    const name = (event.target['name'] as HTMLInputElement).value;
    Promise.all([api.login(name), api.getRooms()])
        .then(([me, rooms]) => updateState({...state, me, rooms, error: null}))
        .catch(err => updateState({...state, error: err}));
    return false;
};

const onLogout = (state: AppState) => () =>
    api.logout(state.me.id)
        .then(() => updateState(initialState))
        .catch(err => updateState({...state, error: err}));

const loggedTemplate = (state: AppState) => () =>
    html`
    <div class="me">
        <span class="name">${state.me.name}</span>, 
        <span class="score">${state.me.score}</span>
        <button type="button" @click=${onLogout(state)}>Logout</button>
    </div>`;

const loginFormTempalte = (state: AppState) => () =>
    html`
      <form name="login" @submit='${onLogin(state)}'>
        <label> Login <input name='name' value='' required></label>
        <button>Login</button>
      </form>`;

export const loginTemplate = (state: AppState) =>
    html`${when(state.me, loggedTemplate(state), loginFormTempalte(state))}`;


