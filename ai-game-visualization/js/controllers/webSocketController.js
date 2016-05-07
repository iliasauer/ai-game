define(['jquery',
		'../common/constants'],
	function ($,
	          constants) {

		const WS_URL = constants.WS_URL;
		const GREETING = 'The connection is open';
		var ws;

		function greetServer() {
			sendMessage(GREETING);
			console.log(GREETING);
		}

		function handleMessage(message) {
			console.log('The server: ' + message);
		}

		function handleServerError(error) {
			console.log('An error occurred on the server')
		}

		function handleClose() {
			console.log('The connection was closed')
		}
		
		function sendMessage(message) {
			if (ws) {
				ws.send(message);
			} else {
				console.log('Failed to send message to the server');
			}
		}
		
		function connectWs() {

			if (!ws) {

				ws = new WebSocket(WS_URL);

				ws.onopen = greetServer;

				ws.onclose = handleClose;

				ws.onerror = handleServerError;

				ws.onmessage = handleMessage;
			}
		}

		return {
			connectWs: connectWs,
			sendMessage: sendMessage
		}

	});