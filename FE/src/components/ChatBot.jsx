import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { catalogApi, orderApi, chatbotApi } from '../api';
import { formatCurrency, formatDate, getStatusInfo } from '../utils/format';

const quickActions = [
  'Tư vấn sản phẩm',
  'Cách mua hàng',
  'Tra đơn #',
  'Đổi trả',
];

const productTerms = [
  { term: 'polo', keyword: 'polo' },
  { term: 'ao polo', keyword: 'polo' },
  { term: 'ao thun', keyword: 'áo thun' },
  { term: 'quan ao the thao', keyword: 'thể thao' },
  { term: 'the thao', keyword: 'thể thao' },
  { term: 'giay nam', keyword: 'giày nam' },
  { term: 'giay nu', keyword: 'giày nữ' },
  { term: 'giay', keyword: 'giày' },
  { term: 'dong ho', keyword: 'đồng hồ' },
  { term: 'phu kien', keyword: 'phụ kiện' },
  { term: 'quan', keyword: 'quần' },
  { term: 'ao', keyword: 'áo' },
];

const paymentLabels = {
  COD: 'Thanh toán khi nhận hàng',
  MOMO: 'MoMo',
  BANK_TRANSFER: 'Chuyển khoản QR',
};

function createMessage(role, payload) {
  return {
    id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    ...payload,
  };
}

function normalize(text) {
  return text
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd');
}

