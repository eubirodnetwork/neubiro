/*
 * Copyright 2014-2025 Stefano Gualdi, AGENAS.
 *
 * Licensed under the European Union Public Licence (EUPL), Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.eubirod.neubiro

import com.jgoodies.forms.layout.CellConstraints
import com.jgoodies.forms.layout.FormLayout
import eu.eubirod.neubiro.config.NeubiroConfig
import eu.eubirod.neubiro.stat.CircularDependencyException
import eu.eubirod.neubiro.stat.DataPreparationException
import eu.eubirod.neubiro.stat.IndicatorsHolder
import griffon.core.artifact.GriffonController
import griffon.metadata.ArtifactProviderFor
import griffon.transform.Threading
import groovy.util.logging.Slf4j
import eu.eubirod.neubiro.db.MappingsHolder
import eu.eubirod.neubiro.transfer.WrongCredentialsException

import javax.inject.Inject
import javax.swing.*
import javax.swing.text.AttributeSet
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import javax.swing.text.StyledDocument
import java.awt.*
import eu.eubirod.neubiro.utils.PatternFilter
import javax.swing.text.AbstractDocument
// import griffon.swing.support.fontawesome.FontAwesomeIcon

/**
 * @author Stefano Gualdi <stefano.gualdi@gmail.com>
 */
@Slf4j
@ArtifactProviderFor(GriffonController)
class NeubiroController {

  @Inject
  private DatabaseService databaseService
  @Inject
  private StatEngineService statEngineService
  @Inject
  private ReportService reportService
  @Inject
  private TransferService transferService
  @Inject
  private NeubiroConfig neubiroConfig

  def model
  def view
  def builder

  private textBoxes = [:]

  void mvcGroupInit(Map args) {
    loadPreferences()

    // Setup operatorName/code input filter
    AbstractDocument doc
    doc = (AbstractDocument) builder.operatorName.getDocument()
    doc.setDocumentFilter(new PatternFilter("[A-Za-z_.0-9]*"))

    // Setup year input filter
    doc = (AbstractDocument) builder.year.getDocument()
    doc.setDocumentFilter(new PatternFilter("\\d{0,4}"))
  }

  void mvcGroupDestroy() {
    savePreferences()
  }

  def onStartupEnd(app) {
    refreshTablesStats()

    // Init indicator panel
    refreshIndicators()
  }

