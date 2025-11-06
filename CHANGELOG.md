## v0.2.1
- Improve Dialogs.getDefaultOwner() logic https://github.com/qupath/qupath-fxtras/pull/60

## v0.2.0
- Update gradle, version https://github.com/qupath/qupath-fxtras/pull/48
- FXUtils commands to get stages https://github.com/qupath/qupath-fxtras/pull/47
- Convert to kotlin https://github.com/qupath/qupath-fxtras/pull/49
- Make the use of country codes optional https://github.com/qupath/qupath-fxtras/pull/50
- Fix publishing https://github.com/qupath/qupath-fxtras/pull/51
- Update version https://github.com/qupath/qupath-fxtras/pull/52
- Improve file chooser behavior https://github.com/qupath/qupath-fxtras/pull/53

## v0.1.7
- Improve screen choice for input display https://github.com/qupath/qupath-fxtras/pull/44

## v0.1.6
- Allow default directory to be set when prompting for a file with FileChoosers (https://github.com/qupath/qupath-fxtras/issues/40)

## v0.1.5
- Improve behavior of `FXUtils.installSelectAllOrNoneMenu` (https://github.com/qupath/qupath/issues/1498)
- New `FXUtils.addCloseWindowShortcuts` methods to enable closing windows from the keyboard
- Improve error notifications when there is no associated text

## v0.1.4
- Make yes/no/cancel dialog non-resizable, for consistency (https://github.com/qupath/qupath-fxtras/issues/26)
- Don't set file/directory chooser initial directory if it doesn't exist
  - Resolves a bug spotted in QuPath (https://github.com/qupath/qupath/issues/1441)

## v0.1.3
- Support prompt text for preferences using text fields or combo boxes
  - Use 'key + .prompt' within resource bundles

## v0.1.2
- Support `File` preferences (not only directories)

## v0.1.1
- Don't require annotated property `Prefs` to be accessible
  - This enables preferences to be stored in private fields

## v0.1.0
- Initial release