function extractOrderId(text) {
  const match = text.match(/#?\b\d{1,12}\b/);
  return match ? match[0].replace('#', '') : null;
}

function hasAny(text, terms) {
  return terms.some((term) => text.includes(term));
}

function getProductKeyword(normalizedText) {
  const found = productTerms.find(({ term }) => normalizedText.includes(term));
  return found?.keyword || null;
}

function getProductPrice(product) {
  if (product.minPrice != null) return Number(product.minPrice);
  const activeSku = product.skus?.find((sku) => sku.isActive !== false && sku.price);
  return activeSku?.price ? Number(activeSku.price) : null;
}

function formatPaymentMethod(method) {
  return paymentLabels[method] || method || 'Chưa rõ';
}

function staticAnswer(normalizedText) {
  if (hasAny(normalizedText, ['mua hang', 'dat hang', 'checkout', 'gio hang'])) {
    return {
      text: 'Bạn chọn sản phẩm, chọn màu/size, thêm vào giỏ hàng rồi vào thanh toán. Khi thanh toán cần đăng nhập, nhập địa chỉ giao hàng và chọn COD, chuyển khoản QR hoặc MoMo.',
      links: [
        { to: '/shop', label: 'Xem sản phẩm', icon: 'storefront' },
        { to: '/cart', label: 'Giỏ hàng', icon: 'shopping_bag' },
      ],
    };
  }

  if (hasAny(normalizedText, ['thanh toan', 'payment', 'momo', 'cod', 'chuyen khoan', 'qr'])) {
    return {
      text: 'Mono Wear hỗ trợ COD, chuyển khoản ngân hàng bằng QR và thanh toán MoMo. Với MoMo hoặc QR, hệ thống sẽ chuyển bạn sang cổng thanh toán sau khi tạo đơn.',
      links: [{ to: '/checkout', label: 'Thanh toán', icon: 'payments' }],
    };
  }

  if (hasAny(normalizedText, ['doi tra', 'hoan tien', 'tra hang', 'bao hanh', 'size khong vua'])) {
    return {
      text: 'Chính sách đổi trả: sản phẩm lỗi hoặc giao sai hỗ trợ trong 30 ngày, đổi size trong 15 ngày, đổi ý/không vừa trong 7 ngày nếu sản phẩm còn nguyên trạng.',
      links: [
        { to: '/returns', label: 'Chính sách đổi trả', icon: 'assignment_return' },
        { to: '/contact', label: 'Liên hệ hỗ trợ', icon: 'support_agent' },
      ],
    };
  }

  if (hasAny(normalizedText, ['lien he', 'hotline', 'email', 'cua hang', 'dia chi', 'store'])) {
    return {
      text: 'Bạn có thể liên hệ Mono Wear qua hotline 1900 8888, email support@monowear.vn hoặc xem hệ thống cửa hàng trên website.',
      links: [
        { to: '/contact', label: 'Liên hệ', icon: 'call' },
        { to: '/stores', label: 'Cửa hàng', icon: 'location_on' },
      ],
    };
  }

  if (hasAny(normalizedText, ['ship', 'giao hang', 'van chuyen'])) {
    return {
      text: 'Khi đặt hàng, bạn nhập địa chỉ giao hàng ở bước thanh toán. Sau khi đơn được xác nhận, trạng thái sẽ chuyển qua đang giao và bạn có thể tra cứu bằng mã đơn.',
      links: [{ to: '/account#orders', label: 'Đơn hàng của tôi', icon: 'package' }],
    };
  }

  if (hasAny(normalizedText, ['xin chao', 'hello', 'hi', 'chao'])) {
    return {
      text: 'Chào bạn, mình có thể hỗ trợ tìm sản phẩm, hướng dẫn mua hàng, thanh toán, đổi trả và tra cứu trạng thái đơn hàng trong website Mono Wear.',
    };
  }

  return null;
}

export default function ChatBot() {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [showConsultCategories, setShowConsultCategories] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  
  // Load chat history from localStorage
  const [messages, setMessages] = useState(() => {
    const saved = localStorage.getItem('monowear_chat_messages');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        console.error("Failed to parse saved chat messages", e);
      }
    }
    return [
      createMessage('bot', {
        text: 'Xin chào, mình là trợ lý Mono Wear. Mình hỗ trợ mua hàng, tư vấn sản phẩm và tra cứu trạng thái đơn theo mã đơn.',
      }),
    ];
  });

  const endRef = useRef(null);
  const typingIntervalRef = useRef(null);

  // Auto-scroll to bottom
  useEffect(() => {
    if (open) endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, open, loading]);

  // Save chat history to localStorage
  useEffect(() => {
    localStorage.setItem('monowear_chat_messages', JSON.stringify(messages));
  }, [messages]);

  // Clean up interval on unmount
  useEffect(() => {
    return () => {
      if (typingIntervalRef.current) {
        clearInterval(typingIntervalRef.current);
      }
    };
  }, []);

  const handleStopTyping = () => {
    if (typingIntervalRef.current) {
      clearInterval(typingIntervalRef.current);
      typingIntervalRef.current = null;
    }
    setLoading(false);
  };

  const handleResetChat = () => {
    if (typingIntervalRef.current) {
      clearInterval(typingIntervalRef.current);
      typingIntervalRef.current = null;
    }
    localStorage.removeItem('monowear_chat_messages');
    setMessages([
      createMessage('bot', {
        text: 'Xin chào, mình là trợ lý Mono Wear. Mình hỗ trợ mua hàng, tư vấn sản phẩm và tra cứu trạng thái đơn theo mã đơn.',
      }),
    ]);
    setSelectedProduct(null);
    setShowConsultCategories(false);
    setLoading(false);
    setInput('');
  };

  const lookupOrder = async (orderId) => {
    const res = await orderApi.trackOrder(orderId);
    if (!res.ok || !res.data) {
      return createMessage('bot', {
        text: `Mình chưa tìm thấy đơn #${orderId}. Bạn kiểm tra lại mã đơn hoặc đăng nhập để xem trong mục tài khoản.`,
        links: [{ to: '/account#orders', label: 'Đơn hàng của tôi', icon: 'package' }],
      });
    }

    const order = res.data;
    const status = getStatusInfo(order.status);
    // Build detailed product list if available
    const items = order.items?.map((item) => {
      const price = formatCurrency(item.unitPrice);
      return `${item.quantity}x ${item.sku?.product?.name || 'Sản phẩm'} - ${price}`;
    }).join('\n');
    const detailText = `Đơn #${order.id} hiện ở trạng thái ${status.label}.
Tổng tiền: ${formatCurrency(order.totalAmount)} (${order.itemCount || 0} sản phẩm).
Thanh toán: ${formatPaymentMethod(order.paymentMethod)}.
Ngày đặt: ${formatDate(order.createdAt)}.`;
    const productInfo = items ? `\nCác mặt hàng:\n${items}` : '';
    return createMessage('bot', {
      text: detailText + productInfo,
      links: [{ to: `/account/orders/${order.id}`, label: 'Xem chi tiết đơn hàng', icon: 'receipt_long' }],
    });
  };

  const recommendProducts = async (keyword) => {
    try {
      const res = await catalogApi.getProducts(0, 4, null, keyword);
      const products = res.ok ? (res.data?.content || []) : [];

      if (products.length === 0) {
        return createMessage('bot', {
          text: keyword
            ? `Mình chưa tìm thấy sản phẩm phù hợp với "${keyword}". Bạn có thể xem toàn bộ danh mục hoặc thử từ khóa khác.`
            : 'Mình chưa lấy được danh sách gợi ý lúc này. Bạn có thể xem trực tiếp ở trang sản phẩm.',
          links: [{ to: '/shop', label: 'Xem tất cả sản phẩm', icon: 'storefront' }],
        });
      }

      return createMessage('bot', {
        text: keyword
          ? `Mình tìm được một vài sản phẩm phù hợp với "${keyword}".`
          : 'Mình gợi ý một vài sản phẩm đang có trên website.',
        products,
        links: [{ to: keyword ? `/shop?keyword=${encodeURIComponent(keyword)}` : '/shop', label: 'Xem thêm', icon: 'arrow_forward' }],
      });
    } catch {
      return createMessage('bot', {
        text: 'Mình chưa kết nối được dữ liệu sản phẩm. Bạn thử lại sau hoặc vào trang sản phẩm để xem danh mục.',
        links: [{ to: '/shop', label: 'Trang sản phẩm', icon: 'storefront' }],
      });
    }
  };

  const sendMessage = async (text = input) => {
    const question = text.trim();
    if (!question) return;

    // Stop current typing if any
    if (typingIntervalRef.current) {
      clearInterval(typingIntervalRef.current);
      typingIntervalRef.current = null;
    }

    setInput('');
    setMessages((current) => [...current, createMessage('user', { text: question })]);
    setLoading(true);

    const normalizedQuestion = normalize(question);
    const orderId = extractOrderId(question);
    const isOrderQuestion = /^\s*#?\d{1,12}\s*$/.test(question)
      || hasAny(normalizedQuestion, ['tra don', 'tra cuu don', 'ma don', 'don hang', 'trang thai don', 'tracking', 'track order']);

    if (isOrderQuestion) {
      let response;
      if (!orderId) {
        response = createMessage('bot', {
          text: 'Bạn gửi giúp mình mã đơn hàng, ví dụ: #123. Mình sẽ tra trạng thái hiện tại cho bạn.',
        });
      } else {
        response = await lookupOrder(orderId);
      }
      setMessages((current) => [...current, response]);
      setLoading(false);
      return;
    }

    const productKeyword = getProductKeyword(normalizedQuestion);
    const isProductQuestion = productKeyword || hasAny(normalizedQuestion, ['tu van', 'san pham', 'goi y', 'mac gi', 'mua gi']);
    if (isProductQuestion && productKeyword) {
      const response = await recommendProducts(productKeyword);
      setMessages((current) => [...current, response]);
      setLoading(false);
      return;
    }

    const botMessageId = `bot-${Date.now()}`;
    setMessages((current) => [
      ...current,
      { id: botMessageId, role: 'bot', text: '' }
    ]);

    try {
      const res = await chatbotApi.chat(question);
      if (!res.ok || !res.data) {
        throw new Error(res?.message || "Lỗi phản hồi từ chatbot");
      }

      const { answer: textAnswer, products: recommendedProducts } = res.data;
      const cleanAnswer = textAnswer || "Xin lỗi, trợ lý chưa thể trả lời câu hỏi này lúc này.";

      // Giả lập stream chữ hiển thị động để tạo hiệu ứng gõ chữ mượt mà
      let currentText = "";
      let index = 0;
      typingIntervalRef.current = setInterval(() => {
        if (index < cleanAnswer.length) {
          currentText += cleanAnswer.charAt(index);
          setMessages((current) =>
            current.map((msg) =>
              msg.id === botMessageId ? { ...msg, text: currentText } : msg
            )
          );
          index++;
        } else {
          if (typingIntervalRef.current) {
            clearInterval(typingIntervalRef.current);
            typingIntervalRef.current = null;
          }
          // Đính kèm sản phẩm gợi ý thực tế sau khi stream hoàn tất
          setMessages((current) =>
            current.map((msg) =>
              msg.id === botMessageId ? { ...msg, products: recommendedProducts } : msg
            )
          );
          setLoading(false);
        }
      }, 12); // Tốc độ gõ chữ 12ms/ký tự
    } catch (error) {
      console.error("ChatBot error:", error);
      setMessages((current) =>
        current.map((msg) =>
          msg.id === botMessageId
            ? {
                ...msg,
                text: "Rất tiếc, trợ lý AI đang bận hoặc gặp lỗi kết nối. Bạn vui lòng thử lại sau hoặc liên hệ bộ phận hỗ trợ!",
                links: [{ to: '/contact', label: 'Liên hệ hỗ trợ', icon: 'support_agent' }]
              }
            : msg
        )
      );
      setLoading(false);
    }
  };

  const handleQuickAction = (action) => {
    if (action === 'Tư vấn sản phẩm') {
      setShowConsultCategories(true);
      setMessages((current) => [
        ...current,
        createMessage('bot', {
          text: 'Bạn muốn mình tư vấn sản phẩm thuộc danh mục nào dưới đây?',
        }),
      ]);
      return;
    }

    if (action === 'Quay lại') {
      setShowConsultCategories(false);
      return;
    }

    sendMessage(action);
    if (showConsultCategories) {
      setShowConsultCategories(false);
    }
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    sendMessage();
  };

  return (
    <div className="chatbot" aria-live="polite">
      {open && (
        <section className="chatbot-window" aria-label="Trợ lý Mono Wear">
          <header className="chatbot-header">
            <div>
              <p className="chatbot-kicker">MONO WEAR</p>
              <h2>Trợ lý mua hàng</h2>
            </div>
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <button
                type="button"
                className="chatbot-icon-btn"
                onClick={handleResetChat}
                aria-label="Xóa lịch sử chat"
                style={{ marginRight: '8px', cursor: 'pointer' }}
              >
                <span className="material-symbols-outlined">restart_alt</span>
              </button>
              <button
                type="button"
                className="chatbot-icon-btn"
                onClick={() => setOpen(false)}
                aria-label="Đóng chat"
                style={{ cursor: 'pointer' }}
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
          </header>

          {selectedProduct ? (
            <div className="chatbot-product-detail-panel" style={{
              display: 'flex',
              flexDirection: 'column',
              height: 'calc(100% - 70px)',
              background: 'var(--bg-primary, #ffffff)',
              padding: '16px',
              overflowY: 'auto',
              borderBottomLeftRadius: '12px',
              borderBottomRightRadius: '12px'
            }}>
              <button
                type="button"
                className="chatbot-detail-back-btn"
                onClick={() => setSelectedProduct(null)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  border: 'none',
                  background: 'none',
                  fontWeight: '600',
                  cursor: 'pointer',
                  padding: '8px 0',
                  marginBottom: '12px',
                  color: 'var(--text-secondary, #666)',
                  textAlign: 'left',
                  fontSize: '14px'
                }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>arrow_back</span>
                Quay lại cuộc trò chuyện
              </button>

              <img
                src={selectedProduct.imageUrl || 'https://via.placeholder.com/300x200'}
                alt={selectedProduct.name}
                style={{
                  width: '100%',
                  height: '200px',
                  objectFit: 'cover',
                  borderRadius: '8px',
                  marginBottom: '16px',
                  border: '1px solid var(--border-color, #eee)'
                }}
              />

              <div style={{ flexGrow: 1, display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <span style={{
                  fontSize: '11px',
                  fontWeight: '600',
                  textTransform: 'uppercase',
                  color: 'var(--color-primary, #111)',
                  letterSpacing: '0.5px'
                }}>
                  {selectedProduct.categoryName || 'Sản phẩm'}
                </span>
                
                <h3 style={{
                  fontSize: '18px',
                  margin: '0',
                  fontWeight: '700',
                  lineHeight: '1.3',
                  color: 'var(--text-primary, #111)'
                }}>
                  {selectedProduct.name}
                </h3>

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', margin: '4px 0' }}>
                  {(() => {
                    const price = getProductPrice(selectedProduct);
                    const hasSale = selectedProduct.onSale && selectedProduct.salePercent > 0;
                    const salePrice = hasSale
                      ? Math.round(Number(price) * (100 - Number(selectedProduct.salePercent || 0)) / 100)
                      : null;
                    return (
                      <>
                        <span style={{ fontSize: '16px', fontWeight: '700', color: '#e63946' }}>
                          {formatCurrency(salePrice || price || 0)}
                        </span>
                        {hasSale && (
                          <span style={{ fontSize: '13px', textDecoration: 'line-through', color: 'var(--text-muted, #999)' }}>
                            {formatCurrency(price || 0)}
                          </span>
                        )}
                      </>
                    );
                  })()}
                </div>

                {selectedProduct.material && (
                  <p style={{ fontSize: '13px', margin: '4px 0', color: 'var(--text-secondary, #555)' }}>
                    <strong>Chất liệu:</strong> {selectedProduct.material}
                  </p>
                )}

                <p style={{
                  fontSize: '13px',
                  color: 'var(--text-secondary, #444)',
                  lineHeight: '1.5',
                  margin: '8px 0 20px 0',
                  whiteSpace: 'pre-line'
                }}>
                  {selectedProduct.description || 'Chưa có mô tả chi tiết cho sản phẩm này.'}
                </p>
              </div>

              <Link
                to={`/product/${selectedProduct.slug}`}
                onClick={() => {
                  setSelectedProduct(null);
                  setOpen(false);
                }}
                className="btn btn-primary"
                style={{
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  gap: '8px',
                  padding: '10px',
                  borderRadius: '6px',
                  fontSize: '13px',
                  fontWeight: '600',
                  textTransform: 'uppercase',
                  marginTop: 'auto',
                  textDecoration: 'none',
                  color: '#fff',
                  backgroundColor: 'var(--color-primary, #111)',
                  textAlign: 'center'
                }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>shopping_cart</span>
                Xem chi tiết & Mua hàng
              </Link>
            </div>
          ) : (
            <>
              <div className="chatbot-messages">
                {messages.map((message) => (
                  <div className={`chatbot-message ${message.role}`} key={message.id}>
                    <div className="chatbot-bubble">
                      <p style={{ whiteSpace: 'pre-line' }}>{message.text}</p>

                      {message.products?.length > 0 && (
                        <div className="chatbot-products" style={{ display: 'flex', flexDirection: 'column', gap: '6px', marginTop: '8px' }}>
                          {message.products.map((product) => {
                            const price = getProductPrice(product);
                            return (
                              <button
                                type="button"
                                className="chatbot-product"
                                onClick={() => setSelectedProduct(product)}
                                key={product.id || product.slug}
                                style={{
                                  display: 'flex',
                                  width: '100%',
                                  alignItems: 'center',
                                  gap: '10px',
                                  padding: '8px',
                                  border: '1px solid var(--border-color, #eee)',
                                  borderRadius: '6px',
                                  background: 'var(--bg-card, #f9f9f9)',
                                  cursor: 'pointer',
                                  textAlign: 'left'
                                }}
                              >
                                <img
                                  src={product.imageUrl || 'https://via.placeholder.com/80'}
                                  alt={product.name}
                                  style={{ width: '48px', height: '48px', objectFit: 'cover', borderRadius: '4px' }}
                                />
                                <span style={{ display: 'flex', flexDirection: 'column', flexGrow: 1 }}>
                                  <strong style={{ fontSize: '13px', color: 'var(--text-primary, #111)', fontWeight: '600' }}>{product.name}</strong>
                                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '2px' }}>
                                    {(() => {
                                      const hasSale = product.onSale && product.salePercent > 0;
                                      const salePrice = hasSale
                                        ? Math.round(Number(price) * (100 - Number(product.salePercent || 0)) / 100)
                                        : null;
                                      return price ? (
                                        <>
                                          <span style={{ fontSize: '12px', fontWeight: '700', color: '#e63946' }}>
                                            {formatCurrency(salePrice || price)}
                                          </span>
                                          {hasSale && (
                                            <span style={{ fontSize: '10px', textDecoration: 'line-through', color: 'var(--text-muted, #999)' }}>
                                              {formatCurrency(price)}
                                            </span>
                                          )}
                                        </>
                                      ) : (
                                        <span style={{ fontSize: '11px', color: 'var(--text-muted, #777)' }}>{product.categoryName}</span>
                                      );
                                    })()}
                                  </div>
                                </span>
                              </button>
                            );
                          })}
                        </div>
                      )}

                      {message.links?.length > 0 && (
                        <div className="chatbot-links">
                          {message.links.map((link) => (
                            <Link to={link.to} className="chatbot-link" key={`${message.id}-${link.to}`}>
                              <span className="material-symbols-outlined">{link.icon}</span>
                              {link.label}
                            </Link>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                ))}

                {loading && (
                  <div className="chatbot-message bot">
                    <div className="chatbot-bubble chatbot-typing">
                      <span />
                      <span />
                      <span />
                    </div>
                  </div>
                )}
                <div ref={endRef} />
              </div>

              <div className="chatbot-quick-actions">
                {showConsultCategories ? (
                  <>
                    <button type="button" onClick={() => handleQuickAction('Áo Polo')}>Áo Polo</button>
                    <button type="button" onClick={() => handleQuickAction('Áo Thun')}>Áo Thun</button>
                    <button type="button" onClick={() => handleQuickAction('Giày Thể Thao')}>Giày Thể Thao</button>
                    <button type="button" onClick={() => handleQuickAction('Đồng Hồ')}>Đồng Hồ</button>
                    <button type="button" onClick={() => handleQuickAction('Quần Nam')}>Quần Nam</button>
                    <button type="button" onClick={() => handleQuickAction('Quay lại')}>← Quay lại</button>
                  </>
                ) : (
                  quickActions.map((action) => (
                    <button type="button" key={action} onClick={() => handleQuickAction(action)}>
                      {action}
                    </button>
                  ))
                )}
              </div>

              <form className="chatbot-form" onSubmit={handleSubmit}>
                <input
                  value={input}
                  onChange={(event) => setInput(event.target.value)}
                  placeholder="Nhập câu hỏi hoặc mã đơn..."
                  aria-label="Nội dung chat"
                />
                {loading && !input.trim() ? (
                  <button
                    type="button"
                    onClick={handleStopTyping}
                    aria-label="Ngừng phản hồi"
                    style={{ backgroundColor: '#dc2626', color: '#fff', cursor: 'pointer' }}
                  >
                    <span className="material-symbols-outlined">stop</span>
                  </button>
                ) : (
                  <button type="submit" aria-label="Gửi tin nhắn" disabled={!input.trim()}>
                    <span className="material-symbols-outlined">send</span>
                  </button>
                )}
              </form>
            </>
          )}
        </section>
      )}

      <button type="button" className="chatbot-toggle" onClick={() => setOpen((value) => !value)} aria-label="Mở trợ lý mua hàng">
        <span className="material-symbols-outlined">{open ? 'expand_more' : 'smart_toy'}</span>
      </button>
    </div>
  );
}
