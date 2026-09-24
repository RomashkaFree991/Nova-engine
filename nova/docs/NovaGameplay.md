# Nova gameplay API

## Vehicle controller

```ns
class CarGame : Script {
    void OnStart() {
        Vehicle.Create("Truck", vec3(0, 0.6, 0));
        Vehicle.Configure("Truck", 22, 12, 24);
    }
    void OnUpdate(float dt) {
        Vehicle.Control("Truck", 1, 0.15, 0);
    }
}
```

The controller stores throttle, steer, brake, speed, acceleration, maximum speed and brake power in the scene. The runtime applies drag, steering, forward movement and a ground clamp. It is an **arcade controller**, not a full rigid-body solver with wheel suspension or collisions.

## Procedural terrain

```ns
class Landscape : Script {
    void OnStart() {
        Terrain.Generate("Valley", 40, 5, 2.5, vec3(0, 0, 0));
        Terrain.SetTexture("Valley", vec3(0.18, 0.42, 0.12), vec3(0.42, 0.26, 0.10));
    }
}
```

`Terrain.Generate` writes a deterministic 16x16 heightmap into `scene.nscn` and the GLES2 scene view renders it as a triangle grid. `Terrain.SetTexture` stores procedural base/detail colors for the material layer. A future renderer can replace this material metadata with streamed GPU noise and splat maps.

## Tree and axe interaction

```ns
class Lumberjack : Script {
    void OnStart() {
        Tree.Create("Oak", vec3(2, 1.5, 1));
    }
    void OnUpdate(float dt) {
        // Call this from an input/button script when the player is near the tree.
        // Three hits reduce health and start a 1.2 second fall animation.
        // Interaction.Chop("Oak", 1);
    }
}
```

`Interaction.Chop(name, damage)` reduces `treeHealth`. When it reaches zero, the tree rotates around its fall axis over approximately 1.2 seconds and becomes invisible after falling. Distance checks, tool inventory, animation events, sound and multiplayer authority still need to be implemented by the game layer.

## 2D and 3D direction

The same script model can drive 2D scenes using `Player`/`Scene.Find` and 3D scenes using transforms, terrain and imported models. The current renderer remains a compact GLES2 editor preview: it does not yet provide a complete production 2D sprite renderer, skeletal animation system, audio engine, rigid-body physics, wheel suspension, navmesh, or cinematic asset pipeline.
