const App = (() => {
  const titles = {
    dashboard: "Dashboard",
    rentals: "Rent a Bike",
    rides: "Ride Sharing",
    payments: "Payments",
    notifications: "Notifications",
    profile: "User Profile"
  };

  const state = {
    bikes: [],
    rides: [],
    transactions: [],
    notifications: [],
    route: "dashboard",
    user: null
  };

  const qs = (selector) => document.querySelector(selector);
  const qsa = (selector) => [...document.querySelectorAll(selector)];

  function init() {
    bindAuth();
    bindNavigation();
    bindInteractions();
    restoreSession();
  }

  function restoreSession() {
    const savedUser = localStorage.getItem("voltRideUser");
    if (savedUser) {
      state.user = JSON.parse(savedUser);
      showApp();
    } else {
      qs("#auth-screen").classList.remove("hidden");
      qs("#app-shell").classList.add("hidden");
    }
  }

  function bindAuth() {
    qsa("[data-auth-tab]").forEach((tab) => {
      tab.addEventListener("click", () => {
        qsa("[data-auth-tab]").forEach((item) => item.classList.remove("is-active"));
        tab.classList.add("is-active");
        qs("#login-form").classList.toggle("hidden", tab.dataset.authTab !== "login");
        qs("#register-form").classList.toggle("hidden", tab.dataset.authTab !== "register");
      });
    });

    qs("#login-form").addEventListener("submit", (event) => {
      event.preventDefault();
      if (!UI.validateForm(event.currentTarget)) return;
      const email = new FormData(event.currentTarget).get("email");
      login({ name: "App Rider", email, phone: "+94 77 123 4567", address: "Malabe, Sri Lanka" });
      UI.toast("Welcome back to VoltRide.", "success");
    });

    qs("#register-form").addEventListener("submit", (event) => {
      event.preventDefault();
      if (!UI.validateForm(event.currentTarget)) return;
      const data = Object.fromEntries(new FormData(event.currentTarget));
      login({ name: data.name, email: data.email, phone: "", address: "" });
      UI.toast("Account created. Your dashboard is ready.", "success");
    });
  }

  function login(user) {
    state.user = user;
    localStorage.setItem("voltRideUser", JSON.stringify(user));
    showApp();
  }

  function logout() {
    localStorage.removeItem("voltRideUser");
    state.user = null;
    qs("#auth-screen").classList.remove("hidden");
    qs("#app-shell").classList.add("hidden");
    UI.toast("Logged out successfully.", "info");
  }

  async function showApp() {
    qs("#auth-screen").classList.add("hidden");
    qs("#app-shell").classList.remove("hidden");
    hydrateUser();
    await Promise.all([loadActivity(), loadBikes(), loadRides(), loadPayments(), loadNotifications()]);
    navigate(location.hash.replace("#", "") || "dashboard");
  }

  function hydrateUser() {
    const user = state.user || {};
    const initials = UI.initials(user.name);
    const profileForm = qs("#profile-form");
    qs("#avatar-initials").textContent = initials;
    qs("#profile-avatar").textContent = initials;
    profileForm.elements.name.value = user.name || "";
    profileForm.elements.email.value = user.email || "";
    profileForm.elements.phone.value = user.phone || "";
    profileForm.elements.address.value = user.address || "";
  }

  function bindNavigation() {
    document.addEventListener("click", (event) => {
      const routeButton = event.target.closest("[data-route]");
      if (routeButton) {
        event.preventDefault();
        navigate(routeButton.dataset.route);
      }
    });

    qs("#sidebar-toggle").addEventListener("click", () => {
      qs("#sidebar").classList.toggle("is-collapsed");
    });

    qs("#logout-btn").addEventListener("click", logout);
    qs("#dropdown-logout").addEventListener("click", logout);
    qs("#profile-menu-btn").addEventListener("click", () => qs("#profile-dropdown").classList.toggle("is-open"));

    window.addEventListener("hashchange", () => {
      navigate(location.hash.replace("#", "") || "dashboard");
    });
  }

  function navigate(route) {
    if (!titles[route]) route = "dashboard";
    state.route = route;
    location.hash = route;
    qsa(".view").forEach((view) => view.classList.toggle("is-active", view.id === route));
    qsa(".nav-link, .bottom-nav button").forEach((link) => link.classList.toggle("is-active", link.dataset.route === route));
    qs("#page-title").textContent = titles[route];
    qs("#profile-dropdown").classList.remove("is-open");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function bindInteractions() {
    qs("#bike-type-filter").addEventListener("change", renderBikes);
    qs("#bike-status-filter").addEventListener("change", renderBikes);
    qs("#bike-price-filter").addEventListener("input", (event) => {
      qs("#price-label").textContent = UI.money(event.target.value);
      renderBikes();
    });

    qsa("[data-ride-tab]").forEach((tab) => {
      tab.addEventListener("click", () => {
        qsa("[data-ride-tab]").forEach((item) => item.classList.remove("is-active"));
        tab.classList.add("is-active");
        qs("#find-ride-pane").classList.toggle("hidden", tab.dataset.rideTab !== "find");
        qs("#offer-ride-form").classList.toggle("hidden", tab.dataset.rideTab !== "offer");
      });
    });

    qs("#offer-ride-form").addEventListener("submit", submitRide);
    qs("#card-form").addEventListener("submit", saveCard);
    qs("#payment-status-filter").addEventListener("change", renderTransactions);
    qs("#payment-from").addEventListener("change", renderTransactions);
    qs("#payment-to").addEventListener("change", renderTransactions);
    qs("#mark-read-btn").addEventListener("click", markAllRead);
    qs("#profile-form").addEventListener("submit", saveProfile);
    qs("#top-up-btn").addEventListener("click", () => UI.toast("Top up flow is ready for payment gateway integration.", "info"));
  }

  async function loadActivity() {
    const activity = await Api.fetchActivity();
    qs("#activity-list").innerHTML = activity.map((item) => `
      <li class="activity-item">
        <i class="fa-solid ${item.icon}"></i>
        <div><strong>${item.title}</strong><p>${item.meta}</p></div>
      </li>
    `).join("");
  }

  async function loadBikes() {
    UI.setSkeleton(qs("#bike-skeleton"), 8);
    state.bikes = await Api.fetchBikes();
    UI.hideSkeleton(qs("#bike-skeleton"));
    renderBikes();
  }

  function renderBikes() {
    const type = qs("#bike-type-filter").value;
    const status = qs("#bike-status-filter").value;
    const maxPrice = Number(qs("#bike-price-filter").value);
    const bikes = state.bikes.filter((bike) =>
      (type === "all" || bike.type === type) &&
      (status === "all" || bike.status === status) &&
      bike.rate <= maxPrice
    );

    qs("#bike-grid").innerHTML = bikes.map((bike) => `
      <article class="bike-card">
        <div class="bike-art"><i class="fa-solid fa-bicycle"></i></div>
        <div class="bike-meta">
          <div><small>Bike ID</small><h3>${bike.id}</h3></div>
          <span class="badge-soft ${bike.status}">${bike.status}</span>
        </div>
        <div class="bike-meta">
          <span>${capitalize(bike.type)} bike</span>
          <strong>${UI.money(bike.rate)}/hr</strong>
        </div>
        <small>Range: ${bike.range}</small>
        <button class="btn btn-primary" ${bike.status !== "available" ? "disabled" : ""} data-rent-bike="${bike.id}">Rent Now</button>
      </article>
    `).join("");

    qsa("[data-rent-bike]").forEach((button) => {
      button.addEventListener("click", () => openRentalModal(button.dataset.rentBike));
    });
  }

  function openRentalModal(bikeId) {
    const bike = state.bikes.find((item) => item.id === bikeId);
    UI.openModal(`
      <h3>Confirm Rental</h3>
      <p style="color: var(--muted); margin-top: .4rem;">${bike.id} - ${capitalize(bike.type)} bike at ${UI.money(bike.rate)} per hour</p>
      <label class="field" style="margin-top: 1rem;">Duration in hours
        <input id="rental-duration" type="number" min="1" max="24" value="1">
        <small class="error-message"></small>
      </label>
      <div class="wallet-card" style="min-height: 120px; margin-top: 1rem;">
        <p>Estimated Cost</p>
        <strong id="rental-estimate">${UI.money(bike.rate)}</strong>
      </div>
      <div class="modal-actions">
        <button class="btn btn-ghost" type="button" data-close-modal>Cancel</button>
        <button class="btn btn-primary" type="button" id="confirm-rental">Confirm Rental</button>
      </div>
    `);

    const duration = qs("#rental-duration");
    duration.addEventListener("input", () => {
      qs("#rental-estimate").textContent = UI.money(Math.max(1, Number(duration.value || 1)) * bike.rate);
    });

    qs("#confirm-rental").addEventListener("click", async () => {
      await Api.createRental({ bikeId, duration: Number(duration.value), estimatedCost: Number(duration.value) * bike.rate });
      UI.closeModal();
      UI.toast(`Rental confirmed for ${bike.id}.`, "success");
    });
  }

  async function loadRides() {
    state.rides = await Api.fetchRides();
    renderRides();
  }

  function renderRides() {
    qs("#ride-list").innerHTML = state.rides.map((ride) => `
      <article class="ride-card">
        <div>
          <div class="ride-route">${ride.from} <i class="fa-solid fa-arrow-right"></i> ${ride.to}</div>
          <small>${ride.id}</small>
        </div>
        <div><small>Time</small><strong>${ride.time}</strong></div>
        <div><small>Seats</small><strong>${ride.seats}</strong></div>
        <button class="btn btn-secondary" data-join-ride="${ride.id}">${UI.money(ride.price)} Join Ride</button>
      </article>
    `).join("");

    qsa("[data-join-ride]").forEach((button) => {
      button.addEventListener("click", () => UI.toast(`Joined ride ${button.dataset.joinRide}.`, "success"));
    });
  }

  async function submitRide(event) {
    event.preventDefault();
    if (!UI.validateForm(event.currentTarget)) return;
    const data = Object.fromEntries(new FormData(event.currentTarget));
    const ride = await Api.createRide({
      from: data.pickup,
      to: data.dropoff,
      time: new Date(data.dateTime).toLocaleString(),
      seats: data.seats,
      price: Number(data.price)
    });
    state.rides.unshift(ride);
    renderRides();
    event.currentTarget.reset();
    UI.toast("Ride offer posted.", "success");
  }

  async function loadPayments() {
    qs("#payment-skeleton").classList.remove("hidden");
    state.transactions = await Api.fetchTransactions();
    qs("#payment-skeleton").classList.add("hidden");
    renderTransactions();
  }

  function renderTransactions() {
    const status = qs("#payment-status-filter").value;
    const from = qs("#payment-from").value;
    const to = qs("#payment-to").value;
    const rows = state.transactions.filter((txn) => {
      const validStatus = status === "all" || txn.status === status;
      const afterFrom = !from || txn.date >= from;
      const beforeTo = !to || txn.date <= to;
      return validStatus && afterFrom && beforeTo;
    });

    qs("#transactions-body").innerHTML = rows.map((txn) => `
      <tr>
        <td>${txn.date}</td>
        <td>${txn.type}</td>
        <td>${UI.money(txn.amount)}</td>
        <td><span class="badge-soft ${txn.status}">${txn.status}</span></td>
      </tr>
    `).join("");
  }

  async function saveCard(event) {
    event.preventDefault();
    if (!UI.validateForm(event.currentTarget)) return;
    const data = Object.fromEntries(new FormData(event.currentTarget));
    await Api.savePaymentMethod(data);
    event.currentTarget.reset();
    UI.toast("Payment method saved.", "success");
  }

  async function loadNotifications() {
    state.notifications = await Api.fetchNotifications();
    renderNotifications();
  }

  function renderNotifications() {
    const unread = state.notifications.filter((item) => item.unread).length;
    qs("#topbar-badge").textContent = unread;
    qs("#topbar-badge").classList.toggle("hidden", unread === 0);
    qs("#empty-notifications").classList.toggle("hidden", state.notifications.length > 0);
    qs("#notification-list").innerHTML = state.notifications.map((item) => `
      <article class="notification-card ${item.unread ? "unread" : ""}">
        <span class="notification-icon"><i class="fa-solid ${item.icon}"></i></span>
        <div><h3>${item.title}</h3><p>${item.description}</p></div>
        <small>${item.timestamp}</small>
      </article>
    `).join("");
  }

  async function markAllRead() {
    state.notifications = await Api.markNotificationsRead();
    renderNotifications();
    UI.toast("All notifications marked as read.", "success");
  }

  async function saveProfile(event) {
    event.preventDefault();
    if (!UI.validateForm(event.currentTarget)) return;
    const data = Object.fromEntries(new FormData(event.currentTarget));
    state.user = { ...state.user, name: data.name, email: data.email, phone: data.phone, address: data.address };
    await Api.updateProfile(state.user);
    localStorage.setItem("voltRideUser", JSON.stringify(state.user));
    hydrateUser();
    UI.toast("Profile changes saved.", "success");
  }

  function capitalize(value) {
    return value.charAt(0).toUpperCase() + value.slice(1);
  }

  return { init };
})();

document.addEventListener("DOMContentLoaded", App.init);
