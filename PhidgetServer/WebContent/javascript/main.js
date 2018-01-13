var main = {
	distance: 0,

	init: function() {
		_V_('video').ready(function() {
			var videoInstance = this;

			// when video has loaded request distance
			videoInstance.addEvent('loadedalldata', main.requestDistance);
		});
	},

	requestDistance: function() {
		var videoInstance = this;

		jQuery.ajax({
			url: 'http://localhost:8080/PhidgetServer/sensorLocation',
			type: 'POST',
			data: {
				'getdata': 'true'
			},
			error: function() {
				// server not responding, cancel
				jQuery('#lastRequest').html('Request timed out, please contact for support.');
				videoInstance.removeEvent('loadedalldata', main.requestDistance);
			}.bind(this),
			success: function(response) {
				var data = jQuery.parseJSON(response);
alert(data);
				// only action if change in distance
				if (data.sensor.distance != this.distance) {
					// set new distance
					this.distance = data.distance;

					// work out time the percentage works out to
					var videoDuration = videoInstance.duration();
					var newTime = (this.distance / 100) * videoDuration;

					// move the video cursor
					videoInstance.currentTime(newTime);
					videoInstance.play();

					// debug information
					jQuery('#percentage').html(this.distance);
					jQuery('#videoTime').html(newTime);
				}
			}.bind(this)
		});
	}
}

jQuery(document).ready(function() {
	main.init();
});