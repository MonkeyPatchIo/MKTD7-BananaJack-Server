import {html, render} from 'lit-html';
import {when} from 'lit-html/directives/when';

import {BackendApi} from '../services/BackendApi';
import {AppState} from '../models/state';
import {loggedTemplate, loginFormTempalte} from './login';
import {roomsTemplate} from './rooms';
import {currentTemplate} from './current';
import {onEvent} from './events';


const url = process.env.BACKEND_URL || 'http://localhost:9898';
export const api = new BackendApi(url, onEvent);
const main = document.querySelector('main');

const errorTemplate = (state: AppState) => () =>
    html`
<div class="error">
  ${state.error}
  <button class="button" @click=${() => updateState({...state, error: null})}>Close</button>
</div>`;

const bodyTemplate = (state: AppState) => {
    if (state.me === null) {
        return loginFormTempalte(state);
    } else if (state.current) {
        return html`${loggedTemplate(state)}
                    <section>${currentTemplate(state)}</section>`;
    } else {
        return html`${loggedTemplate(state)}
                    <section>${roomsTemplate(state)}</section>`;
    }
};

const mainTemplate = (state: AppState) =>
    html`
${when(state.error,
        errorTemplate(state),
        () => bodyTemplate(state))}`;

export const updateState = (state: AppState) => {
    // console.debug('updateState', state);
    api.state = state;
    render(mainTemplate(state), main);
};
