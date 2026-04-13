const API = '';
const TAX = 5;
let menuItems = [], cart = [], activeCategory = 'All', currentOrderId = null;

// ─── TAB NAVIGATION ───────────────────────────────────────────
function showTab(name) {
  document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
  document.getElementById('tab-' + name).classList.add('active');
  event.target.classList.add('active');
  if (name === 'orders') loadOrders();
  if (name === 'dashboard') loadDashboard();
  if (name === 'menu') loadMenuMgmt();
}

// ─── MENU ──────────────────────────────────────────────────────
async function loadMenu() {
  const res = await fetch(API + '/api/menu');
  const json = await res.json();
  menuItems = json.data;
  renderCategoryTabs();
  renderMenu();
}

function renderCategoryTabs() {
  const cats = ['All', ...new Set(menuItems.map(m => m.category))];
  const el = document.getElementById('catTabs');
  el.innerHTML = cats.map(c =>
    `<button class="cat-btn${c === activeCategory ? ' active' : ''}" onclick="setCategory('${c}')">${c}</button>`
  ).join('');
}

function setCategory(cat) {
  activeCategory = cat;
  renderCategoryTabs();
  renderMenu();
}

function filterMenu() {
  renderMenu();
}

function renderMenu() {
  const search = (document.getElementById('menuSearch').value || '').toLowerCase();
  const items = menuItems.filter(m =>
    (activeCategory === 'All' || m.category === activeCategory) &&
    m.name.toLowerCase().includes(search)
  );
  const grid = document.getElementById('menuGrid');
  if (!items.length) { grid.innerHTML = '<div class="loading">No items found</div>'; return; }
  grid.innerHTML = items.map(m =>
    `<div class="menu-card${m.available ? '' : ' unavailable'}" onclick="addToCart(${m.id})">
       <div class="icon">${m.icon || '🍵'}</div>
       <div class="name">${m.name}</div>
       <div class="price">₹${m.price}</div>
     </div>`
  ).join('');
}

// ─── CART ──────────────────────────────────────────────────────
function addToCart(itemId) {
  const item = menuItems.find(m => m.id === itemId);
  if (!item) return;
  const ex = cart.find(c => c.id === itemId);
  if (ex) ex.qty++;
  else cart.push({ id: item.id, name: item.name, price: item.price, icon: item.icon, qty: 1 });
  renderCart();
}

function changeQty(itemId, delta) {
  const ex = cart.find(c => c.id === itemId);
  if (!ex) return;
  ex.qty += delta;
  if (ex.qty <= 0) cart = cart.filter(c => c.id !== itemId);
  renderCart();
}

function removeFromCart(itemId) {
  cart = cart.filter(c => c.id !== itemId);
  renderCart();
}

function clearCart() {
  cart = [];
  currentOrderId = null;
  document.getElementById('discountPct').value = 0;
  renderCart();
}

function renderCart() {
  const el = document.getElementById('cartList');
  document.getElementById('cartCount').textContent = cart.reduce((s, c) => s + c.qty, 0) + ' items';
  if (!cart.length) {
    el.innerHTML = '<div class="empty-state">Tap items from the menu to add</div>';
  } else {
    el.innerHTML = cart.map(c =>
      `<div class="cart-item">
         <div class="ci-name">${c.icon || ''} ${c.name}</div>
         <div class="qty-ctrl">
           <button class="qty-btn" onclick="changeQty(${c.id}, -1)">−</button>
           <span class="qty-num">${c.qty}</span>
           <button class="qty-btn" onclick="changeQty(${c.id}, 1)">+</button>
         </div>
         <div class="ci-total">₹${(c.price * c.qty).toFixed(2)}</div>
         <button class="rm-btn" onclick="removeFromCart(${c.id})">✕</button>
       </div>`
    ).join('');
  }
  updateSummary();
}

function updateSummary() {
  const subtotal = cart.reduce((s, c) => s + c.price * c.qty, 0);
  const disc = Math.min(100, Math.max(0, parseFloat(document.getElementById('discountPct').value) || 0));
  const discAmt = subtotal * disc / 100;
  const afterDisc = subtotal - discAmt;
  const tax = afterDisc * TAX / 100;
  const total = afterDisc + tax;

  document.getElementById('sumSubtotal').textContent = '₹' + subtotal.toFixed(2);
  document.getElementById('sumDiscount').textContent = '-₹' + discAmt.toFixed(2);
  document.getElementById('discLabel').textContent = `Discount (${disc}%)`;
  document.getElementById('discRow').style.display = disc > 0 ? 'flex' : 'none';
  document.getElementById('sumTax').textContent = '₹' + tax.toFixed(2);
  document.getElementById('sumTotal').textContent = '₹' + total.toFixed(2);
}

