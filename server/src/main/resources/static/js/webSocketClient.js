export default class WebSocketClient {
  constructor(wss, element, autoReconnectInterval, onMessage) {
    this.autoReconnectInterval = autoReconnectInterval;
    this.wss = wss;
    this.output = element;
    this.onMessage = onMessage;
    this.socket = new SockJS("/gs-guide-websocket");
    this.stompClient = Stomp.over(this.socket);
    this._init();
  }

  start(message) {
    this.doSend("/app/start", message)
    this._writeToScreen("CONNECTED");
  }

  stop(message) {
    this.doSend("/app/stop", message)
  }

  disconnect() {
    if (this.stompClient !== null) {
      this.stompClient.disconnect();
    }
    this._writeToScreen("DISCONNECTED");
  }

  doSend(endpoint, message) {
    this._writeToScreen("SENT: " + message);
    this.stompClient.send(endpoint, {}, message);
  }

  _init() {
    const self = this;
    this.stompClient.connect({}, function (frame) {
      console.log("Connected: " + frame);
      self.stompClient.subscribe("/topic/game", function (game) {
        self._onMessage(game);
      });
    });
  }

  _onMessage(evt) {
    this._writeToScreen(
      '<span style="color: blue;">RESPONSE: ' + evt.body + "</span>"
    );
    let jsonResult;
    try {
      if (this.onMessage) {
        jsonResult = JSON.parse(evt.body);
        if(jsonResult.game === null || jsonResult.game.status === "finished") {
          this.onMessage(jsonResult)
          this.disconnect()
        } else {
          this.onMessage(jsonResult);
        }
      }
    } catch (error) {
      console.warn("Could not run function with parsed JSON data", evt.data, error.stack || error);
    }
  }

  _onError(evt) {
    this._writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
  }

  _reconnect = function(evt) {
    this._writeToScreen(
      `WebSocketClient: retry in ${this.autoReconnectInterval}ms`,
      evt
    );
    var that = this;
    setTimeout(function() {
      console.log("WebSocketClient: reconnecting...");
      that._init();
    }, this.autoReconnectInterval);
  };

  _writeToScreen(message) {
    var pre = document.createElement("p");
    pre.style.wordWrap = "break-word";
    pre.innerHTML = message;
    this.output.appendChild(pre);
  }
}
