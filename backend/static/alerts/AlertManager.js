// Визуальные и звуковые оповещения панели оператора по уровню угрозы.
// Опирается на OBJECT_META / DIRECTION_META / THREAT_META и formatClock(),
// определённые в основном скрипте index.html (общая глобальная область
// видимости classic-скриптов — порядок подключения см. в index.html).

class AlertManager {
  constructor() {
    this.audioManager = audioManager;
    this.notifPermission = false;
    this.originalTitle = document.title;
    this.titleInterval = null;
    this._requestNotifPermission();

    // Если оператор вернулся на вкладку — мигание заголовка можно остановить
    // сразу, не дожидаясь автоматического таймаута.
    window.addEventListener('focus', () => this._stopTitleBlink());
  }

  async _requestNotifPermission() {
    if ('Notification' in window) {
      try {
        const perm = await Notification.requestPermission();
        this.notifPermission = perm === 'granted';
      } catch (e) {
        this.notifPermission = false;
      }
    }
  }

  // Главный метод — вызывать при каждой новой точке.
  handleNewPoint(data) {
    this.audioManager.playByThreatLevel(data.threat_level);

    switch (data.threat_level) {
      case 'THREAT':
        this._flashScreen('#F44336', 3);
        this._blinkTitle('⚠ УГРОЗА');
        this._showToast(data, 'threat');
        this._browserNotification(data);
        this._focusWindow();
        break;

      case 'ATTENTION':
        this._flashScreen('#FF9800', 1);
        this._showToast(data, 'attention');
        this._browserNotification(data);
        break;

      default:
        this._showToast(data, 'observation');
        break;
    }
  }

  _flashScreen(color, times) {
    const overlay = document.getElementById('flash-overlay');
    if (!overlay) return;
    overlay.style.background = color;
    let count = 0;
    const flash = () => {
      if (count >= times * 2) {
        overlay.style.opacity = '0';
        return;
      }
      overlay.style.opacity = count % 2 === 0 ? '0.45' : '0';
      count++;
      setTimeout(flash, 300);
    };
    flash();
  }

  _blinkTitle(alertText) {
    this._stopTitleBlink();
    let blink = false;
    this.titleInterval = setInterval(() => {
      document.title = blink ? alertText : this.originalTitle;
      blink = !blink;
    }, 700);
    setTimeout(() => this._stopTitleBlink(), 30000);
  }

  _stopTitleBlink() {
    if (this.titleInterval) {
      clearInterval(this.titleInterval);
      this.titleInterval = null;
    }
    document.title = this.originalTitle;
  }

  _showToast(data, level) {
    const objMeta = objectMeta(data.object_type);
    const dirMeta = directionMeta(data.direction);

    const labels = {
      threat: '🔴 УГРОЗА',
      attention: '🟡 ВНИМАНИЕ',
      observation: '🟢 НАБЛЮДЕНИЕ'
    };
    const colors = {
      threat: '#B71C1C',
      attention: '#E65100',
      observation: '#1B5E20'
    };

    const toast = document.createElement('div');
    toast.className = `alert-toast alert-${level}`;
    toast.style.background = colors[level];
    toast.innerHTML = `
      <div class="toast-header">
        ${labels[level]}
        <span class="toast-close">✕</span>
      </div>
      <div class="toast-body">
        <b>${objMeta.icon} ${data.object_type}</b> | ${dirMeta.arrow} ${dirMeta.label}
        <br>
        <small>👤 ${data.user} | 🕐 ${formatClock(data.timestamp, true)}</small>
      </div>
    `;
    toast.querySelector('.toast-close').addEventListener('click', () => toast.remove());

    const timeout = level === 'threat' ? 15000 : level === 'attention' ? 8000 : 4000;
    setTimeout(() => toast.remove(), timeout);

    document.getElementById('toast-container').appendChild(toast);
  }

  // Уведомление браузера — работает, даже когда вкладка свёрнута/в фоне.
  _browserNotification(data) {
    if (!this.notifPermission) return;
    const objMeta = objectMeta(data.object_type);
    const levels = {
      THREAT: '🔴 УГРОЗА',
      ATTENTION: '🟡 ВНИМАНИЕ'
    };
    if (!levels[data.threat_level]) return;

    new Notification(`${levels[data.threat_level]} — Новая точка`, {
      body: `${objMeta.icon} ${data.object_type} | от ${data.user}`,
      requireInteraction: data.threat_level === 'THREAT'
    });
  }

  _focusWindow() {
    window.focus();
    if (document.hidden) {
      window.blur();
      window.focus();
    }
  }

  // Остановить все активные оповещения (кнопка "СБРОС").
  clearAll() {
    this._stopTitleBlink();
    document.querySelectorAll('.alert-toast').forEach((t) => t.remove());
    const overlay = document.getElementById('flash-overlay');
    if (overlay) overlay.style.opacity = '0';
  }
}

const alertManager = new AlertManager();
