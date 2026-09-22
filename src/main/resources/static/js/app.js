const API = "/api";

let state = {
    email: localStorage.getItem("tracker_email") || "",
    signupDate: null,
    entries: {},
    viewYear: null,
    viewMonth: null, // 0-based
};

const STATUS_CYCLE = ["DONE", "TRIED", "NOT_DONE"];

const el = (id) => document.getElementById(id);

function showScreen(name) {
    ["signup-screen", "verify-screen", "calendar-screen"].forEach((id) => {
        el(id).classList.toggle("hidden", id !== name);
    });
}

async function checkVerifiedAndEnter(email) {
    const res = await fetch(`${API}/auth/status?email=${encodeURIComponent(email)}`);
    const data = await res.json();
    if (data.verified) {
        state.email = email;
        localStorage.setItem("tracker_email", email);
        await loadCalendar();
        return true;
    }
    return false;
}

async function handleSignup() {
    const email = el("email-input").value.trim();
    if (!email) return;

    const res = await fetch(`${API}/auth/signup`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
    });
    const data = await res.json();

    if (!res.ok) {
        el("signup-message").textContent = data.error || "Something went wrong.";
        return;
    }

    if (data.verified) {
        state.email = email;
        localStorage.setItem("tracker_email", email);
        await loadCalendar();
    } else {
        state.email = email;
        el("verify-message").textContent = data.message;
        showScreen("verify-screen");
    }
}

async function handleCheckVerified() {
    const ok = await checkVerifiedAndEnter(state.email);
    if (!ok) {
        el("verify-message").textContent = "Not verified yet — click the link in your email first.";
    }
}

async function loadCalendar() {
    const res = await fetch(`${API}/calendar?email=${encodeURIComponent(state.email)}`);
    if (!res.ok) {
        // fall back to signup screen if something is off
        showScreen("signup-screen");
        return;
    }
    const data = await res.json();
    applyCalendarResponse(data);

    const today = new Date();
    state.viewYear = today.getFullYear();
    state.viewMonth = today.getMonth();

    el("user-email").textContent = state.email;
    showScreen("calendar-screen");
    renderMonth();
}

function applyCalendarResponse(data) {
    state.signupDate = data.signupDate; // "yyyy-MM-dd"
    state.entries = data.entries || {};
    el("stat-done").textContent = data.stats.done;
    el("stat-tried").textContent = data.stats.tried;
    el("stat-notdone").textContent = data.stats.notDone;
}

function toISODate(y, m, d) {
    const mm = String(m + 1).padStart(2, "0");
    const dd = String(d).padStart(2, "0");
    return `${y}-${mm}-${dd}`;
}

function renderMonth() {
    const { viewYear, viewMonth } = state;
    const monthNames = ["January","February","March","April","May","June",
        "July","August","September","October","November","December"];
    el("month-label").textContent = `${monthNames[viewMonth]} ${viewYear}`;

    const grid = el("calendar-grid");
    grid.innerHTML = "";

    const firstDay = new Date(viewYear, viewMonth, 1).getDay();
    const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();

    const signup = state.signupDate; // string yyyy-MM-dd, comparable lexically
    const todayISO = toISODate(new Date().getFullYear(), new Date().getMonth(), new Date().getDate());

    for (let i = 0; i < firstDay; i++) {
        const empty = document.createElement("div");
        empty.className = "day-cell empty";
        grid.appendChild(empty);
    }

    for (let d = 1; d <= daysInMonth; d++) {
        const iso = toISODate(viewYear, viewMonth, d);
        const cell = document.createElement("div");
        cell.textContent = d;

        const beforeSignup = iso < signup;
        const afterToday = iso > todayISO;
        const locked = beforeSignup || afterToday;

        const status = state.entries[iso];
        cell.className = "day-cell" + (status ? ` ${status}` : "") + (locked ? " locked" : "");

        if (!locked) {
            cell.addEventListener("click", () => cycleDay(iso, status));
        }
        grid.appendChild(cell);
    }
}

async function cycleDay(iso, currentStatus) {
    // No entry yet -> first click sets DONE. Otherwise step to the next status.
    const currentIndex = currentStatus ? STATUS_CYCLE.indexOf(currentStatus) : -1;
    const next = STATUS_CYCLE[(currentIndex + 1) % STATUS_CYCLE.length];
    await saveDay(iso, next);
}

async function saveDay(iso, status) {
    const res = await fetch(`${API}/calendar/entry`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: state.email, date: iso, status }),
    });
    if (!res.ok) return;
    const data = await res.json();
    applyCalendarResponse(data);
    renderMonth();
}

function changeMonth(delta) {
    let m = state.viewMonth + delta;
    let y = state.viewYear;
    if (m < 0) { m = 11; y--; }
    if (m > 11) { m = 0; y++; }
    state.viewMonth = m;
    state.viewYear = y;
    renderMonth();
}

el("signup-btn").addEventListener("click", handleSignup);
el("check-verified-btn").addEventListener("click", handleCheckVerified);
el("prev-month").addEventListener("click", () => changeMonth(-1));
el("next-month").addEventListener("click", () => changeMonth(1));

// Handle redirect back from the email verification link (/?verified=true)
(async function init() {
    const params = new URLSearchParams(window.location.search);
    const verifiedParam = params.get("verified");

    if (verifiedParam !== null) {
        window.history.replaceState({}, "", "/");
    }

    if (state.email) {
        const ok = await checkVerifiedAndEnter(state.email);
        if (ok) return;
        if (verifiedParam === "true") {
            await loadCalendar();
            return;
        }
    }
    showScreen("signup-screen");
})();
