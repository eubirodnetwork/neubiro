package eu.eubirod.neubiro

actions {
  action(importTheMatrixAction, enabled: bind { model.canImport })
  action(importLookupsAction, enabled: bind { model.canImportLookups })
  action(refreshIndicatorsAction, enabled: bind { model.canRefresh })
  action(runIndicatorsAction, enabled: bind { model.canRun })
  action(refreshFilesAction, enabled: bind { model.canRefreshFiles })
  action(transferFilesAction, enabled: bind { model.canTransferFiles })
  action(clearLogAction, enabled: bind { model.canClearLog })
}
