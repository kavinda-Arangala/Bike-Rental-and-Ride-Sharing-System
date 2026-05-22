const UI = (() => {
  const toastRoot = () => document.getElementById("toast-root");
  const modalRoot = () => document.getElementById("modal-root");

  function toast(message, type = "info") {
    const node = document.createElement("div");
    const icons = { success: "fa-circle-check", error: "fa-circle-exclamation", info: "fa-circle-info" };
    node.className = `toast ${type}`;
    node.innerHTML = `<i class="fa-solid ${icons[type] || icons.info}"></i><span>${message}</span>`;
    toastRoot().appendChild(node);
    setTimeout(() => node.remove(), 3600);
  }

  function openModal(content) {
    const root = modalRoot();
    root.classList.remove("hidden");
    root.setAttribute("aria-hidden", "false");
    root.innerHTML = `<div class="modal-card fade-in" role="dialog" aria-modal="true">${content}</div>`;
    root.querySelector("[data-close-modal]")?.focus();
  }

  function closeModal() {
    const root = modalRoot();
    root.classList.add("hidden");
    root.setAttribute("aria-hidden", "true");
    root.innerHTML = "";
  }

  function setSkeleton(container, count = 4) {
    container.innerHTML = Array.from({ length: count }, () => `<div class="skeleton-card"></div>`).join("");
    container.classList.remove("hidden");
  }

  function hideSkeleton(container) {
    container.classList.add("hidden");
    container.innerHTML = "";
  }

  function validateForm(form) {
    let isValid = true;
    const fields = [...form.querySelectorAll("input, select, textarea")];

    fields.forEach((field) => {
      const wrapper = field.closest(".field");
      const error = wrapper?.querySelector(".error-message");
      const value = field.value.trim();
      let message = "";

      if (field.required && !value) {
        message = "This field is required.";
      } else if (field.type === "email" && value && !/^\S+@\S+\.\S+$/.test(value)) {
        message = "Enter a valid email address.";
      } else if (field.minLength > 0 && value && value.length < field.minLength) {
        message = `Use at least ${field.minLength} characters.`;
      } else if (field.type === "number") {
        const numberValue = Number(value);
        const min = field.min ? Number(field.min) : null;
        const max = field.max ? Number(field.max) : null;
        if (value && min !== null && numberValue < min) message = `Minimum value is ${min}.`;
        if (value && max !== null && numberValue > max) message = `Maximum value is ${max}.`;
      }

      wrapper?.classList.toggle("has-error", Boolean(message));
      if (error) error.textContent = message;
      if (message) isValid = false;
    });

    return isValid;
  }

  function money(value) {
    return `LKR ${Number(value).toLocaleString("en-LK")}`;
  }

  function initials(name = "App Rider") {
    return name
      .split(" ")
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0].toUpperCase())
      .join("");
  }

  document.addEventListener("click", (event) => {
    if (event.target.id === "modal-root" || event.target.closest("[data-close-modal]")) {
      closeModal();
    }
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") closeModal();
  });

  return {
    toast,
    openModal,
    closeModal,
    setSkeleton,
    hideSkeleton,
    validateForm,
    money,
    initials
  };
})();
