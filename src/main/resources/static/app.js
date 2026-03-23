const form = document.querySelector("#subscriptionForm");
const formMessage = document.querySelector("#formMessage");
const verifyForm = document.querySelector("#verifyForm");
const verifyMessage = document.querySelector("#verifyMessage");
const authForm = document.querySelector("#authForm");
const authMessage = document.querySelector("#authMessage");
const memberStatus = document.querySelector("#memberStatus");
const registerBtn = document.querySelector("#registerBtn");
const loginBtn = document.querySelector("#loginBtn");
const logoutBtn = document.querySelector("#logoutBtn");
const platformListEl = document.querySelector("#platformList");
const customPlatformInput = document.querySelector("#customPlatform");
const addPlatformBtn = document.querySelector("#addPlatformBtn");
const list = document.querySelector("#subscriptions");
const totalCount = document.querySelector("#totalCount");

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

const updateMemberStatus = () => {
  const token = getToken();
  if (!token) {
    memberStatus.textContent = "Not signed in";
    return;
  }
  const payload = decodeJwt(token);
  if (!payload || !payload.sub) {
    memberStatus.textContent = "Signed in";
    return;
  }
  memberStatus.textContent = `Signed in as ${payload.sub}`;
};

const defaultPlatforms = [
  "Netflix",
  "Spotify",
  "YouTube Premium",
  "Apple Music",
  "Disney+",
  "HBO Max",
  "Amazon Prime",
  "Notion",
  "ChatGPT Plus",
  "Adobe Creative Cloud",
];

const platformState = defaultPlatforms.map((name) => ({ name, selected: false }));

const formatDate = (value) => {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString("en-US");
};

const renderList = (items) => {
  totalCount.textContent = items.length.toString();
  if (!items.length) {
    list.innerHTML = `<div class="card"><h3>No subscriptions yet</h3><p>Create your first entry to see it here.</p></div>`;
    return;
  }

  list.innerHTML = items
    .map(
      (item) => `
      <div class="card">
        <h3>${item.name || "Untitled"}</h3>
        <p>Email: ${item.email || "-"}</p>
        <p>Price: ${item.currency || ""} ${item.price ?? "-"}</p>
        <p>Next billing: ${formatDate(item.nextBillingDate)}</p>
        <p>Period: ${item.period || "-"}</p>
        <p>Reminder: ${item.noticeDays ?? "-"} days before</p>
        <p>Status: ${item.status || "-"}</p>
        <p>Last update: ${item.lastStatusAt ? formatDate(item.lastStatusAt) : "-"}</p>
        <p>Note: ${item.lastStatusNote || "-"}</p>
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
      list.innerHTML = `<div class="card"><h3>Authentication required</h3><p>Please log in to view your subscriptions.</p></div>`;
      return;
    }
    if (!res.ok) throw new Error("Unable to load subscriptions");
    const data = await res.json();
    renderList(data);
  } catch (err) {
    list.innerHTML = `<div class="card"><h3>Load failed</h3><p>${err.message}</p></div>`;
  }
};

const renderPlatforms = () => {
  platformListEl.innerHTML = platformState
    .map(
      (p, idx) => `
        <label class="chip">
          <input type="checkbox" data-index="${idx}" ${p.selected ? "checked" : ""}/>
          <span>${p.name}</span>
        </label>
      `
    )
    .join("");
};

platformListEl.addEventListener("change", (event) => {
  const input = event.target;
  if (!input || input.dataset.index === undefined) return;
  const index = Number(input.dataset.index);
  platformState[index].selected = input.checked;
});

addPlatformBtn.addEventListener("click", () => {
  const value = customPlatformInput.value.trim();
  if (!value) return;
  const existingIndex = platformState.findIndex(
    (p) => p.name.toLowerCase() === value.toLowerCase()
  );
  if (existingIndex >= 0) {
    platformState[existingIndex].selected = true;
  } else {
    platformState.push({ name: value, selected: true });
  }
  customPlatformInput.value = "";
  renderPlatforms();
});

verifyForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  verifyMessage.textContent = "Sending...";

  const email = new FormData(verifyForm).get("email");
  const platforms = platformState.filter((p) => p.selected).map((p) => p.name);
  if (!platforms.length) {
    verifyMessage.textContent = "Please select at least one platform.";
    return;
  }

  try {
    const res = await fetch("/api/verify/request", {
      method: "POST",
      headers: { "Content-Type": "application/json", ...authHeaders() },
      body: JSON.stringify({ email, platforms }),
    });
    if (res.status === 401) throw new Error("Please log in first.");
    if (!res.ok) throw new Error("Failed to send. Please try again.");
    verifyMessage.textContent = "Verification sent. Please check your inbox.";
  } catch (err) {
    verifyMessage.textContent = err.message;
  }
});

const submitAuth = async (endpoint) => {
  authMessage.textContent = "Processing...";
  const formData = new FormData(authForm);
  const payload = Object.fromEntries(formData.entries());
  try {
    const res = await fetch(`/api/auth/${endpoint}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || "Authentication failed");
    if (data.token) {
      setToken(data.token);
      authMessage.textContent = "Authenticated.";
      updateMemberStatus();
      await fetchSubscriptions();
    } else if (data.message) {
      authMessage.textContent = data.message;
    }
  } catch (err) {
    authMessage.textContent = err.message;
  }
};

registerBtn.addEventListener("click", () => submitAuth("register"));
loginBtn.addEventListener("click", () => submitAuth("login"));
logoutBtn.addEventListener("click", () => {
  clearToken();
  authMessage.textContent = "Logged out.";
  updateMemberStatus();
  fetchSubscriptions();
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  formMessage.textContent = "Submitting...";

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
    if (res.status === 401) throw new Error("Please log in first.");
    if (!res.ok) throw new Error("Create failed. Please try again.");
    form.reset();
    formMessage.textContent = "Subscription created.";
    await fetchSubscriptions();
  } catch (err) {
    formMessage.textContent = err.message;
  }
});

renderPlatforms();
fetchSubscriptions();
updateMemberStatus();

const urlParams = new URLSearchParams(window.location.search);
const oauthToken = urlParams.get("token");
if (oauthToken) {
  setToken(oauthToken);
  authMessage.textContent = "Authenticated via Google.";
  updateMemberStatus();
  const cleanUrl = window.location.origin + window.location.pathname;
  window.history.replaceState({}, document.title, cleanUrl);
  fetchSubscriptions();
}
