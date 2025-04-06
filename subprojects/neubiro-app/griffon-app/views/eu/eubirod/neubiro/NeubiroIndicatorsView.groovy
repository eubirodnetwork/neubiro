package eu.eubirod.neubiro

import ca.odell.glazedlists.swing.EventTableModel
import eu.eubirod.neubiro.utils.PersonalizedTableFormat
import ca.odell.glazedlists.swing.EventSelectionModel

import javax.swing.Box
import javax.swing.BoxLayout
import java.awt.BorderLayout

def referenceSelectionModel = new EventSelectionModel(model.referenceFilesList)
referenceSelectionModel.selectionMode = javax.swing.ListSelectionModel.SINGLE_SELECTION

def inputSelectionModel = new EventSelectionModel(model.inputFilesList)
inputSelectionModel.selectionMode = javax.swing.ListSelectionModel.SINGLE_SELECTION

busyComponent(name: application.messageSource.getMessage('application.tab.indicators.name'),
  id: "i1",
  constraints: BorderLayout.CENTER,
  busy: bind { model.running }) {
  busyModel(description: application.messageSource.getMessage('application.tab.indicators.busy.description'))
  panel() {
    boxLayout(axis: BoxLayout.PAGE_AXIS)
    // panel(border: titledBorder(title: application.messageSource.getMessage('application.form.indicators.engine.title'))) {
    panel(border: titledBorder(title: '')) {
      formLayout(columns: "right:pref, 4dlu, fill:150dlu:grow, 4dlu, pref, min", rows: "pref, 2dlu, pref")

      def engineTypes = [
        (application.messageSource.getMessage('application.form.indicators.field.engineType.local.label'))  : 'local',
        (application.messageSource.getMessage('application.form.indicators.field.engineType.central.label')): 'central'
      ]
      label(application.messageSource.getMessage('application.form.indicators.field.engineType.label'), constraints: cc(x: 1, y: 1))
      comboBox(selectedItem: bind(target: model, targetProperty: "engineType", mutual: true,
        converter: { v -> engineTypes[v] },
        reverseConverter: { v -> engineTypes.find { it.value == v }?.key }
      ),
        items: engineTypes.collect(['']) { k, v -> k },
        constraints: cc(x: 3, y: 1)
      )
    }

    // panel(border: titledBorder(title: application.messageSource.getMessage('application.form.indicators.folders.title'))) {
    panel(border: titledBorder(title: '')) {
      formLayout(columns: "right:pref, 4dlu, fill:150dlu:grow, 4dlu, pref, min", rows: "pref, 2dlu, pref, 2dlu, pref")

      label(application.messageSource.getMessage('application.form.indicators.field.indicatorsDir.label'), constraints: cc(x: 1, y: 1))
      textField(text: bind('indicatorsDir', target: model, mutual: true), editable: false, constraints: cc(x: 3, y: 1))
      button(action: selectIndicatorsDirAction, constraints: cc(x: 5, y: 1))

      label(application.messageSource.getMessage('application.form.indicators.field.workDir.label'), constraints: cc(x: 1, y: 3))
      textField(text: bind('workDir', target: model, mutual: true), editable: false, constraints: cc(x: 3, y: 3))
      button(action: selectWorkDirAction, constraints: cc(x: 5, y: 3))
    }

    // panel(border: titledBorder(title: application.messageSource.getMessage('application.form.indicators.report.title'))) {
    panel(border: titledBorder(title: '')) {
      formLayout(columns: "right:pref, 4dlu, fill:150dlu:grow, 4dlu, pref, min", rows: "pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref")

      def languages = [
        (application.messageSource.getMessage('application.form.indicators.field.language.it.label')): 'it',
        (application.messageSource.getMessage('application.form.indicators.field.language.en.label')): 'en'
      ]
      label(application.messageSource.getMessage('application.form.indicators.field.language.label'), constraints: cc(x: 1, y: 1))
      comboBox(selectedItem: bind(target: model, targetProperty: "language", mutual: true,
        converter: { v -> languages[v] },
        reverseConverter: { v -> languages.find { it.value == v }?.key }
      ),
        items: languages.collect(['']) { k, v -> k },
        constraints: cc(x: 3, y: 1)
      )

      label(application.messageSource.getMessage('application.form.indicators.field.selectUnit.label'), constraints: cc(x: 1, y: 3))
      panel(constraints: cc(x: 3, y: 3)) {
        formLayout(columns: "right:pref, 4dlu, fill:50dlu:grow, min", rows: "pref")
        comboBox(selectedItem: bind(target: model, targetProperty: "selectUnitVariable"),
          model: eventComboBoxModel(source: model.selectUnitVariableList),
          constraints: cc(x: 1, y: 1)
        )
        textField(text: bind('selectUnitValue', target: model), constraints: cc(x: 3, y: 1))
      }

      label(application.messageSource.getMessage('application.form.indicators.field.highlights.label'), constraints: cc(x: 1, y: 7))
      panel(constraints: cc(x: 3, y: 7)) {
        formLayout(columns: "right:pref, 4dlu, fill:50dlu:grow, min", rows: "pref")
        comboBox(selectedItem: bind(target: model, targetProperty: "highlightsVariable"),
          model: eventComboBoxModel(source: model.selectUnitVariableList),
          constraints: cc(x: 1, y: 1)
        )
        textField(text: bind('highlightsValue', target: model), constraints: cc(x: 3, y: 1))
      }

      def referenceTypes = [
        (application.messageSource.getMessage('application.form.indicators.field.reference.none.label'))    : 'none',
        (application.messageSource.getMessage('application.form.indicators.field.reference.internal.label')): '_internal_',
        (application.messageSource.getMessage('application.form.indicators.field.reference.external.label')): '_external_'
      ]
      label(application.messageSource.getMessage('application.form.indicators.field.reference.label'), constraints: cc(x: 1, y: 9))
      comboBox(selectedItem: bind(target: model, targetProperty: "referenceType", mutual: true,
        converter: { v -> referenceTypes[v] },
        reverseConverter: { v -> referenceTypes.find { it.value == v }?.key }
      ),
        items: referenceTypes.collect(['']) { k, v -> k },
        actionPerformed: {
          // Fix panel update!!!
          builder.indicatorsPanel.revalidate()
          builder.indicatorsPanel.repaint()
        },
        constraints: cc(x: 3, y: 9)
      )
    }

    panel(id: 'indicatorsPanel') {
      boxLayout(axis: BoxLayout.Y_AXIS)
      panel(border: titledBorder(title: application.messageSource.getMessage('application.form.indicators.referenceFiles.title')), visible: bind(source: model, sourceProperty: 'canShowExternalFiles')) {
        boxLayout(axis: BoxLayout.X_AXIS)
        scrollPane() {
          table(id: "referenceFilesTable", model: createReferenceFilesTableModel(), selectionModel: referenceSelectionModel)
        }
        noparent {
          referenceSelectionModel.selected.addListEventListener(model.referenceFilesListSelection)
        }
        panel() {
          boxLayout(axis: BoxLayout.Y_AXIS)
          button(addReferenceFileAction)
          button(delReferenceFileAction)
        }
      }
      panel(border: titledBorder(title: application.messageSource.getMessage('application.form.indicators.inputFiles.title')), visible: bind(source: model, sourceProperty: 'canShowInputFiles')) {
        boxLayout(axis: BoxLayout.X_AXIS)
        scrollPane() {
          table(id: "inputFilesTable", model: createInputFilesTableModel(), selectionModel: inputSelectionModel)
        }
        noparent {
          inputSelectionModel.selected.addListEventListener(model.inputFilesListSelection)
        }
        panel() {
          boxLayout(axis: BoxLayout.Y_AXIS)
          button(addInputFileAction)
          button(delInputFileAction)
        }
      }
      scrollPane(border: titledBorder(title: application.messageSource.getMessage('application.form.indicators.indicators.title'))) {
        table(id: "indicatorsTable", model: createIndicatorsTableModel())
      }
    }

    hbox() {
      button(refreshIndicatorsAction)
      button(runIndicatorsAction)
      widget(Box.createHorizontalGlue())
    }
  }
}


