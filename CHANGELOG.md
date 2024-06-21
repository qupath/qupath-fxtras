## v0.1.5
- Improve behavior of `FXUtils.installSelectAllOrNoneMenu` (https://github.com/qupath/qupath/issues/1498)
- New `FXUtils.addCloseWindowShortcuts` methods to enable closing windows from the keyboard

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