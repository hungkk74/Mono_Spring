import { BrowserRouter, Routes, Route, Outlet } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { ToastProvider } from './components/Toast';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import ScrollToTop from './components/ScrollToTop';

import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Category from './pages/Category';
import ProductDetail from './pages/ProductDetail';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import Account from './pages/Account';
import AdminDashboard from './pages/AdminDashboard';
import About from './pages/About';
import Privacy from './pages/Privacy';
import Terms from './pages/Terms';
import Returns from './pages/Returns';
import Stores from './pages/Stores';
import Contact from './pages/Contact';
import PaymentResult from './pages/PaymentResult';
import OrderDetail from './pages/OrderDetail';
import ForgotPassword from './pages/ForgotPassword';

function LayoutWrapper() {
  return (
    <Layout>
      <Outlet />
    </Layout>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <ScrollToTop />
      <AuthProvider>
        <CartProvider>
          <ToastProvider>
            <Routes>
              {/* Admin — no standard layout */}
              <Route path="/admin" element={
                <ProtectedRoute roles={['ADMIN', 'STAFF']}>
                  <AdminDashboard />
                </ProtectedRoute>
              } />

              {/* Public & Customer pages with Layout */}
              <Route element={<LayoutWrapper />}>
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/forgot-password" element={<ForgotPassword />} />
                <Route path="/shop" element={<Category />} />
                <Route path="/about" element={<About />} />
                <Route path="/privacy" element={<Privacy />} />
                <Route path="/terms" element={<Terms />} />
                <Route path="/returns" element={<Returns />} />
                <Route path="/stores" element={<Stores />} />
                <Route path="/contact" element={<Contact />} />
                <Route path="/payment/result" element={<PaymentResult />} />
                <Route path="/product/:slug" element={<ProductDetail />} />
                <Route path="/cart" element={<Cart />} />
                <Route path="/checkout" element={
                  <ProtectedRoute><Checkout /></ProtectedRoute>
                } />
                <Route path="/account" element={
                  <ProtectedRoute><Account /></ProtectedRoute>
                } />
                <Route path="/account/orders/:id" element={
                  <ProtectedRoute><OrderDetail /></ProtectedRoute>
                } />
              </Route>
            </Routes>
          </ToastProvider>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
