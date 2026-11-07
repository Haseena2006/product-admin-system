document.addEventListener('DOMContentLoaded', function () {

    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm(form.getAttribute('data-confirm'))) {
                event.preventDefault();
            }
        });
    });

    var toggle = document.querySelector('[data-nav-toggle]');
    var links = document.getElementById('nav-links');
    if (toggle && links) {
        toggle.addEventListener('click', function () {
            var open = links.classList.toggle('open');
            toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
        });
    }

    document.querySelectorAll('.alert-success').forEach(function (alertBox) {
        setTimeout(function () {
            alertBox.style.transition = 'opacity .4s';
            alertBox.style.opacity = '0';
            setTimeout(function () { alertBox.remove(); }, 450);
        }, 5000);
    });
});
