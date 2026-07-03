// Треки на карте оператора: соединяет отметки одного объекта (тот же track_id,
// присвоенный клиентом — см. Android utils/TrackManager.kt) пунктирной линией.
//
// Каждая точка уже получает свой полноразмерный маркер + стрелку направления через
// handleNewPoint() (см. index.html, промты №2-5) — эта логика НЕ дублируется здесь,
// иначе на карте было бы по два маркера на одну точку. TrackManager отвечает только
// за соединяющую линию трека и (при первичной загрузке истории с сервера) за
// маркер последней точки трека, которую иначе никто бы не нарисовал.
class TrackManager {
  constructor(map) {
    this.map = map;
    this.tracks = new Map(); // trackId -> { points: [[lat,lon],...], polyline, color, objectType, maxThreat, lastMarker }
  }

  // Вызывать при каждой новой точке (живой WS-поток) — достраивает линию трека.
  addPoint(data) {
    const trackId = data.track_id;
    if (!trackId) return;

    const color = threatMeta(data.threat_level).color;

    if (!this.tracks.has(trackId)) {
      this.tracks.set(trackId, {
        points: [],
        polyline: null,
        color,
        objectType: data.object_type,
        maxThreat: data.threat_level,
        lastMarker: null
      });
    }

    const track = this.tracks.get(trackId);
    track.points.push([data.lat, data.lon]);
    track.color = color; // цвет линии = уровень угрозы САМОЙ ПОСЛЕДНЕЙ точки

    if (track.polyline) {
      track.polyline.setLatLngs(track.points);
      track.polyline.setStyle({ color });
    } else if (track.points.length >= 2) {
      track.polyline = L.polyline(track.points, {
        color,
        weight: 2,
        opacity: 0.7,
        dashArray: '6, 4'
      }).addTo(this.map);
    }

    const priority = { THREAT: 3, ATTENTION: 2, OBSERVATION: 1 };
    if ((priority[data.threat_level] || 0) > (priority[track.maxThreat] || 0)) {
      track.maxThreat = data.threat_level;
    }
  }

  // Загрузить треки с сервера при открытии панели — рисует линии трека и, для точек,
  // которые не проходили через живой WS (значит без своего маркера), ставит маркер
  // последней точки трека, чтобы трек не был "невидимой" линией без опознавательного знака.
  async loadFromServer(hours = 12) {
    try {
      const res = await fetch(`/api/tracks/?hours=${hours}`);
      if (!res.ok) return;
      const tracks = await res.json();

      for (const track of tracks) {
        const pointsRes = await fetch(`/api/tracks/${encodeURIComponent(track.track_id)}/points`);
        if (!pointsRes.ok) continue;
        const points = await pointsRes.json();
        if (points.length === 0) continue;

        points.forEach((p) => this.addPoint(p));

        // Полноразмерный маркер на последней точке трека (только для истории —
        // живые точки уже получили маркер через handleNewPoint).
        const last = points[points.length - 1];
        const entry = this.tracks.get(last.track_id);
        if (entry && !entry.lastMarker) {
          entry.lastMarker = placeObjectMarker(last).addTo(this.map);
        }
      }
    } catch (e) {
      console.error('Ошибка загрузки треков:', e);
    }
  }

  // Очистить все треки (вызывается вместе с "Очистить карту").
  clearAll() {
    this.tracks.forEach((track) => {
      if (track.polyline) this.map.removeLayer(track.polyline);
      if (track.lastMarker) this.map.removeLayer(track.lastMarker);
    });
    this.tracks.clear();
  }
}
