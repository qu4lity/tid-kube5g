/*
 * 5G-Hacker Home Assistant Orchestrator: Switch OFF
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

/* Home Assistant power off target eNB */

hass.services.call('turn_off', 'switch', 'almagro_power_1')
  .then(res => console.log('enodeb switched off', res))
  .catch(err => console.error(err));