// ─── ORDER SUBMISSION ──────────────────────────────────────────
async function submitOrder() {
  if (!cart.length) { showToast('Add items to generate a bill', 'error'); return; }
  const payload = {
    tableNumber: document.getElementById('tableNo').value || null,
    customerName: document.getElementById('tableNo').value || null,
    discountPercent: parseFloat(document.getElementById('discountPct').value) || 0,
    items: cart.map(c => ({ menuItemId: c.id, quantity: c.qty }))
  };
  try {
    const res = await fetch(API + '/api/orders', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const json = await res.json();
    if (!json.success) throw new Error(json.message);
    const order = json.data;
    currentOrderId = order.id;

    const closeRes = await fetch(API + `/api/orders/${order.id}/close`, { method: 'PUT' });
    const closeJson = await closeRes.json();
    showReceipt(closeJson.data);
    showToast('Bill generated!', 'success');
  } catch (e) {
    showToast('Error: ' + e.message, 'error');
  }
}

async function saveDraft() {
  if (!cart.length) { showToast('Add items first', 'error'); return; }
  const payload = {
    tableNumber: document.getElementById('tableNo').value || null,
    customerName: document.getElementById('tableNo').value || null,
    discountPercent: parseFloat(document.getElementById('discountPct').value) || 0,
    items: cart.map(c => ({ menuItemId: c.id, quantity: c.qty }))
  };
  try {
    const res = await fetch(API + '/api/orders', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const json = await res.json();
    if (!json.success) throw new Error(json.message);
    currentOrderId = json.data.id;
    showToast('Draft saved (Order #' + json.data.id + ')', 'success');
  } catch (e) {
    showToast('Error: ' + e.message, 'error');
  }
}

// ─── RECEIPT ───────────────────────────────────────────────────
function showReceipt(order) {
  const now = new Date(order.createdAt || Date.now());
  const dateStr = now.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  const itemRows = order.items.map(i =>
    `<tr><td>${i.itemName}</td><td style="text-align:center">${i.quantity}</td><td style="text-align:right">₹${i.unitPrice}</td><td style="text-align:right">₹${i.lineTotal.toFixed(2)}</td></tr>`
  ).join('');

  document.getElementById('receiptContent').innerHTML = `
    <div class="receipt-head">
      <div style="font-size:28px">🍵</div>
      <h3>Chai Aur Kuch</h3>
      <div style="font-size:11px;color:#666">Your Favourite Tea Stall</div>
      <div style="font-size:11px;margin-top:4px">${dateStr} | ${timeStr}</div>
      <div style="font-size:11px"><strong>Bill #${order.id}</strong>${order.tableNumber ? ' | ' + order.tableNumber : ''}</div>
    </div>
    <hr class="receipt-sep"/>
    <table class="receipt-items">
      <thead><tr><th>Item</th><th style="text-align:center">Qty</th><th style="text-align:right">Rate</th><th style="text-align:right">Amt</th></tr></thead>
      <tbody>${itemRows}</tbody>
    </table>
    <hr class="receipt-sep"/>
    <table class="receipt-totals" style="width:100%">
      <tr><td>Subtotal</td><td>₹${order.subtotal.toFixed(2)}</td></tr>
      ${order.discountPercent > 0 ? `<tr><td>Discount (${order.discountPercent}%)</td><td>-₹${order.discountAmount.toFixed(2)}</td></tr>` : ''}
      <tr><td>GST (5%)</td><td>₹${order.taxAmount.toFixed(2)}</td></tr>
      <tr class="receipt-grand"><td>TOTAL</td><td>₹${order.totalAmount.toFixed(2)}</td></tr>
    </table>
    <div class="receipt-footer">Thank you for visiting! 🙏<br>Please come again</div>`;
  document.getElementById('receiptModal').style.display = 'flex';
}

function printReceipt() {
  const content = document.getElementById('receiptContent').innerHTML;
  const w = window.open('', '', 'width=400,height=600');
  w.document.write(`<html><head><title>Receipt</title><style>
    body{font-family:'Courier New',monospace;font-size:13px;padding:20px;max-width:320px}
    table{width:100%;border-collapse:collapse}td{padding:3px 0}
    .receipt-grand{font-weight:700;font-size:15px}
    hr{border:1px dashed #ccc;margin:10px 0}
    h3{font-size:18px;text-align:center}
    div{text-align:center}
  </style></head><body>${content}</body></html>`);
  w.document.close(); w.print();
}

function newBill() { closeModal('receiptModal'); clearCart(); }
function closeModal(id) { document.getElementById(id).style.display = 'none'; }

// ─── ORDERS ────────────────────────────────────────────────────
async function loadOrders() {
  const filter = document.getElementById('orderFilter')?.value || 'today';
  const url = filter === 'today' ? '/api/orders/today' : filter === 'open' ? '/api/orders/open' : '/api/orders';
  const res = await fetch(API + url);
  const json = await res.json();
  renderOrdersTable(json.data, document.getElementById('ordersTable'));
}

function renderOrdersTable(orders, container) {
  if (!orders || !orders.length) { container.innerHTML = '<div class="loading">No orders found</div>'; return; }
  container.innerHTML = `
    <table class="orders-table">
      <thead><tr><th>#</th><th>Table/Name</th><th>Items</th><th>Subtotal</th><th>Tax</th><th>Total</th><th>Status</th><th>Time</th><th>Actions</th></tr></thead>
      <tbody>${orders.map(o => `
        <tr>
          <td><strong>${o.id}</strong></td>
          <td>${o.tableNumber || o.customerName || '—'}</td>
          <td>${o.items.length} item(s)</td>
          <td>₹${(o.subtotal||0).toFixed(2)}</td>
          <td>₹${(o.taxAmount||0).toFixed(2)}</td>
          <td><strong>₹${(o.totalAmount||0).toFixed(2)}</strong></td>
          <td><span class="status-badge status-${o.status}">${o.status}</span></td>
          <td>${new Date(o.createdAt).toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'})}</td>
          <td>
            <button class="btn btn-sm" onclick="viewOrder(${o.id})">View</button>
            ${o.status==='OPEN'?`<button class="btn btn-sm btn-primary" onclick="closeOrder(${o.id})">Close</button>`:''}
          </td>
        </tr>`).join('')}
      </tbody>
    </table>`;
}

async function closeOrder(id) {
  await fetch(API + `/api/orders/${id}/close`, { method: 'PUT' });
  showToast('Order closed', 'success');
  loadOrders();
}

async function viewOrder(id) {
  const res = await fetch(API + `/api/orders/${id}`);
  const json = await res.json();
  showReceipt(json.data);
}

// ─── DASHBOARD ─────────────────────────────────────────────────
async function loadDashboard() {
  const [summRes, todayRes] = await Promise.all([
    fetch(API + '/api/orders/summary'),
    fetch(API + '/api/orders/today')
  ]);
  const summ = (await summRes.json()).data;
  const today = (await todayRes.json()).data;

  document.getElementById('metricsGrid').innerHTML = `
    <div class="metric-card"><div class="metric-label">Today's Revenue</div><div class="metric-value green">₹${(summ.totalRevenue||0).toFixed(2)}</div></div>
    <div class="metric-card"><div class="metric-label">Orders Closed</div><div class="metric-value">${summ.totalOrders||0}</div></div>
    <div class="metric-card"><div class="metric-label">Items Sold</div><div class="metric-value">${summ.itemsSold||0}</div></div>
    <div class="metric-card"><div class="metric-label">Tax Collected</div><div class="metric-value">₹${(summ.totalTax||0).toFixed(2)}</div></div>
    <div class="metric-card"><div class="metric-label">Discounts Given</div><div class="metric-value">₹${(summ.totalDiscount||0).toFixed(2)}</div></div>`;
  renderOrdersTable(today.slice(0, 10), document.getElementById('recentOrders'));
}

// ─── MENU MANAGEMENT ──────────────────────────────────────────
async function loadMenuMgmt() {
  const res = await fetch(API + '/api/menu/all');
  const json = await res.json();
  const items = json.data;
  const el = document.getElementById('menuMgmtTable');
  el.innerHTML = `
    <table class="orders-table">
      <thead><tr><th>Icon</th><th>Name</th><th>Category</th><th>Price</th><th>Available</th><th>Actions</th></tr></thead>
      <tbody>${items.map(m => `
        <tr>
          <td>${m.icon||'🍵'}</td>
          <td>${m.name}</td>
          <td>${m.category}</td>
          <td>₹${m.price}</td>
          <td><label class="toggle-switch"><input type="checkbox" ${m.available?'checked':''} onchange="toggleItem(${m.id},this.checked)"/><span class="slider"></span></label></td>
          <td><button class="btn btn-sm btn-danger" onclick="removeMenuItem(${m.id})">Remove</button></td>
        </tr>`).join('')}
      </tbody>
    </table>`;
}

function openAddItemModal() { document.getElementById('addItemModal').style.display = 'flex'; }

async function addMenuItem() {
  const name = document.getElementById('newItemName').value.trim();
  const category = document.getElementById('newItemCategory').value;
  const price = parseFloat(document.getElementById('newItemPrice').value);
  const icon = document.getElementById('newItemIcon').value || '🍵';
  if (!name || !price) { showToast('Name and price are required', 'error'); return; }
  await fetch(API + '/api/menu', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, category, price, icon, available: true })
  });
  showToast('Item added!', 'success');
  closeModal('addItemModal');
  loadMenuMgmt(); loadMenu();
}

async function toggleItem(id, available) {
  await fetch(API + `/api/menu/${id}`, {
    method: 'PUT', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ available })
  });
  loadMenu();
}

async function removeMenuItem(id) {
  if (!confirm('Remove this item?')) return;
  await fetch(API + `/api/menu/${id}`, { method: 'DELETE' });
  showToast('Item removed', 'success');
  loadMenuMgmt(); loadMenu();
}

// ─── TOAST ─────────────────────────────────────────────────────
function showToast(msg, type = '') {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.className = 'toast ' + type;
  void t.offsetWidth;
  t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 2800);
}

// ─── INIT ──────────────────────────────────────────────────────
loadMenu();
