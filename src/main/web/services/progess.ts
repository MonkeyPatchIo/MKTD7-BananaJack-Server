const progress = document.querySelector<HTMLProgressElement>('body > header progress');
let timerId;

export const startProgress = (time: number = 59) => {
    clearInterval(timerId);
    progress.value = 0;
    progress.max = time;
    timerId = setInterval(() => progress.value += 1, 500);
};

export const stopProgress = () => {
    clearInterval(timerId);
    progress.value = 0;
};