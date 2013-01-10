/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.meveo.commons.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * @author R.AITYAAZZA
 * @created 20 janv. 11
 */
public class NumberUtils {

    public static BigDecimal round(BigDecimal what, int howmuch) {
        if (what == null) {
            return null;
        }
        double number = Double.parseDouble(what.toString());
        double formatedNumber = (double) ((int) (number * Math.pow(10, howmuch) + .5)) / Math.pow(10, howmuch);
        return BigDecimal.valueOf(formatedNumber);
    }

    public static String format(BigDecimal amount, String format) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        DecimalFormat decimalFormat = new DecimalFormat();
        Locale lcl = Locale.FRENCH;
        decimalFormat = (DecimalFormat) DecimalFormat.getInstance(lcl);
        decimalFormat.applyPattern(format);
        String value = decimalFormat.format(amount);
        return value;
    }

}
