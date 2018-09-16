import {initialState} from './models/state';
import {updateState} from './templates/app';

declare global {
    const process: any
}

// Bootstrap
updateState(initialState);
