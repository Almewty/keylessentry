/*
#########################TO-DO#################################

-QR-Code specifications:
    http://w-hs.de/keylessentry?name=<name>&ownid=<phoneid>&remoteid=<türid>&secret=<secret>
    name=test
    ownid=timestamp at the moment of register (for debugging it's: 'debugphone')
    remoteid=generated id to identify raspberry pi (for debugging it's: 'debugdoor')
    secret=md5-hash of time at the moment of register (for debugging it's: 'debugsecret')
            
    -example:
http://w-hs.de/keylessentry?name=test&ownid=8028EFDC4E7E4F1DA3C43AFD7FE76510&remoteid=E20A39F473F54BC4A12F17D1AD07A961&secret=ACACACACACAACCAA
*/

var qr = require('qr-image');

module.exports = {
 
    generateQRCode: function(ownid, remoteid, secret, datapath, datatyp, callback){
        
        var result = "http://w-hs.de/keylessentry?name=test"
        + "&ownid=" + ownid 
        + "&remoteid=" + remoteid
        + "&secret=" + secret;

        var qr_svg = qr.image(result, { type: datatyp });
        qr_svg.pipe(require('fs').createWriteStream(datapath + '.' + datatyp));

        var svg_string = qr.imageSync(result, { type: datatyp });
        
        callback(datapath + "." + datatyp);
       },
};