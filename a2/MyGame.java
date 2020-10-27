package a2;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;

import javax.swing.ImageIcon;

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
import ray.rage.scene.controllers.*;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;

import ray.rage.rendersystem.states.*;

import myGameEngine.MoveBackwardAction;
import myGameEngine.MoveForwardAction;
import myGameEngine.MoveYGP;
import myGameEngine.QuitGameAction;
import myGameEngine.RotateDolphinDown;
import myGameEngine.RotateDolphinLeft;
import myGameEngine.RotateDolphinRight;
import myGameEngine.RotateDolphinUp;
import myGameEngine.RotateDolphinXGP;
import myGameEngine.StretchController;
import myGameEngine.BounceController;
import myGameEngine.Camera3Pcontroller;

import ray.rage.util.BufferUtil;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.rendersystem.shader.*;

public class MyGame extends VariableFrameRateGame implements MouseListener, MouseMotionListener {
	// to minimize variable allocation in update()
	GL4RenderSystem rs;
	float elapsTime = 0.0f;
	String elapsTimeStr, counterStr, dispStr, counterStr2, dispStr2;
	int elapsTimeSec, counter = 0, counter2 = 0;
	
	// Variable for changing different game values
	private int totalPlanets = 5;
	private int spaceSize = 25;
	private float maxSpeed = 0.02f;
	private boolean gameOver = false;
	
	// Entity dolphinE;
	private SceneNode activeNode;
	
	// Camera
	private Camera3Pcontroller orbitController, orbitController2;
	
	// Input and Actions
	private InputManager im;
	private Action quitGameAction, moveForwardAction,
			moveBackwardAction, rotateDolphinRight,
			moveYGP, rotateDolphinDown,
			rotateDolphinUp, rotateDolphinLeft,
			rotateDolphinXGP, rotateCameraYGP,
			rotateDolphinUp2, rotateDolphinDown2
			;
	// robot
	private Robot robot;
	// mouse
	private Canvas canvas;
	private RenderWindow rw;
	private RenderSystem rs2;
	private float prevMouseX, prevMouseY, curMouseX, curMouseY;
	private boolean isRecentering;         //indicates the Robot is in action
	
	// collisions
	private int[] planets = new int[totalPlanets];
	private int pointer = 0;

	
    public MyGame() {
        super();
        System.out.println("Player2: ");
		System.out.println("press W to move forward");
		System.out.println("press S to move backward");
		System.out.println("press D to rotate right");
		System.out.println("press A to rotate left");
		System.out.println("press E to tilt dolphin up");
		System.out.println("press Q to tilt dolphin down");
		System.out.println("use mouse to rotate camera");
		System.out.println("use mouse scroller to zoom camera");
		System.out.println("Player1: ");
		System.out.println("use GAMEPAD left stick to move and rotate");
		System.out.println("use GAMEPAD right stick to rotate camera");
		System.out.println("use GAMEPAD button 4 to tilt dolphin up");
		System.out.println("use GAMEPAD button 5 to tilt dolphin down");
		System.out.println("use GAMEPAD POV to zoom camera");
		

    }

    public static void main(String[] args) {;
        Game game = new MyGame();
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }
	
