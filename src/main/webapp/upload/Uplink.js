// @inspired by tomcat.apache.org/tomcat-9.0/examples/chat.xhtml

var contextPath = document.getElementById('contextPathValue').innerHTML;

var Uplink = {};

Uplink.socket = null;

Uplink.connect = (function(host) {
    if ('WebSocket' in window) {
        Uplink.socket = new WebSocket(host);
    } else if ('MozWebSocket' in window) {
        Uplink.socket = new MozWebSocket(host);
    } else {
        Console.log('Error: WebSocket is not supported by this browser.');
        return;
    }

    Uplink.socket.onopen = function () {
        Console.log('Info: WebSocket connection opened.\n');
    };

    Uplink.socket.onclose = function () {
        clearTimeout(Uplink.timeout);
        Console.log('Info: WebSocket closed.\n');
    };

    Uplink.socket.onmessage = function (message) {
    	Console.log(message.data);
    	if(message.data.indexOf("Indexing job ended")!=-1){
    		Uplink.socket.send("cancel");
        }
    };
});

Uplink.initialize = function() {
    if (window.location.protocol == 'http:') {
        Uplink.connect('ws://' + window.location.host + contextPath + '/upload/websocket/indexingStatus');
    } else {
        Uplink.connect('wss://' + window.location.host + contextPath + '/upload/websocket/indexingStatus');
    }
};

var Console = {};

Console.log = (function(message) {
    var console = document.getElementById('consoleLog');
    console.value += message;
    console.scrollTop = console.scrollHeight;
});

Uplink.initialize();
