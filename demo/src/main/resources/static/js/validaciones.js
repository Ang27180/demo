// validaciones.js
document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("form");

    form.addEventListener("submit", function (e) {
        let valido = true;

        // --- Campos ---
        const nombre = document.querySelector("input[th\\:field='*{nombre}']");
        const email = document.querySelector("input[th\\:field='*{email}']");
        const contrasena = document.getElementById("contrasena");
        const confirmar = document.getElementById("confirmar");
        const terminos = document.getElementById("terminos");

        // --- Elementos de error ---
        const errorNombre = nombre.nextElementSibling;
        const errorEmail = email.nextElementSibling;
        const errorConfirmar = document.getElementById("error-confirmar");
        const errorTerminos = document.getElementById("error-terminos");

        // --- Validar Nombre ---
        const nombrePattern = /^[A-Za-zÁÉÍÓÚáéíóúñÑ\s]{1,30}$/;
        if (!nombre.value.trim()) {
            errorNombre.textContent = "El nombre es obligatorio.";
            valido = false;
        } else if (!nombrePattern.test(nombre.value.trim())) {
            errorNombre.textContent = "Solo letras y espacios, máximo 30 caracteres.";
            valido = false;
        } else {
            errorNombre.textContent = "";
        }

        // --- Validar Email ---
        if (!email.value.trim()) {
            errorEmail.textContent = "El correo es obligatorio.";
            valido = false;
        } else if (!/^\S+@\S+\.\S+$/.test(email.value.trim())) {
            errorEmail.textContent = "Correo no válido.";
            valido = false;
        } else {
            errorEmail.textContent = "";
        }

        // --- Validar Contraseña ---
        const contrasenaPattern = /^(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*.,?]).{8,10}$/;
        if (!contrasena.value) {
            // Si está vacío
            contrasena.setCustomValidity("La contraseña es obligatoria.");
            valido = false;
        } else if (!contrasenaPattern.test(contrasena.value)) {
            contrasena.setCustomValidity("Entre 8-10 caracteres, al menos 1 mayúscula, 1 número y 1 símbolo.");
            valido = false;
        } else {
            contrasena.setCustomValidity("");
        }

        // --- Validar Confirmar Contraseña ---
        if (contrasena.value !== confirmar.value) {
            errorConfirmar.textContent = "Las contraseñas no coinciden.";
            valido = false;
        } else {
            errorConfirmar.textContent = "";
        }

        // --- Validar Términos ---
        if (!terminos.checked) {
            errorTerminos.textContent = "Debe aceptar los términos y condiciones.";
            valido = false;
        } else {
            errorTerminos.textContent = "";
        }

        if (!valido) {
            e.preventDefault(); // Bloquea envío si hay errores
        }
    });
});