  //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
	}
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	protected void setupWindowViewports(RenderWindow rw)  {
		rw.addKeyListener(this);
		Viewport topViewport = rw.getViewport(0);
		topViewport.setDimensions(.51f, .01f, .99f, .49f);// B,L,W,H

		
		Viewport botViewport = rw.createViewport(.01f, .01f, .99f, .49f);
		botViewport.setClearColor(Color.WHITE);
		
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
        
        Camera camera2 = sm.createCamera("MainCamera2", Projection.PERSPECTIVE);
        rw.getViewport(1).setCamera(camera2);
        SceneNode cameraNode2 = rootNode.createChildSceneNode(camera2.getName() + "Node");
        cameraNode2.attachObject(camera2);
        camera2.setMode('n');
        camera2.getFrustum().setFarClipDistance(1000.0f);
        RenderSystem rs = sm.getRenderSystem();
        initMouseMode(rs, rw);
        
    }
    
    protected void setupOrbitCamera(Engine eng, SceneManager sm)  {
    	SceneNode dolphinN = sm.getSceneNode("myDolphinNode");
    	SceneNode cameraN = sm.getSceneNode("MainCameraNode");
    	Camera camera = sm.getCamera("MainCamera");
    	String gpName = im.getFirstGamepadName();
    	System.out.println();
    	System.out.println(gpName);
    	orbitController =
    			new Camera3Pcontroller(camera, cameraN, dolphinN, gpName, im);
    	
    	SceneNode dolphinN2 = sm.getSceneNode("2DolphinNode");
    	SceneNode cameraN2 = sm.getSceneNode("MainCamera2Node");
    	Camera camera2 = sm.getCamera("MainCamera2");
    	String msName = im.getMouseName();
    	System.out.println(msName);
    	String kbName = im.getKeyboardName();
    	System.out.println(kbName);
    	
    	orbitController2 =
    			new Camera3Pcontroller(camera2, cameraN2, dolphinN2, msName, im);
    	
    	
    	} 

  //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Set up scene for game engine
   
    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {
    	im = new GenericInputManager();
    	
        // Create dolphin
        makeDolphin(eng, sm);
        
        Entity dolphinE2 = sm.createEntity("2Dolphin", "dolphinHighPoly.obj");
        dolphinE2.setPrimitive(Primitive.TRIANGLES);
        SceneNode dolphinN2 = sm.getRootSceneNode().createChildSceneNode("2DolphinNode");
        Material mat = sm.getMaterialManager().getAssetByPath("dolphin2.mtl");
	    Texture tex = sm.getTextureManager().getAssetByPath(mat.getTextureFilename());
	    TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    texState.setTexture(tex);
		dolphinE2.setRenderState(texState);
	    dolphinE2.setMaterial(mat);
        dolphinN2.moveUp(0.5f);
        dolphinN2.yaw(Degreef.createFrom(45.0f));
        dolphinN2.attachObject(dolphinE2);
        
      //setup camera
        setupOrbitCamera(eng, sm);
        
     // Setup the input actions
    	setupInputs(sm);
   
        // Create the axis
        setupAxis(eng, sm);
        
        //make the floor
        createTriangle1(eng, sm);
        createTriangle2(eng,sm);
        
        //Light Node
        sm.getAmbientLight().setIntensity(new Color(0.1f, 0.1f, 0.1f));
        Light plight = sm.createLight("testLamp1", Light.Type.POINT);
        plight.setAmbient(new Color(.3f, .3f, .3f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
        plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(15f);
		
        SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);
        
        activeNode = this.getEngine().getSceneManager().getSceneNode("MainCameraNode");
        
        SceneNode planetsNG = sm.getRootSceneNode().createChildSceneNode("planetsNodeG");
    	@SuppressWarnings("unused")
		SceneNode diamondsNG = planetsNG.createChildSceneNode("diamondsNodeG");
       
        // Set Rotation for the planets.
        RotationController rcPlanet = new RotationController(Vector3f.createUnitVectorX(), 0.4f);
        // Make the planets then rotate them.
        for( int i = 0; i < totalPlanets; i++) {
        	makePlanet(eng, sm, i);
        }
        
        sm.addController(rcPlanet);
        
        // Make the diamonds.
        OrbitController ocDiamond = new OrbitController(planetsNG.getChild(1), 1.0f, 1.0f);
     	ocDiamond.addNode(createDiamond(eng, sm, 1));
     	sm.addController(ocDiamond);
     	
     	BounceController bcPlanet = new BounceController();
     	sm.addController(bcPlanet);
     	
     	StretchController scPlanets = new StretchController();
     	sm.addController(scPlanets);

		

    }

  //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //updating game engine
    
    @Override
    protected void update(Engine engine) {
		// Set Up Text Display
		rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		counterStr = Integer.toString(counter);
		counterStr2 = Integer.toString(counter2);
		dispStr = " Planets Visited = " + counterStr;
		dispStr2 = " Planets Visited = " + counterStr2;
		rs.setHUD(dispStr2, 15, 15);
		rs.setHUD2(dispStr, 15, rs.getRenderWindow().getViewport(0).getActualBottom()+5);
		if(counter + counter2 == 4) {
			 gameOver = true;
		}
			
		// Input manager to process the inputs
		im.update(elapsTime);
		
		
		// Check collision of camera and planets
		// Couldnt figure it out
		
		//check collision
	       for(int i = 0; i < totalPlanets; i++) {
	    	   if (checkCollision(engine.getSceneManager().getSceneNode("myDolphinNode"), engine.getSceneManager().getSceneNode("planet" + Integer.toString(i) + "Node")) < 0.5) {
	    		   int j = i;
	    		   if(!IntStream.of(planets).anyMatch(x -> x == j)) {
	    			   planets[pointer] = i;
		    		   engine.getSceneManager().getController(0).addNode(engine.getSceneManager().getSceneNode("planet" + Integer.toString(i) + "Node"));
		    		   counter++;
		    		   pointer++;
	    		   }
	    		   
	    	   }
	    	   if (checkCollision(engine.getSceneManager().getSceneNode("2DolphinNode"), engine.getSceneManager().getSceneNode("planet" + Integer.toString(i) + "Node")) < 0.5) {
	    		   int j = i;
	    		   if(!IntStream.of(planets).anyMatch(x -> x == j)) {
	    			   planets[pointer] = i;
		    		   engine.getSceneManager().getController(2).addNode(engine.getSceneManager().getSceneNode("planet" + Integer.toString(i) + "Node"));
		    		   counter2++;
		    		   pointer++;
	    		   }
	    		   
	    	   }
	       }
	       if(gameOver) {
	    	   engine.getSceneManager().getController(3).addNode(engine.getSceneManager().getSceneNode("planetsNodeG"));
	       }
	        
		
		
		//update camera
		orbitController.updateCameraPosition();
		orbitController2.updateCameraPosition();
		recenterMouse();
		
	}

  //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //set up inputs
   
    protected void setupInputs(SceneManager sm) {
    	String kbName = im.getKeyboardName();
    	String gpName = im.getFirstGamepadName();
    	SceneNode dolphinN =
    			getEngine().getSceneManager().getSceneNode("myDolphinNode");
    	SceneNode dolphinN2 =
    			getEngine().getSceneManager().getSceneNode("2DolphinNode");

    	
    	//creating actions for inputs for player one
    	quitGameAction = new QuitGameAction(this);
		moveYGP = new MoveYGP(dolphinN);
    	rotateDolphinUp = new RotateDolphinUp(dolphinN);
    	rotateDolphinDown = new RotateDolphinDown(dolphinN); 
		rotateDolphinXGP = new RotateDolphinXGP(dolphinN, orbitController);
		
		//creating actions for inputs for player two
		moveForwardAction = new MoveForwardAction(dolphinN2);
		moveBackwardAction = new MoveBackwardAction(dolphinN2);
    	rotateDolphinRight = new RotateDolphinRight(dolphinN2, orbitController2);
    	rotateDolphinLeft = new RotateDolphinLeft(dolphinN2, orbitController2); 
    	rotateDolphinUp2 = new RotateDolphinUp(dolphinN2);
    	rotateDolphinDown2 = new RotateDolphinDown(dolphinN2);
    	
    	// attach the action objects to components
    	
		// keyboard
    	if(im.getKeyboardName() != null) {
    		kbName = im.getKeyboardName();
    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.ESCAPE, 
    			quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    	
    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.W, 
			    moveForwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	
    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.S, 
			    moveBackwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	
    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.D, 
	    		rotateDolphinRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.A, 
	    		rotateDolphinLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	
    	im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.E, 
	    		rotateDolphinUp2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	    
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.Q, 
	    		rotateDolphinDown2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	
    	 
    	}

    	// gamepad 
    	if(im.getFirstGamepadName() != null) {
	    	gpName = im.getFirstGamepadName();
	    		
 		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Y, 
					moveYGP, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.RY, 
		    		rotateCameraYGP, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    	
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.X, 
		    		rotateDolphinXGP, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._0, 
		    		rotateDolphinLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._2, 
		    		rotateDolphinRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._4, 
		    		rotateDolphinDown, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    
		    im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._5, 
		    		rotateDolphinUp, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		    
			im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._8, 
		    		quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		    
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
//    public void mouseMoved(MouseEvent e)  {
//    	// if robot is recentering and the MouseEvent location is in the center,
//    	// then this event was generated by the robot
//    	int centerX = 0, centerY = 0;
//    	if (isRecentering &&centerX == e.getXOnScreen() && centerY == e.getYOnScreen())
//    	{ isRecentering = false; } 
//    	// mouse recentered, recentering complete
//    	else
//    	{  // event was due to a user mouse-move, and must be processed
//    		curMouseX = e.getXOnScreen();
//    		curMouseY = e.getYOnScreen();
//    		prevMouseX = curMouseX;
//    		prevMouseY = curMouseY;
//    		// tell robot to put the cursor to the center (since user just moved it)
//    		recenterMouse();
//    		prevMouseX = centerX;
//    		//reset prev to center
//    		prevMouseY = centerY; 
//    	}
//    }
    
    
    
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //misc functions for game
    
    // Get Active Node
    public SceneNode getActiveNode() {
    	return activeNode;
    }
    
    // Set Active Node
    public void setActiveNode(SceneNode sn) {
    	activeNode = sn;
    }
    

    
    // Check Camera length from Dolphin
    public float checkCamera(SceneNode a, Camera c) {
    	float ax = a.getLocalPosition().x();
    	float ay = a.getLocalPosition().y();
    	float az = a.getLocalPosition().z();
    	float cx = c.getPo().x();
    	float cy = c.getPo().y();
    	float cz = c.getPo().z();
    	
    	
    	
    	return (float)Math.sqrt((double)(ax - cx) * (ax - cx) + (ay-cy) * (ay - cy) + (az - cz) * (az - cz));
    }
    

    
    // Collision Detection 
    public float checkCollision(SceneNode a, SceneNode c) {
    	float ax = a.getLocalPosition().x();
    	float az = a.getLocalPosition().z();
    	float cx = c.getLocalPosition().x();
    	float cz = c.getLocalPosition().z();
    	
    	
    	return (float)Math.sqrt((double)(ax - cx) * (ax - cx)  + (az - cz) * (az - cz));
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
    
    
  //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //object creation
    
    //create dolphins
    private void makeDolphin(Engine eng, SceneManager sm) throws IOException {
    	Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
    	dolphinE.setPrimitive(Primitive.TRIANGLES);

    	SceneNode dolphinN = sm.getRootSceneNode().createChildSceneNode("myDolphinNode");
    	dolphinN.moveLeft(0.4f);
    	dolphinN.moveUp(0.5f);
    	dolphinN.yaw(Degreef.createFrom(45.0f));
    	dolphinN.attachObject(dolphinE);
    	sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));
        Light plight = sm.createLight("dolphinLamp", Light.Type.POINT);
        plight.setAmbient(new Color(.3f, .3f, .3f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
        plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(0.02f);
		
        SceneNode dolphinLightN = sm.getSceneNode("myDolphinNode").createChildSceneNode("dolphinLightNode");
        dolphinLightN.attachObject(plight);
        

    }
    
    //create the axis objects
    private void setupAxis(Engine eng, SceneManager sm) throws IOException {
		ManualObject vertexX = sm.createManualObject("VecX");
		ManualObjectSection vertexSecX = vertexX.createManualSection("VertexSectionX");
		vertexX.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		
		ManualObject vertexY = sm.createManualObject("VecY");
		ManualObjectSection vertexSecY = vertexX.createManualSection("VertexSectionY");
		vertexX.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		
		ManualObject vertexZ = sm.createManualObject("VecZ");
		ManualObjectSection vertexSecZ = vertexX.createManualSection("VertexSectionZ");
		vertexX.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
        
        float[] verticesX = new float[] { 
    			1000.0f, 0.0f, 0.0f,
    			-1000.0f, 0.0f, 0.0f, 
		};
        float[] verticesY = new float[] { 
        		0.0f, 1000.0f, 0.0f,
        		0.0f, -1000.0f, 0.0f,
		};
        float[] verticesZ = new float[] { 
        		0.0f, 0.0f, 1000.0f,
        		0.0f, 0.0f, -1000.0f,
		};     
		
		int[] indicesX = new int[] { 0,1 };
		int[] indicesY = new int[] { 0,1 };
		int[] indicesZ = new int[] { 0,1 };
		
		vertexSecX.setPrimitive(Primitive.LINES);
		vertexSecY.setPrimitive(Primitive.LINES);
		vertexSecZ.setPrimitive(Primitive.LINES);
		
		FloatBuffer vertBufX = BufferUtil.directFloatBuffer(verticesX);
		IntBuffer indexBufX = BufferUtil.directIntBuffer(indicesX);
		FloatBuffer vertBufY = BufferUtil.directFloatBuffer(verticesY);
		IntBuffer indexBufY = BufferUtil.directIntBuffer(indicesY);
		FloatBuffer vertBufZ = BufferUtil.directFloatBuffer(verticesZ);
		IntBuffer indexBufZ = BufferUtil.directIntBuffer(indicesZ);
		
		vertexSecX.setVertexBuffer(vertBufX);
		vertexSecX.setIndexBuffer(indexBufX);
		vertexSecY.setVertexBuffer(vertBufY);
		vertexSecY.setIndexBuffer(indexBufY);
		vertexSecZ.setVertexBuffer(vertBufZ);
		vertexSecZ.setIndexBuffer(indexBufZ);
		
		Material matX = sm.getMaterialManager().getAssetByPath("default.mtl");
	    matX.setEmissive(Color.RED);
	    Texture texX = sm.getTextureManager().getAssetByPath("redColor.jpg");
	    TextureState tstateX = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    tstateX.setTexture(texX);
	    vertexSecX.setRenderState(tstateX);
	    vertexSecX.setMaterial(matX);
	    
	    SceneNode vertexXNode = sm.getRootSceneNode().createChildSceneNode("XNode");
	    vertexXNode.attachObject(vertexX);
	    
	    Material matY = sm.getMaterialManager().getAssetByPath("default.mtl");
	    matY.setEmissive(Color.BLUE);
	    Texture texY = sm.getTextureManager().getAssetByPath("blueColor.jpg");
	    TextureState tstateY = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    tstateY.setTexture(texY);
	    vertexSecY.setRenderState(tstateY);
	    vertexSecY.setMaterial(matY);
	    
	    SceneNode vertexYNode = sm.getRootSceneNode().createChildSceneNode("YNode");
	    vertexYNode.attachObject(vertexY);
	    
	    Material matZ = sm.getMaterialManager().getAssetByPath("default.mtl");
	    matZ.setEmissive(Color.RED);
	    Texture texZ = sm.getTextureManager().getAssetByPath("greenColor.jpg");
	    TextureState tstateZ = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    tstateZ.setTexture(texZ);
	    vertexSecZ.setRenderState(tstateZ);
	    vertexSecZ.setMaterial(matZ);
	    
	    SceneNode vertexZNode = sm.getRootSceneNode().createChildSceneNode("ZNode");
	    vertexZNode.attachObject(vertexZ);
    }
    
    //create planets
    private SceneNode makePlanet(Engine eng, SceneManager sm, int num) throws IOException {
    	SceneNode planetsNG = sm.getSceneNode("planetsNodeG");
    	Texture[] textures = new Texture[3];
    	Entity planetE = sm.createEntity("planet" + Integer.toString(num),	"sphere.obj");
    	planetE.setPrimitive(Primitive.TRIANGLES);
		
		float planetSize = randInRangeFloat(0.1f, 0.9f);
		int planetTexNumber = randInRangeInt(0, 2);
		
		 Texture texE = sm.getTextureManager().getAssetByPath("earth-day.jpeg");
		 Texture texN = sm.getTextureManager().getAssetByPath("sunmap.jpg");
		 Texture texM = sm.getTextureManager().getAssetByPath("moon.jpeg");
		 textures[0] = texE;
		 textures[1] = texN;
		 textures[2] = texM;
		 TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		 texState.setTexture(textures[planetTexNumber]);
		 planetE.setRenderState(texState);
    	
    	SceneNode planetN = planetsNG.createChildSceneNode(planetE.getName() + "Node");
    	planetN.moveForward(randInRangeFloat(-spaceSize, spaceSize));
    	planetN.moveUp(1.5f);
    	planetN.moveRight(randInRangeFloat(-spaceSize, spaceSize));
    	planetN.attachObject(planetE);
    	planetN.scale(planetSize, planetSize, planetSize);

    	return planetN;
    }
   
    
    //create manual object
    protected ManualObject makeDiamond(Engine eng, SceneManager sm, int num)	throws IOException { 
		ManualObject diam = sm.createManualObject("Diamond" + Integer.toString(num));
		ManualObjectSection diamSec = diam.createManualSection("DiamondSection");
		diam.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		float[] vertices = new float[] {
				-0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.0f, 0.5f, 0.0f, 
				0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.0f, 0.5f, 0.0f, 
				0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.0f, 0.5f, 0.0f, 
				-0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, 0.0f, 0.5f, 0.0f, 
				0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f,0.0f, -1.5f, 0.0f, 
				0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.0f, -1.5f, 0.0f, 
				-0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.0f, -1.5f, 0.0f, 
				-0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.0f, -1.5f, 0.0f 
		};
		
		float[] texcoords = new float[] { 
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f
		};
		
		float[] normals = new float[] { 
			0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
			0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f,
			-1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f,
			0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
			0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f
		};
		
		int[] indices = new int[] { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24 };
		
		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
		diamSec.setVertexBuffer(vertBuf);
		diamSec.setTextureCoordsBuffer(texBuf);
		diamSec.setNormalsBuffer(normBuf);
		diamSec.setIndexBuffer(indexBuf);
		
		Material mat = sm.getMaterialManager().getAssetByPath("blue.mtl");
	    Texture tex = sm.getTextureManager().getAssetByPath(mat.getTextureFilename());
	    TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    texState.setTexture(tex);
	    diam.setDataSource(DataSource.INDEX_BUFFER);
		diam.setRenderState(texState);
	    diam.setMaterial(mat);
		
		return diam;
    }
    
  //create floor
    
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
	    Texture tex = sm.getTextureManager().getAssetByPath("whiteColor.jpg");
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
        triangleN.pitch(Degreef.createFrom(-90f));
        triangleN.moveDown(1.5f);
        triangleN.scale(500f, 500f, 500f);
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
	    Texture tex = sm.getTextureManager().getAssetByPath("whiteColor.jpg");
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
        triangleN.pitch(Degreef.createFrom(-90f));
        triangleN.moveDown(1.5f);
        triangleN.scale(500f, 500f, 500f);
        triangleN.attachObject(triangle);
        
        return triangleN;
    }
    
    
    private SceneNode createDiamond(Engine eng, SceneManager sm, int num) throws IOException {
    	SceneNode diamondsNG = sm.getSceneNode("diamondsNodeG");
    	ManualObject diam = makeDiamond(eng, sm, num);
        SceneNode diamN = diamondsNG.createChildSceneNode("Diamond " +  Integer.toString(num) + "Node");
        diamN.scale(0.75f, 0.75f, 0.75f);
        diamN.moveForward(randInRangeFloat(-spaceSize, spaceSize));
        diamN.moveUp(1.5f);
        diamN.moveRight(randInRangeFloat(-spaceSize, spaceSize));
        diamN.attachObject(diam);
        
        
        return diamN;
    }
    
    
}