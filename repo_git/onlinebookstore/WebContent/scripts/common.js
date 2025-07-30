fetch("navbar.html")
  .then((response) => response.text())
  .then((data) => {
    const el = document.getElementById("navbar-placeholder");
    if (el) el.innerHTML = data;
  })
  .catch((error) => console.error("Errore nel caricamento della navbar:", error));

fetch("external-scripts.html")
  .then((res) => res.text())
  .then((data) => {
    const el = document.getElementById("external-scripts");
    if (el) el.innerHTML = data;
  });

document.addEventListener("DOMContentLoaded", () => {
  const loding = document.getElementById("loding");
  if (loding) loding.style.display = "none";
});
