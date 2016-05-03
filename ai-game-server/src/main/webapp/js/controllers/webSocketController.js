define([
    "jquery"
], function ($) {

    const webSocketUrl = "ws://127.0.0.1:8080/logs";
    var ws;
    
    function connectWs() {
        function appendLog(text) {
            var log = $("#log");
            log.val(log.val()+ text)
        }
        if (!ws) {

            ws = new WebSocket(webSocketUrl);

            ws.onopen = function () {
                appendLog("Ready to run.")
            };

            ws.onclose = function () {
                // appendLog("Disconnected.")
            };

            ws.onerror = function (err) {
                console.log("A connection error.")
            };

            ws.onmessage = function (event) {
                appendLog(event.data);
                scrollLogsDown();
            }
        }
    }

    function sendWsMessageRequest(message) {
        if (ws) {
            ws.send(message);
        } else {
            console.log('It needs to initialize the web socket')
        }
    }

    function scrollLogsDown() {
        var logArea = $("#log");
        logArea.animate({
            scrollTop:logArea[0].scrollHeight - logArea.height()
        },10)
    }

    return {
        connectWs: connectWs,
        sendWsMessageRequest: sendWsMessageRequest
    }

});