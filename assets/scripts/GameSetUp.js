var JavaPackages = new JavaImporter(
	Packages.ray.rage.Engine,
	Packages.game.MyGame
);

with (JavaPackages) {
	
	var TREE_COUNT = 4; //4 is a sweet spot, more trees can cause frames to drop
	var PLAYER_HEALTH = 1000; //100 is intended, make 1000 to test game
	var TIME_LIMIT = 210000.0; //240 seconds = 240000, ideally 5 min round
	
}