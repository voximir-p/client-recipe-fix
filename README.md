# 🍳 Client Recipe Fix (JEI / REI)

---

## ✨ What is this?

**Client Recipe Fix** restores access to **all vanilla crafting recipes** in your client so mods like **JEI** and **REI
** can display them properly again even on servers where recipes aren't fully synced.

Since Minecraft 1.21.2, servers only send *unlocked* recipes to players.
That means recipe viewers show… almost nothing 😐

This mod fixes that entirely client-side!

> ⚠️ NOTICE: This only restores **vanilla recipes**<br>
> Custom datapack or server recipes cannot be accessed (they are never sent to the client)

---

## 🚀 Features

* **Restores all vanilla recipes** for display in JEI / REI
* **No OP required**, it works on any server
* **Multiplayer-safe**, there is no server interaction
* **Non-invasive**, it does NOT override server data
* **Lightweight & fast**, it loads instantly with the game
* **Automatic fallback** when recipes are missing

---

## 🧠 How it works

* Loads **vanilla recipe data** directly from the Minecraft client
* Injects missing recipes into the client's recipe system
* Allows JEI / REI to display recipes as if they were unlocked

---

## 📦 Requirements

1. Install **Fabric Loader**
2. Install **Fabric API**
3. Drop this mod into your `mods` folder
4. (Optional) Install **JEI** or **REI**
5. Launch the game 🎉

---

## 🔧 Compatibility

* ✅ Minecraft 1.21.2+
* ✅ Fabric
* ✅ Works with JEI & REI
* ❌ Does NOT require server-side installation
* ❌ Does NOT reveal hidden server/datapack recipes

---

## ⚙️ Configuration (optional)

*(If implemented)*

* Enable/disable recipe injection
* Debug logging
* Only inject when recipes are missing

---

## 🧪 Why use this?

Without this mod:

* JEI / REI show incomplete or empty recipe lists
* You need OP and `/recipe give @s *` to fix it

With this mod:

* Everything just works ✅

---

## 📸 Before / After

| Without Mod       | With Client Recipe Fix |
|-------------------|------------------------|
| ❌ Missing recipes | ✅ Full vanilla recipes |
| ❌ Requires OP     | ✅ Works everywhere     |
| 😵 Confusing      | 😌 Smooth experience   |

---

## 💡 FAQ

### Does this give me recipes in the recipe book?

No — this is **display-only**. It does not unlock recipes for gameplay.

### Can it show modded or datapack recipes?

No — those are controlled by the server and cannot be accessed client-side.

### Is this cheating?

No — it only restores **vanilla knowledge**, similar to using a wiki.

---

## 🛠️ For Developers

* Injects into the client `RecipeManager`
* Loads recipes via `ResourceManager`
* Safely merges with server-provided data
* Designed to be compatible with JEI / REI APIs

---

## ❤️ Credits

* Minecraft modding community
* JEI & REI developers
* Everyone frustrated by missing recipes 😄

---

## 📜 License

*(Choose your license — MIT / MIT-0 / CC0 recommended for mods like this)*
