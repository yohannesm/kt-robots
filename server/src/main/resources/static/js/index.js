import WebSocketClient from "./webSocketClient.js";
import GameBoard from "./gameBoard.js";
import { html, render } from "https://unpkg.com/lit-html?module";

const mainMenu = document.getElementById("mainMenuContainer");
const gameBoardStatsContainer = document.getElementById(
  "gameBoardStatsContainer"
);
let wsClient;
let lastTime = new Date().getTime();
let lastFrameCount = 0;
let gameMessages = [];
const MAX_NUMBER_OF_MESSAGES = 15;


async function init() {
  document.getElementById("apiHost").value = location.hostname;
  const config = await getConfig();
  const gameBoardClient = new GameBoard(
    document.getElementById("gameBoardContainer")
  );
  wsClient = getWSClient(config, gameBoardClient)
  document.getElementById("btnStartGame").addEventListener("click", () => {
    startGameUi();
    gameBoardClient.Repaint(null);
    setTimeout(() => startGame(), 500);
  });
  document
    .getElementById("btnStopGame")
    .addEventListener("click", () => stopGame());
  document.getElementById("btnClear").addEventListener("click", () => {
    localStorage.clear();
    window.location.href = "/";
  });
  document.getElementById("btnReset").addEventListener("click", () => {
    stopGame();
    resetGameUi();
  });
  restoreRobotArns();
  restoreAdvanceConfig();
}

async function getConfig() {
  try {
    const response = await fetch("/config.json");
    return await response.json();
  } catch (error) {
    console.error(error);
    throw error;
  }
}
function getWSClient(config, gameBoardClient) {
  return new WebSocketClient(
      config.wss,
      document.getElementById("output"),
      5000,
      data => {
        if (data.game === null) {
          return;
        }
        gameBoardClient.Repaint(data);
        updateRobotStats(data.game.robots);
        if (data.game.messages.length > 0) {
          messagesUi(data.game.messages);
        }
        if (data.game.status === "start") {
          sessionStorage.setItem("gameId", data.game.id);
          startGameUi();
        }
        if (data.game.status === "finished") {
          stopGameUi();
        }
        document.getElementById("turn").innerText = "turn: " + data.game.info.gameTurn;
        let curTime = new Date().getTime();
        if (curTime - lastTime >= 1000) {
          document.getElementById("fps").innerText = "fps: " + lastFrameCount;
          lastFrameCount = 0;
          lastTime = curTime;
        } else {
          lastFrameCount += 1;
        }
      }
  );
}
function startGame() {
  const robotArns = getRobotArnsFromInputs();
  localStorage.setItem("robotArns", JSON.stringify(robotArns));
  const request = {
    action: "start",
    robotArns: robotArns
  };
  const requestWithConfig = Object.assign(request, getAdvanceConfig());
  wsClient.start(JSON.stringify(requestWithConfig));
}

function stopGame() {
  try {
    const request = {
      action: "stop",
      gameId: sessionStorage.getItem("gameId")
    };
    wsClient.stop(JSON.stringify(request));
  } catch (error) {
    console.warn("unable to stop the game: " + error);
  }
}

function restoreRobotArns() {
  const robotArns = JSON.parse(localStorage.getItem("robotArns")) || [];
  const robotArnsElements = [].slice.call(document.getElementsByName("robots"));
  for (let index = 0; index < robotArns.length; index++) {
    robotArnsElements[index].value = robotArns[index];
  }
}

function getRobotArnsFromInputs() {
  const robotArnsElements = [].slice.call(document.getElementsByName("robots"));
  return robotArnsElements
    .map(robotArn => robotArn.value)
    .filter(robotArn => robotArn.length > 10)
    .map(robotArn => robotArn.trim());
}

function startGameUi() {
  messagesUi([]);
  mainMenu.style.display = "none";
  gameBoardStatsContainer.style.display = "block";
  document.getElementById("btnStopGame").disabled = false;
}

function stopGameUi() {
  document.getElementById("btnReset").disabled = false;
  document.getElementById("btnStopGame").disabled = true;
}

function resetGameUi() {
  mainMenu.style.display = "block";
  gameBoardStatsContainer.style.display = "none";
  document.getElementById("btnStopGame").disabled = false;
  location.reload();
}

function messagesUi(messages) {
  const messagesElement = document.getElementById("statsBoxMessages");
  messagesElement.innerText = "";
  messages.reverse().forEach(message => {
    gameMessages.unshift(message)
  });
  gameMessages = gameMessages.slice(0, MAX_NUMBER_OF_MESSAGES);
  gameMessages.forEach(message => {
    messagesElement.appendChild(
      document.createTextNode(`#${message.gameTurn} ${message.text}\n`)
    );
  });
}

