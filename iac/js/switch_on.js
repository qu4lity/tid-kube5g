/*
 * 5G-Hacker Home Assistant Orchestrator: Switch ON
 * Author: Jose Nuñez
 *
 */
const HomeAssistant = require ('homeassistant');
const HA_HOSTNAME = '10.95.164.110'
const HA_PORT = '8123'

/* HOMEASSISTANT SECTION : power on, off target eNB */

// adding homeassistant API
const hass = new HomeAssistant(
{
        host: 'http://' + HA_HOSTNAME,
        port: HA_PORT,
        ignoreCert: true
});

/* Home Assistant power on, off target eNB */
/* check status hass */

hass.services.call('turn_on', 'switch', 'almagro_power_1')
  .then(res => console.log('enodeb is ON', res))
  .catch(err => console.error(err));
