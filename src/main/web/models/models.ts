import {AppState} from './state';

export interface Hand {
    cards: Card[];
    score: number;
}

export interface Card {
    code: string;
    image: string;
}

export interface Room {
    id: number;
    name: string;
    players: PlayerWithStatus[];
    bank: Hand;
    full: boolean;
}

export interface Player {
    id: string;
    name: string;
    score: number;
}

export interface PlayerWithStatus {
    player: Player;
    status: PlayerStatus;
}

export interface PlayerStatus {
    hand: Hand;
    move: Move;
    canDo: Action[];
}

export type Action = 'draw' | 'stay';
export const actionLabel = (action: Action) => {
    switch (action) {
        case 'draw':
            return 'Draw';
        case 'stay':
            return 'Stay';
        default:
            return '???';
    }
};


export type Move = 'wait' |
    'in-game' |
    'burst' |
    'draw' |
    'stay' |
    'timeout' ;

export type RoomEventListener = (event: RoomEvent, state: AppState) => void;

export interface RoomEvent {
    type: string;
    round?: number;
    room?: Room;
    roomId?: Room;
    winners?: Player[]
    action?: PlayerStatus
}
