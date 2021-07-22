export default class GameBoard {
  constructor(gameBoardContainerElement) {
    this.gameBoardContainerElement = gameBoardContainerElement;
    this.canvas = document.createElement("canvas");
    this.canvas.className = "game-board";
    this.canvas.width = 1000;
    this.canvas.height = 1000;
    gameBoardContainerElement.appendChild(this.canvas);
    this.context = this.canvas.getContext("2d");
  }

  /**
   * Repaint the canvas
   * @param {*} gameStat
   */
  Repaint(gameStat) {
    if (gameStat === null) {
      this._start = new Date();
      this._spinnerInterval = setInterval(() => {
        this._spinner();
      }, 1000 / 30);
      return;
    }
    this._clear();
    if (gameStat.game.status === "start") {
      this.canvas.width = gameStat.game.info.boardWidth;
      this.canvas.height = gameStat.game.info.boardHeight;
      return;
    }
    clearTimeout(this._spinnerInterval);
    if (gameStat.game.status === "nextTurn") {
      this._robots(gameStat.game, gameStat.game.robots);
      this._missiles(gameStat.game, gameStat.game.missiles);
      return;
    }
    if (gameStat.game.status === "finished") {
      this._gameOver();
    }
  }

  _clear() {
    this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
  }

  _robots(game, robots) {
    this.context.save();
    this.context.font = "16px 'Press Start 2P'";
    this.context.fillStyle = "white";
    this.context.textAlign = "center";
    this.context.textBaseline = "middle";
    for (let index = 0; index < robots.length; index++) {
      const robot = robots[index];
      if(robot.status === "alive") {
        this.context.fillText(
          robot.index,
          Math.round(robot.x),
          Math.round(this.canvas.height - robot.y)
        );

        // draw circle around robot with collision radius
        // this.context.beginPath();
        // this.context.strokeStyle = "yellow";
        // this.context.arc(
        //   Math.round(robot.x),
        //   Math.round(robot.y),
        //   Math.round(game.collisionRange),
        //   0,
        //   2 * Math.PI
        // );
        // this.context.stroke();
      }
    }
    this.context.restore();
  }

  _missiles(game, missiles) {
    for (let index = 0; index < missiles.length; index++) {
      const missile = missiles[index];
      this.context.save();

      //(x,y) should start on the bottom left - https://stackoverflow.com/a/7707406/2414540
      this.context.translate(0, this.canvas.height);
      this.context.scale(1, -1);
      this.context.beginPath();
      switch (missile.status) {
        case "flying":
          this.context.moveTo(Math.round(missile.x), Math.round(missile.y));
          const lineLength = 12;
          this.context.lineTo(
            Math.round(
              missile.x +
                Math.sin((missile.heading * Math.PI) / 180) * lineLength
            ),
            Math.round(
              missile.y +
                Math.cos((missile.heading * Math.PI) / 180) * lineLength
            )
          );
          break;
        case "explodingDirect":
          this.context.arc(
            Math.round(missile.x),
            Math.round(missile.y),
            Math.round(game.directHitRange),
            0,
            2 * Math.PI
          );
          break;
        case "explodingNear":
          this.context.arc(
            Math.round(missile.x),
            Math.round(missile.y),
            Math.round(game.nearHitRange),
            0,
            2 * Math.PI
          );
          break;
        case "explodingFar":
          this.context.arc(
            Math.round(missile.x),
            Math.round(missile.y),
            Math.round(game.farHitRange),
            0,
            2 * Math.PI
          );
          break;
      }
      this.context.strokeStyle = "red";
      this.context.lineWidth = 2;
      this.context.stroke();
      this.context.restore();
    }
  }

  _spinner() {
    // https://codepen.io/reneras/pen/HFrmC
    const lines = 16;
    const ctx = this.canvas.getContext("2d");
    ctx.save();
    const rotation =
      parseInt(((new Date() - this._start) / 1000) * lines) / lines;
    ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    ctx.translate(this.canvas.width / 2, this.canvas.height / 2);
    ctx.rotate(Math.PI * 2 * rotation);
    for (let i = 0; i < lines; i++) {
      ctx.beginPath();
      ctx.rotate((Math.PI * 2) / lines);
      ctx.moveTo(this.canvas.width / 10, 0);
      ctx.lineTo(this.canvas.width / 4, 0);
      ctx.lineWidth = this.canvas.width / 30;
      ctx.strokeStyle = "rgba(255, 255, 255," + i / lines + ")";
      ctx.stroke();
    }
    ctx.restore();
  }

  _gameOver() {
    const ctx = this.canvas.getContext("2d");
    ctx.save();
    ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    ctx.font = "100px 'Press Start 2P'";

    // Create gradient
    var gradient = ctx.createLinearGradient(0, 0, this.canvas.width, 0);
    gradient.addColorStop("0", " green");
    gradient.addColorStop("0.5", "blue");
    gradient.addColorStop("1.0", "red");
    // Fill with gradient
    ctx.fillStyle = gradient;
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText("Game Over", this.canvas.width / 2, this.canvas.height / 2);
    ctx.restore();
  }
}