def createReferenceFilesTableModel() {
  def columnFields = ["name"]
  def columnNames = columnFields.collect {
    application.messageSource.getMessage("application.table.referenceFiles.${it}.name", it.capitalize())
  }
  new EventTableModel(model.referenceFilesList, [
    getColumnCount     : { columnNames.size() },
    getColumnName      : { index -> columnNames[index] },
    getColumnValue     : { object, index ->
      object."${columnFields[index]}"
    },
    getColumnClass     : { index ->
      index == 0 ? String.class : Object.class
    },
    isEditable         : { object, index ->
      false
    },
    getColumnComparator: { column ->
      if (column == 1) {
        return alphaComparator
      }
    }
  ] as PersonalizedTableFormat)
}

def createInputFilesTableModel() {
  def columnFields = ["name"]
  def columnNames = columnFields.collect {
    application.messageSource.getMessage("application.table.inputFiles.${it}.name", it.capitalize())
  }
  new EventTableModel(model.inputFilesList, [
    getColumnCount     : { columnNames.size() },
    getColumnName      : { index -> columnNames[index] },
    getColumnValue     : { object, index ->
      object."${columnFields[index]}"
    },
    getColumnClass     : { index ->
      index == 0 ? String.class : Object.class
    },
    isEditable         : { object, index ->
      false
    },
    getColumnComparator: { column ->
      if (column == 1) {
        return alphaComparator
      }
    }
  ] as PersonalizedTableFormat)
}

def createIndicatorsTableModel() {
  def columnFields = ["selected", "id", "description"]
  def columnNames = columnFields.collect {
    application.messageSource.getMessage("application.table.indicators.${it}.name", it.capitalize())
  }
  new EventTableModel(model.indicatorsList, [
    getColumnCount     : { columnNames.size() },
    getColumnName      : { index -> columnNames[index] },
    getColumnValue     : { object, index ->
      object."${columnFields[index]}"
    },
    getColumnClass     : { index ->
      index == 0 ? Boolean.class : Object.class
    },
    isEditable         : { object, index ->
      index == 0
    },
    setColumnValue     : { object, cell, index ->
      def item = model.indicatorsList.find { it.id == object.id }
      item.selected = !item.selected
      return item
    },
    getColumnComparator: { column ->
      if (column == 1) {
        return alphaComparator
      }
    }
  ] as PersonalizedTableFormat)
}
