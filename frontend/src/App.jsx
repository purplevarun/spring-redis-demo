import { useState, useEffect, useCallback } from 'react'
import { getNumbers, getCacheStats, clearCache } from './api.js'
import AddNumberForm from './components/AddNumberForm.jsx'
import NumbersTable from './components/NumbersTable.jsx'

const styles = {
  app: { fontFamily: 'system-ui, sans-serif', maxWidth: 860, margin: '0 auto', padding: '32px 24px', color: '#111' },
  h1: { fontSize: 22, fontWeight: 700, marginBottom: 28, color: '#1a1a2e' },
  row: { display: 'flex', gap: 24, flexWrap: 'wrap' },
  card: { background: '#fff', border: '1px solid #e2e8f0', borderRadius: 10, padding: 20, flex: 1, minWidth: 280 }
}

export default function App() {
  const [numbers, setNumbers] = useState([])
  const [cacheStatus, setCacheStatus] = useState(null)
  const [stats, setStats] = useState({ hits: 0, misses: 0, hitRate: 0 })
  const [error, setError] = useState(null)

  const fetchStats = useCallback(async () => {
    try {
      setStats(await getCacheStats())
    } catch {
      // stats panel shows stale data on error; non-critical
    }
  }, [])

  const fetchNumbers = useCallback(async () => {
    try {
      setError(null)
      const { data, cacheStatus: cs } = await getNumbers()
      setNumbers(data)
      setCacheStatus(cs)
      await fetchStats()
    } catch (e) {
      setError(e.message)
    }
  }, [fetchStats])

  const handleClear = async () => {
    try {
      await clearCache()
      setCacheStatus(null)
      await fetchStats()
    } catch (e) {
      setError(e.message)
    }
  }

  useEffect(() => {
    fetchNumbers()
    const interval = setInterval(fetchStats, 3000)
    return () => clearInterval(interval)
  }, [fetchNumbers, fetchStats])

  return (
    <div style={styles.app}>
      <h1 style={styles.h1}>Spring Redis Cache Demo</h1>
      {error && <p style={{ color: '#c0392b', marginBottom: 16 }}>{error}</p>}
      <div style={styles.row}>
        <AddNumberForm onAdd={fetchNumbers} style={styles.card} />
        <NumbersTable numbers={numbers} cacheStatus={cacheStatus} onFetch={fetchNumbers} style={styles.card} />
        <CacheStatsPanel stats={stats} onClear={handleClear} style={styles.card} />
      </div>
    </div>
  )
}

function CacheStatsPanel({ stats, onClear, style }) {
  const pct = (stats.hitRate * 100).toFixed(1)
  const barColor = stats.hitRate > 0.6 ? '#16a34a' : stats.hitRate > 0.3 ? '#d97706' : '#dc2626'

  return (
    <div style={style}>
      <h2 style={{ fontSize: 15, fontWeight: 600, marginBottom: 16 }}>Cache stats</h2>
      <div style={{ display: 'flex', gap: 16, marginBottom: 16 }}>
        <Stat label="Hits" value={stats.hits} color="#16a34a" />
        <Stat label="Misses" value={stats.misses} color="#dc2626" />
        <Stat label="Hit rate" value={`${pct}%`} color={barColor} />
      </div>
      <div style={{ background: '#f1f5f9', borderRadius: 4, height: 8, marginBottom: 20, overflow: 'hidden' }}>
        <div style={{ width: `${pct}%`, height: '100%', background: barColor, transition: 'width 0.4s ease' }} />
      </div>
      <p style={{ fontSize: 11, color: '#94a3b8', marginBottom: 12 }}>Auto-refreshes every 3 seconds</p>
      <button
        onClick={onClear}
        style={{ padding: '7px 14px', border: '1px solid #fca5a5', color: '#dc2626', background: '#fff', borderRadius: 6, cursor: 'pointer', fontSize: 13 }}
      >
        Clear cache
      </button>
    </div>
  )
}

function Stat({ label, value, color }) {
  return (
    <div style={{ flex: 1, textAlign: 'center' }}>
      <div style={{ fontSize: 22, fontWeight: 700, color }}>{value}</div>
      <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 2 }}>{label}</div>
    </div>
  )
}
