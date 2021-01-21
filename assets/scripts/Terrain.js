var JavaPackages = new JavaImporter(
	Packages.ray.rage.scene.Tessellation,
	Packages.ray.rage.scene.SceneNode,
	Packages.ray.rage.Engine
);

with (JavaPackages) {
	
	var TESSELLATION_QUALITY = 7;
	var TESSELLATION_SUBDIVISIONS = 21;
	
	var TERRAIN_SCALE_X = 100;
	var TERRAIN_SCALE_Y = 200;
	var TERRAIN_SCALE_Z = 100;
	
	var TERRAIN_HEIGHTMAP = "perlin3.png"; // can use (perlin1.png, perlin2.png or perlin3.png)
	var TERRAIN_TEX = "grass.png"; // can use any texture file you create
	
	var TESSELLATION_ENTITY = "tessE";
	var TESSELLATION_NODE = "tessN";
	
	function configureTerrain(engine) {
		var tessE = engine.getSceneManager().createTessellation(TESSELLATION_ENTITY, TESSELLATION_QUALITY);
		var tessN = engine.getSceneManager().getRootSceneNode().createChildSceneNode(TESSELLATION_NODE);
		tessE.setSubdivisions(TESSELLATION_SUBDIVISIONS);
		tessE.setHeightMap(engine, TERRAIN_HEIGHTMAP);
		tessE.setTexture(engine, TERRAIN_TEX);
		tessN.attachObject(tessE);
		tessN.moveDown(2.0);
		tessN.scale(TERRAIN_SCALE_X, TERRAIN_SCALE_Y ,TERRAIN_SCALE_Z);
	}
}