function updateRobotStats(robots) {
  // https://lit-html.polymer-project.org/guide/template-reference
  const robotsStats = document.getElementById("robotsStats");
  let robotTemplates = [];
  assignRobotMedals(robots);
  robots
    .forEach(robot => {
      const score = (robot.alive ? robot.score : robot.score * -1)/1000;
      let robotTemplate = html`
        <details ?open="${robot.status === "Alive"}" class="${robot.status !== "Alive" ? "robot-dead" : ""}">
          <summary>
            <h4>
              ${robot.medal} (R${robot.index}) ${robot.name}: ${score.toFixed(2)}
              <span class="tooltip">
                ℹ️
                <pre class="tooltiptext">${JSON.stringify(robot, null, 2)}</pre>
              </span>
            </h4>
          </summary>
          <table>
            <tr>
              <td>Health: ${Math.round(robot.maxDamage - robot.damage)}</td>
              <td>Collisions: ${robot.totalCollisions}</td>
              <td>Inflicted: ${Math.round(robot.totalDamageDealt)}</td>
            </tr>
            <tr>
              <td>Shots: ${robot.totalMissileFiredCount}</td>
              <td>Hits: ${robot.totalMissileHitCount}</td>
              <td>Kills: ${robot.totalKills}</td>
            </tr>
            <tr>
              <td>Speed: ${Math.round(robot.speed)}</td>
              <td>Heading: ${Math.round(robot.heading)}</td>
              <td>Odometer: ${Math.round(robot.totalTravelDistance)}</td>
            </tr>
            <tr>
              <td>X: ${Math.round(robot.x)}</td>
              <td>Y: ${Math.round(robot.y)}</td>
              <td>Reload: ${robot.reloadCoolDown.toFixed(2)}</td>
            </tr>
          </table>
        </details>
      `;
      robotTemplates.push(robotTemplate);
    });
  render(
    html`
      ${robotTemplates}
    `,
    robotsStats
  );
}

function getAdvanceConfig() {
  var config = {
    boardWidth: Number(document.getElementById("boardWidth").value),
    boardHeight: Number(document.getElementById("boardHeight").value),
    secondsPerTurn: Number(document.getElementById("secondsPerTurn").value),
    maxTurns: Number(document.getElementById("maxTurns").value),
    directHitRange: Number(document.getElementById("directHitRange").value),
    nearHitRange: Number(document.getElementById("nearHitRange").value),
    farHitRange: Number(document.getElementById("farHitRange").value),
    collisionRange: Number(document.getElementById("collisionRange").value),
    maxBuildPoints: Number(document.getElementById("maxBuildPoints").value),
    robotType: document.getElementById("robotType").value,
    apiHost: document.getElementById("apiHost").value,
  };

  // remove properties with zero value
  Object.keys(config).forEach(key => config[key] === 0 && delete config[key]);
  localStorage.setItem("advanceConfig", JSON.stringify(config));
  return config;
}

function restoreAdvanceConfig() {
  const config = JSON.parse(localStorage.getItem("advanceConfig"));
  if (config) {
    Object.keys(config).forEach(key => {
      document.getElementById(key).value = config[key];
    });
  }
}

function assignRobotMedals(robots) {
  robots.forEach(robot => {
    const missileFireCount = robot.totalMissileFiredCount === 0 ? 1 : robot.totalMissileFiredCount;
    const aliveMultiplier = robot.alive ? 1 : -1;
    const damageScore = robot.totalDamageDealt * 1E3;
    const fireHitRatioScore = (robot.totalMissileHitCount / missileFireCount) * 1E3;
    const killScore = robot.totalKills * 1E5;
    robot.score = (damageScore + fireHitRatioScore + killScore) * aliveMultiplier;
  });
  robots.sort((a, b) => {

    // the higher the score, the closer to the top of the leaderboard
    const deltaScore = b.score - a.score;
    if(deltaScore !== 0) {
      return deltaScore;
    }

    // the longer alive the robot has been, the closer to the top of the leaderboard
    const timeOfDeathA = (a.timeOfDeathGameTurn === -1) ? 1E9 : a.timeOfDeathGameTurn;
    const timeOfDeathB = (b.timeOfDeathGameTurn === -1) ? 1E9 : b.timeOfDeathGameTurn;
    const deltaTimeOfDeath = timeOfDeathB - timeOfDeathA;
    if(deltaTimeOfDeath !== 0) {
      return deltaTimeOfDeath;
    }

    // if all fails, just sort by increasing index
    return a.Index - b.Index;
  });
  for (let index = 0; index < robots.length; index++) {
    const robot = robots[index];
    robot.medal = giveMedal(index, robot.score, robot.alive);
  }
}

function giveMedal(position, score, isAlive) {
  if (score === 0 && isAlive) {
    return "🤖";
  } else if(!isAlive) {
    return "💀";
  }

  switch (position) {
    case 0:
      return "🥇";
    case 1:
      return "🥈";
    case 2:
      return "🥉";
    default:
      return "🤖";
  }
}

window.addEventListener("load", init, false);
