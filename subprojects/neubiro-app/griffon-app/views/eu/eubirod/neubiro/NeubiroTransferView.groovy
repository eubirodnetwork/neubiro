package eu.eubirod.neubiro

import ca.odell.glazedlists.swing.EventTableModel
import eu.eubirod.neubiro.utils.PersonalizedTableFormat

import javax.swing.BoxLayout
import java.awt.BorderLayout

busyComponent(name: application.messageSource.getMessage('application.tab.transfer.name'),
  id: "t1",
  constraints: BorderLayout.CENTER,
  busy: bind { model.transferring }) {
  busyModel(description: application.messageSource.getMessage('application.tab.transfer.busy.description'))
  panel() {
    borderLayout()
    panel(border: titledBorder(title: application.messageSource.getMessage('application.form.transfer.title')), constraints: NORTH) {
      formLayout(columns: "right:pref, 4dlu, fill:150dlu:grow, 4dlu, pref, min", rows: "pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref,4dlu")

      label(application.messageSource.getMessage('application.form.transfer.field.workDir.label'), constraints: cc(x: 1, y: 1))
      textField(text: bind('workDir', target: model, mutual: true), editable: false, constraints: cc(x: 3, y: 1))
      button(action: selectWorkDirAction, constraints: cc(x: 5, y: 1))

      label(application.messageSource.getMessage('application.form.transfer.field.server.label'), constraints: cc(x: 1, y: 3))
      textField(text: bind('server', target: model, mutual: true), constraints: cc(x: 3, y: 3))
      button(action: savePreferencesAction, constraints: cc(x: 5, y: 3))

      label(application.messageSource.getMessage('application.form.transfer.field.username.label'), constraints: cc(x: 1, y: 5))
      textField(text: bind('username', target: model, mutual: true), constraints: cc(x: 3, y: 5))

      label(application.messageSource.getMessage('application.form.transfer.field.password.label'), constraints: cc(x: 1, y: 7))
      passwordField(text: bind('password', target: model, mutual: true), constraints: cc(x: 3, y: 7))

      label(application.messageSource.getMessage('application.form.transfer.field.activeFtp.label'), constraints: cc(x: 1, y: 9))
      checkBox(selected: bind('activeFtp', target: model, mutual: true), constraints: cc(x: 3, y: 9))
    }

    panel() {
      boxLayout(axis: BoxLayout.Y_AXIS)
      scrollPane(border: titledBorder(title: application.messageSource.getMessage('application.form.transfer.files.title'))) {
        table(id: "filesTable", model: createFilesTableModel())
      }
    }

    hbox(constraints: SOUTH) {
      button(refreshFilesAction)
      button(transferFilesAction)
    }
  }
}

def createFilesTableModel() {
  def columnFields = ["selected", "name", "path", "date"]
  def columnNames = columnFields.collect {
    application.messageSource.getMessage("application.table.files.${it}.name", it.capitalize())
  }
  new EventTableModel(model.filesList, [
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
      def item = model.filesList.find { it.id == object.id }
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

