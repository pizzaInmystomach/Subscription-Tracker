const form = document.querySelector("#subscriptionForm");
const formMessage = document.querySelector("#formMessage");
const verifyForm = document.querySelector("#verifyForm");
const verifyMessage = document.querySelector("#verifyMessage");
const platformListEl = document.querySelector("#platformList");
const customPlatformInput = document.querySelector("#customPlatform");
const addPlatformBtn = document.querySelector("#addPlatformBtn");
const list = document.querySelector("#subscriptions");
const totalCount = document.querySelector("#totalCount");

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
    const res = await fetch("/api/subscriptions");
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
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, platforms }),
    });
    if (!res.ok) throw new Error("Failed to send. Please try again.");
    verifyMessage.textContent = "Verification sent. Please check your inbox.";
  } catch (err) {
    verifyMessage.textContent = err.message;
  }
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
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
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

const params = new URLSearchParams(window.location.search);
if (params.get("gmail") === "connected") {
  verifyMessage.textContent = "Gmail connected successfully.";
}
