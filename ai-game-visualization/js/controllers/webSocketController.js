define(['jquery',
		'../common/constants',
		'../common/fieldBuilder'],
	function ($,
	          constants,
	          fieldBuilder) {

		const WS_URL = constants.WS_URL;
		const GREETING = 'The connection is open';
		var ws;
		var gameField;

		function greetServer() {
			sendMessage(GREETING);
			console.log(GREETING);
		}

		function handleMessage(message) {
			console.log('The server: ' + message.data);
			const JSON_MESSAGE = JSON.parse(message.data);
			if (JSON_MESSAGE.map) {
				gameField = JSON_MESSAGE.map;
				fieldBuilder.setFieldElement($('#field'));
				fieldBuilder.setFieldData(gameField);
				fieldBuilder.build();
			}
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