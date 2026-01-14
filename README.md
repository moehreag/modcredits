# Mod Credits screen

> With custom poem!
 
This mod adds a screen in the style of the vanilla credits/win screen, but for the
currently installed mods and with expanded functionality as well as a small custom poem.

### Customization options (for modpack devs/users, available starting from version 2.0.0)

The config file is located at `<game dir>/config/moehreag_modcredits.json`.

Currently, the following options are available:

- `enable_poem_in_credits_button`: Whether to display the poem when the mod credits screen is opened
	from the "Credits & Attribution" screen.
- `enable_mod_links`: Whether to allow clicking on mod names to go to their homepage/sources/...
- `show_mod_icons`: Whether to display mod icons in the credits screen. Note that mod authors are able to 
  override the default behavior and may not respect this option.
- `compact_mode`: Whether to display the list in a more compact form. This option may also not be respected
  by mods that implement their own entries (see below).


### Customization options (for mod devs)

<details>
<summary>Custom FMJ property (simple)</summary>

Example:
```json5
{
  /*...*/
  "custom": {
    "moehreag-modcredits:description": "Your custom text!"
  }
}
```
**Note:** This property does not support localization.


Example (using translation keys):
```json5
{
  /*...*/
  "custom": {
    "moehreag-modcredits:description-keys": "modid.modcredits.description"
  }
}
```

**Note:** For both options, the line length is limited to a width of 256 and will be wrapped.
</details>

<details>
<summary>Custom Entrypoint (more powerful, but also more manual work and likely to break with version updates)</summary>

```json5
{
  /*...*/
  entrypoints: {
    "moehreag-modcredits": [
      "com.example.modid.modcredits.ModCreditsImpl"
    ]
  },
  /*...*/
}
```

```java
package com.example.modid.modcredits;

import io.github.moehreag.modcredits.ModCreditsApi;
import io.github.moehreag.modcredits.entries.Entry;

import net.fabricmc.loader.api.ModContainer;

public class ModCreditsImpl implements ModCreditsApi { 
	
	@Override 
	public Entry createEntry(ModContainer self, boolean rightText) {
		/* Your code! */
	}
}
```

</details>

### Maven coordinates

This mod can be found on https://maven.axolotlclient.com/. Alternatively, the modrinth maven can be used.

<details>
<summary>
build.gradle.kts
</summary>

```kotlin

repositories {
    maven("https://maven.axolotlclient.com/releases")
}

dependencies {
    
    modImplementation("io.github.moehreag:modcredits:$VERSION")
}


```
</details>

<details>
<summary>
build.gradle
</summary>

```groovy

repositories {
    maven { url = "https://maven.axolotlclient.com/releases" }
}

dependencies {
    
    modImplementation("io.github.moehreag:modcredits:$VERSION")
}


```
</details>

---

You want to use this mod in a modpack but it doesn't offer the correct options? Head to the issue tracker and let us know!