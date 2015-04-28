var exec = require('cordova/exec')

module.exports = {
    getState: function(success, error, options) {
        cordova.exec(success, error, "NetworkStateMonitor", "getState", [options]);
    },
    registerCallback: function(success, error, options) {
        cordova.exec(success, error, "NetworkStateMonitor", "registerCallback", [options]);
    },
    unregisterCallback: function(success, error, options) {
        cordova.exec(success, error, "NetworkStateMonitor", "unregisterCallback", [options]);
    }
};