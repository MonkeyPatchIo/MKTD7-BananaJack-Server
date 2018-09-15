import {initialState} from './models/state';
import {updateState} from './templates/app';

declare global {
    interface Window {
        debug: Element;
    }
}

// Bootstrap
updateState(initialState);
