# 🍳 Client Recipe Fix (JEI / REI)

[<img alt="Requires Fabric API" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_vector.svg">](https://modrinth.com/mod/fabric-api)

## ✨ What is this?

**Client Recipe Fix** restores access to **all vanilla crafting recipes** in your client so mods like **JEI** and **REI** can display them properly again even on servers where recipes aren't fully synced.

Since Minecraft 1.21.2, servers only send *unlocked* recipes to players.
That means recipe viewers show… almost nothing 😐

This mod fixes that entirely client-side!

> ⚠️ This mod can only restore **vanilla recipes**!<br>
> Custom datapack or server recipes cannot be accessed (they are never sent to the client)

## 🚀 Features

* **Restores all vanilla recipes** for display in JEI / REI
* **No OP required**, it works on any server
* **Multiplayer-safe**, there is no server interaction
* **Non-invasive**, it does NOT override server data
* **Lightweight & fast**, it loads instantly with the game
* **Automatic fallback** when recipes are missing

## 🧠 How it works

* Loads **vanilla recipe data** directly from the Minecraft client
* Injects missing recipes into the client's recipe system
* Allows JEI / REI to display recipes as if they were unlocked

## 🧪 Why use this?

Without this mod:

* JEI / REI show incomplete or empty recipe lists
* You need OP and `/recipe give @s *` to fix it

With this mod:

* Everything just works ✅

## 💡 FAQ

### Does this give me recipes in the recipe book?

No, this is only for JEI / REI. You would still need to unlock the recipes in the recipe book by yourself.

### Can it show modded or datapack recipes?

No, those are controlled by the server and cannot be accessed client-side.

### Is this cheating?

No, it only restores **vanilla knowledge**, similar to using a wiki.

## 📜 License

This project is licensed under the MIT license. See [LICENSE](LICENSE) for more details.
