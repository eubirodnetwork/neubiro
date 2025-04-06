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
package eu.eubirod.neubiro.utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @see: http://www.jroller.com/dpmihai/entry/documentfilter
 */
public class PatternFilter extends DocumentFilter {

  // Useful for every kind of input validation !
  // this is the insert pattern
  // The pattern must contain all subpatterns so we can enter characters into a text component !
  private Pattern pattern;

  public PatternFilter(String pat) {
    pattern = Pattern.compile(pat);
  }

  public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
    String newStr = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
    Matcher m = pattern.matcher(newStr);
    if (m.matches()) {
      super.insertString(fb, offset, string, attr);
    }
  }

  public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr) throws BadLocationException {

    if (length > 0) fb.remove(offset, length);
    insertString(fb, offset, string, attr);
  }
}
