package eu.eubirod.neubiro

import static griffon.util.GriffonApplicationUtils.getIsMacOSX

menuBar {
  if (!isMacOSX) {
    menu(text: application.messageSource.getMessage('application.menu.file.name'),
      mnemonic: application.messageSource.getMessage('application.menu.file.mnemonic')) {
      // menuItem(preferencesAction)
      separator()
      menuItem quitAction
    }
  }

  if (!isMacOSX) {
    menu(text: application.messageSource.getMessage('application.menu.help.name')) {
      menuItem aboutAction
    }
  }
}
