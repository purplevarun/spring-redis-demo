export default function NumbersTable({ numbers, cacheStatus, onFetch, style }) {
  const badgeColor =
    cacheStatus === 'HIT' ? '#16a34a' :
    cacheStatus === 'MISS' ? '#dc2626' : '#64748b'

  return (
    <div style={style}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ fontSize: 15, fontWeight: 600, margin: 0 }}>
          Numbers ({numbers.length})
        </h2>
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
                  <th key={h} style={{ textAlign: 'left', padding: '6px 8px', fontWeight: 600, borderBottom: '1px solid #e2e8f0' }}>
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {numbers.map(n => (
                <tr key={n.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                  <td style={{ padding: '6px 8px', fontWeight: 600 }}>{n.value}</td>
                  <td style={{ padding: '6px 8px', color: '#64748b' }}>
                    {new Date(n.createdAt).toLocaleString()}
                  </td>
                  <td style={{ padding: '6px 8px', color: '#94a3b8', fontSize: 11 }}>
                    {n.id.slice(0, 8)}…
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
    </div>
  )
}