  // =================================================================================
  // Files and directories selection
  // =================================================================================

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void selectIndicatorsDir() {
    def file = neubiroConfig.selectFileOrDir(
      application.windowManager.startingWindow,
      'indicatorsDir',
      JFileChooser.DIRECTORIES_ONLY,
      application.messageSource.getMessage('application.form.indicators.field.indicatorsDir.fileChooser.title'))

    if (file) {
      model.indicatorsDir = file.absoluteFile
      runOutsideUI {
        refreshIndicators()
      }
    }
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void selectWorkDir() {
    def file = neubiroConfig.selectFileOrDir(
      application.windowManager.startingWindow,
      'workDir',
      JFileChooser.DIRECTORIES_ONLY,
      application.messageSource.getMessage('application.form.indicators.field.workDir.fileChooser.title'))

    if (file) {
      model.workDir = file.absoluteFile
    }
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void selectCsvFile() {
    def file = neubiroConfig.selectFileOrDir(
      application.windowManager.startingWindow,
      'csvFile',
      JFileChooser.FILES_ONLY,
      application.messageSource.getMessage('application.form.import.field.csvFile.fileChooser.title'),
      NeubiroConfig.CSV_FILES_FILTER)

    if (file) {
      model.csvFile = file.absolutePath
    }
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void selectSpecsFile() {
    def file = neubiroConfig.selectFileOrDir(
      application.windowManager.startingWindow,
      'specsFile',
      JFileChooser.FILES_ONLY,
      application.messageSource.getMessage('application.form.import.field.specsFile.fileChooser.title'),
      NeubiroConfig.SPECS_FILES_FILTER)

    if (file) {
      model.specsFile = file.absolutePath

      runInsideUISync {
        model.importing = true
      }

      runOutsideUI {
        try {
          // Load the chosen spec file and populate MappingsHolder singleton
          databaseService.loadSpecs(file)
          def mappingsHolder = MappingsHolder.instance

          def variables = mappingsHolder.getVariables()
          runInsideUISync {
            model.lookupsFiles.clear()
            model.variablesList.clear()
            model.variablesList.addAll(variables)
          }

          def mappings = mappingsHolder.getLookupsSpecs()
          createLookupsPanel(mappings)
        }
        catch (Exception e) {
          log.error e.message, e
        }
        finally {
          runInsideUISync {
            model.importing = false
          }
        }
      }
    }
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  private createLookupsPanel(lookups) {
    def totalFiles = lookups.size()

    def formPanel = builder.moreFiles

    formPanel.removeAll()
    textBoxes = [:]

    if (totalFiles == 0) {
      formPanel.setVisible(false)
    } else {
      def rows = []
      for (int i = 0; i < totalFiles; i++) {
        rows << "pref, 2dlu"
      }

      FormLayout layout = new FormLayout(
        "right:pref, 4dlu, fill:150dlu:grow, 4dlu, pref, min",
        rows.join(',')
      )
      CellConstraints cc = new CellConstraints()
      formPanel.setLayout(layout)

      for (int i = 0; i < totalFiles; i++) {
        def id = lookups[i].id

        def pos = (i * 2) + 1
        def label = lookups[i].label
        formPanel.add(new JLabel(label), cc.xy(1, pos))

        JTextField text = new JTextField()
        text.setEditable(false)
        formPanel.add(text, cc.xy(3, pos))
        textBoxes[id] = text

        //JButton button = new JButton(new FontAwesomeIcon('fa-folder-open'))
        JButton button = new JButton(text: '+')
        button.setName(id)
        button.addActionListener({ evt ->
          def file = neubiroConfig.selectFileOrDir(
            application.windowManager.startingWindow,
            'lookupsFilesDir',
            JFileChooser.FILES_ONLY,
            application.messageSource.getMessage('application.form.import.field.lookupsFile.fileChooser.title'),
            NeubiroConfig.CSV_FILES_FILTER)
          def id0 = evt.source.name
          if (file) {
            textBoxes[id0].setText(file.absolutePath)
            model.lookupsFiles[id0] = file.absolutePath
          }
        })

        formPanel.add(button, cc.xy(5, pos))
      }
      formPanel.setVisible(true)
    }
    formPanel.revalidate()
    formPanel.repaint()
  }

  // =================================================================================
  // Import
  // =================================================================================

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void importTheMatrix() {
    def file = new File(model.csvFile)
    def variablesFilled = model.areMandatoryVariablesFilled()
    def lookupsFilled = model.areLookupsTablesFilled()
    if (file && file.exists() && variablesFilled && lookupsFilled) {
      runInsideUISync {
        model.importing = true
      }

      runOutsideUI {
        try {
          // Define context variables
          def context = model.getVariables()

          // Do import
          databaseService.importAll(file, context, model.lookupsFiles)

          refreshTablesStats()
        }
        catch (Exception e) {
          log.error e.message, e

          JOptionPane.showMessageDialog(Window.windows.find {
            it.focused
          }, application.messageSource.getMessage('application.message.import.error'), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
        }
        finally {
          runInsideUISync {
            model.importing = false
          }
        }
      }
    } else {
      if (!lookupsFilled) {
        JOptionPane.showMessageDialog(Window.windows.find {
          it.focused
        }, application.messageSource.getMessage('application.message.fillLookups'), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
      } else {
        JOptionPane.showMessageDialog(Window.windows.find {
          it.focused
        }, application.messageSource.getMessage('application.message.fillVariables'), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
      }
    }
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void importLookups() {
    def lookupsFilled = model.areLookupsTablesFilled()
    if (lookupsFilled) {
      runInsideUISync {
        model.importing = true
      }

      runOutsideUI {
        try {
          // Do import
          databaseService.importAll(null, [:], model.lookupsFiles)

          refreshTablesStats()
        }
        catch (Exception e) {
          log.error e.message, e

          JOptionPane.showMessageDialog(Window.windows.find {
            it.focused
          }, application.messageSource.getMessage('application.message.import.error'), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
        }
        finally {
          runInsideUISync {
            model.importing = false
          }
        }
      }
    } else {
      JOptionPane.showMessageDialog(Window.windows.find {
        it.focused
      }, application.messageSource.getMessage('application.message.fillLookups'), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
    }
  }

  // Import events
  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onImportStarted(max) {
    model.maxProgressBar = max
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onImportProgressBarUpdate(recNo, counter) {
    model.status = application.messageSource.getMessage('application.message.import.status.importing', [recNo])
    model.current = counter
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onImportFinished(maxRecords) {
    model.current = model.maxProgressBar
    model.status = application.messageSource.getMessage('application.message.import.status.done', [maxRecords])
  }

  // Import events
  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onLocfStarted(max) {
    model.maxProgressBar = max
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onLocfProgressBarUpdate(recNo) {
    model.status = application.messageSource.getMessage('application.message.locf.status.processing', [recNo, model.maxProgressBar])
    model.current = recNo
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onLocfFinished(maxRecords) {
    model.current = model.maxProgressBar
    model.status = application.messageSource.getMessage('application.message.locf.status.done', [maxRecords])
  }

  // =================================================================================
  // NeubiroConfig page
  // =================================================================================

  void refreshTablesStats() {
    runInsideUISync {
      model.running = true
      model.tablesList.clear()
    }

    runOutsideUI {
      try {
        model.dbStats = databaseService.collectStats()
        runInsideUIAsync {
          model.dbStats.each { k, v ->
            model.tablesList.add([id: k, count: v])
          }
        }
      }
      catch (Exception e) {
        log.error e.message, e
      }
      finally {
        runInsideUIAsync {
          model.running = false
        }
      }
    }
  }

  // =================================================================================
  // Indicators
  // =================================================================================

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void addReferenceFile() {
    def file = neubiroConfig.selectFileOrDir(
      application.windowManager.startingWindow,
      'referenceFilesDir',
      JFileChooser.FILES_ONLY,
      application.messageSource.getMessage('application.form.indicators.field.referenceFile.fileChooser.title'),
      NeubiroConfig.ZIP_FILES_FILTER)
    if (file) {
      // TODO: Verify if file is valid
      def filename = file.absolutePath
      def item = model.referenceFilesList.find { it.name == filename }
      if (!item) {
        model.referenceFilesList.add([name: filename])
      }
    }
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void delReferenceFile() {
    def selection = model.referenceFilesListSelection.selection
    if (selection) {
      def name = selection[0].name
      def item = model.referenceFilesList.find { it.name == name }
      if (item) {
        model.referenceFilesList.remove(item)
      }
    }
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void addInputFile() {
    def file = neubiroConfig.selectFileOrDir(
      application.windowManager.startingWindow,
      'inputFileDir',
      JFileChooser.FILES_ONLY,
      application.messageSource.getMessage('application.form.indicators.field.inputFile.fileChooser.title'),
      NeubiroConfig.ZIP_FILES_FILTER)
    if (file) {
      // TODO: Verify if file is valid
      def filename = file.absolutePath
      def item = model.inputFilesList.find { it.name == filename }
      if (!item) {
        model.inputFilesList.add([name: filename])
      }
    }
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void delInputFile() {
    def selection = model.inputFilesListSelection.selection
    if (selection) {
      def name = selection[0].name
      def item = model.inputFilesList.find { it.name == name }
      if (item) {
        model.inputFilesList.remove(item)
      }
    }
  }

  void refreshIndicators() {
    def indicatorsDir = new File(model.indicatorsDir)

    runInsideUISync {
      model.running = true
      model.indicatorsList.clear()
      model.selectUnitVariableList.clear()
    }

    runOutsideUI {
      try {
        statEngineService.loadIndicators(indicatorsDir, model.mainLanguage)
        runInsideUIAsync {
          model.indicatorsList.addAll(IndicatorsHolder.instance.visibleIndicatorsForUI)
          model.selectUnitVariableList.add('')
          model.selectUnitVariableList.addAll(IndicatorsHolder.instance.variablesForUI.name)
        }
      }
      catch (Exception e) {
        log.error e.message, e
      }
      finally {
        runInsideUIAsync {
          model.current = 0
          model.status = ""
          model.running = false
        }
      }
    }
  }

  void runIndicators() {
    def indicatorsDir = new File(model.indicatorsDir)
    def workDir = new File(model.workDir)

    if (!statEngineService.isRInstalled()) {
      JOptionPane.showMessageDialog(Window.windows.find {
        it.focused
      }, application.messageSource.getMessage('application.message.rnotinstalled.error'), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)

      return
    }

    runInsideUISync {
      model.running = true
    }

    runOutsideUI {
      savePreferences()

      try {
        if (checkMandatoryVariablesForIndicators()) {
          def highlights = parseHighlightsVariable(model.highlightsVariable, model.highlightsValue)

          def definedReferenceType = model.referenceType == 'none' ? '' : model.referenceType;
          def definedEngineType = model.engineType == 'none' ? '' : model.engineType;

          def runningParams = [
            language       : model.language ?: NeubiroConfig.DEFAULT_LANGUAGE,
            operator       : model.operatorName ?: "000000",
            year           : model.year ?: "0000",
            engine_type    : definedEngineType,
            reference      : definedReferenceType,
            reference_files: definedReferenceType == '_external_' ? model.referenceFilesList?.collect { it.name } : [],
            input_files    : definedEngineType == 'central' ? model.inputFilesList?.collect { it.name } : [],
            funnel_group   : highlights[0]?.value ?: ''
          ]

          def selectedIndicatorsForReport = model.indicatorsList.findAll { it.selected && !it.excludeReport }.obj

          def selectUnit = parseSelectUnitVariable(model.selectUnitVariable, model.selectUnitValue)

          def totalRuns = selectUnit.size()

          boolean doIt = true
          if (totalRuns > 1) {
            doIt = JOptionPane.showConfirmDialog(Window.windows.find {
              it.focused
            }, application.messageSource.getMessage('application.message.indicators.multireportWarning', [totalRuns]), application.messageSource.getMessage('application.dialog.warning.title'), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION
          }

          if (doIt) {
            // Create custom directory for R libraries
            File libsDir = new File(workDir, "rlibs")

            File firstWorkDir = null
            for (int i = 0; i < totalRuns; i++) {
              // Calcola il nome della sottodirectory di workdir (all se non loop)
              def realWorkDir = new File(workDir, selectUnit[i].id)

              // Imposta la select unit
              if (selectUnit[i].value) {
                runningParams['select_unit'] = selectUnit[i].value
                runningParams['reference'] = definedReferenceType
                runningParams['selector'] = [selectUnit[i]?.var, selectUnit[i]?.rawValue ]
              } else {
                runningParams['select_unit'] = ''
                // Impostare reference == _internal_ senza valorizzare select_unit non ha senso. Forziamo reference a blank!
                if (definedReferenceType == "_internal_") {
                  runningParams['reference'] = ''
                } else {
                  runningParams['reference'] = definedReferenceType
                }
              }

              if (statEngineService.runIndicators(model.indicatorsList, runningParams, indicatorsDir, realWorkDir, firstWorkDir, libsDir)) {
                reportService.runReportGeneration(selectedIndicatorsForReport, runningParams, indicatorsDir, realWorkDir)
              }

              if (i == 0) {
                firstWorkDir = realWorkDir
              }
            }
          }
        } else {
          JOptionPane.showMessageDialog(Window.windows.find {
            it.focused
          }, application.messageSource.getMessage('application.message.indicators.variables.error'), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
        }
      }
      catch (CircularDependencyException cde) {
        JOptionPane.showMessageDialog(Window.windows.find {
          it.focused
        }, cde.getMessage(), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
      }
      catch (DataPreparationException dpe) {
        log.error dpe.message, dpe
        JOptionPane.showMessageDialog(Window.windows.find {
          it.focused
        }, dpe.getMessage(), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
      }
      catch (Exception e) {
        log.error e.message, e
        JOptionPane.showMessageDialog(Window.windows.find {
          it.focused
        }, e.getMessage(), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
      }
      finally {
        runInsideUIAsync {
          model.running = false
        }
      }
    }
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onRunIndicatorsStarted(maxSteps) {
    model.current = 0
    model.maxProgressBar = maxSteps
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onRunIndicatorsProgress(indicatorId) {
    model.status = application.messageSource.getMessage('application.message.indicators.status.running', [indicatorId])
    model.current = model.current + 1
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onRunIndicatorsPhase(indicatorId, phaseId) {
    def phase = application.messageSource.getMessage("application.message.indicators.status.phase${phaseId}")
    model.status = application.messageSource.getMessage('application.message.indicators.status.phase', [indicatorId, phase])
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onRunIndicatorsFinished() {
    model.status = application.messageSource.getMessage('application.message.indicators.status.allDone')
    model.current = model.maxProgressBar
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onRunIndicatorError(indicatorId) {
    model.status = application.messageSource.getMessage('application.message.indicators.status.error', [indicatorId])
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onReportGenerationStarted(maxSteps) {
    model.status = application.messageSource.getMessage('application.message.report.status.running')
    model.current = 0
    model.maxProgressBar = maxSteps
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onReportGenerationProgress() {
    model.current = model.current + 1
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onReportGenerationFinished() {
    model.status = application.messageSource.getMessage('application.message.report.status.done')
    model.current = model.maxProgressBar
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onReportGenerationError() {
    model.status = application.messageSource.getMessage('application.message.report.status.error')
  }

  // =================================================================================
  // Transfer
  // =================================================================================

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void refreshFiles() {
    def workDir = new File(model.workDir)

    runInsideUISync {
      model.transferring = true
      model.filesList.clear()
    }

    runOutsideUI {
      try {
        def files = transferService.collectFiles(workDir)
        runInsideUIAsync {
          model.filesList.addAll(files)
        }
      }
      catch (Exception e) {
        log.error e.message, e
      }
      finally {
        runInsideUIAsync {
          model.current = 0
          model.status = ""
          model.transferring = false
        }
      }
    }
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void transferFiles() {
    runInsideUISync {
      model.transferring = true
    }

    runOutsideUI {
      try {
        def selectedFiles = model.filesList.findAll { it.selected }
        if (selectedFiles.size() > 0) {
          def serverParams = [
            server   : model.server,
            username : model.username,
            password : model.password,
            activeFtp: model.activeFtp
          ]

          transferService.transferFiles(serverParams, selectedFiles)
        } else {
          JOptionPane.showMessageDialog(Window.windows.find {
            it.focused
          }, application.messageSource.getMessage('application.message.transfer.files.error'), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
        }
      }
      catch (UnknownHostException uhe) {
        log.error uhe.message, uhe
        JOptionPane.showMessageDialog(Window.windows.find {
          it.focused
        }, application.messageSource.getMessage('application.message.transfer.host.error', [model.server]), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
      }
      catch (WrongCredentialsException wce) {
        log.error wce.message, wce
        JOptionPane.showMessageDialog(Window.windows.find {
          it.focused
        }, application.messageSource.getMessage('application.message.transfer.account.error'), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
      }
      catch (Exception e) {
        log.error e.message, e
        JOptionPane.showMessageDialog(Window.windows.find {
          it.focused
        }, e.getMessage(), application.messageSource.getMessage('application.dialog.error.title'), JOptionPane.ERROR_MESSAGE)
      }
      finally {
        runInsideUIAsync {
          model.transferring = false
        }
      }
    }
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onTransferFilesStarted(maxSteps) {
    model.current = 0
    model.maxProgressBar = maxSteps
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onTransferFilesProgress(filename) {
    model.status = application.messageSource.getMessage('application.message.transfer.status.running', [filename])
    model.current = model.current + 1
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onTransferFilesFinished() {
    model.status = application.messageSource.getMessage('application.message.transfer.status.done')
    model.current = model.maxProgressBar
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onTransferFilesError(filename) {
    model.status = application.messageSource.getMessage('application.message.transfer.status.error', [filename])
  }

  // =================================================================================
  // Logging
  // =================================================================================

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void clearLog() {
    JTextPane tp = builder.logArea
    tp.setText("")
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onWriteLog(txt, type) {
    def color
    if (type == "error") {
      color = Color.RED
    } else if (type == "info") {
      color = Color.BLUE
    } else if (type == "complete") {
      color = Color.GREEN
    } else {
      color = Color.BLACK
    }

    writeLogMessage(txt + "\n", color)
  }

  private void writeLogMessage(String msg, Color c) {
    JTextPane tp = builder.logArea

    StyleContext sc = StyleContext.getDefaultStyleContext();
    AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c)

    StyledDocument doc = tp.getStyledDocument()
    int l = doc.getLength()
    doc.insertString(l, msg, aset)

    // Scroll
    tp.setCaretPosition(tp.getDocument().getLength())
  }

  // =================================================================================
  // OSX specific
  // =================================================================================

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onOSXAbout(app) {
    about()
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void onOSXPrefs(app) {
    preferences()
  }

  void onOSXQuit(app) {
    application.shutdown()
  }

  // =================================================================================
  // General
  // =================================================================================

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void about() {
    showDialog('about')
  }

  @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
  void preferences() {
    showDialog('preferences')
  }

  private showDialog(String dialogName) {
    try {
      withMVC(dialogName) { m, v, c ->
        c.show()
      }
    } catch (Throwable e) {
      log.error e.message, e
    }
  }

  void quit() {
    application.shutdown()
  }

  // =================================================================================
  // NeubiroConfig preferences
  // =================================================================================

  void savePreferencesAction() {
    runOutsideUI {
      savePreferences()
    }
  }

  private void savePreferences() {
    neubiroConfig.set("operatorName", model.operatorName)
    neubiroConfig.set("year", model.year)
    neubiroConfig.set("mainLanguage", model.mainLanguage)
    neubiroConfig.set("engineType", model.engineType)
    neubiroConfig.set("language", model.language)
    neubiroConfig.set("referenceType", model.referenceType)
    neubiroConfig.set("server", model.server)
    neubiroConfig.set("username", model.username)

    neubiroConfig.set("indicatorsDir", model.indicatorsDir)
    neubiroConfig.set("workDir", model.workDir)
    neubiroConfig.set("csvFile", model.csvFile)

    neubiroConfig.save()
  }

  private void loadPreferences() {
    neubiroConfig.loadIfNeeded()

    model.operatorName = neubiroConfig.get("operatorName", NeubiroConfig.DEFAULT_OPERATOR_NAME)
    model.year = neubiroConfig.get("year", NeubiroConfig.DEFAULT_YEAR)
    model.mainLanguage = neubiroConfig.get("mainLanguage", NeubiroConfig.DEFAULT_MAIN_LANGUAGE)
    model.engineType = neubiroConfig.get("engineType", NeubiroConfig.DEFAULT_ENGINE_TYPE)
    model.language = neubiroConfig.get("language", NeubiroConfig.DEFAULT_LANGUAGE)
    model.referenceType = neubiroConfig.get("referenceType", NeubiroConfig.DEFAULT_REFERENCE_TYPE)
    model.server = neubiroConfig.get("server", NeubiroConfig.DEFAULT_SERVER)
    model.username = neubiroConfig.get("username", NeubiroConfig.DEFAULT_USERNAME)

    model.indicatorsDir = neubiroConfig.get("indicatorsDir", NeubiroConfig.DEFAULT_INDICATORS_DIR)
    model.workDir = neubiroConfig.get("workDir", NeubiroConfig.DEFAULT_WORK_DIR)
    model.csvFile = neubiroConfig.get("csvFile", NeubiroConfig.DEFAULT_CSV_FILE)

    refreshIndicators()
  }

  // =================================================================================
  // Utility
  // =================================================================================

  private parseHighlightsVariable(String variable, String val, String op = "||") {
    def result = []

    if (variable && val.indexOf('|') > 0) {
      def inputs = val.split(/\|/).findAll { it.trim() != '' }
      inputs.each { value ->
        result.addAll(parseHighlightsVariableImpl(variable, value, op))
      }
    } else {
      result = parseHighlightsVariableImpl(variable, val, op)
    }
    return result
  }

  private parseHighlightsVariableImpl(String variable, String value, String op = "||") {
    def result = []

    if (variable && value) {
      def parts = value.split(/ /).findAll { it.trim() != '' }
      if (parts.size() == 1 && parts[0] == "*") {
        def values = databaseService.collectUniqueValuesFor(variable)
        values.each {
          result << [id: "${it ?: '_NA_'}", var: variable, value: "${variable}=='${it ?: ''}'"]
        }
      } else {
        result << [id: parts.collect { "${it ?: '_NA_'}" }.join("_"), var: variable, value: parts.collect {
          "${variable}=='${it}'"
        }.join(op)]
      }
    } else if (!variable && value) {
      result << [id: 'SINGLE', var: '', value: value.replaceAll("\"", "'")]
    } else {
      result << [id: 'SINGLE', var: '', value: '']
    }

    return result
  }

  private parseSelectUnitVariable(String variable, String val, String op = "||") {
    def result = []
    if (variable && val.indexOf('|') > 0) {
      def inputs = val.split(/\|/).findAll { it.trim() != '' }
      inputs.each { value ->
        result.addAll(parseSelectUnitVariableImpl(variable, value, op))
      }
    } else {
      result = parseSelectUnitVariableImpl(variable, val, op)
    }

    return result
  }

  private parseSelectUnitVariableImpl(String variable, String value, String op = "||") {
    def result = []

    if (variable && value) {
      def values
      def parts = value.split(/ /).findAll { it.trim() != '' }
      if (parts.size() == 1 && parts[0] == "*") {
        values = databaseService.collectUniqueValuesFor(variable)
      } else {
        values = parts
      }
      values.each {
        result << [id: "${it ?: '_NA_'}", var: variable, value: "${variable}=='${it ?: ''}'", rawValue: "${it ?: ''}"]
      }
    } else if (!variable && value) {
      result << [id: 'SINGLE', var: '', value: value.replaceAll("\"", "'")]
    } else {
      result << [id: 'SINGLE', var: '', value: '']
    }

    return result
  }

  private boolean checkMandatoryVariablesForIndicators() {
    return model.operatorName && model.year.trim()
  }
}
