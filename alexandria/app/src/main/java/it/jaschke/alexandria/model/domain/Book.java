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

package it.jaschke.alexandria.model.domain;

import android.net.Uri;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.parceler.Parcel;

import java.util.List;

/**
 * A book.
 *
 * @author Jesús Adolfo García Pasquel
 */
@Parcel(Parcel.Serialization.BEAN)
public class Book {

    /**
     * The book's identifier.
     */
    private long mId;

    /**
     * The book's title.
     */
    private String mTitle;

    /**
     * The book's subtitle.
     */
    private String mSubtitle;

    /**
     * The book's description.
     */
    private String mDescription;

    /**
     * URI of the book's cover image.
     */
    private Uri mCoverUri;

    /**
     * The book's authors.
     */
    private List<Author> mAuthors;

    /**
     * The book's categories.
     */
    private List<Category> mCategories;

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public void setSubtitle(String subtitle) {
        mSubtitle = subtitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public Uri getCoverUri() {
        return mCoverUri;
    }

    public void setCoverUri(Uri coverUri) {
        mCoverUri = coverUri;
    }

    public List<Author> getAuthors() {
        return mAuthors;
    }

    public void setAuthors(List<Author> authors) {
        this.mAuthors = authors;
    }

    public List<Category> getCategories() {
        return mCategories;
    }

    public void setCategories(List<Category> categories) {
        this.mCategories = categories;
    }

    @Override
    public int hashCode() {
        final int initial = 1051;
        final int multiplier = 571;
        return new HashCodeBuilder(initial, multiplier)
                .append(this.mId)
                .append(this.mTitle)
                .append(this.mSubtitle)
                .append(this.mDescription)
                .append(this.mCoverUri)
                .append(this.mAuthors)
                .append(this.mCategories)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Book)) {
            return false;
        }
        Book that = ((Book) obj);
        return new EqualsBuilder()
                .append(this.mId, that.mId)
                .append(this.mTitle, that.mTitle)
                .append(this.mSubtitle, that.mSubtitle)
                .append(this.mDescription, that.mDescription)
                .append(this.mCoverUri, that.mCoverUri)
                .append(this.mAuthors, that.mAuthors)
                .append(this.mCategories, that.mCategories)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("mId", this.mId)
                .append("mTitle", this.mTitle)
                .append("mSubtitle", this.mSubtitle)
                .append("mDescription", this.mDescription)
                .append("mCoverUri", this.mCoverUri)
                .append("mAuthors", this.mAuthors)
                .append("mCategories", this.mCategories)
                .toString();
    }

}
