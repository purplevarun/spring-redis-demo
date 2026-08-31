export default function CacheStatsPanel({ stats, onClear, style }) {
	const pct = (stats.hitRate * 100).toFixed(1)
	// colour shifts green → amber → red as hit rate drops
	const barColor =
		stats.hitRate > 0.6 ? '#16a34a' :
			stats.hitRate > 0.3 ? '#d97706' : '#dc2626'

	return (
		<div style={style}>
			<h2 style={{ fontSize: 15, fontWeight: 600, marginBottom: 16 }}>Cache stats</h2>

			<div style={{ display: 'flex', gap: 16, marginBottom: 16 }}>
				<Stat label="Hits" value={stats.hits} color="#16a34a" />
				<Stat label="Misses" value={stats.misses} color="#dc2626" />
				<Stat label="Evictions" value={stats.evictions ?? 0} color="#d97706" />
				<Stat label="Hit rate" value={`${pct}%`} color={barColor} />
			</div>

			{/* animated progress bar — width equals hit rate percentage */}
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
