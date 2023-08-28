# QuPath FX

## What's here?
This repository contains code that can help when working with JavaFX.

It's developed alongside [QuPath](https://qupath.github.io), but intended to be reusable across other projects too.

In this way, it's similar in purpose to [ControlsFX](https://controlsfx.github.io) and [JFxtras](https://jfxtras.org) (which gave some inspiration for the name), but distinct from both.

## Features
The main features are:

* **Controls** - several custom controls, including a rotation slider and input display window (to show mouse and keyboard input)
* **Dialogs** - to simplify showing custom dialogs - and notifications, if ControlsFX is available
* **Localization** - to help manage `StringProperty` instances backed by resource bundles
* **Prefs** - to support creating JavaFX properties backed by Java `Preferences`
* **Utils** - Other helper classes, to reduce some boilerplate


## Dependencies
Dependencies are minimal to try to keep things simple. They are:

* JavaFX
* SLF4J-API
* ControlsFX *(optional)*


## License
This project is licensed under Apache 2.0.

(Note that this is a [different open-source license from QuPath's](https://github.com/qupath/qupath))