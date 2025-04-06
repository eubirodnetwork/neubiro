package eu.eubirod.neubiro

import ca.odell.glazedlists.swing.EventSelectionModel
import ca.odell.glazedlists.swing.EventTableModel
import griffon.core.artifact.GriffonView
import griffon.metadata.ArtifactProviderFor
import eu.eubirod.neubiro.utils.AlphanumComparator

import eu.eubirod.neubiro.utils.PersonalizedTableFormat

import javax.swing.BoxLayout
import java.awt.BorderLayout
import javax.swing.SwingConstants
import java.awt.GridBagConstraints

import static griffon.util.GriffonApplicationUtils.isMacOSX

busyComponent(name: application.messageSource.getMessage('application.tab.import.name'),
  id: "c1",
  constraints: BorderLayout.CENTER,
  busy: bind { model.importing }) {
  busyModel(description: application.messageSource.getMessage('application.tab.import.busy.description'))
  panel() {
    borderLayout()
    panel(constraints: NORTH) {
      boxLayout(axis: BoxLayout.Y_AXIS)
      panel(border: titledBorder(title: application.messageSource.getMessage('application.form.import.title'))) {
        formLayout(columns: "right:pref, 4dlu, fill:150dlu:grow, 4dlu, pref, min", rows: "pref, 2dlu, pref, 4dlu")

        label(application.messageSource.getMessage('application.form.import.field.csvFile.label'), constraints: cc(x: 1, y: 1))
        textField(text: bind('csvFile', target: model, mutual: true), editable: false, constraints: cc(x: 3, y: 1))
        button(action: selectCsvFileAction, constraints: cc(x: 5, y: 1))

        label(application.messageSource.getMessage('application.form.import.field.specsFile.label'), constraints: cc(x: 1, y: 3))
        textField(text: bind('specsFile', target: model, mutual: true), editable: false, constraints: cc(x: 3, y: 3))
        button(action: selectSpecsFileAction, constraints: cc(x: 5, y: 3))
      }

      panel(id: 'moreFiles', border: titledBorder(title: application.messageSource.getMessage('application.form.import.lookups.title')), visible: false) {
      }
    }

    panel(constraints: CENTER) {
      boxLayout(axis: BoxLayout.Y_AXIS)
      scrollPane(border: titledBorder(title: application.messageSource.getMessage('application.form.import.variables.title'))) {
        table(id: "variablesTable", model: createVariablesTableModel())
      }
    }

    hbox(constraints: SOUTH) {
      button(importTheMatrixAction)
      button(importLookupsAction, visible: bind { model.canImportLookups })
    }
  }
}

def createVariablesTableModel() {
  def columnFields = ["name", "type", "label", "mandatory", "value"]
  def columnNames = columnFields.collect {
    application.messageSource.getMessage("application.table.variables.${it}.name", it.capitalize())
  }
  new EventTableModel(model.variablesList, [
    getColumnCount: { columnNames.size() },
    getColumnName : { index -> columnNames[index] },
    getColumnValue: { object, index ->
      object."${columnFields[index]}"
    },
    getColumnClass: { index ->
      if (index == 3) {
        Boolean.class
      } else if (index == 4) {
        String.class
      } else {
        Object.class
      }
    },
    isEditable    : { object, index ->
      index == 4
    },
    setColumnValue: { object, cell, index ->
      def item = model.variablesList.find { it.name == object.name }
      item.value = cell
      return item
    }
  ] as PersonalizedTableFormat)
}
