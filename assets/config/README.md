# README: Configuration Properties

The `.properties` files are used to allow engine and game client configurations without hard-coding file system paths and other data into them directly. The file system paths to locate mesh and texture directories is currently specified this way, making updates and maintenance easier. However, there're some details to keep in mind when working with them in RAGE.


### Path Separators are Always `/`

You should **always** use the forward slash (`/`) character as a path separator, *even if you're using Windows*. The engine is responsible for dealing with platform-specific details of how a file system path should look like, such as changing to `\\` if running on a Windows-based OS.

In addition, when specifying a file system directory, always make sure it ends with a trailing `/`. The engine will **not** append this for you automatically. If you forget the trailing `/` for directory paths, the system will not find the resource you're looking for.


### Create Independent `.properties` for Your Game Clients

You should *always* create an independent `.properties` file for your particular game in your separate game repository. The `Configuration` class allows you to specify the path to your `.properties` file (e.g. `my-game.properties`). Note that entries in `rage.properties` are used internally by the engine, which means that pre-existing keys **must** continue to exist and be valid. When creating your custom `.properties`, **always** include a copy of any pre-existing *keys*. Feel free to change their *values* to other things. For example, the textures directory path for your game would have a different path value, but must have the same key.


### Configuration Example

Suppose you want to create a custom configuration for your new game, called `mygame`. Also suppose that the engine already has its own configuration, `assets/config/rage.properties`. Follow these steps:

1. Copy the base `assets/config/rage.properties` to your indepentent game repository.
2. Rename the your copy to `mygame.properties`.
3. Start editing.

Suppose `rage.properties` has the following content:

    # internal engine config;
    # notice always using / as separator and also the trailig / for directories
    assets.textures.path=assets/textures/
    assets.materials.path=assets/materials/

Your `mygame.properties` file should look as follows:

    # Engine-specific config entries require the same keys
    # but the values can be different (e.g. pointing to game-specific assets)
    assets.textures.path=mygame/textures/
    assets.materials.path=mygame/materials/

    # game-specific configuration entries are whatever you want
    mygame.foo=mygame/foo/
    mygame.bar=mygame/bar/
    mygame.baz=mygame/baz/whatever.jpeg

Happy coding! 👍
