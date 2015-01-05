'use strict';

// Development specific configuration
// ==================================
module.exports = {
    nconfPath: './config-dev.json',
    
    // MongoDB connection options
    mongo: {
        uri: 'mongodb://localhost/keylessentry-dev'
    },

    seedDB: true
};