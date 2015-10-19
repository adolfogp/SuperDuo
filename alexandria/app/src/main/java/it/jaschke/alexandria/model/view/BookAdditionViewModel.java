/*
 * Copyright 2015 Jesús Adolfo García Pasquel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.jaschke.alexandria.model.view;

import org.parceler.Parcel;

/**
 * View model for the book addition view. Provides data and behaviour.
 *
 * @author Jesús Adolfo García Pasquel
 */
@Parcel(Parcel.Serialization.BEAN)
public class BookAdditionViewModel {


    /**
     * Regular expression used to verify that the text entered is a valid
     * ISBN-13 number.
     */
    public static final String ISBN13_REGULAR_EXPRESSION =
            "^((978)|(979))[0-9]{10}$";

    /**
     * Identifies the messages written to the log by this class.
     */
    private static final String LOG_TAG = BookAdditionViewModel.class.getSimpleName();

    /**
     * The text entered by the user to specify the book to be added. Must be
     * the ISBN-13 of a book.
     */
    private String mIsbn;

    public String getIsbn() {
        return mIsbn;
    }

    public void setIsbn(String isbn) {
        mIsbn = isbn;
    }

    /**
     * Returns {@code true} if the argument is a valid ISBN-13 number. That is,
     * it is made of 13 digits, starting with 978 or 979.
     *
     * @param scannedCode the strig to check for validity.
     * @return {@code true} if the argument is a valid ISBN-13 number.
     * @see #ISBN13_REGULAR_EXPRESSION
     */
    public boolean isValidIsbn(String scannedCode) {
        if (scannedCode == null) {
            return false;
        }
        return scannedCode.matches(ISBN13_REGULAR_EXPRESSION);
    }
}
