import { useState, useRef, useEffect } from 'react'
import { chatApi } from '../services/api'

const sessionId = 'session_' + Math.random().toString(36).slice(2) + '_' + Date.now()

export default function ChatBox() {
  const [messages, setMessages] = useState([{ text: "Hello! I'm your virtual assistant. How can I help you today?", sender: 'bot' }])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const bottom = useRef(null)

  useEffect(() => { bottom.current?.scrollIntoView({ behavior: 'smooth' }) }, [messages])

  const send = async () => {
    if (!input.trim() || loading) return
    const msg = input.trim()
    setInput('')
    setMessages(prev => [...prev, { text: msg, sender: 'user' }])
    setLoading(true)
    try {
      const { data } = await chatApi.send(sessionId, msg)
      setMessages(prev => [...prev, { text: data.response, sender: 'bot' }])
    } catch {
      setMessages(prev => [...prev, { text: 'Sorry, something went wrong. Please try again.', sender: 'bot' }])
    }
    setLoading(false)
  }

  return (
    <div className="chatbox">
      <div className="chat-header"><h3>Chat Support</h3><span className="online">Online</span></div>
      <div className="chat-messages">
        {messages.map((m, i) => (
          <div key={i} className={`msg ${m.sender}`}>
            <div className="bubble">{m.text}</div>
          </div>
        ))}
        {loading && <div className="msg bot"><div className="bubble typing">Typing...</div></div>}
        <div ref={bottom} />
      </div>
      <div className="chat-input">
        <input value={input} onChange={e => setInput(e.target.value)} onKeyDown={e => e.key === 'Enter' && send()} placeholder="Type your message..." />
        <button onClick={send} disabled={loading}>Send</button>
      </div>
    </div>
  )
}
