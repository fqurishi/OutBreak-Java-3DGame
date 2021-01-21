package game;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.*;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.swing.ImageIcon;

import Dialogs.QuitMenu;
import Dialogs.WaitMenu;
import ray.audio.AudioManagerFactory;
import ray.audio.AudioResource;
import ray.audio.AudioResourceType;
import ray.audio.IAudioManager;
import ray.audio.Sound;
import ray.audio.SoundType;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.rage.*;
import ray.rage.asset.material.Material;
import ray.rage.asset.texture.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.Projection;
import ray.rage.util.*;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.networking.IGameConnection.ProtocolType;
import ray.physics.PhysicsEngine;
import ray.physics.PhysicsObject;
import ray.physics.PhysicsEngineFactory;
import ray.rage.rendersystem.states.*;

import myGameEngine.MoveBackwardAction;
import myGameEngine.MoveForwardAction;
import myGameEngine.MoveYGP;
import myGameEngine.OuttaSightController;
import myGameEngine.QuitGameAction;
import myGameEngine.RotatePlayerLeft;
import myGameEngine.RotatePlayerRight;
import myGameEngine.RotatePlayerXGP;
import myGameEngine.ShootAction;
import myGameEngine.StretchController;
import network.GhostAvatar;
import network.GhostNPC;
import network.ProtocolClient;
import myGameEngine.BulletController;
import myGameEngine.Camera3Pcontroller;
import myGameEngine.LightAction;
import ray.rage.util.BufferUtil;
import ray.rage.rendersystem.shader.*;

public class MyGameNoAudio extends MyGame {
	// to minimize variable allocation in update()
	GL4RenderSystem rs;
	float elapsTime = 0.0f;
	float lastTickTime = 0f, lastThinkTime = 0f, lastShootTime = 0f, lastAnimTime = 0f, timeLimit = 210000f;
	String elapsTimeStr, counterStr, dispStr, counterStr2, dispStr2;
	int elapsTimeSec, counter = 0, counter2 = 0;
	
	// Variable for changing different game values
	private int totalZombies = 5;
	private int spaceSize = 25;
	private float maxSpeed = 0.02f;

	
	// Entity dolphinE;
	private SceneNode activeNode;
	
	// Camera
	private Camera3Pcontroller orbitController;
	
	// Input and Actions
	private InputManager im;
	private Action quitGameAction, moveForwardAction,
			moveBackwardAction, rotatePlayerRight,
			moveYGP,
			shoot2Action, rotatePlayerLeft,
			rotateDolphinXGP, rotateCameraYGP,
			shootAction, lightAction;
	// robot
	private Robot robot;
	// mouse
	private Canvas canvas;
	private RenderWindow rw;
	private RenderSystem rs2;
	private float prevMouseX, prevMouseY, curMouseX, curMouseY;
	private boolean isRecentering;         //indicates the Robot is in action
	
	// collisions
	private int[] zombies = new int[totalZombies];
	private int pointer = 0;
	
	//skybox
	private static final String SKYBOX_NAME = "SkyBox";    
	private boolean skyBoxVisible = true;
	
	//networking
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected;
	private boolean isFullScreen;
	private String avatar;
	private Vector<UUID> gameObjectsToRemove;
	private Vector<Integer> gameNPCsToRemove;
	private Vector<GhostAvatar> ghostAvatars;
	private Vector<GhostNPC> ghostNPCs;
	private boolean isSingle;
	
	//scripting
	protected ScriptEngine jsEngine;
	protected File terrainScript;
	protected File gameScript;
	private Invocable invocableEngine;
	long fileLastModifiedTime;
	
	//physics
	private PhysicsEngine physicsEng;
	private PhysicsObject playerPObj, zombiePObj, grndPObj;
	
	//audio
	private IAudioManager audioMgr;
	private Sound zombieSound1, zombieSound2, zombieSound3,
				  zombieSound4, gunSound, gunSound2,
				  zombieSound5, zombieSound6, bgm;
	private Sound zombGrunts[] = new Sound[6];
	
	//npcs
	private GhostNPC npc[] = new GhostNPC[5];
	
	//animation
	//player 1
	private boolean isRunning = false, isIdle = false, isBRunning = false, isShooting = false, isAnimating = false;
	//player 2
	private boolean isGARunning = false, isGAIdle = false, isGABRunning = false, isGAShooting = false, isGAAnimating = false;
	//able to shoot
	private boolean ableToShoot = true;
	
	//moving avatar
	boolean forward = false;
	boolean backward = false;
	
	//lights
	boolean lightOn = true;
	
	//server start
	boolean start = false;
	
	//player healths
	private int p1Health = 100;
	
	//bullet
	private String bulletID;
	private List<Node> removeBullets = new ArrayList<Node>();
	
	//win condition
	private Boolean survived = null;
	
	//want to use gamepad?
	private boolean gamepad;
	
	//is f zombie running
	private boolean fRunning = true;
	private boolean npcFAnimating;
	
	//scenegraph in single player
	private int fathealth = 100;

	
    public MyGameNoAudio(String serverAddr, int sPort, boolean isFullScreen, String avatar, boolean isSingle, boolean gamepad) {
    	super(avatar, sPort, gamepad, avatar, gamepad, gamepad);
        this.serverAddress = serverAddr;
        this.serverPort = sPort;
        this.serverProtocol = ProtocolType.UDP;
        this.isFullScreen = isFullScreen;
        this.start = isSingle;
        this.isSingle = isSingle;
        this.avatar = avatar;
        this.gamepad = gamepad;
		System.out.println("Player1: ");
		if(gamepad == true) {
		System.out.println("FOR GAMEPAD:");
		System.out.println("use left stick to move and rotate");
		System.out.println("use right stick to rotate camera");
		System.out.println("use button 1 to shoot");
		System.out.println("use button 5 for flashlight");
		System.out.println("use button 9 to quit");
		}else {
		System.out.println("FOR KEYBOARD:");
		System.out.println("press W to move forward");
		System.out.println("press S to move backward");
		System.out.println("press D to rotate right");
		System.out.println("press A to rotate left");
		System.out.println("press SPACE or MOUSE LEFT CLICK to shoot");
		System.out.println("press L for flashlight");
		System.out.println("press Q to quit");
		}

    }
    
