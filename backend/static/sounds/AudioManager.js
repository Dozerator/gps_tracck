// Звуковые оповещения панели оператора.
//
// Реальные mp3-файлы (alert_threat.mp3 и т.п.) не используются — все сигналы
// синтезируются программно через Web Audio API прямо в браузере. Это избавляет
// от необходимости поставлять и хранить бинарные ассеты в репозитории и звук
// работает сразу после клонирования, без дополнительной подготовки.

class AudioManager {
  constructor() {
    this.ctx = null;
    this.enabled = this._loadEnabled();
    this.volume = this._loadVolume();
    this._initOnUserGesture();
  }

  // Браузер требует жест пользователя, прежде чем разрешит воспроизведение звука.
  _initOnUserGesture() {
    const init = () => {
      if (!this.ctx) {
        this.ctx = new (window.AudioContext || window.webkitAudioContext)();
      }
      document.removeEventListener('click', init);
    };
    document.addEventListener('click', init);
  }

  // Генерация тона программно — mp3-файлы не нужны.
  _playTone(frequency, duration, type = 'sine', gain = 0.8, delay = 0) {
    if (!this.ctx || !this.enabled) return;

    const oscillator = this.ctx.createOscillator();
    const gainNode = this.ctx.createGain();

    oscillator.connect(gainNode);
    gainNode.connect(this.ctx.destination);

    oscillator.type = type;
    oscillator.frequency.setValueAtTime(frequency, this.ctx.currentTime + delay);

    gainNode.gain.setValueAtTime(0, this.ctx.currentTime + delay);
    gainNode.gain.linearRampToValueAtTime(gain * this.volume, this.ctx.currentTime + delay + 0.01);
    gainNode.gain.linearRampToValueAtTime(0, this.ctx.currentTime + delay + duration);

    oscillator.start(this.ctx.currentTime + delay);
    oscillator.stop(this.ctx.currentTime + delay + duration);
  }

  // УГРОЗА — резкий прерывистый сигнал (как тревога).
  playThreat() {
    if (!this.enabled) return;
    [0, 0.2, 0.4, 0.6].forEach((delay) => {
      this._playTone(1200, 0.15, 'square', 0.9, delay);
    });
    this._playTone(300, 0.8, 'sawtooth', 0.3, 0);
  }

  // ВНИМАНИЕ — двойной средний сигнал.
  playAttention() {
    if (!this.enabled) return;
    this._playTone(800, 0.3, 'sine', 0.7, 0);
    this._playTone(800, 0.3, 'sine', 0.7, 0.4);
  }

  // НАБЛЮДЕНИЕ — тихий короткий сигнал.
  playObservation() {
    if (!this.enabled) return;
    this._playTone(500, 0.2, 'sine', 0.4, 0);
  }

  playByThreatLevel(threatLevel) {
    switch (threatLevel) {
      case 'THREAT': this.playThreat(); break;
      case 'ATTENTION': this.playAttention(); break;
      case 'OBSERVATION': this.playObservation(); break;
    }
  }

  setVolume(v) {
    this.volume = Math.max(0, Math.min(1, v));
    localStorage.setItem('operator.audio.volume', String(this.volume));
  }

  toggle() {
    this.enabled = !this.enabled;
    localStorage.setItem('operator.audio.enabled', String(this.enabled));
    return this.enabled;
  }

  _loadEnabled() {
    const stored = localStorage.getItem('operator.audio.enabled');
    return stored === null ? true : stored === 'true';
  }

  _loadVolume() {
    const stored = localStorage.getItem('operator.audio.volume');
    const parsed = stored === null ? 0.8 : parseFloat(stored);
    return Number.isFinite(parsed) ? Math.max(0, Math.min(1, parsed)) : 0.8;
  }
}

const audioManager = new AudioManager();
