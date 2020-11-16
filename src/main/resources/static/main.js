(function() {
  // The width and height of the captured photo. We will set the
  // width to the value defined here, but the height will be
  // calculated based on the aspect ratio of the input stream.

function dataURItoBlob(dataURI) {
    // convert base64/URLEncoded data component to raw binary data held in a string
    var byteString;
    if (dataURI.split(',')[0].indexOf('base64') >= 0)
        byteString = atob(dataURI.split(',')[1]);
    else
        byteString = unescape(dataURI.split(',')[1]);

    // separate out the mime component
    var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];

    // write the bytes of the string to a typed array
    var ia = new Uint8Array(byteString.length);
    for (var i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
    }

    return new Blob([ia], {type:mimeString});
}

  var width = 320;    // We will scale the photo width to this
  var height = 0;     // This will be computed based on the input stream

  // |streaming| indicates whether or not we're currently streaming
  // video from the camera. Obviously, we start at false.

  var streaming = false;

  // The various HTML elements we need to configure or control. These
  // will be set by the startup() function.

  var video = null;
  var canvas = null;
  var context = null;
  var photo = null;
  var startbutton = null;
  var result = null;
  var timer = null;
  var ws;

  function startup() {
    ws = new WebSocket("ws://192.168.0.106:8080/socket")
    video = document.getElementById('video');
    result = document.getElementById('result')
    canvas = document.getElementById('canvas');
    context = canvas.getContext('2d')
    photo = document.getElementById('photo');
    startbutton = document.getElementById('startbutton');
    ws.onopen = function(e) {
            ws.onmessage = function(event){
                var target = document.getElementById("result");
                url = window.webkitURL.createObjectURL(event.data)

                target.onload = function(){
                    window.webkitURL.revokeObjectURL(url);
                }
                target.src = url
            }
    }

    startbutton.addEventListener('click', function(event){
        navigator.mediaDevices.getUserMedia({video: true, audio: false})
                    .then(function(stream) {
                        timer = setInterval(function(){
                                    context.drawImage(video, 0 ,0, 320, 240);
                                    var data = canvas.toDataURL('image/jpeg', 0.5);
                                    ws.send(dataURItoBlob(data))
                                    }, 50)
                      video.srcObject = stream;
                      video.play();
                    })
                    .catch(function(err) {
                      console.log("An error occurred: " + err);
                    });
    })



    video.addEventListener('canplay', function(ev){
      if (!streaming) {
        height = video.videoHeight / (video.videoWidth/width);

        // Firefox currently has a bug where the height can't be read from
        // the video, so we will make assumptions if this happens.

        if (isNaN(height)) {
          height = width / (4/3);
        }

        video.setAttribute('width', width);
        video.setAttribute('height', height);
        canvas.setAttribute('width', width);
        canvas.setAttribute('height', height);
        streaming = true;
      }
    }, false);

  }




  // Set up our event listener to run the startup process
  // once loading is complete.
  window.addEventListener('load', startup, false);
})();