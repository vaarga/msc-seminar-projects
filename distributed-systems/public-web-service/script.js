const ipInput = document.getElementById('ip-input');

ipInput.addEventListener("keypress", (event) => {
    if (event.key === "Enter") {
        event.preventDefault();

        getInfoAboutIP();
    }
});

const getInfoAboutIP = () => {
    const IP = ipInput.value.trim();
    const errorMessage = document.getElementById('error-message');
    const organization = document.getElementById('organization');
    const country = document.getElementById('country');
    const currency = document.getElementById('currency');
    const time = document.getElementById('time');

    if (IP === '') {
        errorMessage.textContent = 'IP Address input can\'t be empty.';
    } else {
        errorMessage.textContent = '';

        fetch(`https://api.ipregistry.co/${IP}?key=etlpg2ejm6xigtq0&fields=connection,location,currency,time_zone`)
            .then((response) => {
                return response.json();
            })
            .then((payload) => {
                if (payload.resolution) {
                    errorMessage.textContent = payload.resolution
                } else {
                    organization.textContent = `${payload.connection.organization} (${payload.connection.domain})`
                    country.textContent = `${payload.location.country.name} (${payload.location.country.flag.emoji}), ${payload.location.city}`
                    currency.textContent = `${payload.currency.name} (${payload.currency.symbol})`
                    time.textContent = `${payload.time_zone.current_time} (${payload.time_zone.name})`
                }
            });
    }
}
