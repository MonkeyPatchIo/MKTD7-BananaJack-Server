import {initialState} from './models/state';
import {updateState} from './templates/app';

declare global {
    const process: any
}

console.warn(process.env.BACKEND_URL);

// Bootstrap
updateState(initialState);
