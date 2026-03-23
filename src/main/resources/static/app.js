const form = document.querySelector("#subscriptionForm");
const formMessage = document.querySelector("#formMessage");
const authForm = document.querySelector("#authForm");
const authMessage = document.querySelector("#authMessage");
const authStatusText = document.querySelector("#authStatusText");
const authStatusTile = document.querySelector("#authStatusTile");
const registerBtn = document.querySelector("#registerBtn");
const loginBtn = document.querySelector("#loginBtn");
const logoutBtn = document.querySelector("#logoutBtn");
const list = document.querySelector("#subscriptions");
const totalCount = document.querySelector("#totalCount");
const toolCount = document.querySelector("#toolCount");
const avgMonthly = document.querySelector("#avgMonthly");

const TOKEN_KEY = "subtracker_token";

const getToken = () => localStorage.getItem(TOKEN_KEY);
const setToken = (token) => localStorage.setItem(TOKEN_KEY, token);
const clearToken = () => localStorage.removeItem(TOKEN_KEY);

const authHeaders = () => {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
};

const decodeJwt = (token) => {
  try {
    const payload = token.split(".")[1];
    const decoded = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(decoded);
  } catch {
    return null;
  }
};

const setAuthStatus = (text, isError = false) => {
  authStatusText.textContent = text;
  authStatusText.classList.toggle("status-error", isError);
  authStatusTile.classList.toggle("status-failed", isError);
};

const updateAuthStatusFromToken = () => {
  const token = getToken();
  if (!token) {
    setAuthStatus("NOT SIGNED IN");
    return;
  }
  const payload = decodeJwt(token);
  if (payload && payload.sub) {
    setAuthStatus(`SIGNED IN AS ${payload.sub}`);
  } else {
    setAuthStatus("SIGNED IN");
  }
};

const formatDate = (value) => {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString("en-US");
};

const monthlyCost = (price, period) => {
  if (price == null || Number.isNaN(price)) return 0;
  switch (period) {
    case "MONTHLY":
      return price;
    case "QUARTERLY":
      return price / 3;
    case "BIANNUAL":
      return price / 6;
    case "YEARLY":
      return price / 12;
    default:
      return price;
  }
};

const flipUpdate = (el, value) => {
  if (el.textContent === value) return;
  el.classList.remove("flip");
  void el.offsetWidth;
  el.textContent = value;
  el.classList.add("flip");
};

const updateStats = (items) => {
  const count = items.length;
  const totalMonthly = items.reduce(
    (sum, item) => sum + monthlyCost(Number(item.price), item.period),
    0
  );
  const avg = count === 0 ? 0 : totalMonthly / count;
  flipUpdate(toolCount, String(count));
  flipUpdate(avgMonthly, `$${avg.toFixed(2)}`);
};

const renderList = (items) => {
  totalCount.textContent = items.length.toString();
  updateStats(items);

  if (!items.length) {
    list.innerHTML = `<div class="card"><h3>NO SUBSCRIPTIONS</h3><p>ADD YOUR FIRST TOOL TO SEE IT HERE.</p></div>`;
    return;
  }

  list.innerHTML = items
    .map(
      (item) => `
      <div class="card">
        <h3>${(item.name || "UNTITLED").toUpperCase()}</h3>
        <p>${item.currency || ""} ${item.price ?? "-"}</p>
        <p>START: ${formatDate(item.nextBillingDate)}</p>
        <p>PERIOD: ${item.period || "-"}</p>
        <p>STATUS: ${item.status || "-"}</p>
      </div>
    `
    )
    .join("");
};

const fetchSubscriptions = async () => {
  try {
    const res = await fetch("/api/subscriptions", {
      headers: { ...authHeaders() },
    });
    if (res.status === 401) {
      list.innerHTML = `<div class="card"><h3>AUTH REQUIRED</h3><p>PLEASE LOG IN TO VIEW SUBSCRIPTIONS.</p></div>`;
      updateStats([]);
      return;
    }
    if (!res.ok) throw new Error("UNABLE TO LOAD SUBSCRIPTIONS");
    const data = await res.json();
    renderList(data);
  } catch (err) {
    list.innerHTML = `<div class="card"><h3>LOAD FAILED</h3><p>${err.message}</p></div>`;
    updateStats([]);
  }
};

const submitAuth = async (endpoint) => {
  authMessage.textContent = "PROCESSING...";
  const formData = new FormData(authForm);
  const payload = Object.fromEntries(formData.entries());
  try {
    const res = await fetch(`/api/auth/${endpoint}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || "AUTH FAILED");
    if (data.token) {
      setToken(data.token);
      authMessage.textContent = "AUTHENTICATED.";
      updateAuthStatusFromToken();
      await fetchSubscriptions();
    } else if (data.message) {
      authMessage.textContent = data.message.toUpperCase();
    }
  } catch (err) {
    authMessage.textContent = err.message;
    if (endpoint === "login") {
      setAuthStatus("FAILED TO LOG IN", true);
    }
  }
};

registerBtn.addEventListener("click", () => submitAuth("register"));
loginBtn.addEventListener("click", () => submitAuth("login"));
logoutBtn.addEventListener("click", () => {
  clearToken();
  authMessage.textContent = "LOGGED OUT.";
  updateAuthStatusFromToken();
  fetchSubscriptions();
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  formMessage.textContent = "SUBMITTING...";

  const formData = new FormData(form);
  const payload = Object.fromEntries(formData.entries());
  payload.price = Number(payload.price);
  payload.noticeDays = Number(payload.noticeDays);

  try {
    const res = await fetch("/api/subscriptions", {
      method: "POST",
      headers: { "Content-Type": "application/json", ...authHeaders() },
      body: JSON.stringify(payload),
    });
    if (res.status === 401) throw new Error("PLEASE LOG IN FIRST.");
    if (!res.ok) throw new Error("CREATE FAILED.");
    form.reset();
    formMessage.textContent = "SUBSCRIPTION ADDED.";
    await fetchSubscriptions();
  } catch (err) {
    formMessage.textContent = err.message;
  }
});

const urlParams = new URLSearchParams(window.location.search);
const oauthToken = urlParams.get("token");
if (oauthToken) {
  setToken(oauthToken);
  authMessage.textContent = "AUTHENTICATED VIA GOOGLE.";
  updateAuthStatusFromToken();
  const cleanUrl = window.location.origin + window.location.pathname;
  window.history.replaceState({}, document.title, cleanUrl);
}

updateAuthStatusFromToken();
fetchSubscriptions();
