import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { chatApi, faqApi, ticketApi } from '../services/api'
import ChatBox from '../components/ChatBox'
import FAQList from '../components/FAQList'
import TicketForm from '../components/TicketForm'
import MyTickets from '../components/MyTickets'

export default function Dashboard() {
  const [user, setUser] = useState(null)
  const [activeTab, setActiveTab] = useState('chat')
  const nav = useNavigate()

  useEffect(() => {
    const u = JSON.parse(localStorage.getItem('user') || '{}')
    if (!u.id) { nav('/login'); return }
    setUser(u)
  }, [nav])

  const logout = () => { localStorage.removeItem('user'); nav('/login') }

  if (!user) return null

  return (
    <div className="dashboard">
      <header>
        <h1>Customer Support Portal</h1>
        <div className="user-bar">
          <span>{user.name}</span>
          <button onClick={logout}>Logout</button>
        </div>
      </header>
      <div className="main">
        <div className="chat-panel">
          <ChatBox />
        </div>
        <div className="side-panel">
          <div className="side-tabs">
            <button className={activeTab === 'faq' ? 'active' : ''} onClick={() => setActiveTab('faq')}>FAQs</button>
            <button className={activeTab === 'ticket' ? 'active' : ''} onClick={() => setActiveTab('ticket')}>New Ticket</button>
            <button className={activeTab === 'my' ? 'active' : ''} onClick={() => setActiveTab('my')}>My Tickets</button>
          </div>
          {activeTab === 'faq' && <FAQList />}
          {activeTab === 'ticket' && <TicketForm user={user} />}
          {activeTab === 'my' && <MyTickets userId={user.id} />}
        </div>
      </div>
    </div>
  )
}
