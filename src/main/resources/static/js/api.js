const Api = (() => {
  const wait = (ms = 450) => new Promise((resolve) => setTimeout(resolve, ms));

  const bikes = [
    { id: "BK-1001", type: "electric", rate: 650, status: "available", range: "74 km" },
    { id: "BK-1002", type: "city", rate: 250, status: "available", range: "Manual" },
    { id: "BK-1003", type: "mountain", rate: 420, status: "in-use", range: "Manual" },
    { id: "BK-1004", type: "electric", rate: 720, status: "available", range: "92 km" },
    { id: "BK-1005", type: "city", rate: 180, status: "available", range: "Manual" },
    { id: "BK-1006", type: "mountain", rate: 500, status: "in-use", range: "Manual" },
    { id: "BK-1007", type: "electric", rate: 850, status: "available", range: "110 km" },
    { id: "BK-1008", type: "city", rate: 300, status: "available", range: "Manual" }
  ];

  const rides = [
    { id: "RD-410", from: "Malabe", to: "Colombo Fort", time: "Today, 5:30 PM", seats: 2, price: 450 },
    { id: "RD-411", from: "Kaduwela", to: "SLIIT Campus", time: "Tomorrow, 8:00 AM", seats: 3, price: 250 },
    { id: "RD-412", from: "Battaramulla", to: "Nugegoda", time: "Fri, 6:15 PM", seats: 1, price: 350 },
    { id: "RD-413", from: "Rajagiriya", to: "Bambalapitiya", time: "Sat, 10:00 AM", seats: 4, price: 520 }
  ];

  const transactions = [
    { date: "2026-05-20", type: "Rental", amount: 1300, status: "paid" },
    { date: "2026-05-18", type: "Ride", amount: 450, status: "paid" },
    { date: "2026-05-14", type: "Wallet Top Up", amount: 5000, status: "paid" },
    { date: "2026-05-12", type: "Rental", amount: 720, status: "pending" },
    { date: "2026-05-08", type: "Ride", amount: 300, status: "failed" }
  ];

  const notifications = [
    { id: 1, icon: "fa-bicycle", title: "Bike BK-1001 is ready", description: "Your reserved electric bike is unlocked at Zone A.", timestamp: "4 min ago", unread: true },
    { id: 2, icon: "fa-credit-card", title: "Payment received", description: "LKR 1,300 rental payment was completed successfully.", timestamp: "1 hr ago", unread: true },
    { id: 3, icon: "fa-route", title: "Ride match found", description: "A shared ride from Malabe to Colombo Fort has 2 open seats.", timestamp: "Today", unread: false },
    { id: 4, icon: "fa-bell", title: "Rental reminder", description: "Return BK-1004 by 8:00 PM to avoid overtime charges.", timestamp: "Yesterday", unread: true }
  ];

  const activity = [
    { icon: "fa-bicycle", title: "Rented BK-1001", meta: "Electric bike, 2 hours ago" },
    { icon: "fa-route", title: "Joined ride RD-410", meta: "Malabe to Colombo Fort" },
    { icon: "fa-wallet", title: "Wallet topped up", meta: "LKR 5,000 added" },
    { icon: "fa-check", title: "Returned BK-1005", meta: "City bike, no overdue fees" },
    { icon: "fa-star", title: "Rated shared ride", meta: "5 stars submitted" }
  ];

  async function fetchBikes() {
    // TODO: Replace with real API endpoint -> GET /api/bikes
    await wait();
    return [...bikes];
  }

  async function fetchRides() {
    // TODO: Replace with real API endpoint -> GET /api/rideshares
    await wait(300);
    return [...rides];
  }

  async function createRide(payload) {
    // TODO: Replace with real API endpoint -> POST /api/rideshares
    await wait(350);
    return { id: `RD-${Math.floor(Math.random() * 900 + 100)}`, ...payload };
  }

  async function createRental(payload) {
    // TODO: Replace with real API endpoint -> POST /api/rentals
    await wait(350);
    return { id: `RN-${Date.now()}`, ...payload };
  }

  async function fetchTransactions() {
    // TODO: Replace with real API endpoint -> GET /api/payments
    await wait();
    return [...transactions];
  }

  async function savePaymentMethod(payload) {
    // TODO: Replace with real API endpoint -> POST /api/payments/methods
    await wait(350);
    return { saved: true, last4: payload.cardNumber.slice(-4) };
  }

  async function fetchNotifications() {
    // TODO: Replace with real API endpoint -> GET /api/notifications
    await wait(250);
    return [...notifications];
  }

  async function markNotificationsRead() {
    // TODO: Replace with real API endpoint -> PATCH /api/notifications/read
    await wait(250);
    notifications.forEach((item) => {
      item.unread = false;
    });
    return [...notifications];
  }

  async function fetchActivity() {
    // TODO: Replace with real API endpoint -> GET /api/activity
    await wait(250);
    return [...activity];
  }

  async function updateProfile(payload) {
    // TODO: Replace with real API endpoint -> PUT /api/users/me
    await wait(350);
    return payload;
  }

  return {
    fetchBikes,
    fetchRides,
    createRide,
    createRental,
    fetchTransactions,
    savePaymentMethod,
    fetchNotifications,
    markNotificationsRead,
    fetchActivity,
    updateProfile
  };
})();
