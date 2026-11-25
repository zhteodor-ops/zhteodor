# Jerry Mod (Minecraft Forge 1.20.1)

One player becomes Jerry the dog. Everyone else stays a normal player, and every dog in the world starts out as a normal Minecraft dog.

## How it works in game
1. **Join the world** – a window asks for **voice chat permission** (Allow / Don't allow). Your answer is remembered.
2. **A chest with bones** appears next to you the first time you join (64 bones, a "Jerry" name tag, steak, leads). If no dogs are nearby, two normal wolves spawn so you can tame one.
3. **Type `/consoleJerry YOURNAME`** (or just `consoleJerry YOURNAME` / `[consoleJerry YOURNAME]` in chat) to turn into **Jerry**.
   - Only **one** player can be Jerry at a time.
   - Normal players can only turn themselves into Jerry; admins can pick anyone.
4. As Jerry:
   - **F** → **Demonic Jerry** (big horned beast: Strength II, Speed II, Resistance, fire immune; hits give Wither). Press F again to go back.
   - **J** → **Skinwalker Jerry** (tall, hunched, long arms: Speed III, Jump III; hits give Slowness + Darkness). Press J again to go back.
   - Normal Jerry: dog-sized hitbox, Speed I, Jump I, night vision.
   - F doesn't swap your hands while you are Jerry.
5. **`/unJerry`** – stop being Jerry (Jerry himself or an admin).

Keys can be changed in Options → Controls → Key Binds → "Jerry Mod".

## Voice chat
Install **Simple Voice Chat** (Forge 1.20.1) next to this mod for proximity voice.
- If you press **Don't allow**, your microphone is muted for everyone.
- Demonic Jerry's voice becomes a deep growl, Skinwalker Jerry's voice becomes warped and wrong.
- Without Simple Voice Chat the mod still works, just without voice.
- To answer the permission question again, delete `config/jerrymod-voice.txt`.

## Building the .jar
**Windows:** install Java 17 (https://adoptium.net), then double-click **build.bat**.
The first build downloads Minecraft + Forge (5–15 min). The jar appears as `jerrymod-1.0.0.jar`.

**GitHub:** push this folder to a GitHub repo – the included workflow builds the jar automatically
(Actions tab → latest run → download "jerrymod-jar").

## Installing
Put `jerrymod-1.0.0.jar` (and optionally Simple Voice Chat) in `.minecraft/mods` with Forge 1.20.1.
For multiplayer the **server and every player** need the mod.

Textures are in `src/main/resources/assets/jerrymod/textures/entity/` – you can repaint them.
