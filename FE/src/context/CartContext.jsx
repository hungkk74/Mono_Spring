import { createContext, useContext, useState, useCallback, useMemo } from 'react';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const [cart, setCart] = useState(() => {
    const stored = localStorage.getItem('cart');
    return stored ? JSON.parse(stored) : [];
  });

  // Selection: which SKU IDs are selected for checkout
  const [selectedIds, setSelectedIds] = useState(() => {
    const stored = localStorage.getItem('cart');
    const items = stored ? JSON.parse(stored) : [];
    return new Set(items.map((i) => i.skuId));
  });

  const persistCart = (newCart) => localStorage.setItem('cart', JSON.stringify(newCart));

  const addToCart = useCallback((item) => {
    setCart((prev) => {
      const existing = prev.find((i) => i.skuId === item.skuId);
      let newCart;
      if (existing) {
        newCart = prev.map((i) =>
          i.skuId === item.skuId
            ? { ...i, quantity: i.quantity + (item.quantity || 1) }
            : i
        );
      } else {
        newCart = [...prev, { ...item, quantity: item.quantity || 1 }];
      }
      persistCart(newCart);
      return newCart;
    });
    // Auto-select newly added item
    setSelectedIds((prev) => new Set(prev).add(item.skuId));
  }, []);

  const removeFromCart = useCallback((skuId) => {
    setCart((prev) => {
      const newCart = prev.filter((i) => i.skuId !== skuId);
      persistCart(newCart);
      return newCart;
    });
    setSelectedIds((prev) => {
      const next = new Set(prev);
      next.delete(skuId);
      return next;
    });
  }, []);

  const updateQuantity = useCallback((skuId, qty) => {
    setCart((prev) => {
      const newCart = prev.map((i) =>
        i.skuId === skuId ? { ...i, quantity: Math.max(1, qty) } : i
      );
      persistCart(newCart);
      return newCart;
    });
  }, []);

  const clearCart = useCallback(() => {
    setCart([]);
    setSelectedIds(new Set());
    localStorage.removeItem('cart');
  }, []);

  // Remove only selected items from cart (after successful checkout)
  const clearSelected = useCallback(() => {
    setCart((prev) => {
      const newCart = prev.filter((i) => !selectedIds.has(i.skuId));
      persistCart(newCart);
      return newCart;
    });
    setSelectedIds(new Set());
  }, [selectedIds]);

  // Selection helpers
  const toggleSelect = useCallback((skuId) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      next.has(skuId) ? next.delete(skuId) : next.add(skuId);
      return next;
    });
  }, []);

  const selectAll = useCallback(() => {
    setSelectedIds((prev) => {
      // Need current cart from state; read from localStorage as fallback
      const stored = localStorage.getItem('cart');
      const items = stored ? JSON.parse(stored) : [];
      return new Set(items.map((i) => i.skuId));
    });
  }, []);

  const deselectAll = useCallback(() => {
    setSelectedIds(new Set());
  }, []);

  const isSelected = useCallback((skuId) => selectedIds.has(skuId), [selectedIds]);

  // Computed values
  const cartCount = useMemo(() => cart.reduce((sum, i) => sum + i.quantity, 0), [cart]);
  const cartTotal = useMemo(() => cart.reduce((sum, i) => sum + i.price * i.quantity, 0), [cart]);

  const selectedCart = useMemo(() => cart.filter((i) => selectedIds.has(i.skuId)), [cart, selectedIds]);
  const selectedCount = useMemo(() => selectedCart.reduce((sum, i) => sum + i.quantity, 0), [selectedCart]);
  const selectedTotal = useMemo(() => selectedCart.reduce((sum, i) => sum + i.price * i.quantity, 0), [selectedCart]);
  const allSelected = useMemo(() => cart.length > 0 && cart.every((i) => selectedIds.has(i.skuId)), [cart, selectedIds]);

  const toOrderItems = useCallback(() => selectedCart.map((i) => ({ skuId: i.skuId, quantity: i.quantity })), [selectedCart]);

  const value = useMemo(() => ({
    cart, cartCount, cartTotal,
    selectedCart, selectedCount, selectedTotal, allSelected,
    addToCart, removeFromCart, updateQuantity, clearCart, clearSelected, toOrderItems,
    toggleSelect, selectAll, deselectAll, isSelected,
  }), [
    cart, cartCount, cartTotal,
    selectedCart, selectedCount, selectedTotal, allSelected,
    addToCart, removeFromCart, updateQuantity, clearCart, clearSelected, toOrderItems,
    toggleSelect, selectAll, deselectAll, isSelected,
  ]);

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used within CartProvider');
  return ctx;
}
