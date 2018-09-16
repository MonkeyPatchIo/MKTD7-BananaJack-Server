import {Player, Room, RoomEvent} from './models';

export interface AppState {
    error: string | null;
    me: Player | null,
    rooms: Room[];
    current: Room | null;
    lastEvent: RoomEvent | null;
}

export const initialState: AppState = {
    error: null,
    me: null,
    rooms: [],
    current: null,
    lastEvent: null
};

export type RoomEventListener = (event: RoomEvent, state: AppState) => void;