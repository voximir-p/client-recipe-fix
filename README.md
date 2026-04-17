![Client Recipe Fix Banner](https://github.com/voximir-p/asset/blob/main/client-recipe-fix/title_card.png?raw=true)

<img alt="Separate" src="https://github.com/voximir-p/asset/blob/main/common/separate.png?raw=true" style="margin: -16px 0;"/>

<img alt="About" src="https://github.com/voximir-p/asset/blob/main/common/about.png?raw=true" style="margin: 24px 0 -6px;"/>

Since Minecraft 1.21.2, recipes are stored on the server, and only unlocked recipes are sent to the client.<br/>
As a result, **JEI / REI can only show the recipes you have unlocked** 😔

This mod fixes that entirely on the **client side**.

**Client Recipe Fix** restores access to **all vanilla crafting recipes** in your client, allowing mods like
**JEI** and **REI** to display them properly again, even on servers where recipes are not fully synced.

> ⚠️ **This mod can only restore vanilla recipes.**<br/>
> Custom datapack or server-side recipes cannot be accessed, because they are never sent to the client.<br/>
>
> ⚠️ **JEI support requires Minecraft 1.21.10 or newer.**<br/>
> On Minecraft 1.21.9 and earlier, this is limited by the Fabric API.<br/>
> For full support on older versions, consider using REI instead.

<img alt="Separate" src="https://github.com/voximir-p/asset/blob/main/common/separate.png?raw=true" style="margin: -16px 0;"/>

<img alt="Features" src="https://github.com/voximir-p/asset/blob/main/common/features.png?raw=true" style="margin: 24px 0 -6px;"/>

* **Restores all vanilla recipes** for display in JEI / REI
* **No OP required**, works on any server
* **Multiplayer-safe**, no server interaction needed
* **Non-invasive**, it does NOT override server data
* **Lightweight & fast**, loads instantly with the game

<img alt="Separate" src="https://github.com/voximir-p/asset/blob/main/common/separate.png?raw=true" style="margin: -12px 0;"/>

<img alt="Media" src="https://github.com/voximir-p/asset/blob/main/common/media.png?raw=true" style="margin: 2px 0 -2px;"/><br/>

<img alt="Media 1" src="https://github.com/voximir-p/asset/blob/main/client-recipe-fix/media_1_X.png?raw=true" width=500/><br/>
No more REI warnings<br/><br/>
<img alt="Media 2" src="https://github.com/voximir-p/asset/blob/main/client-recipe-fix/media_2_X.png?raw=true" width=500/><br/>
No more JEI warnings

<img alt="Separate" src="https://github.com/voximir-p/asset/blob/main/common/separate.png?raw=true" style="margin: -12px 0;"/>

<img alt="FAQ" src="https://github.com/voximir-p/asset/blob/main/common/faq.png?raw=true" style="margin: 24px 0 -2px;"/>

### Does this mod unlock the recipes in the recipe book?

No. This is only for JEI / REI. You still need to unlock recipes in the recipe book yourself.

### Can it show modded or datapack recipes?

No. Those are controlled by the server and cannot be accessed client-side.

### Is this cheating?

No. It only restores **vanilla knowledge**, similar to using a wiki.

<img alt="Separate" src="https://github.com/voximir-p/asset/blob/main/common/separate.png?raw=true" style="margin: -12px 0;"/>

<img alt="Building" src="https://github.com/voximir-p/asset/blob/main/common/building.png?raw=true" style="margin: 24px 0 -2px;"/>

1. Clone the repository:
   ```bash
   $ git clone https://github.com/voximir-p/client-recipe-fix.git
   ```

2. `cd` into the project directory:

   ```bash
   $ cd client-recipe-fix
   ```
3. Build the mod using Gradle:

   ```bash
   $ ./gradlew build
   ```

The output JAR file can be found in `build/libs/`.

<img alt="Separate" src="https://github.com/voximir-p/asset/blob/main/common/separate.png?raw=true" style="margin: -16px 0;"/>

<img alt="License" src="https://github.com/voximir-p/asset/blob/main/common/license.png?raw=true" style="margin: 24px 0 -2px;"/>

This project is licensed under the MIT license. See [LICENSE](LICENSE) for more details.
