// Экспорт данных смены (GeoJSON/KMZ/CSV) — панель в нижнем левом углу.
// Скачивание идёт напрямую по прямой ссылке на backend-эндпоинт: сервер уже
// отдаёт Content-Disposition: attachment, поэтому клика по <a href> достаточно —
// не нужен fetch+Blob и не возникает редиректов.
class ExportManager {
  constructor() {
    this.panel = document.getElementById('export-panel');
    this.btn = document.getElementById('export-btn');
    this.hoursSelect = document.getElementById('export-hours');
    this.threatSelect = document.getElementById('export-threat');
    this.statsPreview = document.getElementById('export-stats-preview');

    this.btn.addEventListener('click', () => this.toggle());
    this.hoursSelect.addEventListener('change', () => this.loadStats());

    document.getElementById('export-geojson-btn').addEventListener('click', () => this.exportData('geojson'));
    document.getElementById('export-kmz-btn').addEventListener('click', () => this.exportData('kmz'));
    document.getElementById('export-csv-btn').addEventListener('click', () => this.exportData('csv'));
  }

  toggle() {
    const opening = !this.panel.classList.contains('open');
    this.panel.classList.toggle('open');
    if (opening) this.loadStats();
  }

  async loadStats() {
    const hours = this.hoursSelect.value;
    this.statsPreview.textContent = 'Загрузка статистики…';
    try {
      const res = await fetch(`/api/export/stats?hours=${hours}`);
      if (!res.ok) throw new Error('stats request failed');
      const s = await res.json();
      this.statsPreview.innerHTML = `
        <div class="stat-line"><span>Всего точек</span><span>${s.total}</span></div>
        <div class="stat-line"><span>🔴 Угроза</span><span>${s.threats}</span></div>
        <div class="stat-line"><span>🟡 Внимание</span><span>${s.attention}</span></div>
        <div class="stat-line"><span>🟢 Наблюдение</span><span>${s.observation}</span></div>
        <div class="stat-line"><span>Треков</span><span>${s.total_tracks}</span></div>
        <div class="stat-line"><span>БПЛА / Квадрик</span><span>${s.uav_count} / ${s.quad_count}</span></div>
      `;
    } catch (e) {
      this.statsPreview.textContent = 'Не удалось загрузить статистику';
    }
  }

  exportData(format) {
    const hours = this.hoursSelect.value;
    const threat = this.threatSelect.value;
    let url = `/api/export/${format}?hours=${encodeURIComponent(hours)}`;
    if (threat) url += `&threat_level=${encodeURIComponent(threat)}`;

    const link = document.createElement('a');
    link.href = url;
    document.body.appendChild(link);
    link.click();
    link.remove();
  }
}

const exportManager = new ExportManager();
