var exec = require("cordova/exec");

var CORDOVA_SERVICE_NAME = "MaskImage";

var MaskImage = function() {};

MaskImage.makeMaskImage = function(image, frame, success, error) {
	success = success || function() {};
	error = error || function() {};
	
	exec(success, error, CORDOVA_SERVICE_NAME, "makeMaskImage", [image, frame]);
}

MaskImage.makeMaskImageScaleFit = function(image, frame, success, error) {
	success = success || function() {};
	error = error || function() {};

	exec(success, error, CORDOVA_SERVICE_NAME, "makeMaskImageScaleFit", [image, frame]);
}

module.exports = MaskImage;