    @Override
	public void startup() {
		setupNetworking();
		super.startup();
	}

    
    public void init() {
		try {
			startup();
			while(start != true) {
				new WaitMenu(this.getStart());
				protClient.processPackets();
				this.start = getStart();
			}
			run();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//shutdown();
			exit();
			
		}
	}
    
    
   //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void setupNetworking() 
    {   gameObjectsToRemove = new Vector<UUID>();
    	gameNPCsToRemove = new Vector<Integer>();
    	ghostAvatars = new Vector<GhostAvatar>();
    	ghostNPCs = new Vector<GhostNPC>();
    	isClientConnected = false;
    	try 
    	{   protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
    	}   catch (UnknownHostException e) { e.printStackTrace();
    	}   catch (IOException e) { e.printStackTrace();
    	} 
    	if (protClient == null)
    	{   System.out.println("missing protocol host"); }
    	else
    	{ // ask client protocol to send initial join message
    	//to server, with a unique identifier for this client
    		if(!isSinglePlayer())
    		protClient.sendJoinMessage();
    
    	}	
    }
    
	
  //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		if (isFullScreen) {
			rs.createRenderWindow(true);
		}
		else
			rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
	}
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	protected void setupWindowViewports(RenderWindow rw)  {
		rw.addKeyListener(this);
		Viewport topViewport = rw.getViewport(0);
		
	} 
	

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        SceneNode rootNode = sm.getRootSceneNode();
        Camera camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera);
        SceneNode cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
        cameraNode.attachObject(camera);
        camera.setMode('n');
        camera.getFrustum().setFarClipDistance(1000.0f);
        
        RenderSystem rs = sm.getRenderSystem();
        initMouseMode(rs, rw);
        
    }
    
    protected void setupOrbitCamera(Engine eng, SceneManager sm)  {
    	SceneNode playerN = sm.getSceneNode("myPlayerNode");
    	SceneNode cameraN = sm.getSceneNode("MainCameraNode");
    	Camera camera = sm.getCamera("MainCamera");
    	String gpName = im.getFirstGamepadName();
    	System.out.println();
    	System.out.println("INPUT DEVICES BEING USED:");
    	System.out.println(gpName);
    	orbitController =
    			new Camera3Pcontroller(camera, cameraN, playerN, gpName, im);
    	

    	String msName = im.getMouseName();
    	System.out.println(msName);
    	String kbName = im.getKeyboardName();
    	System.out.println(kbName);

    	
    	}
    
    protected void setupLights(SceneManager sm)  {
    	
    	sm.getAmbientLight().setIntensity(new Color(0.1f, 0.1f, 0.1f));
        Light plight = sm.createLight("testLamp1", Light.Type.POINT);
		plight.setAmbient(new Color(0.3f, 0.3f, 0.3f));
        plight.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
		plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
		plight.setLinearAttenuation(0.0005f);
		plight.setConstantAttenuation(0.01f);
		plight.setQuadraticAttenuation(0.0f);
        plight.setRange(800.0f);
        
        Light plight2 = sm.createLight("testLamp2", Light.Type.SPOT);
		plight2.setAmbient(new Color(0.00f, 0.00f, 0.00f));
        plight2.setDiffuse(new Color(0.07f, 0.0f, 0.0f));
		plight2.setSpecular(new Color(0.05f, 0.0f, 0.0f));
		plight2.setLinearAttenuation(0.00005f);
		plight2.setConstantAttenuation(0.001f);
		plight.setQuadraticAttenuation(0.0f);
        plight2.setRange(1.5f);
		
        SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);
        plightNode.setLocalPosition(0, 750, 0);
        
        SceneNode plight2Node = sm.getSceneNode("myPlayerNode").createChildSceneNode("plightNode2");
        plight2Node.setLocalPosition(sm.getSceneNode("myPlayerNode").getLocalPosition().x(),
        		sm.getSceneNode("myPlayerNode").getLocalPosition().y(), sm.getSceneNode("myPlayerNode").getLocalPosition().z()+2f);
        plight2Node.pitch(Degreef.createFrom(-90.0f));

    	
    	}
    protected void setupWalls(Engine eng, SceneManager sm) throws IOException{
    	createTriangle1(eng, sm);
        createTriangle2(eng, sm);
        createTriangle3(eng, sm);
        createTriangle4(eng, sm);
        createTriangle5(eng, sm);
        createTriangle6(eng, sm);
        createTriangle7(eng, sm);
        createTriangle8(eng, sm);
    }

  //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Set up scene for game engine
   
    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {
    	im = new GenericInputManager();
    	
    	//skybox
        makeSkyBox(eng, sm);
    	
    	
        // Create player (change name)
        makePlayer(eng, sm);
        
        
        //create gun for player
        makeGun(eng, sm, 1);
        makeShotgun(eng,sm,1);
        
        //make zombies
        SceneNode zombiesNG = sm.getRootSceneNode().createChildSceneNode("zombiesNodeG");
        SceneNode smallZombie = zombiesNG.createChildSceneNode("smallZombiesNodeG");
        
        if(isSinglePlayer()) {
        	//make fat zombie
        	makeFatZombie(eng, sm);
        	for( int i = 0; i < npc.length; i++) {
        		npc[i] = new GhostNPC(i, Vector3f.createFrom(randInRangeFloat(-spaceSize, spaceSize), 0.0f, randInRangeFloat(-spaceSize, spaceSize)));
        		addGhostNPCToGameWorld(npc[i]);
        	}
        	//mmake small zombie
        	makeSmallZombie(eng, sm, 1);
        	makeSmallZombie(eng, sm, 2);
        }
            
        
      //setup camera
        setupOrbitCamera(eng, sm);
        
     // Setup the input actions
    	setupInputs(sm);
        
        
        
        // setup the light Nodes
        setupLights(sm);
        
        activeNode = this.getEngine().getSceneManager().getSceneNode("MainCameraNode");
        
       
        //make the walls
        setupWalls(eng, sm);
        
        //scripting
        setupScripting();
        setupTerrain();

        //make the tree's
        for (int i = 0; i < ((Integer) jsEngine.get("TREE_COUNT")); i++) {
        	makeTree(eng, sm, i);
        	updateTreePosition(sm.getSceneNode("tree" + i + "Node"));
        }
        
        timeLimit = ((Double) jsEngine.get("TIME_LIMIT")).floatValue();
        p1Health = (Integer) jsEngine.get("PLAYER_HEALTH");
        
        //make the grass
        makeGrass(eng, sm);
        
       //physics
        initPhysicsSystem();
        createRagePhysicsWorld();
        
        //audio
        //initAudio(sm);
        
        //controllers (getting rid of my bullets)
        BulletController bC = new BulletController(getPhysicsEngine(), sm, this, removeBullets);
        sm.addController(bC);
        
        //outta sight
        OuttaSightController OSZombies = new OuttaSightController();
     	sm.addController(OSZombies);
        
        
        

    }
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //scripting
    private void setupScripting() {
    	ScriptEngineManager factory = new ScriptEngineManager();
    	java.util.List<ScriptEngineFactory> list = factory.getEngineFactories();
    	jsEngine = factory.getEngineByName("js");
    	//terrain
    	terrainScript = new File("assets/scripts/Terrain.js");
    	this.runScript(terrainScript);
    	//tree
    	gameScript = new File("assets/scripts/GameSetUp.js");
    	this.runScript(gameScript);
    	jsEngine.put("sm", this.getEngine().getSceneManager());
    	
    	
    	this.invocableEngine = (Invocable)jsEngine;
    	
    	
    	
    	
    	
    }
    
    private void runScript(File scriptFile) {
    	{try
    	{	FileReader fileReader = new FileReader(scriptFile);
    		jsEngine.eval(fileReader);
    		fileReader.close();
    	}
    	catch (FileNotFoundException e1)
    	{	System.out.println(scriptFile + "not found" +e1);}
    	catch (IOException e2)
    	{	System.out.println(scriptFile + "IO problem" +e2);}
    	catch (ScriptException e3)
    	{	System.out.println(scriptFile + "script exception" +e3);}
    	catch (NullPointerException e4)
    	{	System.out.println(scriptFile + "Null ptr" + e4);}
    	}
    }
    
    private void invokeScript(String function, Object ... args) {
    	try {
    		this.invocableEngine.invokeFunction(function, args);
    	} catch (ScriptException e) {
        	e.printStackTrace();
        } catch (NoSuchMethodException e) {
        	e.printStackTrace();
        } catch (NullPointerException e) {
        	e.printStackTrace();
        }
    }
    
    private void setupTerrain() {
    	invokeScript("configureTerrain", this.getEngine());
    }
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // physics
    private void initPhysicsSystem() {  
    	String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
    	float[] gravity = {0, -3f, 0};
    	physicsEng = PhysicsEngineFactory.createPhysicsEngine(engine);
    	physicsEng.initSystem();
    	physicsEng.setGravity(gravity);
    	
    } 
    
    private void createRagePhysicsWorld() {
    	float up[] = {0,1,0};
    	double[] temptf;
    
		//setup terrain
    	final SceneNode terrainN = this.getEngine().getSceneManager().getSceneNode("tessN");
    	
		temptf = toDoubleArray(terrainN.getLocalTransform().toFloatArray());
		final PhysicsObject grndPObj = 
				physicsEng.addStaticPlaneObject(physicsEng.nextUID(), temptf, up, 0.0f);
		
		grndPObj.setBounciness(1.0f);
		grndPObj.setFriction(1.0f);
		terrainN.setLocalScale(100f, 200f, 100f);
		terrainN.setLocalPosition(0f, -2f, 0f);
		terrainN.setPhysicsObject(grndPObj);
    
    } 
    
    public PhysicsEngine getPhysicsEngine() {
		return physicsEng;
	}
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // audio
    
    public void setEarParameters(SceneManager sm) {
    	SceneNode playerN;
    	if(isSinglePlayer()) 
    		playerN = sm.getSceneNode("myPlayerNode");
    	else {
    		if(p1Health > 0)
    			playerN = sm.getSceneNode("myPlayerNode");
    		else
    			playerN = ghostAvatars.get(0).getNode();
    	}
    	Vector3 avDir = playerN.getWorldForwardAxis();
    	audioMgr.getEar().setLocation(playerN.getWorldPosition());
    	audioMgr.getEar().setOrientation(avDir, Vector3f.createFrom(0, 1, 0));
    	
    }
    
    public IAudioManager getAudioManager() {
    	return this.audioMgr;
    }
    
    public void initAudio(SceneManager sm) {
    	AudioResource resource1, resource2, resource3, resource4, resource5, resource6, resource7, resource8;
    	audioMgr = AudioManagerFactory.createAudioManager("ray.audio.joal.JOALAudioManager");
    	if (!audioMgr.initialize()) {
    		System.out.println("Audio Manager failed to initialize");
    		return;
    	}
    	resource1 = audioMgr.createAudioResource("gun.wav", AudioResourceType.AUDIO_SAMPLE);
    	resource2 = audioMgr.createAudioResource("Zombie SFX_1.wav", AudioResourceType.AUDIO_SAMPLE);
    	resource3 = audioMgr.createAudioResource("Zombie SFX_2.wav", AudioResourceType.AUDIO_SAMPLE);
    	resource4 = audioMgr.createAudioResource("Zombie SFX_3.wav", AudioResourceType.AUDIO_SAMPLE);
    	resource5 = audioMgr.createAudioResource("Zombie SFX_4.wav", AudioResourceType.AUDIO_SAMPLE);
    	resource6 = audioMgr.createAudioResource("Zombie SFX_5.wav", AudioResourceType.AUDIO_SAMPLE);
    	resource7 = audioMgr.createAudioResource("Zombie SFX_6.wav", AudioResourceType.AUDIO_SAMPLE);
    	resource8 = audioMgr.createAudioResource("OutBreakSoundTrack.wav", AudioResourceType.AUDIO_SAMPLE);

    	gunSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, false);
    	gunSound2 = new Sound(resource1, SoundType.SOUND_EFFECT, 100, false);
    	zombieSound1 = new Sound(resource2, SoundType.SOUND_EFFECT, 100, false);
    	zombieSound2 = new Sound(resource3, SoundType.SOUND_EFFECT, 100, false);
    	zombieSound3 = new Sound(resource4, SoundType.SOUND_EFFECT, 100, false);
    	zombieSound4 = new Sound(resource5, SoundType.SOUND_EFFECT, 100, false);
    	zombieSound5 = new Sound(resource6, SoundType.SOUND_EFFECT, 100, false);
    	zombieSound6 = new Sound(resource7, SoundType.SOUND_EFFECT, 100, false);
    	bgm = new Sound(resource8, SoundType.SOUND_MUSIC, 30, true);
    	
    	zombieSound1.initialize(audioMgr);
    	zombieSound1.setMaxDistance(10.0f);
    	zombieSound1.setMinDistance(0.5f);
    	zombieSound1.setRollOff(5.0f);
    	zombieSound2.initialize(audioMgr);
    	zombieSound2.setMaxDistance(10.0f);
    	zombieSound2.setMinDistance(0.5f);
    	zombieSound2.setRollOff(5.0f);
    	zombieSound3.initialize(audioMgr);
    	zombieSound3.setMaxDistance(10.0f);
    	zombieSound3.setMinDistance(0.5f);
    	zombieSound3.setRollOff(5.0f);
    	zombieSound4.initialize(audioMgr);
    	zombieSound4.setMaxDistance(10.0f);
    	zombieSound4.setMinDistance(0.5f);
    	zombieSound4.setRollOff(5.0f);
    	zombieSound5.initialize(audioMgr);
    	zombieSound5.setMaxDistance(10.0f);
    	zombieSound5.setMinDistance(0.5f);
    	zombieSound5.setRollOff(5.0f);
    	zombieSound6.initialize(audioMgr);
    	zombieSound6.setMaxDistance(10.0f);
    	zombieSound6.setMinDistance(0.5f);
    	zombieSound6.setRollOff(5.0f);
    	
    	gunSound.initialize(audioMgr);
    	gunSound2.initialize(audioMgr);
    	gunSound.setMaxDistance(10.0f);
    	gunSound.setMinDistance(0.5f);
    	gunSound.setRollOff(5.0f);
    	gunSound2.setMaxDistance(10.0f);
    	gunSound2.setMinDistance(0.5f);
    	gunSound2.setRollOff(5.0f);
    	
    	bgm.initialize(audioMgr);
    	bgm.setMaxDistance(10.0f);
    	bgm.setMinDistance(0.1f);
    	bgm.setRollOff(5.0f);
    	
    	gunSound.setLocation(sm.getSceneNode("myPlayerNode").getWorldPosition());
    	bgm.setLocation(sm.getSceneNode("myPlayerNode").getWorldPosition());
		setEarParameters(sm);
		
		bgm.play();
		
		//add sounds to sound array
		zombGrunts[0] = zombieSound1;
		zombGrunts[1] = zombieSound2;
		zombGrunts[2] = zombieSound3;
		zombGrunts[3] = zombieSound4;
		zombGrunts[4] = zombieSound5;
		zombGrunts[5] = zombieSound6;
		
		System.out.println("zombie sounds: " + zombGrunts.length);
    	
    	
    }
    
    public void playGunSound(){
		//this.gunSound.play();
	}
    
    public void playGAGunSound() {
    	//this.gunSound2.play();
    }
    
    public void zombieGrowl(int index){
    	int num = index;
    	switch(num) {
    	case 0:
    		zombieSound1.play();
    		break;
    	case 1:
    		zombieSound2.play();
    		break;
    	case 2:
    		zombieSound3.play();
    		break;
    	case 3:
    		zombieSound4.play();
    		break;
    	case 4:
    		zombieSound5.play();
    		break;
    	case 5:
    		zombieSound6.play();
    		break;
    	default:
    		break;
    	
    	}
	}
    
    

  //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //updating game engine
    
    @Override
    protected void update(Engine engine) {
    	if(survived == null) {
		// Set Up Text Display
		rs = (GL4RenderSystem) engine.getRenderSystem();
		if(isSinglePlayer()) {
			elapsTime += engine.getElapsedTimeMillis();
		}else {
			if(ghostAvatars.isEmpty() != true) {
				elapsTime += engine.getElapsedTimeMillis();
			}
		}
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		timeLimit = (timeLimit - elapsTimeSec);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		counterStr = Integer.toString(counter);
		float elapsThinkSec = (elapsTimeSec - lastThinkTime);
		float elapsShootSec = (elapsTimeSec - lastShootTime);
		if (p1Health > 0) {
			dispStr = " Health = " + p1Health;
			im.update(elapsTime);
		}
		else
			dispStr = " You have died ";
		dispStr2 = " Time left: " + Math.round(timeLimit / 1000.0f);
		rs.setHUD(dispStr, 15, rs.getRenderWindow().getViewport(0).getActualBottom()+5);
		rs.setHUD2(dispStr2, 215, rs.getRenderWindow().getViewport(0).getActualBottom()+5);
		SceneManager sm = engine.getSceneManager();
		
		// process movement for keyboard, doing this way to make animations smooth
		Node avN = sm.getSceneNode("myPlayerNode");
		//if forward key pressed move forward
		if (this.getForward() == true) {
			//in bounds?
			if ((avN.getLocalPosition().x() < 50.0f && avN.getLocalPosition().x() > -50.0f) && (avN.getLocalPosition().z() < 50.0f && avN.getLocalPosition().z() > -50.0f)) {
				avN.moveUp(0.08f);
				avN.setLocalPosition(avN.getLocalPosition().x(), 0.5f, avN.getLocalPosition().z());

				updateVerticalPosition();
				protClient.sendMoveMessage(avN.getWorldPosition());
			}
			else
				avN.moveDown(2.5f);
		}
		//if backward key pressed move backward
		if (this.getBackward() == true) {
			//in bounds?
			if ((avN.getLocalPosition().x() < 50.0f && avN.getLocalPosition().x() > -50.0f) && (avN.getLocalPosition().z() < 50.0f && avN.getLocalPosition().z() > -50.0f)) {
				avN.moveDown(0.08f);
				avN.setLocalPosition(avN.getLocalPosition().x(), 0.5f, avN.getLocalPosition().z());

				updateVerticalPosition();
				protClient.sendMoveMessage(avN.getWorldPosition());
			}
			else
				avN.moveUp(2.5f);
		}
		
		
		for(int i = 0; i<ghostNPCs.size(); i++) {
			//update sound location
			//zombGrunts[i].setLocation(ghostNPCs.get(i).getNode().getWorldPosition());
			// Check collision of bullets and zombies
			bulletID = getBulletID();
			if(bulletID != null) {
				if(isSinglePlayer()) {
					if (checkCollision(sm.getSceneNode("bullet" + bulletID + "node"), ghostNPCs.get(i).getNode())){
						ghostNPCs.get(i).getNode().moveDown(0.7f);
					}
					else if(checkCollision(sm.getSceneNode("bullet"+bulletID+"node"), sm.getSceneNode("npcNfat"))) {
						sm.getSceneNode("npcNfat").moveDown(0.2f);
						fathealth = fathealth - 10;
					}
				}
				else {
					if (checkCollision(sm.getSceneNode("bullet" + bulletID + "node"), ghostNPCs.get(i).getNode())){
						ghostNPCs.get(i).getNode().moveDown(0.7f);
					}
				}
			}
			if(isSinglePlayer()) {
					//check collision with zombie, zombie npc behavior
					if (checkCollision(sm.getSceneNode("myPlayerNode"), ghostNPCs.get(i).getNode())){
						this.player1Attacked();
						if(this.p1Health <= 0) {
							this.lookAtPlayer(ghostNPCs.get(i).getNode());
						}
					}
					else {
						this.lookAtPlayer(ghostNPCs.get(i).getNode());
					}
				}
			SkeletalEntity zombSE = (SkeletalEntity) sm.getEntity("ghostNPC"+i);
			zombSE.update();
					//npc AI constant behavior, also collision with players
					//only do this if two players
					if(ghostAvatars.isEmpty() != true) {
						//if player 1 and zombie collide, attack him
						if (checkCollision(sm.getSceneNode("myPlayerNode"), ghostNPCs.get(i).getNode())){
							this.player1Attacked();
							if(this.p1Health <= 0)
								this.lookAtPlayer(ghostNPCs.get(i).getNode());
						}
						// same thing but with player 2!
						else if (checkCollision(ghostAvatars.get(0).getNode(), ghostNPCs.get(i).getNode())) {
							if(ghostAvatars.get(0).getHealth() <= 0)
								this.lookAtPlayer(ghostNPCs.get(i).getNode());
						}
						//else do npc behavior and find closest player and move towards
						else {
							this.lookAtPlayer(ghostNPCs.get(i).getNode());
						}
					}
		}
		//fat zombie npc behavior
		if(isSinglePlayer()) {
			//update sound location
			//zombGrunts[5].setLocation(sm.getSceneNode("npcNfat").getWorldPosition());
			SkeletalEntity fatSE = (SkeletalEntity) sm.getEntity("ghostNPCFat");
			fatSE.update();
			SkeletalEntity smallSE1 = (SkeletalEntity) sm.getEntity("ghostNPCSmall1");
			smallSE1.update();
			SkeletalEntity smallSE2 = (SkeletalEntity) sm.getEntity("ghostNPCSmall2");
			smallSE2.update();
			//check if fat zombie is colliding with player, if so attack else look at and move towards him
			if (checkCollision(sm.getSceneNode("myPlayerNode"), sm.getSceneNode("npcNfat"))
					|| checkCollision(sm.getSceneNode("myPlayerNode"), sm.getSceneNode("npcNsmall2"))
					|| checkCollision(sm.getSceneNode("myPlayerNode"), sm.getSceneNode("npcNsmall1"))){
				this.player1Attacked();
				npcSwiping();
				fRunning(false);
				if(this.p1Health <= 0) {
					this.lookAtPlayer(sm.getSceneNode("npcNfat"));
					this.lookAtPlayer(sm.getSceneNode("npcNfat"));
					this.lookAtPlayer(sm.getSceneNode("npcNfat"));
				}
				}else {
					this.lookAtPlayer(sm.getSceneNode("npcNfat"));
					this.lookAtPlayer(sm.getSceneNode("npcNsmall1"));
					this.lookAtPlayer(sm.getSceneNode("npcNsmall2"));
					fRunning(true);
				}
			if(npcFAnimating() == false && getFRunning() == true) {
					fatSE.playAnimation("zombRun", 0.5f, SkeletalEntity.EndType.LOOP, 0);
					setNPCFAnimating(true);
			}
			if(fathealth <= 0) {
				 engine.getSceneManager().getController(1).addNode(engine.getSceneManager().getSceneNode("zombiesNodeG"));
			}

		}
		
		
		
		 //check ghosts (2 player game so only should have one)
	      for(int i = 0; i<ghostAvatars.size(); i++) {
	    	  gunSound2.setLocation(ghostAvatars.elementAt(i).getNode().getWorldPosition());
	      	}
		
		//check if player dead
		if(this.p1Health <= 0) {
			this.playerDeath();
			if(ghostNPCs.isEmpty() != true && isSinglePlayer() != true)
				orbitController.updateCameraOnDeath(ghostAvatars.get(0).getNode()); 
		}
		//npc AI think behavior
		if (elapsThinkSec >= 5.0f){
			lastThinkTime = elapsTime / 1000.0f;
			//play zombie sound
			for(int i = 0; i<ghostNPCs.size(); i++) {
				if(ghostNPCs.isEmpty() != true) {
					//zombieGrowl(i);
				}
			}
			//zombieGrowl(5);
		}
		// can player shoot?
		if (this.getIsIdle() && this.getIsShooting()) {
			lastShootTime = elapsTime / 1000.0f;
			this.setIsShooting(false);
		}
		if (elapsShootSec >= 0.5f) {
			this.canShoot(true);
		}
		else {
			this.canShoot(false);
		}
		// player animation behavior times
		SkeletalEntity playerSE = (SkeletalEntity) sm.getEntity("playerAv");
		playerSE.update();
		if (this.getIsIdle()) {
			this.setIsAnimating(false);
			sm.getSceneNode("gun1Node").getAttachedObject(0).setVisible(true);
			sm.getSceneNode("shotgun1Node").getAttachedObject(0).setVisible(true);
			if(this.getCanShoot())
				this.playerIdle();
			else
				this.playerShooting();
		}
		if(this.getIsAnimating() == true) {
			sm.getSceneNode("shotgun1Node").getAttachedObject(0).setVisible(false);
		}
		//player 2 animation behavior times
		if(ghostAvatars.isEmpty() != true) {
			SkeletalEntity player2SE = (SkeletalEntity) sm.getEntity("ghostAv");
			player2SE.update();
			if (this.getIsGAIdle()) {
				this.setIsGAAnimating(false);
				sm.getSceneNode("gun2Node").getAttachedObject(0).setVisible(true);
				sm.getSceneNode("shotgun2Node").getAttachedObject(0).setVisible(true);
			}
			if(this.getIsGAAnimating() == true) {
				sm.getSceneNode("shotgun2Node").getAttachedObject(0).setVisible(false);
			}
			//load up gun with player2
			//updateGunLocation(sm.getSceneNode("gun2Node"), ghostAvatars.get(0).getNode());
		}
		  
		
		
		//update camera
		orbitController.updateCameraPosition();
		
		//update gun visibility
		
		//updateGunLocation(sm.getSceneNode("gun1Node"), sm.getSceneNode("myPlayerNode"));
		
		// networking
		processNetworking(elapsTime);
		
		//scripting for terrain
		long modTime = terrainScript.lastModified();
		if(modTime > fileLastModifiedTime) {
			fileLastModifiedTime = modTime;
			this.runScript(terrainScript);
			sm.getTessellation((String) jsEngine.get("TESSELLATION_ENTITY")).setHeightMap(engine, (String) jsEngine.get("TERRAIN_HEIGHTMAP"));
		}
		
		//physics for bullets
		Matrix4 mat;
		physicsEng.update(elapsTime);
		for (SceneNode s : sm.getSceneNodes()){
			if (s.getPhysicsObject() != null){
				mat = Matrix4f.createFrom(toFloatArray(s.getPhysicsObject().getTransform()));
				s.setLocalPosition(mat.value(0, 3), mat.value(1, 3), mat.value(2, 3));
				}
			}  
		//audio
		//gunSound.setLocation(sm.getSceneNode("myPlayerNode").getWorldPosition());
		//if(isSinglePlayer())
		//	bgm.setLocation(sm.getSceneNode("myPlayerNode").getWorldPosition());
		//else {
		//	if(p1Health > 0)
		//		bgm.setLocation(sm.getSceneNode("myPlayerNode").getWorldPosition());
		//	else
		//		bgm.setLocation(ghostAvatars.get(0).getNode().getWorldPosition());
		//}
		//setEarParameters(sm);
		
		
		
		//win condition
		if(isSinglePlayer()) {
			if(timeLimit <= 0 || p1Health <= 0) {
				if(p1Health > 0) {
					survived = true;
					endGame();
				
				}
				else {
					survived = false;
					endGame();
				}
			}
		}else {
			if(timeLimit <= 0 || (p1Health <= 0 && ghostAvatars.get(0).getHealth() <= 0)) {
				if(p1Health > 0 || ghostAvatars.get(0).getHealth() > 0) {
					survived = true;
					endGame();
				}
				else {
					survived = false;
					endGame();
				}
			}
		}
	
		
    	}	
	}
    
    
    
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //networking
    
    protected void processNetworking(float elapsTime){ 
    	// Process packets received by the client from the server
    	if (protClient != null)
    		protClient.processPackets();
    	// remove ghost avatars for players who have left the game
    	Iterator<UUID> it = gameObjectsToRemove.iterator();
    	while(it.hasNext()){
    		this.getEngine().getSceneManager().destroySceneNode("ghostAvNode");
    	}
    	gameObjectsToRemove.clear();
    	Iterator<Integer> it2 = gameNPCsToRemove.iterator();
    	while(it2.hasNext()) {
    		
    		//System.out.println(this.getEngine().getSceneManager().hasSceneNode("npcN" + it2.next()));
    	}
    	gameNPCsToRemove.clear();
    	
    } 
    
    public String getPlayerAvatar() {
    	return this.avatar;
    }
    
    public Vector3 getPlayerPosition() {
    	SceneNode playerN = this.getEngine().getSceneManager().getSceneNode("myPlayerNode");
    	return playerN.getWorldPosition();
    	
    } 
    
    public Vector3 getPlayerRotationX() {
		SceneNode playerN = this.getEngine().getSceneManager().getSceneNode("myPlayerNode");
		return playerN.getLocalRightAxis();
	}
    
    public Vector3 getPlayerRotationY() {
		SceneNode playerN = this.getEngine().getSceneManager().getSceneNode("myPlayerNode");
		return playerN.getLocalUpAxis();
	}
    
    public Vector3 getPlayerRotationZ() {
		SceneNode playerN = this.getEngine().getSceneManager().getSceneNode("myPlayerNode");
		return playerN.getLocalForwardAxis();
	}
    
    public void setIsConnected(boolean isConnected) {
		this.isClientConnected = isConnected;
	}
    
    public boolean getIsConnected() {
		return isClientConnected;
	}
    
    public void addGhostAvatarToGameWorld(GhostAvatar avatar, String skin)throws IOException{
    	
    	
    	 if (avatar != null)
    	 {	
    		SkeletalEntity ghostE;
    		Texture tex;
    		if (skin.compareTo("Grim") == 0) { 
    			ghostE = this.getEngine().getSceneManager().createSkeletalEntity("ghostAv", "swatM.rkm", "swatS.rks");
    			tex = this.getEngine().getSceneManager().getTextureManager().getAssetByPath("swat1.png");
    			ghostE.loadAnimation("shoot", "swatSA1.rka");
            	ghostE.loadAnimation("run", "swatRA.rka");
            	ghostE.loadAnimation("bRun", "swatRBA.rka");
            	ghostE.loadAnimation("idle", "swatIA.rka");
            	ghostE.loadAnimation("death", "swatDA.rka");
    		}
    		else {
    			ghostE = this.getEngine().getSceneManager().createSkeletalEntity("ghostAv", "alexM.rkm", "alexS.rks");
    			tex = this.getEngine().getSceneManager().getTextureManager().getAssetByPath("alex1.png");
    			ghostE.loadAnimation("shoot", "alexSA1.rka");
            	ghostE.loadAnimation("run", "alexRA.rka");
            	ghostE.loadAnimation("bRun", "alexRBA.rka");
            	ghostE.loadAnimation("idle", "alexIA.rka");
            	ghostE.loadAnimation("death", "alexDA.rka");
    		}
    	 	SceneNode ghostN = this.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode("ghostAvNode");
    	    TextureState texState = (TextureState) this.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	    texState.setTexture(tex);
    		ghostE.setRenderState(texState);
    	 	ghostN.attachObject(ghostE);
    	 	ghostN.setLocalPosition(avatar.getPosition());
    		ghostN.pitch(Degreef.createFrom(90.0f));
    		ghostN.roll(Degreef.createFrom(-45.0f));
    		ghostN.scale(0.005f, 0.005f, 0.005f);
    	 	avatar.setNode(ghostN);
    	 	avatar.setEntity(ghostE);
    	 	avatar.setPosition(ghostN.getLocalPosition());
    	 	System.out.println("Player 2 Created");
    	 	ghostAvatars.add(avatar);
    	 	shoot2Action = new ShootAction(ghostN, this, protClient);
            //create gun for player2
            makeGun(this.getEngine(), this.getEngine().getSceneManager(), 2);
            makeShotgun(this.getEngine(),this.getEngine().getSceneManager(), 2);
            this.getEngine().getSceneManager().getSceneNode("gun2Node").getAttachedObject(0).setVisible(false);
    	 	
    	 }
    	 	
    	
    }
    
    public void removeGhostAvatarFromGameWorld(GhostAvatar avatar){   
    	if(avatar != null) gameObjectsToRemove.add(avatar.getUUID());
    }
    
    //same now for NPC
    
    public void addGhostNPCToGameWorld(GhostNPC npc)throws IOException{
    	
   		if (npc != null) {
   			SkeletalEntity ghostE = this.getEngine().getSceneManager().createSkeletalEntity("ghostNPC" + npc.getID(), "mZombieM.rkm", "mZombieS.rks");
   			ghostE.setPrimitive(Primitive.TRIANGLES);
   	   	 	SceneNode ghostN = this.getEngine().getSceneManager().getRootSceneNode(). createChildSceneNode("npcN"+npc.getID());
   	   	 	
   	   	 Texture[] textures = new Texture[3];
   	   	 	
   	   	 int zombTex = randInRangeInt(0, 2);
   			 Texture tex1 = this.getEngine().getSceneManager().getTextureManager().getAssetByPath("mZombie2.png");
   			 Texture tex2 = this.getEngine().getSceneManager().getTextureManager().getAssetByPath("mZombie1.png");
   			 Texture tex3 = this.getEngine().getSceneManager().getTextureManager().getAssetByPath("mZombie3.png");
   			 textures[0] = tex1;
   			 textures[1] = tex2;
   			 textures[2] = tex3;
   			 TextureState texState = (TextureState) this.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
   			 texState.setTexture(textures[zombTex]);
   	   	 	

   	   		ghostE.setRenderState(texState);
   	   	 	ghostN.attachObject(ghostE);
   	   	 	ghostN.setLocalPosition(npc.getPosition());
   	   	 	ghostN.scale(0.005f, 0.005f, 0.005f);
   	   	 	
   	   	 	ghostE.loadAnimation("zombRun", "mZombieRA.rka");
	    	ghostE.loadAnimation("zombHit", "mZombieHA.rka");
	    	ghostE.playAnimation("zombRun", 0.5f, SkeletalEntity.EndType.LOOP, 0);
	    	
	    	
   	   	 	npc.setNode(ghostN);
   	   	 	npc.setEntity(ghostE);
   	   	 	npc.setPosition(ghostN.getLocalPosition());
   	   	 	ghostNPCs.add(npc);

   	 }
   }
   
    public void removeGhostNPCFromGameWorld(GhostNPC npc){   
    	if(npc != null) {
    		this.getEngine().getSceneManager().destroySceneNode(npc.getNode().getName());
			ghostNPCs.remove(npc);
    	}
    }
     
    
    
    

  //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //set up inputs
   
    protected void setupInputs(SceneManager sm) {
    	String kbName = im.getKeyboardName();
    	String gpName = im.getFirstGamepadName();
    	String mName = im.getMouseName();
    	SceneNode playerN =
    			getEngine().getSceneManager().getSceneNode("myPlayerNode");


    	
    	//creating actions for inputs for player one
    	quitGameAction = new QuitGameAction(this, protClient);
		moveYGP = new MoveYGP(playerN, this, protClient);
    	rotatePlayerRight = new RotatePlayerRight(playerN, orbitController, protClient);
    	rotatePlayerLeft = new RotatePlayerLeft(playerN, orbitController, protClient);
		rotateDolphinXGP = new RotatePlayerXGP(playerN, orbitController, protClient);
		moveForwardAction = new MoveForwardAction(playerN, this, protClient);
		moveBackwardAction = new MoveBackwardAction(playerN, this, protClient);
		shootAction = new ShootAction(playerN, this, protClient);
		lightAction = new LightAction(this);
		
		

    	
    	// attach the action objects to components
		if(this.gamepad == false) {
    	
			// keyboard
			if(im.getKeyboardName() != null && this.p1Health > 0) {
    			kbName = im.getKeyboardName();
    		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.Q, 
    				quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    	
    		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.W, 
    				moveForwardAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);
    	
    		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.S, 
    				moveBackwardAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);
    	
    		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.D, 
    				rotatePlayerRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    
			im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.A, 
					rotatePlayerLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	
	    
			im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.SPACE, 
	    			shootAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
			im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.L, 
	    			lightAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    	
    	 
    		}

    	//mouse
    		if(im.getMouseName() != null) {
    			mName = im.getMouseName();
    		
    			im.associateAction(mName, net.java.games.input.Component.Identifier.Button.LEFT, 
    					shootAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    		
    		}
		
		}
		else {
			// gamepad 
			if(im.getFirstGamepadName() != null && this.p1Health > 0) {
	    		gpName = im.getFirstGamepadName();
	    		
 		    	im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Y, 
 		    			moveYGP, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);
		    
 		    	im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.RY, 
		    			rotateCameraYGP, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    	
		    	im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.X, 
		    			rotateDolphinXGP, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    
		    	im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._0, 
		    			shootAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		    
		    	im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._4, 
		    			lightAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		    
				im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._8, 
						quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		    	
    		}
		}	

    }
 
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //mouse set up
    
    private void initMouseMode(RenderSystem r, RenderWindow w)
    {
    	rw = w;
    	rs2 = r;
    	Viewport v = rw.getViewport(0);
    	int left = rw.getLocationLeft();
    	int top = rw.getLocationTop();
    	int widt = v.getActualScissorWidth();
    	int hei = v.getActualScissorHeight();
    	int centerX = left + widt/2;
    	int centerY = top + hei/2;
    	isRecentering = false;
    	try// note that some platforms may not support the Robot class
    	{ 
    		robot = new Robot();
    		} catch (AWTException ex)
    	{ throw new RuntimeException("Couldn't create Robot!"); }
    	recenterMouse();
    	prevMouseX = centerX;
    	// 'prevMouse' defines the initial
    	prevMouseY = centerY;//   mouse position
    	
//    	// also change the cursor
//    	Image mouseImage = new
//    			ImageIcon("./assets/images/mouse.gif").getImage();
//    	Cursor mouseCursor = Toolkit.getDefaultToolkit().
//    			createCustomCursor(mouseImage, new Point(0,0), "mouseCursor");
//    	canvas = rs2.getCanvas();
//    	canvas.setCursor(mouseCursor);
    	
    	} 
    private void recenterMouse()  {
    	// use the robot to move the mouse to the center point.
    	// Note that this generates one MouseEvent. 
    	Viewport v = rw.getViewport(0);
    	int left = rw.getLocationLeft();
    	int top = rw.getLocationTop();
    	int widt = v.getActualScissorWidth();
    	int hei = v.getActualScissorHeight();
    	int centerX = left + widt/2;
    	int centerY = top + hei/2;
    	isRecentering = true;
    	robot.mouseMove((int)centerX, (int)centerY); 
    }
    public void mouseMoved(MouseEvent e)  {
    	// if robot is recentering and the MouseEvent location is in the center,
    	// then this event was generated by the robot
    	int centerX = 0, centerY = 0;
    	if (isRecentering &&centerX == e.getXOnScreen() && centerY == e.getYOnScreen())
    	{ isRecentering = false; } 
    	// mouse recentered, recentering complete
    	else
    	{  // event was due to a user mouse-move, and must be processed
    		curMouseX = e.getXOnScreen();
    		curMouseY = e.getYOnScreen();
    		prevMouseX = curMouseX;
    		prevMouseY = curMouseY;
    		// tell robot to put the cursor to the center (since user just moved it)
    		recenterMouse();
    		prevMouseX = centerX;
    		//reset prev to center
    		prevMouseY = centerY; 
    	}
    }
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //animation functions
    
    //player 1
    public void playerShoot(UUID id) {
    	this.setIsShooting(true);
    	this.playGunSound();
    	this.playerShooting();
    	this.getEngine().getSceneManager().getController(0).addNode(this.getEngine().getSceneManager().getSceneNode("bullet" + id.toString() + "node"));
    	this.setBulletID(id.toString());
    }
    
    public void canShoot(boolean shoot) {
    	this.ableToShoot = shoot;
    }
    
    public boolean getCanShoot() {
    	return this.ableToShoot;
    }
    
    public boolean getIsShooting() {
    	return this.isShooting;
    }
    
    public void setIsShooting(boolean shooting) {
    	this.isShooting = shooting;
    }
    
    public void playerRun() {
    	SkeletalEntity playerSE = (SkeletalEntity) this.getEngine().getSceneManager().getEntity("playerAv");
    	if(this.getIsRunning() == true && this.getIsAnimating() == false)
    		playerSE.playAnimation("run", 0.6f, SkeletalEntity.EndType.LOOP, 0);
    	this.setIsAnimating(true);
    }
    
    
    public void playerBRun() {
    	SkeletalEntity playerSE = (SkeletalEntity) this.getEngine().getSceneManager().getEntity("playerAv");
    	if(this.getIsBRunning() == true && this.getIsAnimating() == false)
    		playerSE.playAnimation("bRun", 0.6f, SkeletalEntity.EndType.LOOP, 0);
    	this.setIsAnimating(true);
    }
    
    public void playerIdle() {
    	SkeletalEntity playerSE = (SkeletalEntity) this.getEngine().getSceneManager().getEntity("playerAv");
    	if (this.getIsShooting() == false) {
    		playerSE.playAnimation("idle", 0.5f, SkeletalEntity.EndType.LOOP, 0);
    		this.getEngine().getSceneManager().getSceneNode("gun1Node").getAttachedObject(0).setVisible(false);
    	}
    }
    
    public void playerShooting() {
    	SkeletalEntity playerSE = (SkeletalEntity) this.getEngine().getSceneManager().getEntity("playerAv");
    	playerSE.stopAnimation();
    	playerSE.playAnimation("shoot", 0.5f, SkeletalEntity.EndType.NONE, 0);
    	this.getEngine().getSceneManager().getSceneNode("gun1Node").getAttachedObject(0).setVisible(true);
    }
    
    public void playerDeath() {
    	SkeletalEntity playerSE = (SkeletalEntity) this.getEngine().getSceneManager().getEntity("playerAv");
    	this.setIsIdle(false);
    	this.setIsRunning(false);
    	this.setIsBRunning(false);
    	this.canShoot(false);
    	this.moveAvatarForward(false);
    	this.moveAvatarBackward(false);
    	playerSE.stopAnimation();
    	playerSE.playAnimation("death", 0.5f, SkeletalEntity.EndType.NONE, 0);
    	this.getEngine().getSceneManager().getSceneNode("shotgun1Node").getAttachedObject(0).setVisible(false);
    	protClient.sendAnimation("death");
    }
    
    public boolean getIsRunning() {
    	return this.isRunning;
    }
    
    public boolean getIsIdle() {
    	return this.isIdle;
    }
    
    public boolean getIsBRunning() {
    	return this.isBRunning;
    }
    
    public void setIsRunning(boolean run) {
    	this.isRunning = run;
    }
    
    public void setIsIdle(boolean idle) {
    	this.isIdle = idle;
    }
    
    public void setIsBRunning(boolean brun) {
    	this.isBRunning = brun;
    }
    
    public void setIsAnimating(boolean anim) {
    	this.isAnimating = anim;
    }
    
    public boolean getIsAnimating() {
    	return this.isAnimating;
    }
    
    //player 2
    
    public void player2Shoot(UUID id) {
    	this.playGAGunSound();
    	this.player2Shooting();
    	this.getEngine().getSceneManager().getController(0).addNode(this.getEngine().getSceneManager().getSceneNode("bullet" + id.toString() + "node"));
    	this.setBulletID(id.toString());
    }
    
    public void player2Bullet() {
    	try {
    	if(getIsGAAnimating() != true) {
			UUID id = UUID.randomUUID();
			Entity bulletE = getEngine().getSceneManager().createEntity("bullet" + id.toString(), "earth.obj");
			Texture tex = getEngine().getSceneManager().getTextureManager().getAssetByPath("whiteColor.jpg");
		    TextureState texState = (TextureState) getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		    texState.setTexture(tex);
			bulletE.setRenderState(texState);
			SceneNode bulletNode = getEngine().getSceneManager().getRootSceneNode().createChildSceneNode("bullet" + id.toString() + "node");
			bulletNode.scale(0.02f, 0.02f, 0.02f);
			bulletNode.attachObject(bulletE);
			Vector3 bulletNodePos = ghostAvatars.get(0).getNode().getWorldPosition();
			bulletNode.setLocalPosition(bulletNodePos.x(), bulletNodePos.y()+0.7f, bulletNodePos.z());
			
			PhysicsEngine physEng = getPhysicsEngine();
			double[] temptf = toDoubleArray(bulletNode.getLocalTransform().toFloatArray());
			PhysicsObject physObj = physEng.addSphereObject(physEng.nextUID(), 100f, temptf, 25f);
			physObj.setBounciness(0.25f);
			bulletNode.setPhysicsObject(physObj);
			
			final Vector3 forward = ghostAvatars.get(0).getNode().getLocalUpAxis();
			float xForce = forward.x() == 0 ? 0 : forward.x() * 400000f;
			float yForce = forward.y() == 0 ? 0 : forward.y() * 400000f;
			float zForce = forward.z() == 0 ? 0 : forward.z() * 400000f;
			
			physObj.applyForce(xForce, yForce, zForce, 0, 0, 0);
			
			
			player2Shoot(id);
    	}
    	}
		catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public boolean getIsGAShooting() {
    	return this.isShooting;
    }
    
    public void setIsGAShooting(boolean shooting) {
    	this.isShooting = shooting;
    }
    
    public void player2Run() {
    	SkeletalEntity playerSE = (SkeletalEntity) this.getEngine().getSceneManager().getEntity("ghostAv");
    	if(this.getIsGARunning() == true && this.getIsGAAnimating() == false)
    		playerSE.playAnimation("run", 0.6f, SkeletalEntity.EndType.LOOP, 0);
    	this.setIsGAAnimating(true);
    	this.getEngine().getSceneManager().getSceneNode("gun2Node").getAttachedObject(0).setVisible(false);
    }
    
    public void player2BRun() {
    	SkeletalEntity playerSE = (SkeletalEntity) this.getEngine().getSceneManager().getEntity("ghostAv");
    	if(this.getIsGABRunning() == true && this.getIsGAAnimating() == false)
    		playerSE.playAnimation("bRun", 0.6f, SkeletalEntity.EndType.LOOP, 0);
    	this.setIsGAAnimating(true);
    	this.getEngine().getSceneManager().getSceneNode("gun2Node").getAttachedObject(0).setVisible(false);
    }
    
    public void player2Idle() {
    	SkeletalEntity playerSE = (SkeletalEntity) this.getEngine().getSceneManager().getEntity("ghostAv");
    	if (this.getIsGAShooting() == false)
    		playerSE.playAnimation("idle", 0.5f, SkeletalEntity.EndType.LOOP, 0);
    	this.getEngine().getSceneManager().getSceneNode("gun2Node").getAttachedObject(0).setVisible(false);
    }
    
    public void player2Shooting() {
    	SkeletalEntity playerSE = (SkeletalEntity) this.getEngine().getSceneManager().getEntity("ghostAv");
    	playerSE.stopAnimation();
    	playerSE.playAnimation("shoot", 0.5f, SkeletalEntity.EndType.NONE, 0);
    	this.getEngine().getSceneManager().getSceneNode("gun2Node").getAttachedObject(0).setVisible(true);
    }
    
    public void playerGADeath() {
    	SkeletalEntity playerSE = (SkeletalEntity) this.getEngine().getSceneManager().getEntity("ghostAv");
    	this.setIsGAIdle(false);
    	this.setIsGARunning(false);
    	this.setIsGABRunning(false);
    	playerSE.stopAnimation();
    	playerSE.playAnimation("death", 0.5f, SkeletalEntity.EndType.NONE, 0);
    	this.getEngine().getSceneManager().getSceneNode("shotgun2Node").getAttachedObject(0).setVisible(false);
    	this.getEngine().getSceneManager().getSceneNode("gun2Node").getAttachedObject(0).setVisible(false);
    }
    
    public boolean getIsGARunning() {
    	return this.isGARunning;
    }
    
    public boolean getIsGAIdle() {
    	return this.isGAIdle;
    }
    
    public boolean getIsGABRunning() {
    	return this.isGABRunning;
    }
    
    public void setIsGARunning(boolean run) {
    	this.isGARunning = run;
    }
    
    public void setIsGAIdle(boolean idle) {
    	this.isGAIdle = idle;
    }
    
    public void setIsGABRunning(boolean brun) {
    	this.isGABRunning = brun;
    }
    
    public void setIsGAAnimating(boolean anim) {
    	this.isGAAnimating = anim;
    }
    
    public boolean getIsGAAnimating() {
    	return this.isGAAnimating;
    }
    
    //npc
    public void npcSwiping() {
    	SkeletalEntity ghostSE = (SkeletalEntity) this.getEngine().getSceneManager().getEntity("ghostNPCFat");
    	fRunning(false);
    	setNPCFAnimating(false);
    	ghostSE.stopAnimation();
    	ghostSE.playAnimation("zombHit", 0.5f, SkeletalEntity.EndType.NONE, 0);
    }
    
    public void fRunning(boolean run) {
    	this.fRunning = run;
    }
    
    public boolean getFRunning() {
    	return this.fRunning;
    }
    
    public boolean npcFAnimating() {
    	return npcFAnimating;
    }
    public void setNPCFAnimating(boolean anim) {
    	this.npcFAnimating = anim;
    }
    
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //misc functions for game
    
    //change display when game is over
    public void endGame() {
    	if (survived) {
    		this.getEngine().getRenderSystem().setHUD("Survived, good job agent",
    				rs.getRenderWindow().getViewport(0).getActualScissorWidth()/2 - 100, rs.getRenderWindow().getViewport(0).getActualScissorHeight()/2);
    		this.getEngine().getRenderSystem().setHUD2("");
    		for (int i = 0; i < ghostNPCs.size(); i++){
    			this.getEngine().getSceneManager().destroySceneNode(ghostNPCs.get(i).getNode());
    		}
    		if(isSinglePlayer())
    			this.getEngine().getSceneManager().destroySceneNode("npcNfat");
    	}
    	else {
    		this.getEngine().getRenderSystem().setHUD("This is an outbreak, survival is your own responsibility",
    				rs.getRenderWindow().getViewport(0).getActualScissorWidth()/2 - 200, rs.getRenderWindow().getViewport(0).getActualScissorHeight()/2);
    		this.getEngine().getRenderSystem().setHUD2("");
    	}
    	new QuitMenu(this, protClient);
    }
    
    //is game single player?
    public boolean isSinglePlayer() {
    	return this.isSingle;
    }
    
    //player gets hit
    public void player1Attacked() {
    	this.p1Health = p1Health - 5;
    	protClient.sendHealthMessage(5);
    }
    
    
    //bullet collision functions
    public String getBulletID() {
    	return this.bulletID;
    }
    
    public void setBulletID(String id) {
    	this.bulletID = id;
    }
    
    
    //server start
    public void setStart() {
    	this.start = true;
    }
    public boolean getStart() {
    	return this.start;
    }
    
    //update gun loc
    public void updateGunLocation(SceneNode gun, SceneNode player) {
    	gun.setLocalPosition(player.getWorldPosition());
    	gun.setLocalRotation(player.getWorldRotation());
    	gun.rotate(Degreef.createFrom(-90), Vector3f.createFrom(1, 0, 0));
    	gun.rotate(Degreef.createFrom(-90), Vector3f.createFrom(0,1,0));
    	gun.moveUp(0.68f);
    	gun.moveRight(0.28f);
    	gun.moveForward(0.05f);
    }
    
    
    // moving avatars with keyboard
    //forward
    public void moveAvatarForward(boolean move) {
    	this.forward = move;
    }
    //backward
    public void moveAvatarBackward(boolean move) {
    	this.backward = move;
    }
    //get functions for moving
    
    public boolean getForward() {
    	return this.forward;
    }
    public boolean getBackward() {
    	return this.backward;
    }
    
    // Get Active Node
    public SceneNode getActiveNode() {
    	return activeNode;
    }
    
    // Set Active Node
    public void setActiveNode(SceneNode sn) {
    	activeNode = sn;
    }
    
    // light switch
    public void setLightOn(boolean flip) {
    	this.lightOn = flip;
    }
    
    public boolean getLightOn() {
    	return this.lightOn;
    }
    
    
    // Collision Detection 
    public boolean checkCollision(SceneNode a, SceneNode b) {
    	float ax = a.getWorldPosition().x();
    	float ay = a.getWorldPosition().y();
    	float az = a.getWorldPosition().z();
    	float bx = b.getWorldPosition().x();
    	float by = b.getWorldPosition().y();
    	float bz = b.getWorldPosition().z();
    	
    	
    	
    	return (ax - bx < 0.6f && az - bz < 0.6f && ay - by < 1.0f && ax - bx > -0.6f && az - bz > -0.6f && ay - by > -1.0f );
    }
    
    // Create a random float variable
    public static float randInRangeFloat(float min, float max) {
        return min + (float) (Math.random() * ((1 + max) - min));
    }
 // Create a random int variable
    public static int randInRangeInt(int min, int max) {
        return min + (int) (Math.random() * ((1 + max) - min));
    }
    
	// Get Speed of player
    public float getSpeed() {
    	return maxSpeed;
    }
    
    //update height of player
    public void updateVerticalPosition(){
    	SceneNode playerN = this.getEngine().getSceneManager().getSceneNode("myPlayerNode");
    	SceneNode tessN = this.getEngine().getSceneManager().getSceneNode("tessN");
    	Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));
    	// Figure out Avatar's position relative to plane
    	Vector3 worldAvatarPosition = playerN.getWorldPosition();
    	Vector3 localAvatarPosition = playerN.getLocalPosition();
    	// use avatar World coordinates to get coordinates for height
    	Vector3 newAvatarPosition = Vector3f.createFrom(
    			// Keep the X coordinate
    			localAvatarPosition.x(),
    			// The Y coordinate is the varying height
    			tessE.getWorldHeight(
    					worldAvatarPosition.x(),
    					worldAvatarPosition.z()),
    					//Keep the Z coordinate
    					localAvatarPosition.z()
    	);
    	// use avatar Local coordinates to set position, including height
    	playerN.setLocalPosition(newAvatarPosition);
    } 
    
    //update tree heights
    public void updateTreePosition(SceneNode node){
    	SceneNode treeN = node;
    	SceneNode tessN = this.getEngine().getSceneManager().getSceneNode("tessN");
    	Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));
    	// Figure out Avatar's position relative to plane
    	Vector3 worldAvatarPosition = treeN.getWorldPosition();
    	Vector3 localAvatarPosition = treeN.getLocalPosition();
    	// use avatar World coordinates to get coordinates for height
    	Vector3 newAvatarPosition = Vector3f.createFrom(
    			// Keep the X coordinate
    			localAvatarPosition.x(),
    			// The Y coordinate is the varying height
    			tessE.getWorldHeight(
    					worldAvatarPosition.x(),
    					worldAvatarPosition.z()),
    					//Keep the Z coordinate
    					localAvatarPosition.z()
    	);
    	// use avatar Local coordinates to set position, including height
    	treeN.setLocalPosition(newAvatarPosition);
    } 
    
    //to float array
    public float[] toFloatArray(double[] arr){   
    	if (arr == null) return null;
    	int n = arr.length;
    	float[] ret = new float[n];
    	for (int i = 0; i < n; i++) {
    		ret[i] = (float)arr[i];
    		} 
    	return ret;
    	
    }
    //to double array
    public double[] toDoubleArray(float[] arr){
    	if (arr == null) 
    		return null;
    	int n = arr.length;
    	double[] ret = new double[n];
    	for (int i = 0; i < n; i++) {
    		ret[i] = (double)arr[i];
    		}
    	return ret;
    	
    }
    
    //npc functions
    
    public boolean whoseCloser(SceneNode node) {
    	if(this.getEngine().getSceneManager().hasEntity("ghostAv") && ghostAvatars.get(0).getHealth() >= 0) {
    		SceneNode player1 = this.getEngine().getSceneManager().getSceneNode("myPlayerNode");
    		SceneNode player2 = ghostAvatars.get(0).getNode();
    		Vector3 p1 = player1.getWorldPosition();
    		Vector3 p2 = player2.getWorldPosition();
    		Vector3 n = node.getWorldPosition();
    		if(Math.sqrt(Math.pow(n.x() - p1.x(), 2) + Math.pow(n.z() - p1.z(), 2)) < Math.sqrt(Math.pow(n.x() - p2.x(), 2) + Math.pow(n.z() - p2.z(), 2)) && this.p1Health >= 0) {
    			return true;
    		}
    		else {
    			return false;
    		}
    	}
    	else
    		return true;
    }
    
    public void lookAtPlayer(SceneNode node) {
    	if(this.whoseCloser(node)) {
    		node.lookAt(this.getEngine().getSceneManager().getSceneNode("myPlayerNode"));
    		node.pitch(Degreef.createFrom(90.0f));
    	}
    	else {
    		node.lookAt(ghostAvatars.get(0).getNode());
    		node.pitch(Degreef.createFrom(90.0f));
    	}
    	if(isSinglePlayer())
    		node.moveUp(0.073f);
    	else
    		node.moveUp(0.077f);
    	SceneNode tessN = this.getEngine().getSceneManager().getSceneNode("tessN");
    	Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));
    	// Figure out zombies' position relative to plane
    	Vector3 worldAvatarPosition = node.getWorldPosition();
    	Vector3 localAvatarPosition = node.getLocalPosition();
    	// use zombie World coordinates to get coordinates for height
    	Vector3 newAvatarPosition = Vector3f.createFrom(
    			// Keep the X coordinate
    			localAvatarPosition.x(),
    			// The Y coordinate is the varying height
    			tessE.getWorldHeight(
    					worldAvatarPosition.x(),
    					worldAvatarPosition.z())+0.1f,
    					//Keep the Z coordinate
    					localAvatarPosition.z()
    	);
    	// use zombie Local coordinates to set position, including height
    	node.setLocalPosition(newAvatarPosition);
    	
    	
    }
   
    
    
  //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //object creation
    
    //create skybox
    private void makeSkyBox(Engine eng, SceneManager sm) throws IOException{
    	Configuration conf = eng.getConfiguration();
    	TextureManager tm = getEngine().getTextureManager();
    	tm.setBaseDirectoryPath(conf.valueOf("assets.skyboxes.path"));
    	Texture front = tm.getAssetByPath("front.jpg");
    	Texture back = tm.getAssetByPath("back.jpg");
    	Texture left = tm.getAssetByPath("left.jpg");
    	Texture right = tm.getAssetByPath("right.jpg");
    	Texture top = tm.getAssetByPath("top.jpg");
    	Texture bottom = tm.getAssetByPath("bottom.jpg");
    	tm.setBaseDirectoryPath(conf.valueOf("assets.textures.path"));
    	// cubemap textures are flipped upside-down.
    	// All textures must have the same dimensions, so any image’s
    	// heights will work since they are all the same height
    	AffineTransform xform = new AffineTransform();
    	xform.translate(0, front.getImage().getHeight());
    	xform.scale(1d, -1d);
    	front.transform(xform);
    	back.transform(xform);
    	left.transform(xform);
    	right.transform(xform);
    	top.transform(xform);
    	bottom.transform(xform);
    	SkyBox sb = sm.createSkyBox(SKYBOX_NAME);
    	sb.setTexture(front, SkyBox.Face.FRONT);
    	sb.setTexture(back, SkyBox.Face.BACK);
    	sb.setTexture(left, SkyBox.Face.LEFT);
    	sb.setTexture(right, SkyBox.Face.RIGHT);
    	sb.setTexture(top, SkyBox.Face.TOP);
    	sb.setTexture(bottom, SkyBox.Face.BOTTOM);
    	sm.setActiveSkyBox(sb);
    }
    
    //create players
    private void makePlayer(Engine eng, SceneManager sm) throws IOException {
    	if (this.avatar == "Grim") {
    		SkeletalEntity playerSE = sm.createSkeletalEntity("playerAv", "grimM.rkm", "swatS.rks");
    	

	    	Texture tex = sm.getTextureManager().getAssetByPath("swat1.png");
	    	TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    	texState.setTexture(tex);
			playerSE.setRenderState(texState);

    		SceneNode playerN = sm.getRootSceneNode().createChildSceneNode("myPlayerNode");
    		playerN.moveLeft(0.4f);
    		playerN.pitch(Degreef.createFrom(90.0f));
    		playerN.roll(Degreef.createFrom(-45.0f));
    		playerN.scale(0.005f, 0.005f, 0.005f);
    		playerN.moveForward(0.3f);
    		playerN.attachObject(playerSE);
    	
    		playerSE.loadAnimation("shoot", "swatSA1.rka");
        	
        	playerSE.loadAnimation("run", "swatRA.rka");
        	
        	playerSE.loadAnimation("bRun", "swatRBA.rka");
        	
        	playerSE.loadAnimation("idle", "swatIA.rka");
        	
        	playerSE.loadAnimation("death", "swatDA.rka");
    	}else {
    		
    		SkeletalEntity playerSE = sm.createSkeletalEntity("playerAv", "alexM.rkm", "alexS.rks");

    	    	Texture tex = sm.getTextureManager().getAssetByPath("alex1.png");
    	    	TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	    	texState.setTexture(tex);
    			playerSE.setRenderState(texState);

        		SceneNode playerN = sm.getRootSceneNode().createChildSceneNode("myPlayerNode");
        		playerN.moveLeft(0.4f);
        		playerN.pitch(Degreef.createFrom(90.0f));
        		playerN.roll(Degreef.createFrom(-45.0f));
        		playerN.scale(0.005f, 0.005f, 0.005f);
        		playerN.moveForward(0.3f);
        		playerN.attachObject(playerSE);
        	
        		playerSE.loadAnimation("shoot", "alexSA1.rka");
            	
            	playerSE.loadAnimation("run", "alexRA.rka");
            	
            	playerSE.loadAnimation("bRun", "alexRBA.rka");
            	
            	playerSE.loadAnimation("idle", "alexIA.rka");
            	
            	playerSE.loadAnimation("death", "alexDA.rka");
    		
    	}
        

    }
    
    // create trees
    private SceneNode makeTree(Engine eng, SceneManager sm, int num) throws IOException {
    	Entity treeE = sm.createEntity("tree" + Integer.toString(num),	"tree.obj");
    	treeE.setPrimitive(Primitive.TRIANGLES);
		
		
		Material mat = sm.getMaterialManager().getAssetByPath("tree.mtl");
		 Texture tex = sm.getTextureManager().getAssetByPath("tree.jpg");
		 TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		 texState.setTexture(tex);
		 treeE.setRenderState(texState);
		 treeE.setMaterial(mat);
    	
    	SceneNode treeN = sm.getRootSceneNode().createChildSceneNode(treeE.getName() + "Node");
    	treeN.moveForward(randInRangeFloat(-spaceSize, spaceSize));
    	treeN.moveRight(randInRangeFloat(-spaceSize, spaceSize));
    	treeN.attachObject(treeE);

    	return treeN;
    	
    }
    
    //create gun
    private SceneNode makeGun(Engine eng, SceneManager sm, int num) throws IOException{
    	Entity gunE = sm.createEntity("gun" + Integer.toString(num), "gun.obj");
    	gunE.setPrimitive(Primitive.TRIANGLES);
		
		
		Material mat = sm.getMaterialManager().getAssetByPath("gun.mtl");
		 Texture tex = sm.getTextureManager().getAssetByPath("gunTex.png");
		 TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		 texState.setTexture(tex);
		 gunE.setRenderState(texState);
		 gunE.setMaterial(mat);
		 
		 SceneNode gunN;
		 if(num == 1) {
			 gunN = sm.getSceneNode("myPlayerNode").createChildSceneNode(gunE.getName() + "Node");
		 }
		 else {
			 gunN = sm.getSceneNode("ghostAvNode").createChildSceneNode(gunE.getName() + "Node");
		 }
    	
    	gunN.scale(6f, 6f, 6f);
    	gunN.moveUp(55f);
    	gunN.moveBackward(135f);
    	gunN.moveLeft(5f);
    	gunN.rotate(Degreef.createFrom(-90), Vector3f.createFrom(1, 0, 0));
    	gunN.rotate(Degreef.createFrom(-90), Vector3f.createFrom(0,1,0));
    	gunN.attachObject(gunE);

    	return gunN;
    }
    
    //create shotgun to put on players back
    private SceneNode makeShotgun(Engine eng, SceneManager sm, int num) throws IOException{
    	Entity gunE = sm.createEntity("shotgun" + Integer.toString(num), "shotgun.obj");
    	gunE.setPrimitive(Primitive.TRIANGLES);
		
		
		Material mat = sm.getMaterialManager().getAssetByPath("shotgun.mtl");
		 Texture tex = sm.getTextureManager().getAssetByPath("shotgunTEX.png");
		 TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		 texState.setTexture(tex);
		 gunE.setRenderState(texState);
		 gunE.setMaterial(mat);
		 
		 SceneNode gunN;
		 if(num == 1) {
			 gunN = sm.getSceneNode("myPlayerNode").createChildSceneNode(gunE.getName() + "Node");
		 }
		 else {
			 gunN = sm.getSceneNode("ghostAvNode").createChildSceneNode(gunE.getName() + "Node");
		 }
		 
		gunN.scale(25f, 25f, 25f);
		gunN.moveBackward(130f);
		gunN.moveDown(15.0f);
		gunN.moveRight(2.0f);
		gunN.pitch(Degreef.createFrom(-90.0f));
		gunN.roll(Degreef.createFrom(-45.0f));
    	gunN.attachObject(gunE);

    	return gunN;
    }
    
    
    //make grassy plane
    private SceneNode makeGrass(Engine eng, SceneManager sm) throws IOException {
    	Entity grassE = sm.createEntity("grass",	"grass.obj");
    	grassE.setPrimitive(Primitive.TRIANGLES);
		
		
		Material mat = sm.getMaterialManager().getAssetByPath("grass.mtl");
		 Texture tex = sm.getTextureManager().getAssetByPath("grass.jpg");
		 TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		 texState.setTexture(tex);
		 grassE.setRenderState(texState);
		 grassE.setMaterial(mat);
    	
    	SceneNode grassN = sm.getRootSceneNode().createChildSceneNode(grassE.getName() + "Node");
    	grassN.moveDown(2.1f);
    	grassN.attachObject(grassE);

    	return grassN;
    	
    }
    
    
    //create zombies
    private void makeFatZombie(Engine eng, SceneManager sm) throws IOException {
    	SceneNode zombiesNG = sm.getSceneNode("zombiesNodeG");
		SkeletalEntity ghostE = this.getEngine().getSceneManager().createSkeletalEntity("ghostNPCFat", "fZombieM.rkm", "fZombieS.rks");
  	 	SceneNode ghostN = zombiesNG.createChildSceneNode("npcNfat");
  	 	
  	 	
		 
		 Texture[] textures = new Texture[3];
	   	 	
   	   	 int zombTex = randInRangeInt(0, 2);
   			 Texture tex1 = this.getEngine().getSceneManager().getTextureManager().getAssetByPath("fZombie2.png");
   			 Texture tex2 = this.getEngine().getSceneManager().getTextureManager().getAssetByPath("fZombie1.png");
   			 Texture tex3 = this.getEngine().getSceneManager().getTextureManager().getAssetByPath("fZombie3.png");
   			 textures[0] = tex1;
   			 textures[1] = tex2;
   			 textures[2] = tex3;
   			 TextureState texState = (TextureState) this.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
  	 	
   		texState.setTexture(textures[zombTex]);
  		ghostE.setRenderState(texState);
  	 	ghostN.attachObject(ghostE);
  	 	ghostN.setLocalPosition(Vector3f.createFrom(randInRangeFloat(-spaceSize, spaceSize), 0.0f, randInRangeFloat(-spaceSize, spaceSize)));
		ghostN.scale(0.008f, 0.008f, 0.008f);
  	 	
	
    	
    	ghostE.loadAnimation("zombRun", "fZombieRA.rka");
    	
    	ghostE.loadAnimation("zombHit", "fZombieHA.rka");
    	
    	ghostE.playAnimation("zombRun", 0.5f, SkeletalEntity.EndType.LOOP, 0);
    	
    }
    
    // make small zombie
    
    private void makeSmallZombie(Engine eng, SceneManager sm, int num) throws IOException {
    	SceneNode smallZombiesNG = sm.getSceneNode("smallZombiesNodeG");
		SkeletalEntity ghostE = this.getEngine().getSceneManager().createSkeletalEntity("ghostNPCSmall" + Integer.toString(num), "fZombieM.rkm", "fZombieS.rks");
  	 	SceneNode ghostN = smallZombiesNG.createChildSceneNode("npcNsmall" + Integer.toString(num));
  	 	
  	 	
		 Texture[] textures = new Texture[3];
	   	 	
   	   	 int zombTex = randInRangeInt(0, 2);
   			 Texture tex1 = this.getEngine().getSceneManager().getTextureManager().getAssetByPath("fZombie2.png");
   			 Texture tex2 = this.getEngine().getSceneManager().getTextureManager().getAssetByPath("fZombie1.png");
   			 Texture tex3 = this.getEngine().getSceneManager().getTextureManager().getAssetByPath("fZombie3.png");
   			 textures[0] = tex1;
   			 textures[1] = tex2;
   			 textures[2] = tex3;
   			 TextureState texState = (TextureState) this.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
  	 	
   		texState.setTexture(textures[zombTex]);
  		ghostE.setRenderState(texState);
  	 	ghostN.attachObject(ghostE);
  	 	ghostN.setLocalPosition(Vector3f.createFrom(randInRangeFloat(-spaceSize, spaceSize), 0.0f, randInRangeFloat(-spaceSize, spaceSize)));
		ghostN.scale(0.003f, 0.003f, 0.003f);
  	 	
	
    	
    	ghostE.loadAnimation("zombRun", "fZombieRA.rka");
    	
    	ghostE.loadAnimation("zombHit", "fZombieHA.rka");
    	
    	ghostE.playAnimation("zombRun", 0.5f, SkeletalEntity.EndType.LOOP, 0);
    	
    }
    
  //create walls (8 triangles)
    
    protected ManualObject makeTriangle1(Engine eng, SceneManager sm)	throws IOException { 
		ManualObject triangle = sm.createManualObject("triangle1");
		ManualObjectSection triangleSec = triangle.createManualSection("triangleSection");
		triangle.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		float[] vertices = new float[] {
				-0.5f, -0.5f, 0.0f,
			     0.5f, -0.5f, 0.0f,
			     -0.5f,  0.5f, 0.0f
			};
		
		float[] texcoords = new float[] { 
				   -0.5f, -0.5f,   
				    0.5f, -0.5f, 
				   -0.5f, 0.5f   
				};
		
		
		int[] indices = new int[] { 0,1,2};
		
		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(vertices);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		triangleSec.setVertexBuffer(vertBuf);
		triangleSec.setTextureCoordsBuffer(texBuf);
		triangleSec.setNormalsBuffer(normBuf);
		triangleSec.setIndexBuffer(indexBuf);
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
		mat.setEmissive(Color.DARK_GRAY);
	    Texture tex = sm.getTextureManager().getAssetByPath("wall.jpeg");
	    TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    texState.setTexture(tex);
	    triangle.setDataSource(DataSource.INDEX_BUFFER);
		triangle.setRenderState(texState);
	    triangle.setMaterial(mat);
		
		return triangle;
    }
    
    private SceneNode createTriangle1(Engine eng, SceneManager sm) throws IOException {
    	ManualObject triangle = makeTriangle1(eng, sm);
        SceneNode triangleN = sm.getRootSceneNode().createChildSceneNode("Triangle1 " + "Node");
        //triangleN.pitch(Degreef.createFrom(-90f));
        triangleN.scale(100f, 5f, 100f);
        triangleN.moveBackward(49.5f);
        triangleN.moveDown(2f);
        triangleN.attachObject(triangle);
        
        return triangleN;
    }
    
  
    
    protected ManualObject makeTriangle2(Engine eng, SceneManager sm)	throws IOException { 
		ManualObject triangle = sm.createManualObject("triangle2");
		ManualObjectSection triangleSec = triangle.createManualSection("triangleSection");
		triangle.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		float[] vertices = new float[] {
				 -0.5f,  0.5f, 0.0f,
			     0.5f, -0.5f, 0.0f,
			     0.5f,  0.5f, 0.0f
			};
		
		float[] texcoords = new float[] { 
				    -0.5f,  0.5f,   
				    0.5f, -0.5f, 
				   0.5f, 0.5f   
				};
		
		
		int[] indices = new int[] { 0,1,2};
		
		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(vertices);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		triangleSec.setVertexBuffer(vertBuf);
		triangleSec.setTextureCoordsBuffer(texBuf);
		triangleSec.setNormalsBuffer(normBuf);
		triangleSec.setIndexBuffer(indexBuf);
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
		mat.setEmissive(Color.DARK_GRAY);
	    Texture tex = sm.getTextureManager().getAssetByPath("wall.jpeg");
	    TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    texState.setTexture(tex);
	    triangle.setDataSource(DataSource.INDEX_BUFFER);
		triangle.setRenderState(texState);
	    triangle.setMaterial(mat);
		
		return triangle;
    }
    
    private SceneNode createTriangle2(Engine eng, SceneManager sm) throws IOException {
    	ManualObject triangle = makeTriangle2(eng, sm);
        SceneNode triangleN = sm.getRootSceneNode().createChildSceneNode("Triangle2 " + "Node");
        //triangleN.pitch(Degreef.createFrom(-90f));
        triangleN.scale(100f, 5f, 100f);
        triangleN.moveBackward(49.5f);
        triangleN.moveDown(2f);
        triangleN.attachObject(triangle);
        
        return triangleN;
    }
    
    protected ManualObject makeTriangle3(Engine eng, SceneManager sm)	throws IOException { 
		ManualObject triangle = sm.createManualObject("triangle3");
		ManualObjectSection triangleSec = triangle.createManualSection("triangleSection");
		triangle.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		float[] vertices = new float[] {
				-0.5f, -0.5f, 0.0f,
			     0.5f, -0.5f, 0.0f,
			     -0.5f,  0.5f, 0.0f
			};
		
		float[] texcoords = new float[] { 
				   -0.5f, -0.5f,   
				    0.5f, -0.5f, 
				   -0.5f, 0.5f   
				};
		
		
		int[] indices = new int[] { 0,1,2};
		
		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(vertices);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		triangleSec.setVertexBuffer(vertBuf);
		triangleSec.setTextureCoordsBuffer(texBuf);
		triangleSec.setNormalsBuffer(normBuf);
		triangleSec.setIndexBuffer(indexBuf);
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
		mat.setEmissive(Color.DARK_GRAY);
	    Texture tex = sm.getTextureManager().getAssetByPath("wall.jpeg");
	    TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    texState.setTexture(tex);
	    triangle.setDataSource(DataSource.INDEX_BUFFER);
		triangle.setRenderState(texState);
	    triangle.setMaterial(mat);
		
		return triangle;
    }
    
    private SceneNode createTriangle3(Engine eng, SceneManager sm) throws IOException {
    	ManualObject triangle = makeTriangle3(eng, sm);
        SceneNode triangleN = sm.getRootSceneNode().createChildSceneNode("Triangle3 " + "Node");
        //triangleN.pitch(Degreef.createFrom(-90f));
        triangleN.scale(100f, 5f, 100f);
        triangleN.moveRight(50f);
        triangleN.moveDown(2f);
        triangleN.yaw(Degreef.createFrom(-90.0f));
        triangleN.attachObject(triangle);
        
        return triangleN;
    }
    
  
    
    protected ManualObject makeTriangle4(Engine eng, SceneManager sm)	throws IOException { 
		ManualObject triangle = sm.createManualObject("triangle4");
		ManualObjectSection triangleSec = triangle.createManualSection("triangleSection");
		triangle.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		float[] vertices = new float[] {
				 -0.5f,  0.5f, 0.0f,
			     0.5f, -0.5f, 0.0f,
			     0.5f,  0.5f, 0.0f
			};
		
		float[] texcoords = new float[] { 
				    -0.5f,  0.5f,   
				    0.5f, -0.5f, 
				   0.5f, 0.5f   
				};
		
		
		int[] indices = new int[] { 0,1,2};
		
		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(vertices);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		triangleSec.setVertexBuffer(vertBuf);
		triangleSec.setTextureCoordsBuffer(texBuf);
		triangleSec.setNormalsBuffer(normBuf);
		triangleSec.setIndexBuffer(indexBuf);
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
		mat.setEmissive(Color.DARK_GRAY);
	    Texture tex = sm.getTextureManager().getAssetByPath("wall.jpeg");
	    TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    texState.setTexture(tex);
	    triangle.setDataSource(DataSource.INDEX_BUFFER);
		triangle.setRenderState(texState);
	    triangle.setMaterial(mat);
		
		return triangle;
    }
    
    private SceneNode createTriangle4(Engine eng, SceneManager sm) throws IOException {
    	ManualObject triangle = makeTriangle4(eng, sm);
        SceneNode triangleN = sm.getRootSceneNode().createChildSceneNode("Triangle4 " + "Node");
        //triangleN.pitch(Degreef.createFrom(-90f));
        triangleN.scale(100f, 5f, 100f);
        triangleN.moveRight(50f);
        triangleN.moveDown(2f);
        triangleN.yaw(Degreef.createFrom(-90.0f));
        triangleN.attachObject(triangle);
        
        return triangleN;
    }
    
    
    protected ManualObject makeTriangle5(Engine eng, SceneManager sm)	throws IOException { 
		ManualObject triangle = sm.createManualObject("triangle5");
		ManualObjectSection triangleSec = triangle.createManualSection("triangleSection");
		triangle.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		float[] vertices = new float[] {
				-0.5f, -0.5f, 0.0f,
			     0.5f, -0.5f, 0.0f,
			     -0.5f,  0.5f, 0.0f
			};
		
		float[] texcoords = new float[] { 
				   -0.5f, -0.5f,   
				    0.5f, -0.5f, 
				   -0.5f, 0.5f   
				};
		
		
		int[] indices = new int[] { 0,1,2};
		
		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(vertices);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		triangleSec.setVertexBuffer(vertBuf);
		triangleSec.setTextureCoordsBuffer(texBuf);
		triangleSec.setNormalsBuffer(normBuf);
		triangleSec.setIndexBuffer(indexBuf);
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
		mat.setEmissive(Color.DARK_GRAY);
	    Texture tex = sm.getTextureManager().getAssetByPath("wall.jpeg");
	    TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    texState.setTexture(tex);
	    triangle.setDataSource(DataSource.INDEX_BUFFER);
		triangle.setRenderState(texState);
	    triangle.setMaterial(mat);
		
		return triangle;
    }
    
    private SceneNode createTriangle5(Engine eng, SceneManager sm) throws IOException {
    	ManualObject triangle = makeTriangle5(eng, sm);
        SceneNode triangleN = sm.getRootSceneNode().createChildSceneNode("Triangle5 " + "Node");
        //triangleN.pitch(Degreef.createFrom(-90f));
        triangleN.scale(100f, 5f, 100f);
        triangleN.moveLeft(50f);
        triangleN.moveDown(2f);
        triangleN.yaw(Degreef.createFrom(90.0f));
        triangleN.attachObject(triangle);
        
        return triangleN;
    }
    
  
    
    protected ManualObject makeTriangle6(Engine eng, SceneManager sm)	throws IOException { 
		ManualObject triangle = sm.createManualObject("triangle6");
		ManualObjectSection triangleSec = triangle.createManualSection("triangleSection");
		triangle.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		float[] vertices = new float[] {
				 -0.5f,  0.5f, 0.0f,
			     0.5f, -0.5f, 0.0f,
			     0.5f,  0.5f, 0.0f
			};
		
		float[] texcoords = new float[] { 
				    -0.5f,  0.5f,   
				    0.5f, -0.5f, 
				   0.5f, 0.5f   
				};
		
		
		int[] indices = new int[] { 0,1,2};
		
		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(vertices);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		triangleSec.setVertexBuffer(vertBuf);
		triangleSec.setTextureCoordsBuffer(texBuf);
		triangleSec.setNormalsBuffer(normBuf);
		triangleSec.setIndexBuffer(indexBuf);
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
		mat.setEmissive(Color.DARK_GRAY);
	    Texture tex = sm.getTextureManager().getAssetByPath("wall.jpeg");
	    TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    texState.setTexture(tex);
	    triangle.setDataSource(DataSource.INDEX_BUFFER);
		triangle.setRenderState(texState);
	    triangle.setMaterial(mat);
		
		return triangle;
    }
    
    private SceneNode createTriangle6(Engine eng, SceneManager sm) throws IOException {
    	ManualObject triangle = makeTriangle6(eng, sm);
        SceneNode triangleN = sm.getRootSceneNode().createChildSceneNode("Triangle6 " + "Node");
        //triangleN.pitch(Degreef.createFrom(-90f));
        triangleN.scale(100f, 5f, 100f);
        triangleN.moveLeft(50f);
        triangleN.moveDown(2f);
        triangleN.yaw(Degreef.createFrom(90.0f));
        triangleN.attachObject(triangle);
        
        return triangleN;
    }
    
    protected ManualObject makeTriangle7(Engine eng, SceneManager sm)	throws IOException { 
		ManualObject triangle = sm.createManualObject("triangle7");
		ManualObjectSection triangleSec = triangle.createManualSection("triangleSection");
		triangle.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		float[] vertices = new float[] {
				-0.5f, -0.5f, 0.0f,
			     0.5f, -0.5f, 0.0f,
			     -0.5f,  0.5f, 0.0f
			};
		
		float[] texcoords = new float[] { 
				   -0.5f, -0.5f,   
				    0.5f, -0.5f, 
				   -0.5f, 0.5f   
				};
		
		
		int[] indices = new int[] { 0,1,2};
		
		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(vertices);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		triangleSec.setVertexBuffer(vertBuf);
		triangleSec.setTextureCoordsBuffer(texBuf);
		triangleSec.setNormalsBuffer(normBuf);
		triangleSec.setIndexBuffer(indexBuf);
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
		mat.setEmissive(Color.DARK_GRAY);
	    Texture tex = sm.getTextureManager().getAssetByPath("wall.jpeg");
	    TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    texState.setTexture(tex);
	    triangle.setDataSource(DataSource.INDEX_BUFFER);
		triangle.setRenderState(texState);
	    triangle.setMaterial(mat);
		
		return triangle;
    }
    
    private SceneNode createTriangle7(Engine eng, SceneManager sm) throws IOException {
    	ManualObject triangle = makeTriangle7(eng, sm);
        SceneNode triangleN = sm.getRootSceneNode().createChildSceneNode("Triangle7 " + "Node");
        //triangleN.pitch(Degreef.createFrom(-90f));
        triangleN.scale(100f, 5f, 100f);
        triangleN.moveForward(50f);
        triangleN.moveDown(2f);
        triangleN.pitch(Degreef.createFrom(180.0f));
        triangleN.attachObject(triangle);
        
        return triangleN;
    }
    
  
    
    protected ManualObject makeTriangle8(Engine eng, SceneManager sm)	throws IOException { 
		ManualObject triangle = sm.createManualObject("triangle8");
		ManualObjectSection triangleSec = triangle.createManualSection("triangleSection");
		triangle.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		float[] vertices = new float[] {
				 -0.5f,  0.5f, 0.0f,
			     0.5f, -0.5f, 0.0f,
			     0.5f,  0.5f, 0.0f
			};
		
		float[] texcoords = new float[] { 
				    -0.5f,  0.5f,   
				    0.5f, -0.5f, 
				   0.5f, 0.5f   
				};
		
		
		int[] indices = new int[] { 0,1,2};
		
		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(vertices);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		triangleSec.setVertexBuffer(vertBuf);
		triangleSec.setTextureCoordsBuffer(texBuf);
		triangleSec.setNormalsBuffer(normBuf);
		triangleSec.setIndexBuffer(indexBuf);
		
		Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
		mat.setEmissive(Color.DARK_GRAY);
	    Texture tex = sm.getTextureManager().getAssetByPath("wall.jpeg");
	    TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    texState.setTexture(tex);
	    triangle.setDataSource(DataSource.INDEX_BUFFER);
		triangle.setRenderState(texState);
	    triangle.setMaterial(mat);
		
		return triangle;
    }
    
    private SceneNode createTriangle8(Engine eng, SceneManager sm) throws IOException {
    	ManualObject triangle = makeTriangle8(eng, sm);
        SceneNode triangleN = sm.getRootSceneNode().createChildSceneNode("Triangle8 " + "Node");
        //triangleN.pitch(Degreef.createFrom(-90f));
        triangleN.scale(100f, 5f, 100f);
        triangleN.moveForward(50f);
        triangleN.moveDown(2f);
        triangleN.pitch(Degreef.createFrom(180.0f));
        triangleN.attachObject(triangle);
        
        return triangleN;
    }
    
    
    
    
}