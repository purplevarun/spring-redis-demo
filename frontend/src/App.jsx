import { useCallback, useEffect, useState } from 'react'
import { clearCache, getCacheStats, getNumberById, getNumbers } from './api.js'
import AddNumberForm from './components/AddNumberForm.jsx'
import CacheStatsPanel from './components/CacheStatsPanel.jsx'
import NumbersTable from './components/NumbersTable.jsx'

const styles = {
	app: { fontFamily: 'system-ui, sans-serif', maxWidth: 860, margin: '0 auto', padding: '32px 24px', color: '#111' },
	h1: { fontSize: 22, fontWeight: 700, marginBottom: 28, color: '#1a1a2e' },
	row: { display: 'flex', gap: 24, flexWrap: 'wrap' },
	card: { background: '#fff', border: '1px solid #e2e8f0', borderRadius: 10, padding: 20, flex: 1, minWidth: 280 }
}

export default function App() {
	const [numbers, setNumbers] = useState([])
	// per-row cache status: { [id]: 'HIT'|'MISS' }
	const [rowStatuses, setRowStatuses] = useState({})
	const [stats, setStats] = useState({ hits: 0, misses: 0, evictions: 0, hitRate: 0 })
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
			const { data } = await getNumbers()
			setNumbers(data)
			setRowStatuses({}) // reset per-row badges when list refreshes
			await fetchStats()
		} catch (e) {
			setError(e.message)
		}
	}, [fetchStats])

	const handleLookup = async (id) => {
		try {
			const { cacheStatus } = await getNumberById(id)
			setRowStatuses(prev => ({ ...prev, [id]: cacheStatus }))
			await fetchStats()
		} catch (e) {
			setError(e.message)
		}
	}

	const handleClear = async () => {
		try {
			await clearCache()
			setRowStatuses({})
			await fetchStats()
		} catch (e) {
			setError(e.message)
		}
	}

	useEffect(() => {
		// only poll stats on mount; numbers are fetched on explicit button click
		fetchStats()
		const interval = setInterval(fetchStats, 3000)
		return () => clearInterval(interval)
	}, [fetchStats])

	return (
		<div style={styles.app}>
			<h1 style={styles.h1}>Spring Redis Cache Demo</h1>
			{error && <p style={{ color: '#c0392b', marginBottom: 16 }}>{error}</p>}
			<div style={styles.row}>
				<AddNumberForm onAdd={fetchNumbers} style={styles.card} />
				<NumbersTable numbers={numbers} rowStatuses={rowStatuses} onFetch={fetchNumbers} onLookup={handleLookup} style={styles.card} />
				<CacheStatsPanel stats={stats} onClear={handleClear} style={styles.card} />
			</div>
		</div>
	)
}
