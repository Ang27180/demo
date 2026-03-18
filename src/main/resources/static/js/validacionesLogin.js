// Espera a que se cargue la página
document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("form");
    const emailInput = document.querySelector("#email");
    const passwordInput = document.querySelector("#contrasena");

    form.addEventListener("submit", function (e) {
        let errores = [];

        // Validar email
        const emailValue = emailInput.value.trim();
        if (emailValue === "") {
            errores.push("El correo es obligatorio.");
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailValue)) {
            errores.push("El correo no es válido.");
        }

        // Validar contraseña
        const passwordValue = passwordInput.value.trim();
        if (passwordValue === "") {
            errores.push("La contraseña es obligatoria.");
        } else if (passwordValue.length < 6) {
            errores.push("La contraseña debe tener al menos 6 caracteres.");
        }

        // Si hay errores, prevenir envío y mostrarlos
        if (errores.length > 0) {
            e.preventDefault();

            // Limpiar errores anteriores
            let errorDiv = document.querySelector("#erroresLogin");
            if (!errorDiv) {
                errorDiv = document.createElement("div");
                errorDiv.id = "erroresLogin";
                errorDiv.style.color = "red";
                errorDiv.style.marginTop = "10px";
                form.appendChild(errorDiv);
            }
            errorDiv.innerHTML = errores.join("<br>");
        }
    });
});
