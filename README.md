# KT-Robots

In KT-Robots, you program a battle robot that participates on a square game field. Each turn, the server invokes your robot's Lambda function to get its action for the turn until either the robot wins or is destroyed.

**KT-Robots is a port of the [λ-Robots](https://github.com/LambdaSharp/LambdaRobots) and the 90s [P-Robots](https://corewar.co.uk/probots.htm) game to [kotlin](https://kotlinlang.org/) and [spring](https://spring.io/).**

 
## Level 0: Setup

### Install the required tools
Make sure the following tools are installed in your computer
<details>
<summary>Setup Instructions</summary>

- [Download and install the JDK 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [Download and isntall the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html)
- [Download and install Terraform 12](https://learn.hashicorp.com/terraform/getting-started/install.html)
</details>

### Setup AWS Account and CLI
The challenge requires an AWS account. AWS provides a [*Free Tier*](https://aws.amazon.com/free/), which is sufficient for the challenge.
<details>
<summary>Setup Instructions</summary>

- [Create an AWS Account](https://aws.amazon.com)
- [Configure your AWS profile with the AWS CLI for us-east-2 (Ohio)](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html#cli-quick-configuration)
</details>

> **NOTE:** 
> 
> For this challenge we will be using the US-EAST-2 region

### Clone Git Challenge Repository

Run the following command to clone the KT-Robots challenge. 
```bash
git@github.com:onema/kt-robots.git
cd kt-robots
```



## Level 1: Deploy functions and server
### Deploy using the Terraform
From the command line use `gradlew` to run the `deployRobots` task: 
```bash
./gradlew deployRobots
```

This task will compile the `lambda-robots` project, and deploy the lambda functions to your AWS account in the `us-east-2` (Ohio) region. 

<details>
<summary>Use the InjelliJ Gradle Plugin</summary>

Or use the IntelliJ gradle plugin to execute the task.

![deployRobots](images/deployRobots.png)
</details>



Once the command has finished running, the output will show you the ARN of the lambda robots.
```
Outputs:

HotShotRobotArn = arn:aws:lambda:us-east-2:123456789012:function:HotShot
RoboDogRobotArn = arn:aws:lambda:us-east-2:123456789012:function:RoboDog
YosemiteSamRobotArn = arn:aws:lambda:us-east-2:1234567890120:function:YosemiteSam
YourRobotARN = arn:aws:lambda:us-east-2:123456789012:function:BringYourOwnRobot
```

The `BringYourOwnRobot` is the robot you will be working on!

> **NOTE:** 
> 
> Open `lambda-robots/src/main/kotlin/io.onema.ktrobots.lambda/functions/BringYourOwnRobot` and customize the `NAME` of your robot to distinguish it from other robots.

### Deploy the game server using Terraform 
From the command line use `gradlew` to run the `deployServer` task:
```bash
./gradlew deployServer
```
This task will compile the `server` project, and deploy it to AWS Fargate in the `us-east-2` (Ohio) region.

<details>
<summary>Deployment details</summary>

This command will create and do a few things:

1. Copile the server
2. Creates a docker image that runs the server
4. Create an ECR docker repository to host the image
5. Pushes the image to the new docker repository
6. Creates a Fargate cluster
7. Creates a service and runs a task exposing port 8080

</details>

Once the deployment has finished, you will have to login to AWS to get the server IP:
- Amazon ECS
- Clusters
- ktrobots-server-cluster
- Tasks
- Select the task from the list
- Copy the Public IP
- You can also expand the task details and get a link to the cloudwatch logs

Once you have the IP paste it in your broser usin the port `8080`
```
# For example
http://3.15.171.35:8080/
```

You can add the robot lambda function ARN to the game board client in the browser.  **You can add the ARN multiple times.**

![Game configuration](images/.png)

Use the **Advance Configuration** to change any default settings.  Use **Clear Saved Config** to reset all settings to default.


## Level 2: Create an Attack Strategy

Now that you have deployed all the robots to your account, add the ARN of the `TargetRobot` multiple times to the KT-Robots server to create targets.


Now update the behavior of `BringYourOwnRobot` to shoot down the target robots. 

### Use Luck - YosemiteSam 
For example, you can use luck, like `YosemiteSam`, which shoots in random directions

<details>
<summary>YosemiteSam Details</summary>

![Yosemite Sam](images/yosemiteSam.png)
Yosemite Sam is fast and trigger happy!

This robot chooses a random angle on every turn and fires a missile. It has an extra large engine which helps it avoid attacks and keeps it's distance from the edges of the game board to avoid collisions!

| Equipment | Type              | Points | Details |
| --------- | ----------------- | ------ | ------- |
| Armor     | Light             | 1      |         |
| Engine    | Extra Large       | 4      |         |
| Radar     | Ultra Short Range | 0      |         |
| Missile   | Dart              | 0      |         |
| Total     |                   | 5      |         |
</details>


### Use Targeting - HotShot 

This robot uses the `scan()` method to find enemies and aim missiles at them. 
<details>
<summary>HotShot Details</summary>

![HotShot](images/hotShot.jpg)
HotShot is patient and accurate, it hardly ever misses it's target!

This robot uses the `scan()` method to find targets, if it doesn't find targets it moves to a new location. If it receives damage it initiates an evasive move. 

| Equipment | Type        | Points | Details |
| --------- | ----------- | ------ | ------- |
| Armor     | Medium      | 2      |         |
| Engine    | Large       | 3      |         |
| Radar     | Short Range | 1      |         |
| Missile   | Javelin     | 2      |         |
| Total     |             | 8      |         |
</details>

### Chase like a dog - RoboDog 

This robot uses the `scan()` method to find enemies and chases them. 
<details>
<summary>RoboDog Details</summary>

![RoboDog](images/roboDog.jpg)
RoboDog moves at random and scans what is right in front of it, when this dog bites it won't let go!

This robot uses the `scan()` method to find targets right in from of it, if it does it adjust it's heading to move towards the target, this dog can hit you with a missile and with collision damage!

| Equipment | Type              | Points | Details |
| --------- | ----------------- | ------ | ------- |
| Armor     | Light             | 2      |         |
| Engine    | Standard          | 3      |         |
| Radar     | Ultra Short Range | 0      |         |
| Missile   | Cannon            | 3      |         |
| Total     |                   | 8      |         |
</details>

### TargetRobot 

This robot just sits down and waits to be hit. 
<details>
<summary>TargetRobot Details</summary>

![TargetRobot](images/targetRobot.jpg)
Please don't be the target robot, no body wants to be the target robot!

| Equipment | Type              | Points | Details |
| --------- | ----------------- | ------ | ------- |
| Armor     | Heavy             | 3      |         |
| Engine    | Economy           | 0      |         |
| Radar     | Ultra Short Range | 0      |         |
| Missile   | Dart              | 0      |         |
| Total     |                   | 3      |         |
</details>


### Remember that 
- Other robots may be out of radar range, requiring your robot to move periodically. 
- Your robot can be damaged by its own missiles. 
- Check `gameInfo.farHitRange` to make sure your target is beyond the damage range. 
- If you don't mind a bit of self-inflicted pain, you can also use `gameInfo.nearHitRange` or even `game.directHitRange` instead.

## Level 3: Create an Evasion Strategy

Add the `YosemiteSam` ARN twice to the KT-Robots server to create two attackers.

Now update the behavior of `BringYourOwnRobot` to avoid getting shot. 

<details>
<summary>Examples</summary>

You can continuous motion, like `YosemiteSam`, which zig-zags across the board.

Reacting to damage like `HotShotRobot`. 

Beware that a robot cannot change heading without suddenly stopping if its speed exceeds `Robot.MaxSpeed`.
</details>

## Level 4: Take on the Champ

Add the `HotShot` ARN once to the KT-Robots server to create one formidable opponent.

Consider modifying your robot build by tuning the 
- engine
- armor 
- missile
- radar 
Set the proper equipment to suit your attack and evasion strategies. 

**Remember that your build cannot exceed 8 points, or your robot will be disqualified from the competition.**

## BOSS LEVEL: Enter the Multi-Team Deathmatch Competition

For the boss level, your opponent is every other team! Submit your Robot ARN to final showdown and see how well it fares.

**May the odds be ever in your favor!**

## Programming Reference

<details>
<summary>Reference Details</summary>

### Pre-Build Lambda-Robots

The `lambda-robots/src/main/kotlin/io.onema.ktrobots.lambda/functions/` folder contains additional robots that are deployed, these have different behaviors.
Next, we need a few robots to battle it out. 
* `TargetRobot`: This is a stationary robot for other robots to practice on.
* `YosemiteSam`: This robot runs around shooting in random directions as fast as it can.
* `HotShot`: This robot uses its radar to find other robots and fire at them. When hit, this robot moves around the board.
* `RoboDog`: This robot moves around shooting straight in front of it, when it finds a target it changes direction and chasses it, this robot will do collision damage.

### LambdaRobots SDK

Derive your Lambda-Robot from the `LambdaRobotFunction` provided by the SDK.

#### Abstract Methods
The base class requires two methods to be implemented:

| Method                                                                              | Description                                                                                                                                                                                                                                                    |
| ----------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `fun getBuild(state: LambdaRobotState): Pair<LambdaRobotBuild, LambdaRobotState>`   | This method returns the robot build information, including its name, armor, engine, missile type, and radar, and the robot state object. Note that by default, a build cannot exceed 8 points or the robot will be disqualified at the beginning of the match. |
| `fun getAction(state: LambdaRobotState): Pair<LambdaRobotAction, LambdaRobotState>` | This method returns the actions taken by the robot during the turn and the updated robot state                                                                                                                                                                 |

#### Properties
The most commonly needed properties are readily available as properties from the base class. Additional information about the game or the robot is available via the `Game` and `Robot` properties, respectively.

| Property           | Type          | Description                                                                                                                                      |
| ------------------ | ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| `breakingDistance` | `double`      | Distance required to stop at the current speed.                                                                                                  |
| `damage`           | `double`      | Robot damage. Value is always between 0 and `Robot.MaxDamage`. When the value is equal to `Robot.MaxDamage` the robot is considered killed.      |
| `game`             | `Game`        | Game information data structure. _See below._                                                                                                    |
| `heading`          | `double`      | Robot heading. Value is always between `-180` and `180`.                                                                                         |
| `random`           | `Random`      | Initialized random number generator. Instance of [Random Class](https://docs.microsoft.com/en-us/dotnet/api/system.random?view=netstandard-2.0). |
| `reloadCoolDown`   | `double`      | Number of seconds until the missile launcher is ready again.                                                                                     |
| `robot`            | `LambdaRobot` | Robot information data structure. _See below._                                                                                                   |
| `speed`            | `double`      | Robot speed. Value is between `0` and `Robot.MaxSpeed`.                                                                                          |
| `state`            | `TState`      | Robot state based on custom type. Used to store information between turns.                                                                       |
| `x`                | `double`      | Horizontal position of robot. Value is between `0` and `Game.BoardWidth`.                                                                        |
| `y`                | `double`      | Vertical position of robot. Value is between `0` and `Game.BoardHeight`.                                                                         |

##### `Robot` Properties
| Property                      | Type                | Description                                                                               |
| ----------------------------- | ------------------- | ----------------------------------------------------------------------------------------- |
| `acceleration`                | `double`            | Acceleration when speeding up. (m/s^2)                                                    |
| `collisionDamage`             | `double`            | Amount of damage the robot receives from a collision.                                     |
| `damage`                      | `double`            | Accumulated robot damage. Between `0` and `MaxDamage`.                                    |
| `deceleration`                | `double`            | Deceleration when speeding up. (m/s^2)                                                    |
| `directHitDamage`             | `double`            | Amount of damage the robot receives from a direct hit.                                    |
| `farHitDamage`                | `double`            | Amount of damage the robot receives from a far hit.                                       |
| `Heading`                     | `double`            | Robot heading. Between `0` and `360`. (degrees)                                           |
| `id`                          | `string`            | Globally unique robot ID.                                                                 |
| `index`                       | `int`               | Index position of robot. Starts at `0`.                                                   |
| `maxDamage`                   | `double`            | Maximum damage before the robot is destroyed.                                             |
| `maxSpeed`                    | `double`            | Maximum speed for robot. (m/s)                                                            |
| `maxTurnSpeed`                | `double`            | Maximum speed at which the robot can change heading without a sudden stop. (m/s)          |
| `missileDirectHitDamageBonus` | `double`            | Bonus damage on target for a direct hit.                                                  |
| `missileFarHitDamageBonus`    | `double`            | Bonus damage on target for a far hit.                                                     |
| `missileNearHitDamageBonus`   | `double`            | Bonus damage on target for a near hit.                                                    |
| `missileRange`                | `double`            | Maximum range for missile. (m)                                                            |
| `missileReloadCooldown`       | `double`            | Number of seconds between each missile launch. (s)                                        |
| `missileVelocity`             | `double`            | Travel velocity for missile. (m/s)                                                        |
| `name`                        | `string`            | Robot display name.                                                                       |
| `nearHitDamage`               | `double`            | Amount of damage the robot receives from a near hit.                                      |
| `radarMaxResolution`          | `double`            | Maximum degrees the radar can scan beyond the selected heading. (degrees)                 |
| `radarRange`                  | `double`            | Maximum range at which the radar can detect an opponent. (m)                              |
| `reloadCoolDown`              | `double`            | Number of seconds before the robot can fire another missile. (s)                          |
| `speed`                       | `double`            | Robot speed. Between `0` and `MaxSpeed`. (m/s)                                            |
| `status`                      | `LambdaRobotStatus` | Robot status. Either `Alive` or `Dead`.                                                   |
| `targetHeading`               | `double`            | Desired heading for robot. The heading will be adjusted accordingly every turn. (degrees) |
| `targetSpeed`                 | `double`            | Desired speed for robot. The current speed will be adjusted accordingly every turn. (m/s) |
| `timeOfDeathGameTurn`         | `int`               | Game turn during which the robot died. `-1` if robot is alive.                            |
| `totalCollisions`             | `int`               | Number of collisions with walls or other robots during match.                             |
| `totalDamageDealt`            | `double`            | Damage dealt by missiles during match.                                                    |
| `totalKills`                  | `int`               | Number of confirmed kills during match.                                                   |
| `totalMissileFiredCount`      | `int`               | Number of missiles fired by robot during match.                                           |
| `totalMissileHitCount`        | `int`               | Number of missiles that hit a target during match.                                        |
| `totalTravelDistance`         | `double`            | Total distance traveled by robot during the match. (m)                                    |
| `x`                           | `double`            | Robot horizontal position.                                                                |
| `y`                           | `double`            | Robot vertical position.                                                                  |

##### `Game` Properties
| Property         | Type     | Description                                             |
| ---------------- | -------- | ------------------------------------------------------- |
| `id`             | `string` | Unique Game ID.                                         |
| `boardWidth`     | `double` | Width of the game board.                                |
| `boardHeight`    | `double` | Height of the game board.                               |
| `secondsPerTurn` | `double` | Number of seconds elapsed per game turn.                |
| `directHitRange` | `double` | Distance for missile impact to count as direct hit.     |
| `nearHitRange`   | `double` | Distance for missile impact to count as near hit.       |
| `farHitRange`    | `double` | Distance for missile impact to count as far hit.        |
| `collisionRange` | `double` | Distance between robots to count as a collision.        |
| `gameTurn`       | `int`    | Current game turn. Starts at `1`.                       |
| `maxGameTurns`   | `int`    | Maximum number of turns before the game ends in a draw. |
| `maxBuildPoints` | `int`    | Maximum number of build points a robot can use.         |
| `apiUrl`         | `string` | URL for game server API.                                |

#### Primary Methods
The following methods represent the core capabilities of the robot. They are used to move, fire missiles, and scan its surroundings.

| Method                                                       | Description                                                                                                                                                                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `void FireMissile(double heading, double distance)`          | Fire a missile in a given direction with impact at a given distance. A missile can only be fired if `Robot.ReloadCoolDown` is `0`.                                                                                  |
| `Task<double?> ScanAsync(double heading, double resolution)` | Scan the game board in a given heading and resolution. The resolution specifies in the scan arc centered on `heading` with +/- `resolution` tolerance. The max resolution is limited to `Robot.RadarMaxResolution`. |
| `void SetHeading(double heading)`                            | Set heading in which the robot is moving. Current speed must be below `Robot.MaxTurnSpeed` to avoid a sudden stop.                                                                                                  |
| `void SetSpeed(double speed)`                                | Set the speed for the robot. Speed is adjusted according to `Robot.Acceleration` and `Robot.Deceleration` characteristics.                                                                                          |

#### Support Methods
The following methods are provided to make some common operations easier, but do not introduce n

| Method                                     | Description                                                                                                                                                               |
| ------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `double AngleToXY(double x, double y)`     | Determine angle in degrees relative to current robot position. Return value range from `-180` to `180` degrees.                                                           |
| `double DistanceToXY(double x, double y)`  | Determine distance relative to current robot position.                                                                                                                    |
| `void FireMissileToXY(double x, double y)` | Fire a missile in at the given position. A missile can only be fired if `Robot.ReloadCoolDown` is `0`.                                                                    |
| `bool MoveToXY(double x, double y)`        | Adjust speed and heading to move robot to specified coordinates. Call this method on every turn to keep adjusting the speed and heading until the destination is reached. |
| `double NormalizeAngle(double angle)`      | Normalize angle to be between `-180` and `180` degrees.                                                                                                                   |

### Robot Build

By default, 8 build points are available to allocate in any fashion. The robot is disqualified if its build exceeds the maximum number of build points.

The default configuration for each is shown in bold font and an asterisk (*).

#### Radar

| Radar Type       | Radar Range  | Radar Resolution | Points |
| ---------------- | ------------ | ---------------- | ------ |
| UltraShortRange  | 200 meters   | 45 degrees       | 0      |
| ShortRange       | 400 meters   | 20 degrees       | 1      |
| **MidRange (*)** | 600 meters   | 10 degrees       | 2      |
| LongRange        | 800 meters   | 8 degrees        | 3      |
| UltraLongRange   | 1,000 meters | 5 degrees        | 4      |

#### Engine

| Engine Type      | Max. Speed | Acceleration | Points |
| ---------------- | ---------- | ------------ | ------ |
| Economy          | 60 m/s     | 7 m/s^2      | 0      |
| Compact          | 80 m/s     | 8 m/s^2      | 1      |
| **Standard (*)** | 100 m/s    | 10 m/s^2     | 2      |
| Large            | 120 m/s    | 12 m/s^2     | 3      |
| ExtraLarge       | 140 m/s    | 13 m/s^2     | 4      |

#### Armor

| Armor Type     | Direct Hit | Near Hit | Far Hit | Collision | Max. Speed | Deceleration | Points |
| -------------- | ---------- | -------- | ------- | --------- | ---------- | ------------ | ------ |
| UltraLight     | 50         | 25       | 12      | 10        | +35 m/s    | 30 m/s^2     | 0      |
| Light          | 16         | 8        | 4       | 3         | +25 m/s    | 25 m/s^2     | 1      |
| **Medium (*)** | 8          | 4        | 2       | 2         | -          | 20 m/s^2     | 2      |
| Heavy          | 4          | 2        | 1       | 1         | -25 m/s    | 15 m/s^2     | 3      |
| UltraHeavy     | 2          | 1        | 0       | 1         | -45 m/s    | 10 m/s^2     | 4      |

#### Missile

| Missile Type    | Max. Range   | Velocity | Direct Hit Bonus | Near Hit Bonus | Far Hit Bonus | Cooldown | Points |
| --------------- | ------------ | -------- | ---------------- | -------------- | ------------- | -------- | ------ |
| Dart            | 1,200 meters | 250 m/s  | 0                | 0              | 0             | 0 sec    | 0      |
| Arrow           | 900 meters   | 200 m/s  | 1                | 1              | 0             | 1 sec    | 1      |
| **Javelin (*)** | 700 meters   | 150 m/s  | 3                | 2              | 1             | 2 sec    | 2      |
| Cannon          | 500 meters   | 100 m/s  | 6                | 4              | 2             | 3 sec    | 3      |
| BFG             | 350 meters   | 75 m/s   | 12               | 8              | 4             | 5 sec    | 4      |

</details>
