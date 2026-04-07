import { useState, useEffect } from 'react'
import { faqApi } from '../services/api'

export default function FAQList() {
  const [faqs, setFaqs] = useState([])
  const [open, setOpen] = useState(null)

  useEffect(() => {
    faqApi.getAll().then(({ data }) => setFaqs(data)).catch(console.error)
  }, [])

  return (
    <div className="faq-list">
      <h3>Frequently Asked Questions</h3>
      {faqs.map(f => (
        <div key={f.id} className="faq-item">
          <div className="faq-q" onClick={() => setOpen(open === f.id ? null : f.id)}>{f.question}</div>
          {open === f.id && <div className="faq-a">{f.answer}</div>}
        </div>
      ))}
      {faqs.length === 0 && <p>No FAQs available.</p>}
    </div>
  )
}
