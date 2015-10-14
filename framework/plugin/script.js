/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
(function() {
    var sock = null;

    function connect() {
        if (sock !== null) {
            return;
        }

        console.log('Connecting...');
        sock = new WebSocket('ws://localhost:9000/');
        
        sock.onopen = function() {
            console.log('Connected!');
        };
        
        sock.onclose = function(e) {
            sock = null;
            console.log('Connection closed (' + e.code + ')');
            reconnect();
        };
        
        sock.onmessage = function(e) {
            console.log('Message received: ' + e.data);
            var j = JSON.parse(e.data);
            
            var id = j[0];   // message id
            var type = j[1]; // message type (see handlers)
            var data = j[2]; // message data (depends on handler)
            
            if (handlers[type]) {
                send(id, handlers[type](data));
            } else {
                console.log('Invalid message: ' + e.type);
            }
        };
        
        sock.onerror = function(e) {
            sock = null;
            reconnect();
        };
    }
    
    function reconnect() {
        setTimeout(connect, 1000);
    }
    
    function send(id, data) {
        var msg = JSON.stringify([id, data || {}]);
        sock.send(msg);
    }

    // define supported message handlers
    var handlers = {
        'click': click,
        'drag': drag,
        'mousewheel': mousewheel,
        'screenshot': screenshot
    };
    
    // generate a mouse click event
    function click(data) {
        var x = data['x'];
        var y = data['y'];
        
        var canvas = $('canvas');
        var offset = canvas.offset();
        x = offset.left + x;
        y = offset.top + y;
        
        var evt = document.createEvent('MouseEvent');
        evt.initMouseEvent('mousedown', true, true, window, 1, 0, 0, x, y, false, false, false, false, 0, null);
        canvas[0].dispatchEvent(evt);
        
        evt = document.createEvent('MouseEvent');
        evt.initMouseEvent('mouseup', true, true, window, 1, 0, 0, x, y, false, false, false, false, 0, null);
        canvas[0].dispatchEvent(evt);
    }
    
    // generate a mouse drag event
    function drag(data) {
        var x = data['x'];
        var y = data['y'];
        var dx = data['dx'];
        var dy = data['dy'];
        
        var canvas = $('canvas');
        var offset = canvas.offset();
        x = offset.left + x;
        y = offset.top + y;
        dx = x + dx;
        dy = y + dy;
        
        var evt = document.createEvent('MouseEvent');
        evt.initMouseEvent('mousedown', true, true, window, 1, 0, 0, x, y, false, false, false, false, 0, null);
        canvas[0].dispatchEvent(evt);
        
        evt = document.createEvent('MouseEvent');
        evt.initMouseEvent('mousemove', true, true, window, 0, 0, 0, dx, dy, false, false, false, false, 0, null);
        canvas[0].dispatchEvent(evt);
        
        evt = document.createEvent('MouseEvent');
        evt.initMouseEvent('mouseup', true, true, window, 1, 0, 0, dx, dy, false, false, false, false, 0, null);
        canvas[0].dispatchEvent(evt);
    }

    // generate a mouse wheel event
	function mousewheel(data) {
		var delta = data['delta'];
		var canvas = $('canvas');
		//var evt = document.createEvent('WheelEvent');
		var eventInit = { deltaX: 0, deltaY: -delta*120 }
		var evt = new WheelEvent("mousewheel", eventInit);
		//evt.initWebKitWheelEvent(0, delta, window, 0, 0, 0, 0, false, false, false, false);
		canvas[0].dispatchEvent(evt);
	}
    
    // capture a screenshot and send it to the client
    function screenshot(data) {
        // get the original canvas
        var srcCanvas = $('#playn-root canvas');
        
        // create a new canvas
        var canvas = $('<canvas />').attr('height', srcCanvas.height() + 'px').attr('width', srcCanvas.width() + 'px')[0];
        var context = canvas.getContext('2d');
        
        // copy the original canvas into the new canvas
        srcCanvas = srcCanvas[0];
        context.drawImage(srcCanvas, 0, 0);
        
        // obtain a png of the canvas
        var imageString = canvas.toDataURL();
        
        return {'data': imageString, 'time': new Date().getTime()};
    }
    
    // wait for the client to connect
    connect();
})();
