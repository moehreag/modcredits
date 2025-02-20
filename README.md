# Mod Credits screen

> With poem!

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
<summary>Custom Entrypoint (more powerful, but also more manual work)</summary>

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