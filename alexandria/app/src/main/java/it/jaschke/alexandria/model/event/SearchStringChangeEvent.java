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

package it.jaschke.alexandria.model.event;

/**
 * Event that occurs when the search string is modified. For example, when the
 * user types in a word.
 *
 * @author Jesús Adolfo García Pasquel
 */
public class SearchStringChangeEvent {

    /**
     * The previous value of the search string.
     */
    private final String mOldSearchString;

    /**
     * The new value of the search string.
     */
    private final String mNewSearchString;


    /**
     * Creates a new instance of {@link SearchStringChangeEvent}.
     *
     * @param oldValue the previous value of the search string.
     * @param newValue the new value of the search string.
     */
    public SearchStringChangeEvent(String oldValue, String newValue) {
        mOldSearchString = oldValue;
        mNewSearchString = newValue;
    }

    public String getOldSearchString() {
        return mOldSearchString;
    }

    public String getNewSearchString() {
        return mNewSearchString;
    }

}
