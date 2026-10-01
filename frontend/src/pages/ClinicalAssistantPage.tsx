import React, { useEffect, useState, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { chatApi } from '../api/chatApi';
import { ConversationSummary, ConversationDetail, ChatMessage } from '../types';
import {
  MessageSquare,
  Send,
  PlusCircle,
  Trash2,
  Sparkles,
  ShieldCheck,
  BookOpen,
  HelpCircle,
  AlertCircle,
  FileText,
  User,
  Bot
} from 'lucide-react';

export const ClinicalAssistantPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const linkedReportId = searchParams.get('reportId') || undefined;

  const [conversations, setConversations] = useState<ConversationSummary[]>([]);
  const [activeConversation, setActiveConversation] = useState<ConversationDetail | null>(null);
  const [loadingList, setLoadingList] = useState(false);
  const [loadingActive, setLoadingActive] = useState(false);
  const [sending, setSending] = useState(false);
  const [inputMessage, setInputMessage] = useState('');
  const [error, setError] = useState<string | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [activeConversation?.messages, sending]);

  // Load conversations on mount
  useEffect(() => {
    loadConversations();
  }, []);

  const loadConversations = async () => {
    setLoadingList(true);
    try {
      const list = await chatApi.getConversations();
      setConversations(list);

      // If user came with a linked reportId or there are existing conversations
      if (list.length > 0 && !activeConversation) {
        // If query param reportId is present, check if there's already a conversation for it
        if (linkedReportId) {
          const match = list.find((c) => c.reportId === linkedReportId);
          if (match) {
            selectConversation(match.id);
            return;
          }
          // Otherwise auto-create one for this report
          startNewConversation(linkedReportId);
          return;
        }
        selectConversation(list[0].id);
      } else if (list.length === 0 && linkedReportId) {
        startNewConversation(linkedReportId);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load conversations.');
    } finally {
      setLoadingList(false);
    }
  };

  const selectConversation = async (id: string) => {
    setLoadingActive(true);
    setError(null);
    try {
      const detail = await chatApi.getConversation(id);
      setActiveConversation(detail);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load conversation details.');
    } finally {
      setLoadingActive(false);
    }
  };

  const startNewConversation = async (reportId?: string) => {
    setSending(true);
    setError(null);
    try {
      const detail = await chatApi.createConversation({
        reportId: reportId || undefined,
        title: reportId ? 'Report Laboratory Consultation' : 'General Clinical Consultation',
      });
      setActiveConversation(detail);
      // Refresh list
      const list = await chatApi.getConversations();
      setConversations(list);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create new conversation.');
    } finally {
      setSending(false);
    }
  };

  const handleDeleteConversation = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!confirm('Are you sure you want to delete this clinical conversation?')) return;
    try {
      await chatApi.deleteConversation(id);
      setConversations((prev) => prev.filter((c) => c.id !== id));
      if (activeConversation?.id === id) {
        setActiveConversation(null);
      }
    } catch (err: any) {
      alert('Failed to delete conversation: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleSendMessage = async (textToSend?: string) => {
    const text = textToSend || inputMessage;
    if (!text.trim() || sending) return;

    let targetConv = activeConversation;

    setSending(true);
    setError(null);
    setInputMessage('');

    try {
      // If no active conversation exists, create one first
      if (!targetConv) {
        targetConv = await chatApi.createConversation({
          reportId: linkedReportId,
          title: text.length > 40 ? text.substring(0, 37) + '...' : text,
          initialMessage: text,
        });
        setActiveConversation(targetConv);
        const list = await chatApi.getConversations();
        setConversations(list);
        setSending(false);
        return;
      }

      // Optimistically push the user message to UI
      const optimisticUserMsg: ChatMessage = {
        id: 'temp-' + Date.now(),
        sender: 'USER',
        content: text,
        citedSources: [],
        suggestedQuestions: [],
        safetyPassed: true,
        createdAt: new Date().toISOString(),
      };

      setActiveConversation({
        ...targetConv,
        messages: [...targetConv.messages, optimisticUserMsg],
      });

      await chatApi.sendMessage(targetConv.id, text);

      // Reload full conversation to ensure accurate state
      const reloaded = await chatApi.getConversation(targetConv.id);
      setActiveConversation(reloaded);

      // Update sidebar list
      const list = await chatApi.getConversations();
      setConversations(list);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error communicating with clinical assistant.');
    } finally {
      setSending(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-140px)] bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
      {/* Top Clinical Safety Bar */}
      <div className="px-6 py-2.5 bg-slate-900 text-slate-200 text-xs flex items-center justify-between border-b border-slate-800">
        <div className="flex items-center space-x-2">
          <ShieldCheck className="w-4 h-4 text-emerald-400" />
          <span className="font-semibold text-white">MediLens Clinical Assistant</span>
          <span className="text-slate-400">|</span>
          <span className="text-slate-300">Evidence-Grounded RAG (ADA / KDIGO / AHA Guidelines)</span>
        </div>
        <span className="text-[11px] text-amber-300 bg-amber-950/60 border border-amber-800/40 px-2 py-0.5 rounded">
          Educational Only • Not a Diagnostic Prescription
        </span>
      </div>

      <div className="flex flex-1 overflow-hidden">
        {/* Left Sidebar: Conversations List */}
        <div className="w-80 border-r border-slate-200 flex flex-col bg-slate-50/50">
          <div className="p-3 border-b border-slate-200">
            <button
              onClick={() => startNewConversation(linkedReportId)}
              disabled={sending}
              className="w-full flex items-center justify-center space-x-2 py-2 px-3 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-sm transition disabled:opacity-50"
            >
              <PlusCircle className="w-4 h-4" />
              <span>New Consultation</span>
            </button>
          </div>

          <div className="flex-1 overflow-y-auto p-2 space-y-1">
            {loadingList ? (
              <div className="p-4 text-center text-xs text-slate-400">Loading conversations...</div>
            ) : conversations.length === 0 ? (
              <div className="p-6 text-center text-xs text-slate-400">
                <MessageSquare className="w-8 h-8 mx-auto mb-2 text-slate-300" />
                <p>No previous conversations.</p>
                <p className="mt-1 text-[11px]">Start a new consultation to ask about your laboratory values.</p>
              </div>
            ) : (
              conversations.map((conv) => {
                const isSelected = activeConversation?.id === conv.id;
                return (
                  <div
                    key={conv.id}
                    onClick={() => selectConversation(conv.id)}
                    className={`group relative flex items-start justify-between p-3 rounded-xl cursor-pointer text-xs transition ${
                      isSelected
                        ? 'bg-emerald-50 text-emerald-900 border border-emerald-200 font-medium'
                        : 'text-slate-700 hover:bg-slate-100 border border-transparent'
                    }`}
                  >
                    <div className="flex-1 min-w-0 pr-2">
                      <div className="flex items-center space-x-1.5">
                        <MessageSquare className={`w-3.5 h-3.5 flex-shrink-0 ${isSelected ? 'text-emerald-600' : 'text-slate-400'}`} />
                        <span className="truncate font-semibold">{conv.title}</span>
                      </div>
                      {conv.reportId && (
                        <div className="flex items-center space-x-1 text-[10px] text-emerald-700 mt-1">
                          <FileText className="w-3 h-3" />
                          <span>Linked Lab Report</span>
                        </div>
                      )}
                      <p className="text-[10px] text-slate-400 mt-1">
                        {new Date(conv.updatedAt).toLocaleDateString()} • {conv.messageCount} msg(s)
                      </p>
                    </div>

                    <button
                      onClick={(e) => handleDeleteConversation(conv.id, e)}
                      title="Delete consultation"
                      className="opacity-0 group-hover:opacity-100 p-1 text-slate-400 hover:text-rose-600 rounded transition"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                );
              })
            )}
          </div>
        </div>

        {/* Right Main Chat Container */}
        <div className="flex-1 flex flex-col bg-white">
          {error && (
            <div className="px-6 py-2 bg-rose-50 border-b border-rose-200 text-rose-700 text-xs flex items-center space-x-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Active Conversation Messages Stream */}
          <div className="flex-1 overflow-y-auto p-6 space-y-6">
            {loadingActive ? (
              <div className="h-full flex flex-col items-center justify-center space-y-2">
                <div className="w-8 h-8 border-3 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
                <p className="text-xs text-slate-400 font-medium">Loading conversation messages...</p>
              </div>
            ) : !activeConversation || activeConversation.messages.length === 0 ? (
              <div className="max-w-2xl mx-auto py-12 text-center space-y-6">
                <div className="w-14 h-14 bg-emerald-50 rounded-2xl flex items-center justify-center mx-auto text-emerald-600 border border-emerald-100">
                  <Sparkles className="w-7 h-7" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-slate-900">How can I assist with your lab results today?</h2>
                  <p className="text-xs text-slate-500 mt-1 max-w-md mx-auto">
                    Ask questions regarding reference intervals, what out-of-range markers indicate, or evidence-based lifestyle context grounded in peer-reviewed clinical guidelines.
                  </p>
                </div>

                {/* Suggested Prompts */}
                <div className="text-left space-y-2 max-w-lg mx-auto">
                  <p className="text-[11px] font-bold uppercase tracking-wider text-slate-400">Suggested Questions</p>
                  {(activeConversation?.suggestedPrompts || [
                    "What do my latest glucose and HbA1c results mean for my health?",
                    "Why is my serum creatinine or potassium flagged as abnormal?",
                    "What specific questions should I ask my doctor during my next visit?"
                  ]).map((prompt, idx) => (
                    <button
                      key={idx}
                      onClick={() => handleSendMessage(prompt)}
                      className="w-full text-left p-3 rounded-xl border border-slate-200 hover:border-emerald-300 hover:bg-emerald-50/50 text-xs text-slate-700 transition flex items-center justify-between group"
                    >
                      <span>{prompt}</span>
                      <Send className="w-3.5 h-3.5 text-slate-400 group-hover:text-emerald-600 transition" />
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              activeConversation.messages.map((msg) => {
                const isAssistant = msg.sender === 'ASSISTANT';
                return (
                  <div
                    key={msg.id}
                    className={`flex items-start space-x-3 ${isAssistant ? 'justify-start' : 'justify-end'}`}
                  >
                    {isAssistant && (
                      <div className="w-8 h-8 rounded-xl bg-emerald-600 flex items-center justify-center text-white flex-shrink-0 shadow-sm mt-0.5">
                        <Bot className="w-4 h-4" />
                      </div>
                    )}

                    <div
                      className={`max-w-2xl rounded-2xl p-4 text-xs leading-relaxed ${
                        isAssistant
                          ? 'bg-slate-50 border border-slate-200 text-slate-800'
                          : 'bg-emerald-600 text-white shadow-sm'
                      }`}
                    >
                      <div className="whitespace-pre-wrap font-sans">{msg.content}</div>

                      {/* Evidence Citations if available */}
                      {isAssistant && msg.citedSources && msg.citedSources.length > 0 && (
                        <div className="mt-3 pt-3 border-t border-slate-200">
                          <div className="flex items-center space-x-1.5 text-[11px] font-semibold text-slate-600 mb-1.5">
                            <BookOpen className="w-3.5 h-3.5 text-emerald-600" />
                            <span>Clinical Literature Citations</span>
                          </div>
                          <div className="flex flex-wrap gap-1.5">
                            {msg.citedSources.map((source, sIdx) => (
                              <span
                                key={sIdx}
                                className="px-2 py-0.5 bg-white border border-slate-200 text-slate-600 rounded text-[10px] font-mono"
                              >
                                {source}
                              </span>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Suggested Follow-up Questions */}
                      {isAssistant && msg.suggestedQuestions && msg.suggestedQuestions.length > 0 && (
                        <div className="mt-3 pt-3 border-t border-slate-200">
                          <div className="flex items-center space-x-1.5 text-[11px] font-semibold text-slate-600 mb-1.5">
                            <HelpCircle className="w-3.5 h-3.5 text-emerald-600" />
                            <span>Suggested Follow-up Questions</span>
                          </div>
                          <div className="space-y-1">
                            {msg.suggestedQuestions.map((q, qIdx) => (
                              <button
                                key={qIdx}
                                onClick={() => handleSendMessage(q)}
                                className="block w-full text-left text-[11px] text-emerald-700 hover:text-emerald-900 hover:underline py-0.5"
                              >
                                • {q}
                              </button>
                            ))}
                          </div>
                        </div>
                      )}

                      <div className={`mt-2 text-[9px] ${isAssistant ? 'text-slate-400' : 'text-emerald-200'} text-right`}>
                        {new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </div>
                    </div>

                    {!isAssistant && (
                      <div className="w-8 h-8 rounded-xl bg-slate-900 flex items-center justify-center text-white flex-shrink-0 shadow-sm mt-0.5">
                        <User className="w-4 h-4" />
                      </div>
                    )}
                  </div>
                );
              })
            )}

            {sending && (
              <div className="flex items-start space-x-3 justify-start">
                <div className="w-8 h-8 rounded-xl bg-emerald-600 flex items-center justify-center text-white flex-shrink-0 shadow-sm animate-pulse">
                  <Bot className="w-4 h-4" />
                </div>
                <div className="bg-slate-50 border border-slate-200 rounded-2xl p-4 text-xs text-slate-500 flex items-center space-x-2">
                  <div className="w-2 h-2 rounded-full bg-emerald-600 animate-bounce"></div>
                  <div className="w-2 h-2 rounded-full bg-emerald-600 animate-bounce [animation-delay:0.2s]"></div>
                  <div className="w-2 h-2 rounded-full bg-emerald-600 animate-bounce [animation-delay:0.4s]"></div>
                  <span className="text-[11px] font-medium ml-1">Analyzing laboratory context with clinical guidelines...</span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Bottom Chat Input Bar */}
          <div className="p-4 border-t border-slate-200 bg-white">
            <div className="relative rounded-2xl border border-slate-200 focus-within:border-emerald-500 focus-within:ring-2 focus-within:ring-emerald-100 transition shadow-sm bg-white">
              <textarea
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Ask about your laboratory values or clinical guidelines (e.g., 'What does high glucose mean?')..."
                rows={2}
                disabled={sending}
                className="w-full px-4 pt-3 pb-10 text-xs text-slate-900 placeholder-slate-400 bg-transparent resize-none focus:outline-none"
              />
              <div className="absolute bottom-2 right-2 flex items-center space-x-2">
                <span className="text-[10px] text-slate-400">Shift + Enter for new line</span>
                <button
                  type="button"
                  onClick={() => handleSendMessage()}
                  disabled={!inputMessage.trim() || sending}
                  className="p-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 disabled:opacity-40 text-white transition shadow-sm"
                >
                  <Send className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
            <p className="text-[10px] text-center text-slate-400 mt-2">
              MediLens Assistant is for educational guidance only. Never alter medical treatments without professional clinical supervision.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
export default ClinicalAssistantPage;
