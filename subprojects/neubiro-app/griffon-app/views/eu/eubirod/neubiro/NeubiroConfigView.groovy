package eu.eubirod.neubiro

import ca.odell.glazedlists.swing.EventTableModel
import eu.eubirod.neubiro.utils.PersonalizedTableFormat

import javax.swing.BoxLayout
import java.awt.BorderLayout

busyComponent(name: application.messageSource.getMessage('application.tab.config.name'),
  id: "c0",
  constraints: BorderLayout.CENTER,
  busy: bind { model.importing || model.running }) {
  busyModel(description: application.messageSource.getMessage('application.tab.config.busy.description'))
  panel() {
    borderLayout()
    panel(border: titledBorder(title: application.messageSource.getMessage('application.form.config.title')), constraints: NORTH) {
      formLayout(columns: "right:pref, 4dlu, fill:150dlu:grow, 4dlu, pref, min", rows: "pref, 2dlu, pref, 2dlu, pref, 4dlu")

      // See filter in NeubiroController.mvcGroupInit()
      label(application.messageSource.getMessage('application.form.config.field.operatorName.label'), constraints: cc(x: 1, y: 1))
      textField(id: 'operatorName', text: bind('operatorName', target: model, mutual: true), constraints: cc(x: 3, y: 1))

      // See filter in NeubiroController.mvcGroupInit()
      label(application.messageSource.getMessage('application.form.config.field.year.label'), constraints: cc(x: 1, y: 3))
      textField(id: 'year', text: bind('year', target: model, mutual: true), constraints: cc(x: 3, y: 3))

      def languages = [
        (application.messageSource.getMessage('application.form.config.field.mainLanguage.it.label')): 'it',
        (application.messageSource.getMessage('application.form.config.field.mainLanguage.en.label')): 'en'
      ]
      label(application.messageSource.getMessage('application.form.config.field.mainLanguage.label'), constraints: cc(x: 1, y: 5))
      comboBox(selectedItem: bind(target: model, targetProperty: "mainLanguage", mutual: true,
        converter: { v -> languages[v] },
        reverseConverter: { v -> languages.find { it.value == v }?.key }
      ),
        items: languages.collect(['']) { k, v -> k },
        constraints: cc(x: 3, y: 5)
      )
    }

    panel() {
      boxLayout(axis: BoxLayout.Y_AXIS)
      scrollPane(border: titledBorder(title: application.messageSource.getMessage('application.form.config.tables.title'))) {
        table(id: "tablesTable", model: createTablesTableModel())
      }
    }

    hbox(constraints: SOUTH) {
      button(savePreferencesAction)
    }
  }
}

def createTablesTableModel() {
  def columnFields = ["id", "count"]
  def columnNames = columnFields.collect {
    application.messageSource.getMessage("application.table.tables.${it}.name", it.capitalize())
  }

  new EventTableModel(model.tablesList, [
    getColumnCount: { columnNames.size() },
    getColumnName : { index -> columnNames[index] },
    getColumnValue: { object, index ->
      object."${columnFields[index]}"
    },
    getColumnClass: { index ->
      if (index == 1) {
        Long.class
      } else {
        String.class
      }
    },
    isEditable    : { object, index ->
      false
    }
  ] as PersonalizedTableFormat)
}
