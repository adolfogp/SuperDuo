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

import it.jaschke.alexandria.model.domain.Author;

/**
 * View model for the book authors. Provides data and behaviour.
 *
 * @author Jesús Adolfo García Pasquel
 */
@Parcel(Parcel.Serialization.BEAN)
public class AuthorListItemViewModel {

    /**
     * Identifies the messages written to the log by this class.
     */
    private static final String LOG_TAG =
            AuthorListItemViewModel.class.getSimpleName();

    /**
     * Author data to be displayed.
     */
    private Author mAuthor;

    /**
     * Returns the author's name.
     *
     * @return the author's name.
     */
    public String getAuthorName() {
        return mAuthor == null ? null : mAuthor.getName();
    }

    public Author getAuthor() {
        return mAuthor;
    }

    public void setAuthor(Author author) {
        mAuthor = author;
    }

}
