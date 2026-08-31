import { useState, useEffect, useCallback } from 'react'
import { getNumbers, getCacheStats, clearCache } from './api.js'

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

// keep components in App.jsx for now — split out in the next commit
function AddNumberForm({ onAdd, style }) {
  const [value, setValue] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!value) return
    setLoading(true)
    try {
      const { addNumber } = await import('./api.js')
      await addNumber(value)
      setValue('')
      onAdd()
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={style}>
      <h2 style={{ fontSize: 15, fontWeight: 600, marginBottom: 16 }}>Add a number</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8 }}>
        <input
          type="number"
          value={value}
          onChange={e => setValue(e.target.value)}
          placeholder="Enter integer"
          style={{ flex: 1, padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: 6, fontSize: 14 }}
        />
        <button
          type="submit"
          disabled={loading}
          style={{ padding: '8px 16px', background: '#4f46e5', color: '#fff', border: 'none', borderRadius: 6, cursor: 'pointer', fontSize: 14 }}
        >
          {loading ? '...' : 'Post'}
        </button>
      </form>
    </div>
  )
}

function NumbersTable({ numbers, cacheStatus, onFetch, style }) {
  const badgeColor = cacheStatus === 'HIT' ? '#16a34a' : cacheStatus === 'MISS' ? '#dc2626' : '#64748b'

  return (
    <div style={style}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ fontSize: 15, fontWeight: 600, margin: 0 }}>Numbers ({numbers.length})</h2>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          {cacheStatus && (
            <span style={{ background: badgeColor, color: '#fff', fontSize: 11, fontWeight: 700, padding: '3px 8px', borderRadius: 4 }}>
              {cacheStatus}
            </span>
          )}
          <button
            onClick={onFetch}
            style={{ padding: '5px 12px', border: '1px solid #cbd5e1', borderRadius: 6, cursor: 'pointer', fontSize: 13 }}
          >
            Fetch
          </button>
        </div>
      </div>
      {numbers.length === 0
        ? <p style={{ color: '#94a3b8', fontSize: 13 }}>No numbers yet. Post one above.</p>
        : (
          <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: '#f8fafc' }}>
                {['Value', 'Created at', 'ID'].map(h => (
                  <th key={h} style={{ textAlign: 'left', padding: '6px 8px', fontWeight: 600, borderBottom: '1px solid #e2e8f0' }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {numbers.map(n => (
                <tr key={n.id}>
                  <td style={{ padding: '6px 8px', fontWeight: 600 }}>{n.value}</td>
                  <td style={{ padding: '6px 8px', color: '#64748b' }}>{new Date(n.createdAt).toLocaleString()}</td>
                  <td style={{ padding: '6px 8px', color: '#94a3b8', fontSize: 11 }}>{n.id.slice(0, 8)}…</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
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
      {/* progress bar showing hit rate */}
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
