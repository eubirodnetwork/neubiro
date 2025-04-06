package eu.eubirod.neubiro.db

import groovy.time.TimeCategory
import groovy.time.TimeDuration

class ImportEnhancerFunctions {
  static int ageInDaysFrom(Date fromDate, Date toDate) {
    TimeDuration t = TimeCategory.minus(toDate, fromDate)
    t.days
  }

  static int ageInYearsFrom(Date fromDate, Date toDate) {
    TimeDuration t = TimeCategory.minus(toDate, fromDate)
    t.days / 365.25
  }
}
