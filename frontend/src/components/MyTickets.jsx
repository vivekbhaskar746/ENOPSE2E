import { useState, useEffect } from 'react'
import { ticketApi } from '../services/api'

export default function MyTickets({ userId }) {
  const [tickets, setTickets] = useState([])

  useEffect(() => {
    ticketApi.getByUser(userId).then(({ data }) => setTickets(data)).catch(console.error)
  }, [userId])

  return (
    <div className="my-tickets">
      <h3>My Tickets</h3>
      {tickets.map(t => (
        <div key={t.id} className={`ticket-item status-${t.status.toLowerCase()}`}>
          <div className="ticket-head"><span>#{t.id}</span><span className="badge">{t.status}</span></div>
          <strong>{t.subject}</strong>
          <p>Priority: {t.priority}</p>
          {t.solution && <p className="solution">Solution: {t.solution}</p>}
        </div>
      ))}
      {tickets.length === 0 && <p>No tickets yet.</p>}
    </div>
  )
}
