# OutBreak-Java-3DGame

**Faisl Qurishi**

**OUTBREAK: BioHazard Containment**

![](RackMultipart20210121-4-4mqy1z_html_a125e6f7b3dfb128.png)

**To Compile and Run:**

Classpath must be set correctly for game engine to work. Please download the files listed in the &quot;classpathFiles.txt&quot; file and follow the guide on how to correctly install them.
 If game does not start, it may be because of audio issues. You can try running the game without audio checked.

Game can take up to 3 minutes to load up.

For single player:
 You can just use compile.bat and run.bat.
 JDialog box will open saying what port and IP to enter, ignore.
 Check singleplayer. Gamepad and full screen options available.
 Click play and enjoy.
 If using multiplayer (networking):
 compile.bat then server.bat, take note of your servers IP address then run.bat
 enter IP address and port number then press play
 Player 2 needs to use run.bat and enter the same IP address and port number.
 Game will not start until 2 players join the server.
 Server can only handle 2 players, if a third player joins server will shut down.

**Special Devices:**
 No special devices needed, keyboard and mouse and gamepad to use as controls.

**How Game Is Played**

Outbreak is a survival game. At the start of the game the character is immediately chased by zombies who if get close will damage the players health (can be changed with scripting but ideally its 100)
 if player reaches 100, he will die to which the game will end, or he will view second player&#39;s camera until their death, or the time limit is reached.
 Players win the game by reaching the end of the time limit. The goal of the game is to survive the zombie onslaught. The players can shoot zombies, but this only pushes the zombies back, they cannot die.
 If playing single player there is a large zombie who is the parent of 2 mini zombies, the mini zombies cannot be shot so the player must shoot the large zombie to get rid of it and its children. (approx. 2-6 shots). Players can help each other survive or let zombies kill the other player but the game does not end until time limit is reached and will show you a screen depending if you survived or not. If a player survives, game is won.

**Game Manual**

**Inputs**

MOUSE AND KEYBOARD:

press W to move forward

press S to move backward

press D to rotate right

press A to rotate left

press SPACE to shoot or Mouse Left Click

press L to use flashlight

press Q to quit game

GAMEPAD:
 (ps4 controller used)

use GAMEPAD left stick to move and rotate

use GAMEPAD right stick to rotate camera
 use GAMEPAD button 1 to shoot

use GAMEPAD button 4 (ps4 triangle) to use flashlight
 use GAMEPAD button 6 to quit game

**Scripting:**
Two javascripts are written, Terrain.js and GameSetUp.js .
 Terrain.js sets up terrain and can update the terrain on the fly while game is running.
 GameSerUp.js is used to setup the games tree count, player health and time limit.
 These 3 variables were chosen because they affect gameplay. Tree count comes with performance issues if raised and can increase frame limit is lowered. Player health can be changed to suit difficulty and time limit as well.

**Genre, Theme, Dimensionality, and Activities**

**Genre:** Action, third person shooter survival.
**Theme:** Zombie, horror game.
**Dimensionality:** Fully 3D player motion. 3rd person camera. Ground world.
**Activities:** Combat (shooting zombies). Survival (running way from zombies).

**Contributions**

Everything was coded by Faisl Qurishi.
 Models: gun.obj, shotgun.obj, tree.obj, and grass.obj
 Sounds: gun.wav
 Textures: shotgunTEX.png, gunTex.png, fZombie1.png, mZombie1.png, grass.png, tree.png, chris1.png grass.jpg, wall.jpeg

Height-Maps: perlin1.png, perlin2.png, perlin3.png
 All contributed by Faisl Qurishi.

**Items created by us**
Models: gun.obj, shotgun.obj, tree.obj, and grass.obj (as well as their materials)
 Sounds: gun.wav
 Textures: shotgunTEX.png, gunTex.png, fZombie1.png, fZombie2.png, fZombie3.png mZombie1.png, mZombie2.png, mZombie3.png, grass.png, tree.png, chris1.png, grass.jpg, wall.jpeg
 Height-Maps: perlin1.png, perlin2.png, perlin3.png
 Skeletal Animation: swatSA.rka and alexSA.rka rigged onto outside models

**Evidence of permission to use models**

Models mZombieM.rkm, fZombieM.rkm, alexM.rkm, and swatM.rkm
 Animations: alexDA.rka, aalexIA.rka, alexRA.rka, alexRBA.rka, fZombieHA.rka, fZombieRA.rka, mZombieHA.rka, mZombieRA.rka, swatDA.rka, swatIA.rka, swatRA.rka, swatRBA.rka
 Textures: Alex1.png, Swat1.png
 were downloaded from mixamo.com then exported using rages blender tool
 mixamo models and animations are royalty free for personal projects as stated in their FAQS here: [https://helpx.adobe.com/creative-cloud/faq/mixamo-faq.html](https://helpx.adobe.com/creative-cloud/faq/mixamo-faq.html)
 ![](RackMultipart20210121-4-4mqy1z_html_242b917b117d8c42.png)
 Sounds: OutBreakSoundTrack.wav and Zombie SFX\_1.wav, Zombie SFX\_2.wav, Zombie SFX\_3.wav, Zombie SFX\_4.wav, Zombie SFX\_5.wav, Zombie SFX\_6.wav
 created by Konstantin Laukart who has given me permission to use for personal use.
 ![](RackMultipart20210121-4-4mqy1z_html_d6942799caa25869.png)
